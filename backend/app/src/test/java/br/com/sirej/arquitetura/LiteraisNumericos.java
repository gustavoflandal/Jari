package br.com.sirej.arquitetura;

import java.io.IOException;
import java.io.InputStream;
import java.lang.classfile.AccessFlags;
import java.lang.classfile.Annotation;
import java.lang.classfile.AnnotationElement;
import java.lang.classfile.AnnotationValue;
import java.lang.classfile.Attributes;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeModel;
import java.lang.classfile.FieldModel;
import java.lang.classfile.MethodModel;
import java.lang.classfile.Opcode;
import java.lang.classfile.instruction.ConstantInstruction;
import java.lang.classfile.instruction.LineNumber;
import java.lang.classfile.instruction.NewObjectInstruction;
import java.lang.classfile.instruction.StackInstruction;
import java.lang.constant.ConstantDesc;
import java.lang.reflect.AccessFlag;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

/**
 * Regra customizada 7 do doc 12: lê o bytecode das classes (API {@code java.lang.classfile} do JDK) e
 * acusa todo literal numérico diferente de -1, 0 e 1.
 *
 * <p>Onde procura: instruções de constante no código de todos os métodos (inclusive corpos de lambda),
 * constantes de campo ({@code static final}, que o compilador também copia para quem as usa) e valores
 * numéricos de anotações na classe, em campos e em métodos.
 *
 * <p>O que ignora, por ser gerado pelo compilador e não escrito por alguém: classes sintéticas, métodos
 * ponte, o método {@code $values()} de enums e o ordinal que o compilador passa ao construtor de cada
 * constante de enum.
 *
 * <p>Limitação conhecida: literal {@code char} vira inteiro no bytecode e também é acusado. Em prazos e
 * secretaria isso é intencional (opção mais restritiva, D-40).
 */
final class LiteraisNumericos {

    private LiteraisNumericos() {
    }

    static ArchCondition<JavaClass> naoConterLiteralDePrazo() {
        return new ArchCondition<>("não conter literal numérico além de -1, 0 e 1") {
            @Override
            public void check(JavaClass classe, ConditionEvents eventos) {
                Optional<byte[]> bytecode = lerBytecode(classe);
                if (bytecode.isEmpty()) {
                    // Falha fechada: sem bytecode, não há como provar a ausência do literal.
                    eventos.add(SimpleConditionEvent.violated(classe,
                            "bytecode de " + classe.getName() + " não encontrado para verificação"));
                    return;
                }
                for (String achado : literaisProibidos(bytecode.get())) {
                    eventos.add(SimpleConditionEvent.violated(classe, classe.getName() + ": " + achado));
                }
            }
        };
    }

    /** Descrição de cada literal proibido encontrado no bytecode, com método e linha quando houver. */
    static List<String> literaisProibidos(byte[] bytecode) {
        ClassModel classe = ClassFile.of().parse(bytecode);
        List<String> achados = new ArrayList<>();
        if (classe.flags().has(AccessFlag.SYNTHETIC)) {
            return achados;
        }
        boolean ehEnum = classe.flags().has(AccessFlag.ENUM);
        String nomeInterno = classe.thisClass().asInternalName();

        verificarAnotacoes(classe.findAttribute(Attributes.runtimeVisibleAnnotations())
                .map(a -> a.annotations()).orElse(List.of()), "anotação da classe", achados);
        verificarAnotacoes(classe.findAttribute(Attributes.runtimeInvisibleAnnotations())
                .map(a -> a.annotations()).orElse(List.of()), "anotação da classe", achados);

        for (FieldModel campo : classe.fields()) {
            String onde = "campo " + campo.fieldName().stringValue();
            campo.findAttribute(Attributes.constantValue()).ifPresent(valor ->
                    verificar(valor.constant().constantValue(), onde, achados));
            verificarAnotacoes(anotacoesDoCampo(campo), "anotação do " + onde, achados);
        }

        for (MethodModel metodo : classe.methods()) {
            String nome = metodo.methodName().stringValue();
            AccessFlags flags = metodo.flags();
            if (flags.has(AccessFlag.BRIDGE) || (ehEnum && nome.equals("$values"))) {
                continue;
            }
            verificarAnotacoes(anotacoesDoMetodo(metodo), "anotação do método " + nome, achados);
            Optional<CodeModel> codigo = metodo.code();
            if (codigo.isPresent()) {
                verificarCodigo(codigo.get(), nome, ehEnum && nome.equals("<clinit>"), nomeInterno, achados);
            }
        }
        return achados;
    }

