/*
 * Ambiente do relator — protótipo navegável.
 * Caminho: relatar → votar (assinar) → próximo, com um único clique entre um processo e o seguinte.
 * Dados fictícios. Nada é enviado a servidor algum.
 */
(function () {
  "use strict";
  var P = window.Proto;
  var $ = function (id) { return document.getElementById(id); };

  var MEMBRO = { nome: "Membro Fictício B", cpf: "***.456.789-**" };

  /* Rol fechado de resultados: config/regimentos/sp.yaml (resultados) e doc 04, seção 4. */
  var RESULTADOS = [
    { codigo: "REJEICAO_ADMINISTRATIVA", rotulo: "Rejeição administrativa do recurso", altera: false },
    { codigo: "NAO_CONHECIMENTO_INTEMPESTIVIDADE", rotulo: "Não conhecimento por intempestividade", altera: false },
    { codigo: "NAO_CONHECIMENTO_ILEGITIMIDADE", rotulo: "Não conhecimento por ilegitimidade de parte", altera: false },
    { codigo: "MANUTENCAO_PENALIDADE", rotulo: "Manutenção da penalidade", altera: false },
    { codigo: "CANCELAMENTO_PENALIDADE", rotulo: "Cancelamento da penalidade", altera: true }
  ];

  /* Checklist de três eixos: sp.yaml relatoria.checklist (RN13). */
  var EIXOS = [
    { codigo: "REGULARIDADE_PROCEDIMENTO", titulo: "Regularidade do procedimento", dica: "O auto, as notificações e os prazos do órgão seguiram as regras?" },
    { codigo: "FORCA_MAIOR_ALEGADA", titulo: "Força maior alegada", dica: "O recorrente alegou força maior? Há prova?" },
    { codigo: "ANTECEDENTES_RECORRENTE", titulo: "Antecedentes do recorrente", dica: "Veja o histórico do veículo no resumo do caso." }
  ];

  var MODELOS = {
    padrao: "<p><strong>RELATÓRIO</strong></p><p>Trata-se de recurso interposto contra a penalidade decorrente do auto de infração indicado. O recorrente alega, em síntese, …</p><p><strong>VOTO</strong></p><p>Analisados os autos, …</p>",
    intempestivo: "<p><strong>RELATÓRIO</strong></p><p>Trata-se de recurso interposto contra a penalidade decorrente do auto de infração indicado.</p><p><strong>VOTO</strong></p><p>O recurso foi apresentado após o prazo legal, conforme datas do resumo do caso. Voto pelo não conhecimento por intempestividade.</p>",
    forca: "<p><strong>RELATÓRIO</strong></p><p>O recorrente alega força maior, consistente em …</p><p><strong>VOTO</strong></p><p>Quanto à força maior alegada, a prova juntada …</p>"
  };

  /* ---------- Processos fictícios desta sessão ---------- */
  var PROCESSOS = [
    {
      numero: "2026.000777", papel: "RELATOR", placa: "ABC1D23", recorrente: "Maria Exemplo",
      dossie: {
        veiculo: "ABC1D23 · automóvel fictício, prata",
        autuacao: "Velocidade até 20% acima do limite · 15/07/2026 · Rod. Modelo, km 12 · auto PROTO-000789",
        penalidade: "Multa (valor informado pelo sistema de multas; o SIREJ não calcula)",
        historico: "Mesmo veículo: PROTO-000123 (multa aplicada, sem recurso) · PROTO-000456 (aviso de autuação)",
        tempestividade: { ok: true, texto: "Tempestivo: protocolado em 14/09/2026; prazo até 22/09/2026" },
        ocorrencias: "Nenhuma"
      },
      docs: [
        { nome: "Recurso do recorrente", html: "<h3>RECURSO À JARI</h3><p>Recorrente: Maria Exemplo (fictícia)</p><p>Auto de infração PROTO-000789</p><p>Venho recorrer da multa por excesso de velocidade. O equipamento medidor não estava com a verificação do Inmetro em dia na data da autuação, conforme consulta pública anexa.</p><p>Peço o cancelamento da penalidade.</p><p>Assinado eletronicamente em 14/09/2026.</p>" },
        { nome: "Auto de infração", html: "<h3>AUTO DE INFRAÇÃO (fictício)</h3><table><tr><th>Número</th><td>PROTO-000789</td></tr><tr><th>Placa</th><td>ABC1D23</td></tr><tr><th>Data/hora</th><td>15/07/2026 07:41</td></tr><tr><th>Local</th><td>Rod. Modelo, km 12</td></tr><tr><th>Medição</th><td>72 km/h (via 60 km/h)</td></tr><tr><th>Equipamento</th><td>EQ-0001 (fictício)</td></tr></table>" },
        { nome: "Foto do equipamento", foto: "ABC1D23" },
        { nome: "Informação do órgão autuador", html: "<h3>INFORMAÇÃO DO ÓRGÃO AUTUADOR</h3><p>O equipamento EQ-0001 teve verificação em 10/01/2026, válida por 12 meses (certificado fictício anexo).</p>" }
      ]
    },
    {
      numero: "2026.000781", papel: "RELATOR", placa: "DEF4G56", recorrente: "João Teste",
      dossie: {
        veiculo: "DEF4G56 · utilitário fictício, branco",
        autuacao: "Estacionar em local proibido · 02/08/2026 · Rua Fictícia, 300 · auto PROTO-001001",
        penalidade: "Multa (valor informado pelo sistema de multas)",
        historico: "Nenhuma outra autuação do veículo nos últimos 12 meses",
        tempestividade: { ok: true, texto: "Tempestivo: protocolado em 05/09/2026; prazo até 11/09/2026" },
        ocorrencias: "Exigência de procuração emitida em 08/09 e atendida em 10/09"
      },
      docs: [
        { nome: "Recurso do recorrente", html: "<h3>RECURSO À JARI</h3><p>Recorrente: João Teste (fictício)</p><p>O veículo quebrou no local e ficou parado aguardando o guincho, que chegou 40 minutos depois. Junto a nota do serviço de guincho.</p>" },
        { nome: "Nota do guincho", html: "<h3>NOTA DE SERVIÇO (fictícia)</h3><p>Serviço de reboque · 02/08/2026 · 14:10 · Rua Fictícia, 300 · veículo DEF4G56.</p>" },
        { nome: "Foto do local", foto: "DEF4G56" }
      ]
    },
    {
      numero: "2026.000790", papel: "RELATOR", placa: "GHI7J89", recorrente: "Ana Modelo",
      dossie: {
        veiculo: "GHI7J89 · automóvel fictício, azul",
        autuacao: "Avançar o sinal vermelho · 20/07/2026 · Av. Exemplo, 1000 · auto PROTO-001050",
        penalidade: "Multa (valor informado pelo sistema de multas)",
        historico: "Recurso anterior do mesmo veículo julgado em 2025 (penalidade mantida)",
        tempestividade: { ok: false, texto: "Intempestivo pelo cálculo do sistema: protocolado em 30/09/2026; prazo terminou em 25/09/2026. A decisão é sua." },
        ocorrencias: "Nenhuma"
      },
      docs: [
        { nome: "Recurso do recorrente", html: "<h3>RECURSO À JARI</h3><p>Recorrente: Ana Modelo (fictícia)</p><p>O sinal estava amarelo quando passei. Peço o cancelamento.</p><p>Assinado eletronicamente em 30/09/2026.</p>" },
        { nome: "Comprovante de ciência da NP", html: "<h3>CIÊNCIA DA NOTIFICAÇÃO DA PENALIDADE</h3><p>Ciência efetiva registrada em 26/08/2026 (fictícia). Prazo de 30 dias.</p>" }
      ]
    },
    {
      numero: "2026.000802", papel: "REVISOR", placa: "JKL0M12", recorrente: "Carlos Fictício",
      relatorVoto: {
        autor: "Membro Fictício C (posição C)", quando: "14/10/2026 às 09:31", resultado: "MANUTENCAO_PENALIDADE",
        dispositivo: "CTB, art. 280 (requisitos do auto de infração)",
        texto: "O auto contém todos os requisitos. A alegação de erro na placa não se confirma pela foto. Voto pela manutenção da penalidade."
      },
      dossie: {
        veiculo: "JKL0M12 · automóvel fictício, vermelho",
        autuacao: "Avançar o sinal vermelho · 11/08/2026 · Av. Exemplo, 2000 · auto PROTO-001077",
        penalidade: "Multa (valor informado pelo sistema de multas)",
        historico: "Nenhuma outra autuação do veículo",
        tempestividade: { ok: true, texto: "Tempestivo: protocolado em 10/09/2026; prazo até 19/09/2026" },
        ocorrencias: "Nenhuma"
      },
      docs: [
        { nome: "Recurso do recorrente", html: "<h3>RECURSO À JARI</h3><p>Recorrente: Carlos Fictício</p><p>A placa da foto não é a do meu carro. Peço o cancelamento.</p>" },
        { nome: "Foto do equipamento", foto: "JKL0M12" }
      ]
    }
  ];

  var atual = 0, docAtual = 0, zoom = 100, assinados = [];

  /* ---------- Fila e cabeçalho do processo ---------- */
  function renderFila() {
    $("fila").innerHTML = PROCESSOS.map(function (p, i) {
      var cls = assinados[i] ? "feito" : "";
      return "<li class='" + cls + "'" + (i === atual ? " aria-current='true'" : "") + ">" +
        (i + 1) + ". " + p.numero + " · " + (p.papel === "RELATOR" ? "relator" : "revisor") + (assinados[i] ? " · assinado" : "") + "</li>";
    }).join("");
  }

  function carregar(i) {
    atual = i; docAtual = 0; zoom = 100;
    var p = PROCESSOS[i];
    renderFila();
    $("t-proc").textContent = "Processo " + p.numero + " (" + (i + 1) + " de " + PROCESSOS.length + ")";
    $("papel").textContent = p.papel === "RELATOR"
      ? "Você é o relator: escreva o relatório e o voto."
      : "Você é o revisor: o relator já assinou o voto dele. Acompanhe ou divirja.";
    renderDossie(p);
    renderAbas(p);
    renderDoc();
    renderFormulario(p);
    document.title = "Processo " + p.numero + " — Relatar e votar (protótipo SIREJ)";
  }

  function renderDossie(p) {
    var d = p.dossie;
    $("dossie").innerHTML = "<dl class='dados'>" +
      "<dt>Veículo</dt><dd>" + P.esc(d.veiculo) + "</dd>" +
      "<dt>Autuação</dt><dd>" + P.esc(d.autuacao) + "</dd>" +
      "<dt>Penalidade</dt><dd>" + P.esc(d.penalidade) + "</dd>" +
      "<dt>Histórico</dt><dd>" + P.esc(d.historico) + "</dd>" +
      "<dt>Prazo</dt><dd><span class='etiqueta " + (d.tempestividade.ok ? "etiqueta-verde" : "etiqueta-vermelha") + "'>" +
      (d.tempestividade.ok ? "Tempestivo" : "Fora do prazo") + "</span> " + P.esc(d.tempestividade.texto) + "</dd>" +
      "<dt>Ocorrências</dt><dd>" + P.esc(d.ocorrencias) + "</dd>" +
      "<dt>Recorrente</dt><dd>" + P.esc(p.recorrente) + " (fictício)</dd></dl>";
  }

  /* ---------- Visualizador com marca d'água (RN29) ---------- */
  function renderAbas(p) {
    $("abas").innerHTML = p.docs.map(function (doc, i) {
      return "<button type='button' role='tab' id='aba-" + i + "' aria-selected='" + (i === docAtual) + "' tabindex='" + (i === docAtual ? 0 : -1) + "' data-doc='" + i + "'>" + P.esc(doc.nome) + "</button>";
    }).join("");
    Array.prototype.forEach.call($("abas").querySelectorAll("[role=tab]"), function (b) {
      b.addEventListener("click", function () { docAtual = +b.getAttribute("data-doc"); renderAbas(p); renderDoc(); $("aba-" + docAtual).focus(); });
      b.addEventListener("keydown", function (e) {
        var n = p.docs.length;
        if (e.key === "ArrowRight" || e.key === "ArrowLeft") {
          e.preventDefault();
          docAtual = (docAtual + (e.key === "ArrowRight" ? 1 : n - 1)) % n;
          renderAbas(p); renderDoc(); $("aba-" + docAtual).focus();
        }
      });
    });
  }

  function fotoSvg(placa) {
    return "<svg viewBox='0 0 400 220' role='img' aria-label='Foto fictícia do veículo de placa " + placa + "' style='width:100%;height:auto;background:#cfd8dc'>" +
      "<rect x='60' y='70' width='280' height='90' rx='20' fill='#90a4ae'/><rect x='100' y='40' width='180' height='50' rx='12' fill='#b0bec5'/>" +
      "<circle cx='120' cy='165' r='24' fill='#263238'/><circle cx='280' cy='165' r='24' fill='#263238'/>" +
      "<rect x='150' y='120' width='100' height='28' fill='#fff' stroke='#263238'/><text x='200' y='140' font-family='monospace' font-size='16' text-anchor='middle' fill='#111'>" + placa + "</text>" +
      "<text x='10' y='210' font-family='monospace' font-size='12' fill='#111'>EQ-0001 · imagem sintética</text></svg>";
  }

  function marcaDagua() {
    // Marca d'água como imagem de fundo (SVG): visível em todas as páginas, não selecionável como texto.
    var t = MEMBRO.nome + " · " + MEMBRO.cpf + " · " + P.data(P.agora()) + " " + P.hora(P.agora()) + " · uso restrito";
    var svg = "<svg xmlns='http://www.w3.org/2000/svg' width='420' height='220'><text x='10' y='160' transform='rotate(-25 210 110)' " +
      "font-family='Arial, sans-serif' font-size='15' font-weight='700' fill='rgba(176,22,31,0.2)'>" + P.esc(t) + "</text></svg>";
    return "<div class='marca-dagua' aria-hidden='true' style=\"background-image:url(&quot;data:image/svg+xml;charset=utf-8," +
      encodeURIComponent(svg).replace(/'/g, "%27") + "&quot;)\"></div>";
  }

  function renderDoc() {
    var doc = PROCESSOS[atual].docs[docAtual];
    var corpo = doc.foto ? fotoSvg(doc.foto) : doc.html;
    $("visualizador").innerHTML = "<div class='pagina' style='transform:scale(" + zoom / 100 + ")'>" + corpo + marcaDagua() + "</div>";
    $("visualizador").setAttribute("aria-labelledby", "aba-" + docAtual);
    $("zoom-valor").textContent = zoom + "%";
  }
  $("zoom-mais").addEventListener("click", function () { zoom = Math.min(200, zoom + 25); renderDoc(); });
  $("zoom-menos").addEventListener("click", function () { zoom = Math.max(50, zoom - 25); renderDoc(); });

  /* ---------- Formulário de voto ---------- */
  function radiosResultado(nome, sel) {
    return RESULTADOS.map(function (r, i) {
      return "<label class='opcao' for='" + nome + "-" + i + "'><input type='radio' name='" + nome + "' id='" + nome + "-" + i + "' value='" + r.codigo + "'" + (sel === r.codigo ? " checked" : "") + ">" +
        "<span><strong>" + P.esc(r.rotulo) + "</strong><span class='dica'>" + (r.altera ? "Altera a penalidade: gera aviso ao sistema de multas." : "Não altera a penalidade.") + "</span></span></label>";
    }).join("");
  }

  function rotuloResultado(c) { return RESULTADOS.filter(function (r) { return r.codigo === c; })[0].rotulo; }

  function renderFormulario(p) {
    $("erros").innerHTML = "";
    $("editor").innerHTML = p.rascunho || "";
    $("editor").removeAttribute("aria-invalid");
    $("salvo").textContent = p.rascunho ? "Rascunho recuperado." : "";
    $("dispositivo").value = p.dispositivo || "";
    $("dispositivo").removeAttribute("aria-invalid");
    $("modelo").value = "";
    $("imp-motivo").value = ""; $("imp-texto").value = ""; $("imp-erro").classList.add("oculto");
    $("impedimento").open = false;

    if (p.papel === "RELATOR") {
      $("t-voto").textContent = "Seu relatório e voto";
      $("rotulo-editor").textContent = "Relatório e voto";
      $("area-revisor").innerHTML = "";
      $("area-checklist").classList.remove("oculto");
      $("checklist").innerHTML = EIXOS.map(function (e, i) {
        return "<fieldset class='eixo' id='eixo-" + i + "'><legend>" + P.esc(e.titulo) + "</legend><p class='dica'>" + P.esc(e.dica) + "</p><div class='opcoes-linha'>" +
          "<label class='opcao' for='ex" + i + "-a'><input type='radio' name='ex" + i + "' id='ex" + i + "-a' value='SEM_RESSALVA'><span>Verifiquei, sem ressalvas</span></label>" +
          "<label class='opcao' for='ex" + i + "-b'><input type='radio' name='ex" + i + "' id='ex" + i + "-b' value='COM_RESSALVA'><span>Verifiquei, com ressalvas (explico no voto)</span></label>" +
          "</div></fieldset>";
      }).join("");
      $("area-resultado").classList.remove("oculto");
      $("resultados").innerHTML = radiosResultado("resultado", null);
      $("area-texto").classList.remove("oculto");
      $("area-dispositivo").classList.remove("oculto");
    } else {
      var v = p.relatorVoto;
      $("t-voto").textContent = "Seu voto como revisor";
      $("rotulo-editor").textContent = "Fundamentação da divergência";
      $("area-checklist").classList.add("oculto");
      $("area-revisor").innerHTML =
        "<div class='voto-relator'><h3>Voto do relator</h3><p><strong>" + P.esc(rotuloResultado(v.resultado)) + "</strong></p>" +
        "<p>" + P.esc(v.texto) + "</p><p class='dica'>Dispositivo: " + P.esc(v.dispositivo) + " · Assinado por " + P.esc(v.autor) + " em " + P.esc(v.quando) + "</p></div>" +
        "<fieldset id='rev-escolha'><legend>Seu voto</legend>" +
        "<label class='opcao' for='rev-a'><input type='radio' name='rev' id='rev-a' value='ACOMPANHA'><span><strong>Acompanho o relator</strong><span class='dica'>Seu voto terá o mesmo resultado e fundamento.</span></span></label>" +
        "<label class='opcao' for='rev-d'><input type='radio' name='rev' id='rev-d' value='DIVERGE'><span><strong>Divirjo do relator</strong><span class='dica'>Escolha outro resultado e explique por quê.</span></span></label></fieldset>";
      $("resultados").innerHTML = radiosResultado("resultado", null);
      atualizarRevisor();
      Array.prototype.forEach.call(document.querySelectorAll("input[name=rev]"), function (r) { r.addEventListener("change", atualizarRevisor); });
    }
  }

  function atualizarRevisor() {
    var s = document.querySelector("input[name=rev]:checked");
    var diverge = s && s.value === "DIVERGE";
    ["area-texto", "area-resultado", "area-dispositivo"].forEach(function (id) { $(id).classList.toggle("oculto", !diverge); });
  }

  /* Editor: formatação restrita (negrito, itálico, lista) e salvamento automático simulado. */
  Array.prototype.forEach.call(document.querySelectorAll(".editor-barra [data-cmd]"), function (b) {
    b.addEventListener("mousedown", function (e) { e.preventDefault(); });
    b.addEventListener("click", function () { $("editor").focus(); document.execCommand(b.getAttribute("data-cmd")); salvarLogo(); });
  });
  $("modelo").addEventListener("change", function () {
    if (!this.value) return;
    var ed = $("editor");
    // Sem janela de confirmação (doc 11: sem modais): o modelo entra no fim do texto atual.
    ed.innerHTML += MODELOS[this.value];
    this.value = "";
    salvarLogo();
    ed.focus();
  });
  // Atalhos opcionais: Ctrl+B / Ctrl+I funcionam nativamente no contenteditable. Nenhum atalho é obrigatório.
  var tSalvar = null;
  function salvarLogo() {
    clearTimeout(tSalvar);
    $("salvo").textContent = "Salvando…";
    tSalvar = setTimeout(function () {
      PROCESSOS[atual].rascunho = $("editor").innerHTML;
      $("salvo").textContent = "Salvo automaticamente às " + P.hora(P.agora()) + ".";
    }, 800);
  }
  $("editor").addEventListener("input", salvarLogo);
  $("dispositivo").addEventListener("change", function () { PROCESSOS[atual].dispositivo = this.value; });

  /* ---------- Assinar e abrir o próximo (um clique) ---------- */
  $("form-voto").addEventListener("submit", function (e) {
    e.preventDefault();
    var p = PROCESSOS[atual];
    var erros = [];
    var marcar = function (el, invalido) { if (invalido) el.setAttribute("aria-invalid", "true"); else el.removeAttribute("aria-invalid"); };
    var resultado = document.querySelector("input[name=resultado]:checked");
    var texto = $("editor").textContent.trim();
    var exige = true;

    if (p.papel === "REVISOR") {
      var rev = document.querySelector("input[name=rev]:checked");
      if (!rev) erros.push({ id: "rev-a", msg: "Escolha se acompanha ou diverge do relator." });
      exige = rev && rev.value === "DIVERGE";
      if (exige && resultado && resultado.value === p.relatorVoto.resultado) {
        erros.push({ id: "resultado-0", msg: "Na divergência, escolha um resultado diferente do relator, ou marque \"Acompanho o relator\"." });
      }
    } else {
      EIXOS.forEach(function (ex, i) {
        if (!document.querySelector("input[name=ex" + i + "]:checked")) erros.push({ id: "ex" + i + "-a", msg: "Verificação \"" + ex.titulo + "\" sem resposta." });
      });
    }
    if (exige) {
      if (texto.length < 30) erros.push({ id: "editor", msg: p.papel === "RELATOR" ? "Escreva o relatório e o voto (o voto do relator precisa ser motivado)." : "Explique a divergência." });
      if (!resultado) erros.push({ id: "resultado-0", msg: "Escolha o resultado." });
      if (!$("dispositivo").value) erros.push({ id: "dispositivo", msg: "Escolha o dispositivo normativo." });
      marcar($("editor"), texto.length < 30);
      marcar($("dispositivo"), !$("dispositivo").value);
    }

    if (erros.length) {
      $("erros").innerHTML = "<div class='resumo-erros' role='alert'><strong>Falta completar " + erros.length + (erros.length === 1 ? " item" : " itens") + " para assinar:</strong><ul>" +
        erros.map(function (x) { return "<li><a href='#" + x.id + "'>" + P.esc(x.msg) + "</a></li>"; }).join("") + "</ul></div>";
      Array.prototype.forEach.call($("erros").querySelectorAll("a"), function (a) {
        a.addEventListener("click", function (ev) { ev.preventDefault(); var alvo = $(a.getAttribute("href").slice(1)); P.focar(alvo); alvo.scrollIntoView({ block: "center" }); });
      });
      P.focar($("erros"));
      return;
    }

    var desc;
    if (p.papel === "REVISOR" && !exige) desc = "acompanhando o relator (" + rotuloResultado(p.relatorVoto.resultado) + ")";
    else desc = rotuloResultado(resultado.value);
    assinados[atual] = { numero: p.numero, papel: p.papel, desc: desc, quando: P.hora(P.agora()) };
    P.marco("Assinado " + p.numero);
    proximo("Voto do processo " + p.numero + " assinado às " + assinados[atual].quando + ": " + desc + ".");
  });

  function proximo(msg) {
    var seguinte = -1;
    for (var i = 0; i < PROCESSOS.length; i++) if (!assinados[i]) { seguinte = i; break; }
    $("aviso").innerHTML = "<div class='msg msg-sucesso'><p>" + P.esc(msg) + "</p></div>";
    if (seguinte < 0) { fim(); return; }
    carregar(seguinte);
    window.scrollTo(0, 0);
    P.focar($("t-proc"));
  }

  /* ---------- Impedimento / suspeição (RN34) ---------- */
  $("imp-enviar").addEventListener("click", function () {
    if (!$("imp-motivo").value || $("imp-texto").value.trim().length < 5) { $("imp-erro").classList.remove("oculto"); P.focar($("imp-motivo")); return; }
    var p = PROCESSOS[atual];
    assinados[atual] = { numero: p.numero, papel: p.papel, desc: "impedimento/suspeição declarado (" + $("imp-motivo").value + ")", quando: P.hora(P.agora()) };
    proximo("Declaração assinada no processo " + p.numero + ". A turma será recomposta pelo sistema.");
  });

  function fim() {
    $("tela-trabalho").classList.add("oculto");
    $("tela-fim").classList.remove("oculto");
    $("fim-resumo").innerHTML = "<table><caption>Seus votos nesta sessão</caption><thead><tr><th scope='col'>Processo</th><th scope='col'>Papel</th><th scope='col'>Voto</th><th scope='col'>Assinado às</th></tr></thead><tbody>" +
      assinados.map(function (a) { return "<tr><td>" + a.numero + "</td><td>" + (a.papel === "RELATOR" ? "Relator" : "Revisor") + "</td><td>" + P.esc(a.desc) + "</td><td>" + a.quando + "</td></tr>"; }).join("") +
      "</tbody></table>";
    document.title = "Processos concluídos — Relatar e votar (protótipo SIREJ)";
    P.focar($("t-fim"));
  }

  carregar(0);
})();
