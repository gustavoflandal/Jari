/*
 * Portal do Recorrente — protótipo navegável.
 * Jornada: placa/AIT → peça cabível → escrever e anexar → revisar → assinar → recibo → linha do tempo.
 * Todos os dados são fictícios. Nada é enviado a servidor algum.
 */
(function () {
  "use strict";
  var P = window.Proto;
  var $ = function (id) { return document.getElementById(id); };
  var LIMITE_MB = 20;                                   // sp.yaml: documentos.tamanhoMaximoMb
  var FORMATOS = ["application/pdf", "image/jpeg", "image/png"]; // sp.yaml: documentos.formatosAceitos
  Array.prototype.forEach.call(document.querySelectorAll(".lim-mb"), function (e) { e.textContent = LIMITE_MB; });

  function d(dia, mes) { return new Date(2026, mes - 1, dia, 23, 59, 0); }

  /* ---------- Peças (textos em linguagem simples) ---------- */
  var PECAS = {
    RECURSO_1A_INSTANCIA: {
      titulo: "Recorrer da multa à JARI",
      subtitulo: "1ª instância",
      explica: "A multa já foi aplicada e você quer pedir que ela seja cancelada. Quem julga é a JARI, um grupo de julgadores independente de quem fez a autuação.",
      decisor: "JARI (Junta Administrativa de Recursos de Infrações)",
      rotuloTexto: "Por que a multa deve ser cancelada?",
      jari: true
    },
    DEFESA_AUTUACAO: {
      titulo: "Apresentar defesa da autuação",
      subtitulo: "antes de a multa ser aplicada",
      explica: "Você recebeu o aviso da autuação, mas a multa ainda não foi aplicada. Quem decide a defesa é o órgão que fez a autuação, não a JARI. Se a defesa for negada, você ainda poderá recorrer à JARI depois.",
      decisor: "Órgão autuador (não é a JARI)",
      rotuloTexto: "Por que a autuação está errada?"
    },
    INDICACAO_CONDUTOR: {
      titulo: "Indicar quem estava dirigindo",
      subtitulo: "se não era você",
      explica: "Se outra pessoa dirigia o veículo, informe quem era para que os pontos fiquem com ela. Quem analisa é o órgão que fez a autuação.",
      decisor: "Órgão autuador (não é a JARI)",
      rotuloTexto: "Quem estava dirigindo? Conte o que aconteceu"
    },
    RECURSO_2A_INSTANCIA: {
      titulo: "Recorrer ao CETRAN",
      subtitulo: "2ª instância",
      explica: "A JARI já julgou o seu recurso e manteve a multa. Você pode pedir uma nova análise ao Conselho Estadual de Trânsito (CETRAN-SP).",
      decisor: "CETRAN-SP (o processo é enviado para lá)",
      rotuloTexto: "Por que a decisão da JARI deve ser mudada?"
    },
    JUNTADA_DOCUMENTOS: {
      titulo: "Enviar novos documentos ao seu recurso",
      subtitulo: "recurso ainda não julgado",
      explica: "Seu recurso à JARI está em andamento. Você pode enviar novas provas ou explicações; elas entram no mesmo processo.",
      decisor: "Entra no processo que já existe",
      rotuloTexto: "O que você está enviando e por quê?"
    }
  };

  /* ---------- Veículos e autuações fictícios ---------- */
  var VEICULOS = {
    ABC1D23: {
      descricao: "Automóvel fictício, cor prata",
      aits: [
        {
          numero: "PROTO-000123", infracao: "Avançar o sinal vermelho (exemplo)", local: "Av. Exemplo, 1000", data: "02/08/2026",
          fase: "Multa aplicada. Notificação da penalidade recebida em 22/09/2026.", etiqueta: "Multa aplicada",
          pecas: [{ tipo: "RECURSO_1A_INSTANCIA", fim: d(22, 10), base: "30 dias contados do recebimento da notificação da penalidade" }],
          porque: "A multa já foi aplicada. Por isso não cabe mais defesa da autuação nem indicação de quem dirigia: essas opções só existem logo depois do aviso da autuação."
        },
        {
          numero: "PROTO-000456", infracao: "Estacionar em local proibido (exemplo)", local: "Rua Fictícia, 50", data: "20/09/2026",
          fase: "Autuação notificada em 28/09/2026. A multa ainda não foi aplicada.", etiqueta: "Aviso de autuação",
          pecas: [
            { tipo: "INDICACAO_CONDUTOR", fim: d(13, 10), base: "15 dias contados do recebimento do aviso da autuação" },
            { tipo: "DEFESA_AUTUACAO", fim: d(28, 10), base: "data impressa no aviso da autuação" }
          ],
          porque: "Ainda não é possível recorrer à JARI: a JARI só julga depois que a multa é aplicada. Nesta fase, a defesa vai para o órgão que fez a autuação."
        },
        {
          numero: "PROTO-000789", infracao: "Velocidade até 20% acima do limite (exemplo)", local: "Rod. Modelo, km 12", data: "15/07/2026",
          fase: "Recurso à JARI em andamento (processo 2026.000777).", etiqueta: "Recurso em andamento",
          processo: "2026.000777",
          pecas: [{ tipo: "JUNTADA_DOCUMENTOS", fim: null, base: "enquanto o recurso não for julgado" }],
          porque: "Você já recorreu desta multa. Agora só é possível enviar documentos para o recurso que já existe."
        }
      ]
    },
    XYZ9W87: {
      descricao: "Motocicleta fictícia, cor preta",
      aits: [
        {
          numero: "PROTO-000321", infracao: "Usar o celular ao dirigir (exemplo)", local: "Praça Teste, s/n", data: "10/06/2026",
          fase: "A JARI julgou e manteve a multa. Resultado publicado em 30/09/2026.", etiqueta: "Julgado pela JARI",
          pecas: [{ tipo: "RECURSO_2A_INSTANCIA", fim: d(30, 10), base: "30 dias contados da publicação do resultado" }],
          porque: "O recurso à JARI já foi julgado. O próximo passo possível é o CETRAN."
        }
      ]
    }
  };

  /* ---------- Processos já existentes (fictícios) ---------- */
  var PROCESSOS = [
    {
      numero: "2026.000777", ait: "PROTO-000789", placa: "ABC1D23", peca: "RECURSO_1A_INSTANCIA",
      situacao: "Distribuído para julgamento", etiqueta: "etiqueta-azul",
      resumo: "<p><strong>Distribuído. Julgamento previsto na semana de 12/10.</strong></p>" +
        "<p class='dica'>A distribuição é feita por sorteio eletrônico, toda semana. Por regra do regimento, ninguém sabe qual grupo vai julgar antes do dia da sessão, nem mesmo os servidores. Depois do julgamento você verá o resultado aqui, com o motivo.</p>",
      linha: [
        { f: 1, t: "Pedido recebido", q: "14/09/2026 às 18:42", x: "Recibo 2026.000777." },
        { f: 1, t: "Pedido conferido e aceito para julgamento", q: "18/09/2026" },
        { f: 1, t: "Informações do órgão autuador juntadas", q: "25/09/2026" },
        { f: 1, a: 1, t: "Distribuído por sorteio eletrônico", q: "28/09/2026", x: "Julgamento previsto na semana de 12/10." },
        { t: "Julgamento", x: "Você será avisado do resultado." },
        { t: "Resultado publicado" }
      ]
    },
    {
      numero: "2026.000555", ait: "PROTO-000321", placa: "XYZ9W87", peca: "RECURSO_1A_INSTANCIA",
      situacao: "Julgado: penalidade mantida", etiqueta: "etiqueta-vermelha",
      resumo: "<p><strong>Resultado: manutenção da penalidade.</strong> A multa continua valendo.</p>" +
        "<p><strong>Motivo:</strong> a JARI concluiu que a autuação seguiu as regras e que as fotos enviadas não mostram o que foi alegado.</p>" +
        "<p><strong>Próximo passo possível:</strong> você pode recorrer ao CETRAN-SP até <strong>30/10/2026</strong>.</p>" +
        "<p class='dica'>Julgado na sessão de 24/09/2026 da 3ª Junta (fictícia). Os nomes dos julgadores constam da decisão.</p>" +
        "<button type='button' class='btn btn-primario' id='btn-2a'>Recorrer ao CETRAN</button>",
      linha: [
        { f: 1, t: "Pedido recebido", q: "01/08/2026" },
        { f: 1, t: "Pedido conferido e aceito para julgamento", q: "06/08/2026" },
        { f: 1, t: "Distribuído por sorteio eletrônico", q: "31/08/2026" },
        { f: 1, t: "Julgado", q: "24/09/2026", x: "Penalidade mantida." },
        { f: 1, a: 1, t: "Resultado publicado", q: "30/09/2026", x: "Prazo para recorrer ao CETRAN-SP: até 30/10/2026." }
      ]
    }
  ];

  /* ---------- Estado da jornada ---------- */
  var st = { placa: null, ait: null, peca: null, texto: "", anexos: [], enviando: 0, seq: 1000, processoAtual: null };

  function diasRestantes(fim) {
    var h = new Date(P.HOJE.getFullYear(), P.HOJE.getMonth(), P.HOJE.getDate());
    var f = new Date(fim.getFullYear(), fim.getMonth(), fim.getDate());
    return Math.round((f - h) / 86400000);
  }

  function prazoHtml(p) {
    if (!p.fim) return "<p class='dica'>Prazo: " + P.esc(p.base) + ".</p>";
    var n = diasRestantes(p.fim);
    return "<div class='prazo'><span class='dias'>" + (n === 0 ? "Último dia" : "Faltam " + n + " dias") + "</span>" +
      "<span>Prazo até <strong>" + P.data(p.fim) + "</strong></span></div>" +
      "<p class='dica'>Contagem: " + P.esc(p.base) + ".</p>";
  }

  /* ---------- Navegação por hash (o botão Voltar do celular funciona) ---------- */
  var TITULOS = {
    inicio: "Recorrer de multa", aits: "Autuações do veículo", passo1: "Etapa 1 de 4: escolher",
    passo2: "Etapa 2 de 4: escrever e anexar", passo3: "Etapa 3 de 4: revisar", passo4: "Etapa 4 de 4: assinar",
    recibo: "Recibo de protocolo", meus: "Meus processos", processo: "Processo"
  };

  function ir(nome) {
    if (location.hash === "#" + nome) mostrar(nome); else location.hash = nome;
  }

  function mostrar(nome) {
    var precisa = { aits: "placa", passo1: "ait", passo2: "peca", passo3: "peca", passo4: "peca", recibo: "recibo", processo: "processoAtual" };
    if (precisa[nome] && !st[precisa[nome]]) nome = "inicio";
    if (!TITULOS[nome]) nome = "inicio";
    Array.prototype.forEach.call(document.querySelectorAll("main > section"), function (s) { s.classList.add("oculto"); });
    var sec = $("tela-" + nome);
    sec.classList.remove("oculto");
    var render = { aits: renderAits, passo1: renderPasso1, passo2: renderPasso2, passo3: renderPasso3, meus: renderMeus, processo: renderProcesso };
    if (render[nome]) render[nome]();
    document.title = TITULOS[nome] + " — Portal (protótipo SIREJ)";
    window.scrollTo(0, 0);
    P.focar(sec.querySelector("h1"));
  }

  window.addEventListener("hashchange", function () { mostrar(location.hash.replace("#", "") || "inicio"); });
  document.addEventListener("click", function (ev) {
    var b = ev.target.closest("[data-ir]");
    if (b) { ev.preventDefault(); ir(b.getAttribute("data-ir")); }
  });
  $("link-inicio").addEventListener("click", function (e) { e.preventDefault(); ir("inicio"); });
  $("link-meus").addEventListener("click", function (e) { e.preventDefault(); ir("meus"); });

  /* ---------- Início ---------- */
  $("form-busca").addEventListener("submit", function (e) {
    e.preventDefault();
    var v = $("busca").value.toUpperCase().replace(/[\s-]/g, "");
    var erro = $("busca-erro");
    var achouPlaca = null, achouAit = null;
    Object.keys(VEICULOS).forEach(function (pl) {
      if (pl === v) achouPlaca = pl;
      VEICULOS[pl].aits.forEach(function (a) { if (a.numero.replace("-", "") === v) { achouPlaca = pl; achouAit = a; } });
    });
    if (!v) {
      erro.textContent = "Digite a placa ou o número do auto de infração.";
    } else if (!achouPlaca) {
      erro.textContent = "Não encontramos autuações para \"" + v + "\". Confira se digitou certo. No teste, use ABC1D23 ou XYZ9W87.";
    } else {
      erro.classList.add("oculto"); $("busca").removeAttribute("aria-invalid");
      P.marco("Busca feita");
      st.placa = achouPlaca;
      if (achouAit) { st.ait = achouAit; ir("passo1"); } else { ir("aits"); }
      return;
    }
    erro.classList.remove("oculto");
    $("busca").setAttribute("aria-invalid", "true");
    $("busca").focus();
  });

  /* ---------- Lista de AITs ---------- */
  function renderAits() {
    var v = VEICULOS[st.placa];
    $("aits-placa").textContent = st.placa;
    $("aits-veiculo").textContent = v.descricao;
    var html = "";
    v.aits.forEach(function (a, i) {
      var p = a.pecas[0];
      var urg = p.fim ? " · faltam " + diasRestantes(p.fim) + " dias para o prazo mais próximo" : "";
      html += "<button type='button' class='cartao ait-item' data-ait='" + i + "'>" +
        "<span class='linha1'><strong>" + P.esc(a.infracao) + "</strong><span class='etiqueta etiqueta-amarela'>" + P.esc(a.etiqueta) + "</span></span>" +
        "<span class='dica' style='display:block'>Auto " + P.esc(a.numero) + " · " + P.esc(a.data) + " · " + P.esc(a.local) + "</span>" +
        "<span style='display:block'>" + P.esc(a.fase) + P.esc(urg) + "</span></button>";
    });
    $("aits-lista").innerHTML = html;
    Array.prototype.forEach.call($("aits-lista").querySelectorAll("[data-ait]"), function (b) {
      b.addEventListener("click", function () {
        var novo = v.aits[+b.getAttribute("data-ait")];
        if (st.ait !== novo) { st.peca = null; }
        st.ait = novo; ir("passo1");
      });
    });
  }

  /* ---------- Passo 1 ---------- */
  function renderPasso1() {
    var a = st.ait;
    $("p1-ait").innerHTML = "<h2>" + P.esc(a.infracao) + "</h2>" +
      "<dl class='dados'><dt>Auto</dt><dd>" + P.esc(a.numero) + "</dd><dt>Placa</dt><dd>" + P.esc(st.placa) + "</dd>" +
      "<dt>Data</dt><dd>" + P.esc(a.data) + "</dd><dt>Situação</dt><dd>" + P.esc(a.fase) + "</dd></dl>";
    var html = "";
    a.pecas.forEach(function (p, i) {
      var pc = PECAS[p.tipo];
      html += "<label class='opcao' for='peca-" + i + "'><input type='radio' name='peca' id='peca-" + i + "' value='" + p.tipo + "'" +
        (st.peca && st.peca.tipo === p.tipo ? " checked" : "") + (a.pecas.length === 1 ? " checked" : "") + ">" +
        "<span><strong>" + P.esc(pc.titulo) + "</strong> <span class='dica'>(" + P.esc(pc.subtitulo) + ")</span>" +
        "<span style='display:block'>" + P.esc(pc.explica) + "</span>" + prazoHtml(p) +
        "<span class='dica' style='display:block'>Quem decide: " + P.esc(pc.decisor) + "</span></span></label>";
    });
    $("p1-pecas").innerHTML = html;
    $("p1-porque-texto").textContent = a.porque;
    $("p1-erro").classList.add("oculto");
  }

  $("form-peca").addEventListener("submit", function (e) {
    e.preventDefault();
    var sel = document.querySelector("input[name=peca]:checked");
    if (!sel) { $("p1-erro").classList.remove("oculto"); P.focar(document.querySelector("input[name=peca]")); return; }
    var tipo = sel.value;
    var nova = st.ait.pecas.filter(function (p) { return p.tipo === tipo; })[0];
    st.peca = nova;
    ir("passo2");
  });

  /* ---------- Passo 2 ---------- */
  function renderPasso2() {
    var pc = PECAS[st.peca.tipo];
    $("p2-peca").textContent = pc.titulo;
    document.querySelector("label[for=texto]").textContent = pc.rotuloTexto;
    $("texto").value = st.texto;
    contar();
    renderAnexos();
  }

  function contar() { $("texto-cont").textContent = $("texto").value.length + " caracteres"; }
  $("texto").addEventListener("input", function () { st.texto = this.value; contar(); });

  function renderAnexos() {
    var html = "";
    st.anexos.forEach(function (a, i) {
      html += "<div class='anexo'><span class='nome'>" + P.esc(a.nome) + " <span class='dica'>(" + a.tam + ")</span></span>" +
        (a.pct >= 100 ? "<span class='etiqueta etiqueta-verde'>Enviado</span>" : "<span class='etiqueta etiqueta-amarela'>" + (a.pausa ? "Rede instável, vai retomar" : "Enviando " + a.pct + "%") + "</span>") +
        "<button type='button' class='btn btn-texto' data-rem='" + i + "' aria-label='Remover " + P.esc(a.nome) + "'>Remover</button>" +
        "<div class='progresso' role='progressbar' aria-label='Envio de " + P.esc(a.nome) + "' aria-valuemin='0' aria-valuemax='100' aria-valuenow='" + a.pct + "'><span style='width:" + a.pct + "%'></span></div></div>";
    });
    $("anexos").innerHTML = html;
    Array.prototype.forEach.call($("anexos").querySelectorAll("[data-rem]"), function (b) {
      b.addEventListener("click", function () {
        var a = st.anexos[+b.getAttribute("data-rem")];
        a.removido = true;
        st.anexos.splice(+b.getAttribute("data-rem"), 1);
        P.anunciar(a.nome + " removido.");
        renderAnexos();
      });
    });
  }

  function tamanhoTexto(bytes) { return (bytes / 1048576).toFixed(1).replace(".", ",") + " MB"; }

  function adicionarAnexo(nome, bytes, conteudo) {
    var a = { nome: nome, tam: tamanhoTexto(bytes), pct: 0, pausa: false, hash: P.hashTexto(conteudo) };
    st.anexos.push(a);
    st.enviando++;
    var caiu = false;
    var t = setInterval(function () {
      if (a.removido) { clearInterval(t); st.enviando--; return; }
      if (a.pausa) return;
      a.pct = Math.min(100, a.pct + 10);
      if ($("rede-ruim").checked && !caiu && a.pct >= 50) {
        caiu = true; a.pausa = true;
        P.anunciar("Conexão instável. O envio de " + nome + " continua de onde parou quando a rede voltar.");
        setTimeout(function () { a.pausa = false; renderAnexos(); }, 2500);
      }
      if (a.pct >= 100) { clearInterval(t); st.enviando--; P.anunciar(nome + " enviado."); }
      renderAnexos();
    }, 150);
    renderAnexos();
  }

  $("arquivo").addEventListener("change", function () {
    var f = this.files && this.files[0];
    var erro = $("anexo-erro");
    erro.classList.add("oculto");
    this.removeAttribute("aria-invalid");
    if (!f) return;
    if (FORMATOS.indexOf(f.type) < 0) {
      erro.textContent = "O arquivo \"" + f.name + "\" não é PDF, JPG ou PNG. Escolha outro arquivo.";
    } else if (f.size > LIMITE_MB * 1048576) {
      erro.textContent = "O arquivo \"" + f.name + "\" tem " + tamanhoTexto(f.size) + ". O limite é " + LIMITE_MB + " MB. Tente uma foto com resolução menor.";
    } else {
      // Não lemos o conteúdo do arquivo: o hash é calculado sobre nome+tamanho (protótipo).
      adicionarAnexo(f.name, f.size, f.name + "|" + f.size + "|" + f.lastModified);
      this.value = "";
      return;
    }
    erro.classList.remove("oculto");
    this.setAttribute("aria-invalid", "true");
    this.value = "";
  });

  var exemplos = 0;
  $("anexo-exemplo").addEventListener("click", function () {
    exemplos++;
    adicionarAnexo("foto-exemplo-" + exemplos + ".jpg", 1250000 + exemplos * 310000, "conteudo-sintetico-" + exemplos);
  });

  $("form-texto").addEventListener("submit", function (e) {
    e.preventDefault();
    var ok = true;
    if ($("texto").value.trim().length < 20) {
      $("texto-erro").classList.remove("oculto"); $("texto").setAttribute("aria-invalid", "true"); $("texto").focus(); ok = false;
    } else { $("texto-erro").classList.add("oculto"); $("texto").removeAttribute("aria-invalid"); }
    if (ok && st.enviando > 0) {
      $("anexo-erro").textContent = "Aguarde o envio das provas terminar.";
      $("anexo-erro").classList.remove("oculto"); ok = false;
    }
    if (ok) ir("passo3");
  });

  /* ---------- Passo 3 ---------- */
  function renderPasso3() {
    var pc = PECAS[st.peca.tipo];
    var anexos = st.anexos.length ? st.anexos.map(function (a) { return P.esc(a.nome); }).join("<br>") : "Nenhuma prova anexada";
    $("p3-resumo").innerHTML =
      "<dt>Pedido</dt><dd>" + P.esc(pc.titulo) + " (" + P.esc(pc.subtitulo) + ")</dd>" +
      "<dt>Quem decide</dt><dd>" + P.esc(pc.decisor) + "</dd>" +
      "<dt>Auto de infração</dt><dd>" + P.esc(st.ait.numero) + " · placa " + P.esc(st.placa) + "</dd>" +
      (st.peca.fim ? "<dt>Prazo</dt><dd>até " + P.data(st.peca.fim) + " (faltam " + diasRestantes(st.peca.fim) + " dias)</dd>" : "") +
      "<dt>Seu texto</dt><dd style='white-space:pre-wrap'>" + P.esc(st.texto) + "</dd>" +
      "<dt>Provas</dt><dd>" + anexos + "</dd>";
  }

  /* ---------- Passo 4: assinar ---------- */
  $("form-assinar").addEventListener("submit", function (e) {
    e.preventDefault();
    if (!$("declaro").checked) {
      $("declaro-erro").classList.remove("oculto"); $("declaro").setAttribute("aria-invalid", "true"); $("declaro").focus(); return;
    }
    $("declaro-erro").classList.add("oculto"); $("declaro").removeAttribute("aria-invalid");
    protocolar();
  });

  function protocolar() {
    var agora = P.agora();
    var pc = PECAS[st.peca.tipo];
    st.seq++;
    var numero = "2026.00" + st.seq;
    var docs = [{ nome: "pedido-assinado.pdf", hash: P.hashTexto(st.texto + "|" + numero) }].concat(
      st.anexos.map(function (a) { return { nome: a.nome, hash: a.hash }; }));
    var hashRecibo = P.hashTexto(numero + "|" + agora.toISOString() + "|" + docs.map(function (x) { return x.hash; }).join("|"));
    var docsHtml = docs.map(function (x) {
      return "<li><strong>" + P.esc(x.nome) + "</strong><br><span class='mono'>SHA-256: " + x.hash + "</span></li>";
    }).join("");
    $("recibo").innerHTML =
      "<p class='dica'>Número do protocolo</p><p class='recibo-numero'>" + numero + "</p>" +
      "<dl class='dados'><dt>Recebido em</dt><dd>" + P.dataHora(agora) + " (horário de Brasília)</dd>" +
      "<dt>Pedido</dt><dd>" + P.esc(pc.titulo) + "</dd>" +
      "<dt>Quem decide</dt><dd>" + P.esc(pc.decisor) + "</dd>" +
      "<dt>Auto</dt><dd>" + P.esc(st.ait.numero) + "</dd><dt>Placa</dt><dd>" + P.esc(st.placa) + "</dd>" +
      "<dt>Assinado por</dt><dd>Maria Exemplo (conta de teste)</dd></dl>" +
      "<h2 style='margin-top:1rem'>Documentos recebidos</h2><ul>" + docsHtml + "</ul>" +
      "<p><strong>Código de verificação do recibo</strong><br><span class='mono'>" + hashRecibo + "</span></p>" +
      "<p class='dica'>Os códigos (hash) provam que os documentos não foram alterados depois do envio. Protótipo: o número e os códigos são ilustrativos.</p>";

    var proc;
    if (st.peca.tipo === "JUNTADA_DOCUMENTOS") {
      proc = PROCESSOS.filter(function (p) { return p.numero === st.ait.processo; })[0];
      proc.linha.splice(proc.linha.length - 2, 0, { f: 1, t: "Novos documentos enviados por você", q: P.dataHora(agora), x: "Recibo " + numero + "." });
    } else {
      proc = novoProcesso(numero, agora, pc);
      PROCESSOS.unshift(proc);
    }
    st.recibo = numero;
    st.processoAtual = proc;
    P.marco("Protocolo concluído");
    // Limpa o rascunho para um novo pedido.
    st.texto = ""; st.anexos = []; st.peca = null; $("declaro").checked = false;
    ir("recibo");
  }

  function novoProcesso(numero, agora, pc) {
    var linha;
    if (pc.jari) {
      linha = [
        { f: 1, a: 1, t: "Pedido recebido", q: P.dataHora(agora), x: "Recibo " + numero + "." },
        { t: "Conferência do pedido", x: "A secretaria da JARI confere se o pedido pode ser julgado. Se faltar algo, você recebe um aviso com prazo para completar." },
        { t: "Informações do órgão autuador", x: "O órgão que fez a autuação envia as informações dele." },
        { t: "Distribuição por sorteio", x: "Feita automaticamente toda semana. Ninguém escolhe quem vai julgar." },
        { t: "Julgamento", x: "Três julgadores de origens diferentes analisam o seu pedido." },
        { t: "Resultado publicado", x: "Você verá o resultado, o motivo e o que pode fazer depois." }
      ];
    } else if (st.peca.tipo === "RECURSO_2A_INSTANCIA") {
      linha = [
        { f: 1, a: 1, t: "Pedido recebido", q: P.dataHora(agora), x: "Recibo " + numero + "." },
        { t: "Envio ao CETRAN-SP", x: "O processo completo é enviado ao CETRAN-SP." },
        { t: "Decisão do CETRAN-SP" }
      ];
    } else {
      linha = [
        { f: 1, a: 1, t: "Pedido recebido", q: P.dataHora(agora), x: "Recibo " + numero + "." },
        { t: "Análise pelo órgão autuador", x: "Esse pedido não é julgado pela JARI." },
        { t: "Resposta do órgão autuador" }
      ];
    }
    return {
      numero: numero, ait: st.ait.numero, placa: st.placa, peca: st.peca.tipo,
      situacao: "Pedido recebido", etiqueta: "etiqueta-verde",
      resumo: "<p><strong>Pedido recebido em " + P.dataHora(agora) + ".</strong></p><p>Próximo passo: " +
        P.esc(linha[1].t.toLowerCase()) + ". Você não precisa fazer nada agora; avisaremos se precisarmos de algo.</p>",
      linha: linha
    };
  }

  $("ver-processo").addEventListener("click", function () { ir("processo"); });

  /* ---------- Meus processos e linha do tempo ---------- */
  function renderMeus() {
    var html = "";
    PROCESSOS.forEach(function (p, i) {
      html += "<div class='cartao'><div style='display:flex;justify-content:space-between;gap:0.5rem;flex-wrap:wrap'>" +
        "<h2 style='margin:0'>Processo " + P.esc(p.numero) + "</h2><span class='etiqueta " + p.etiqueta + "'>" + P.esc(p.situacao) + "</span></div>" +
        "<p class='dica'>" + P.esc(PECAS[p.peca].titulo) + " · auto " + P.esc(p.ait) + " · placa " + P.esc(p.placa) + "</p>" +
        "<button type='button' class='btn' data-proc='" + i + "'>Ver andamento <span class='sr-only'>do processo " + P.esc(p.numero) + "</span></button></div>";
    });
    $("meus-lista").innerHTML = html;
    Array.prototype.forEach.call($("meus-lista").querySelectorAll("[data-proc]"), function (b) {
      b.addEventListener("click", function () { st.processoAtual = PROCESSOS[+b.getAttribute("data-proc")]; ir("processo"); });
    });
  }

  function renderProcesso() {
    var p = st.processoAtual;
    $("proc-num").textContent = p.numero;
    $("proc-situacao").innerHTML = "<p class='dica'>" + P.esc(PECAS[p.peca].titulo) + " · auto " + P.esc(p.ait) + " · placa " + P.esc(p.placa) + "</p>" + p.resumo;
    $("proc-linha").innerHTML = p.linha.map(function (e) {
      var cls = e.a ? "atual" : (e.f ? "feito" : "");
      return "<li class='" + cls + "'>" + (e.a ? "<span class='sr-only'>Etapa atual: </span>" : (e.f ? "<span class='sr-only'>Concluído: </span>" : "<span class='sr-only'>Próxima etapa: </span>")) +
        "<strong>" + P.esc(e.t) + "</strong>" + (e.q ? "<span class='quando'>" + P.esc(e.q) + "</span>" : "") +
        (e.x ? "<span style='display:block'>" + P.esc(e.x) + "</span>" : "") + "</li>";
    }).join("");
    var b2 = $("btn-2a");
    if (b2) b2.addEventListener("click", function () {
      st.placa = "XYZ9W87"; st.ait = VEICULOS.XYZ9W87.aits[0]; st.peca = null; ir("passo1");
    });
  }

  mostrar(location.hash.replace("#", "") || "inicio");
})();