    private static void verificarCodigo(CodeModel codigo, String metodo, boolean inicializacaoDeEnum,
            String nomeInterno, List<String> achados) {
        int linha = -1;
        // Na inicialização de enum: new E; dup; ldc "NOME"; <ordinal>. O ordinal não é literal escrito.
        int passoDoOrdinal = 0;
        for (CodeElement elemento : codigo) {
            if (elemento instanceof LineNumber numero) {
                linha = numero.line();
                continue;
            }
            if (inicializacaoDeEnum) {
                if (elemento instanceof NewObjectInstruction novo) {
                    String criado = novo.className().asInternalName();
                    passoDoOrdinal = criado.equals(nomeInterno) || criado.startsWith(nomeInterno + "$") ? 1 : 0;
                    continue;
                }
                if (passoDoOrdinal == 1 && elemento instanceof StackInstruction pilha && pilha.opcode() == Opcode.DUP) {
                    passoDoOrdinal = 2;
                    continue;
                }
                if (passoDoOrdinal == 2 && elemento instanceof ConstantInstruction texto
                        && texto.constantValue() instanceof String) {
                    passoDoOrdinal = 3;
                    continue;
                }
                if (passoDoOrdinal == 3 && elemento instanceof ConstantInstruction) {
                    passoDoOrdinal = 0;
                    continue;
                }
                passoDoOrdinal = 0;
            }
            if (elemento instanceof ConstantInstruction constante) {
                verificar(constante.constantValue(), "método " + metodo + (linha > 0 ? ", linha " + linha : ""), achados);
            }
        }
    }

    private static void verificar(ConstantDesc valor, String onde, List<String> achados) {
        if (valor instanceof Number numero && proibido(numero)) {
            achados.add("literal " + numero + " em " + onde);
        }
    }

    /** Só -1, 0 e 1 são permitidos (D-40). */
    static boolean proibido(Number numero) {
        double valor = numero.doubleValue();
        return !(valor == -1 || valor == 0 || valor == 1);
    }

    private static List<Annotation> anotacoesDoCampo(FieldModel campo) {
        List<Annotation> todas = new ArrayList<>();
        campo.findAttribute(Attributes.runtimeVisibleAnnotations()).ifPresent(a -> todas.addAll(a.annotations()));
        campo.findAttribute(Attributes.runtimeInvisibleAnnotations()).ifPresent(a -> todas.addAll(a.annotations()));
        return todas;
    }

    private static List<Annotation> anotacoesDoMetodo(MethodModel metodo) {
        List<Annotation> todas = new ArrayList<>();
        metodo.findAttribute(Attributes.runtimeVisibleAnnotations()).ifPresent(a -> todas.addAll(a.annotations()));
        metodo.findAttribute(Attributes.runtimeInvisibleAnnotations()).ifPresent(a -> todas.addAll(a.annotations()));
        return todas;
    }

    private static void verificarAnotacoes(List<Annotation> anotacoes, String onde, List<String> achados) {
        for (Annotation anotacao : anotacoes) {
            for (AnnotationElement elemento : anotacao.elements()) {
                verificarValor(elemento.value(), onde + " (" + elemento.name().stringValue() + ")", achados);
            }
        }
    }

    private static void verificarValor(AnnotationValue valor, String onde, List<String> achados) {
        switch (valor) {
            case AnnotationValue.OfInt v -> verificar(v.intValue(), onde, achados);
            case AnnotationValue.OfLong v -> verificar(v.longValue(), onde, achados);
            case AnnotationValue.OfShort v -> verificar((int) v.shortValue(), onde, achados);
            case AnnotationValue.OfByte v -> verificar((int) v.byteValue(), onde, achados);
            case AnnotationValue.OfDouble v -> verificar(v.doubleValue(), onde, achados);
            case AnnotationValue.OfFloat v -> verificar(v.floatValue(), onde, achados);
            case AnnotationValue.OfArray v -> v.values().forEach(item -> verificarValor(item, onde, achados));
            case AnnotationValue.OfAnnotation v -> verificarAnotacoes(List.of(v.annotation()), onde, achados);
            default -> {
                // Texto, booleano, char, classe e enum não são literal numérico de prazo.
            }
        }
    }

    private static Optional<byte[]> lerBytecode(JavaClass classe) {
        String recurso = classe.getName().replace('.', '/') + ".class";
        ClassLoader carregador = Thread.currentThread().getContextClassLoader();
        try (InputStream entrada = classe.getSource()
                .map(fonte -> abrir(fonte.getUri()))
                .orElseGet(() -> carregador.getResourceAsStream(recurso))) {
            return entrada == null ? Optional.empty() : Optional.of(entrada.readAllBytes());
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private static InputStream abrir(java.net.URI uri) {
        try {
            return uri.toURL().openStream();
        } catch (IOException e) {
            return null;
        }
    }
}
