/*
 * Painel do presidente — protótipo navegável da condução da sessão.
 * Roteiro, presenças e quórum vêm de config/regimentos/sp.yaml (sessao.*, votacao.*, turmas.*).
 * Dados fictícios. Nada é enviado a servidor algum.
 */
(function () {
  "use strict";
  var P = window.Proto;
  var $ = function (id) { return document.getElementById(id); };

  /* ---------- Configuração (espelha sp.yaml) ---------- */
  var ROTEIRO = [ // sessao.roteiro
    { passo: "ABERTURA", titulo: "Abertura", obrigatorio: true },
    { passo: "APROVACAO_ATA_ANTERIOR", titulo: "Aprovação da ata anterior", obrigatorio: false },
    { passo: "DISTRIBUICAO_INTERNA_E_TURMAS", titulo: "Distribuição interna e turmas", obrigatorio: true },
    { passo: "DISTRIBUICAO_AOS_MEMBROS", titulo: "Distribuição aos membros", obrigatorio: true },
    { passo: "JULGAMENTO", titulo: "Julgamento", obrigatorio: true },
    { passo: "PROPOSICOES", titulo: "Proposições", obrigatorio: false },
    { passo: "ENCERRAMENTO", titulo: "Encerramento", obrigatorio: true }
  ];
  var QUORUM = { minimoMembros: 3, segmentosDistintos: 3 };           // sessao.quorumAbertura
  var VOTOS_MINIMOS = 3;                                               // votacao.votosMinimos
  var EXCECAO = { permitida: true, minimo: 2, exigePresidenteOuVice: true }; // votacao.excecaoMaioriaSimples
  var SEGMENTOS = { COMUNIDADE: "Comunidade", ENTIDADE_EXECUTIVA: "Entidade executiva", SOCIEDADE_CIVIL: "Sociedade civil" };
  var RESULTADOS = {
    REJEICAO_ADMINISTRATIVA: "Rejeição administrativa",
    NAO_CONHECIMENTO_INTEMPESTIVIDADE: "Não conhecimento (intempestividade)",
    NAO_CONHECIMENTO_ILEGITIMIDADE: "Não conhecimento (ilegitimidade)",
    MANUTENCAO_PENALIDADE: "Manutenção da penalidade",
    CANCELAMENTO_PENALIDADE: "Cancelamento da penalidade"
  };

  var POSICOES = [
    { letra: "A", segmento: "COMUNIDADE", nome: "Membro Fictício A", papel: "presidente", voce: true },
    { letra: "B", segmento: "COMUNIDADE", nome: "Membro Fictício B" },
    { letra: "C", segmento: "ENTIDADE_EXECUTIVA", nome: "Membro Fictício C" },
    { letra: "D", segmento: "ENTIDADE_EXECUTIVA", nome: "Membro Fictício D", papel: "vice-presidente" },
    { letra: "E", segmento: "SOCIEDADE_CIVIL", nome: "Membro Fictício E" },
    { letra: "F", segmento: "SOCIEDADE_CIVIL", nome: "Membro Fictício F" }
  ];
  var PARTICOES = [ // as 4 partições possíveis com 6 posições (doc 07, seção 5)
    [["A", "C", "E"], ["B", "D", "F"]],
    [["A", "C", "F"], ["B", "D", "E"]],
    [["A", "D", "E"], ["B", "C", "F"]],
    [["A", "D", "F"], ["B", "C", "E"]]
  ];
  var USADAS_NO_CICLO = [0, 1];    // histórico fictício
  var PARTICAO_SORTEADA = 2;       // fixa para o teste coincidir com a tela do relator

  /* Designação selada (só é "decifrada" na abertura). Votos simulados por processo: relator, revisor, 3º. */
  var M = "MANUTENCAO_PENALIDADE", C = "CANCELAMENTO_PENALIDADE", R = "REJEICAO_ADMINISTRATIVA",
      T = "NAO_CONHECIMENTO_INTEMPESTIVIDADE", L = "NAO_CONHECIMENTO_ILEGITIMIDADE";
  var SELADO = [
    { numero: "2026.000770", pos: "A", seq: 1, votos: [M, M, M] },
    { numero: "2026.000771", pos: "A", seq: 2, votos: [C, C, M] },
    { numero: "2026.000777", pos: "B", seq: 1, votos: [M, M, C] },
    { numero: "2026.000781", pos: "B", seq: 2, votos: [C, C, C] },
    { numero: "2026.000790", pos: "B", seq: 3, votos: [T, T, T] },
    { numero: "2026.000802", pos: "C", seq: 1, votos: [M, M, M] },
    { numero: "2026.000803", pos: "C", seq: 2, votos: [M, C, R] },
    { numero: "2026.000810", pos: "D", seq: 1, votos: [L, L, L] },
    { numero: "2026.000815", pos: "E", seq: 1, votos: [M, M, M] },
    { numero: "2026.000816", pos: "E", seq: 2, votos: [C, M, M] },
    { numero: "2026.000820", pos: "F", seq: 1, votos: [R, R, R] },
    { numero: "2026.000821", pos: "F", seq: 2, votos: [M, C, M] }
  ];
  var COMPROMISSO = P.hashTexto("sal-ficticio|" + JSON.stringify(SELADO.map(function (s) { return [s.numero, 14, s.pos, s.seq]; })) + "|semente-ficticia");

  /* ---------- Estado ---------- */
  var st = {
    atual: 0, feito: {}, pulado: {}, presentes: { A: true },
    aberta: false, abertaEm: null, processos: [], turmas: null, liberado: false, encerrada: false, proposicoes: ""
  };

  function pos(l) { return POSICOES.filter(function (p) { return p.letra === l; })[0]; }
  function presente(l) { return !!st.presentes[l]; }

  function quorum() {
    var ps = POSICOES.filter(function (p) { return presente(p.letra); });
    var segs = {};
    ps.forEach(function (p) { segs[p.segmento] = 1; });
    var nSeg = Object.keys(segs).length;
    return { membros: ps.length, segmentos: nSeg, ok: ps.length >= QUORUM.minimoMembros && nSeg >= QUORUM.segmentosDistintos };
  }

  function presidenteOuVicePresente() { return presente("A") || presente("D"); }

  /* ---------- Roteiro ---------- */
  function podeIr(i) {
    if (i === st.atual) return true;
    if (st.feito[i] || st.pulado[i]) return true;
    // Só pode ir para o próximo passo se todos os anteriores obrigatórios estiverem concluídos.
    for (var k = 0; k < i; k++) if (!st.feito[k] && !st.pulado[k]) return false;
    return true;
  }

  function renderRoteiro() {
    $("roteiro").innerHTML = ROTEIRO.map(function (r, i) {
      var estado = st.feito[i] ? "concluído" : st.pulado[i] ? "pulado" : i === st.atual ? "em andamento" : "pendente";
      var cls = st.feito[i] ? "feito" : st.pulado[i] ? "pulado" : "";
      return "<li><button type='button' data-passo='" + i + "' class='" + cls + "'" + (i === st.atual ? " aria-current='step'" : "") + (podeIr(i) ? "" : " disabled") + ">" +
        (i + 1) + ". " + P.esc(r.titulo) + "<span class='tipo'>" + (r.obrigatorio ? "Obrigatório" : "Opcional") + " · " + estado + "</span></button></li>";
    }).join("");
    Array.prototype.forEach.call($("roteiro").querySelectorAll("button"), function (b) {
      b.addEventListener("click", function () { irPara(+b.getAttribute("data-passo")); });
    });
  }

  function irPara(i) {
    st.atual = i;
    renderRoteiro();
    renderPasso();
    P.focar($("t-passo"));
  }

  function concluir(i) {
    st.feito[i] = true;
    P.marco("Passo " + (i + 1) + " concluído");
    if (i + 1 < ROTEIRO.length) irPara(i + 1); else { renderRoteiro(); renderPasso(); }
  }

  function aviso(html, tipo) { $("aviso").innerHTML = "<div class='msg msg-" + (tipo || "sucesso") + "'><p>" + html + "</p></div>"; }

  function rodapePasso(i, podeConcluir, rotulo, motivo) {
    var r = ROTEIRO[i];
    if (st.feito[i]) return "<p class='msg msg-sucesso'>Passo concluído.</p>" + (i + 1 < ROTEIRO.length ? "<div class='acoes'><button type='button' class='btn' id='btn-seguir'>Ir para o passo " + (i + 2) + "</button></div>" : "");
    var h = "<div class='acoes'>";
    if (rotulo) {
      h += "<button type='button' class='btn btn-primario' id='btn-concluir'" + (podeConcluir ? "" : " aria-disabled='true' aria-describedby='motivo-bloqueio'") + ">" + rotulo + "</button>";
    }
    if (!r.obrigatorio) h += "<button type='button' class='btn' id='btn-pular'>Pular este passo (opcional)</button>";
    h += "</div>";
    if (!podeConcluir && motivo) h += "<p class='erro-campo' id='motivo-bloqueio'>" + motivo + "</p>";
    return h;
  }

  function ligarRodape(i, podeConcluir, acao) {
    var s = $("btn-seguir"); if (s) s.addEventListener("click", function () { irPara(i + 1); });
    var c = $("btn-concluir");
    if (c) c.addEventListener("click", function () {
      if (!podeConcluir) { P.focar($("motivo-bloqueio")); return; }
      if (acao) acao(); else concluir(i);
    });
    var p = $("btn-pular");
    if (p) p.addEventListener("click", function () { st.pulado[i] = true; irPara(i + 1); });
  }

  /* ---------- Passos ---------- */
  function renderPasso() {
    var r = ROTEIRO[st.atual];
    document.title = r.titulo + " — Painel do presidente (protótipo SIREJ)";
    var fn = {
      ABERTURA: passoAbertura, APROVACAO_ATA_ANTERIOR: passoAta, DISTRIBUICAO_INTERNA_E_TURMAS: passoTurmas,
      DISTRIBUICAO_AOS_MEMBROS: passoMembros, JULGAMENTO: passoJulgamento, PROPOSICOES: passoProposicoes, ENCERRAMENTO: passoEncerramento
    }[r.passo];
    fn(st.atual);
  }

  function cabecalho(i, texto) {
    var r = ROTEIRO[i];
    return "<h2 id='t-passo'>" + (i + 1) + ". " + P.esc(r.titulo) + " <span class='etiqueta " + (r.obrigatorio ? "etiqueta-azul" : "") + "'>" + (r.obrigatorio ? "Obrigatório" : "Opcional") + "</span></h2>" +
      (texto ? "<p>" + texto + "</p>" : "");
  }

  /* 1. Abertura: presenças, quórum e revelação */
  function passoAbertura(i) {
    var q = quorum();
    var h = cabecalho(i, "Registre quem está presente. A sessão só abre com quórum, e só então a distribuição da semana é revelada.");
    h += "<div class='tabela-wrap'><table><caption>Presenças</caption><thead><tr><th scope='col'>Presente</th><th scope='col'>Posição</th><th scope='col'>Membro</th><th scope='col'>Segmento</th></tr></thead><tbody>";
    POSICOES.forEach(function (p) {
      var id = "pres-" + p.letra;
      h += "<tr><td><input type='checkbox' id='" + id + "' data-pos='" + p.letra + "'" + (presente(p.letra) ? " checked" : "") + ((st.aberta || p.voce) ? " disabled" : "") + ">" +
        "<label class='sr-only' for='" + id + "'>" + P.esc(p.nome) + " presente</label></td>" +
        "<td>" + p.letra + "</td><td>" + P.esc(p.nome) + (p.papel ? " <span class='etiqueta'>" + p.papel + "</span>" : "") + (p.voce ? " (você)" : "") + "</td><td>" + SEGMENTOS[p.segmento] + "</td></tr>";
    });
    h += "</tbody></table></div>";
    h += "<div class='msg " + (q.ok ? "msg-sucesso" : "msg-alerta") + "' id='quorum' aria-live='polite'><div class='quorum'>" +
      "<span class='grande'>" + (q.ok ? "Quórum atingido" : "Sem quórum") + "</span>" +
      "<span>" + q.membros + " presentes · " + q.segmentos + " de 3 segmentos</span>" +
      "<span class='dica' style='margin:0'>Mínimo: " + QUORUM.minimoMembros + " membros de " + QUORUM.segmentosDistintos + " segmentos diferentes (art. 13).</span></div></div>";

    if (!st.aberta) {
      h += "<div class='selo'><p><strong>Distribuição da semana: selada.</strong> Antes da abertura, ninguém (nem o presidente, a secretaria ou o administrador) sabe quais processos são desta junta.</p>" +
        "<p class='dica' style='margin:0'>Compromisso do lote 2026-S41 (público): <span class='mono'>" + COMPROMISSO + "</span></p></div>";
      h += "<p class='dica'>Ao abrir, o sistema pede sua confirmação com segundo fator de autenticação (simulado aqui).</p>";
      h += rodapePasso(i, q.ok, "Abrir sessão e revelar a distribuição", "Sem quórum a sessão não abre e nada é revelado. Marque as presenças.");
    } else {
      h += "<div class='selo'><p><strong>Sessão aberta às " + st.abertaEm + ".</strong> Distribuição revelada: " + st.processos.length + " processos desta junta.</p>" +
        "<p class='dica' style='margin:0'>Conferência do selo: o compromisso <span class='mono'>" + COMPROMISSO.slice(0, 16) + "…</span> confere com os dados revelados.</p></div>";
      h += rodapePasso(i, true);
    }
    $("passo").innerHTML = h;
    Array.prototype.forEach.call(document.querySelectorAll("[data-pos]"), function (cb) {
      cb.addEventListener("change", function () {
        st.presentes[cb.getAttribute("data-pos")] = cb.checked;
        var foco = cb.id;
        passoAbertura(i);
        $(foco).focus();
      });
    });
    ligarRodape(i, q.ok, abrirSessao);
  }

  function abrirSessao() {
    // Transação única (doc 07, seção 4): presenças, quórum, revelação, partição, mapeamento.
    st.aberta = true;
    st.abertaEm = P.hora(P.agora());
    st.turmas = PARTICOES[PARTICAO_SORTEADA];
    st.processos = SELADO.map(function (s) { return montarProcesso(s); });
    aviso("Sessão aberta às " + st.abertaEm + ". A distribuição foi revelada e a abertura ficou registrada na trilha de auditoria.");
    concluir(0);
  }

  function turmaDe(letra) { return st.turmas.filter(function (t) { return t.indexOf(letra) >= 0; })[0]; }

  function substituto(letra) {
    // RN35: ausente substituído por presente do mesmo segmento de outra turma, no 2º ou 3º voto.
    var seg = pos(letra).segmento;
    var outra = st.turmas.filter(function (t) { return t.indexOf(letra) < 0; })[0];
    var cand = outra.filter(function (l) { return pos(l).segmento === seg && presente(l); })[0];
    return cand || null;
  }

  function montarProcesso(s) {
    var p = { numero: s.numero, pos: s.pos, seq: s.seq, script: s.votos, votos: [null, null, null], proclamado: null, retirado: null };
    if (!presente(s.pos)) {
      p.retirado = "Relator ausente: volta à pauta da próxima sessão, mesma posição.";
      p.assentos = [];
      return p;
    }
    var t = turmaDe(s.pos);
    var outros = t.filter(function (l) { return l !== s.pos; });
    p.assentos = [{ papel: "Relator", letra: s.pos }];
    ["Revisor", "3º membro"].forEach(function (papel, k) {
      var l = outros[k];
      if (presente(l)) p.assentos.push({ papel: papel, letra: l });
      else {
        var sub = substituto(l);
        p.assentos.push({ papel: papel, letra: sub, substitui: l });
      }
    });
    return p;
  }

  /* 2. Ata anterior (opcional) */
  function passoAta(i) {
    $("passo").innerHTML = cabecalho(i, "Ata da sessão de 07/10/2026 (fictícia), já disponibilizada aos membros.") +
      rodapePasso(i, true, "Registrar ata como aprovada");
    ligarRodape(i, true);
  }

  /* 3. Distribuição interna e turmas */
  function nomeAssento(a) {
    if (!a.letra) return "<span class='etiqueta etiqueta-vermelha'>Vago</span> (sem substituto do mesmo segmento)";
    var p = pos(a.letra);
    return P.esc(p.nome) + " (" + a.letra + ")" + (a.substitui ? " <span class='etiqueta etiqueta-amarela'>substitui " + a.substitui + "</span>" : "");
  }

  function passoTurmas(i) {
    var h = cabecalho(i, "O sistema sorteou a divisão das turmas entre as que ainda não foram usadas neste ciclo (rodízio, art. 17 §3). Ninguém escolhe as turmas.");
    h += "<p class='dica'>Divisões já usadas no ciclo: " + USADAS_NO_CICLO.length + " de 4. Sorteada agora: divisão " + (PARTICAO_SORTEADA + 1) + ".</p><div class='turmas'>";
    st.turmas.forEach(function (t, k) {
      h += "<div class='cartao' style='box-shadow:none;border:1px solid var(--cinza-20)'><h3>Turma " + (k + 1) + "</h3><ul>";
      t.forEach(function (l) {
        var p = pos(l);
        h += "<li>" + l + " · " + P.esc(p.nome) + " · " + SEGMENTOS[p.segmento] + " " +
          (presente(l) ? "<span class='etiqueta etiqueta-verde'>presente</span>" : "<span class='etiqueta etiqueta-vermelha'>ausente</span>") + "</li>";
      });
      h += "</ul></div>";
    });
    h += "</div>";
    var ausentes = POSICOES.filter(function (p) { return !presente(p.letra); });
    if (ausentes.length) {
      h += "<div class='msg msg-alerta'><p>Ausentes: " + ausentes.map(function (p) { return p.letra; }).join(", ") +
        ". Os processos dessas posições voltam à próxima sessão. Nos 2º e 3º votos, o ausente é substituído por um presente do mesmo segmento da outra turma.</p></div>";
    }
    h += rodapePasso(i, true, "Registrar turmas e seguir");
    $("passo").innerHTML = h;
    ligarRodape(i, true);
  }

  /* 4. Distribuição aos membros */
  function passoMembros(i) {
    var h = cabecalho(i, "Pauta da sessão, na ordem de posição e sequência. Ao liberar, cada membro passa a ver os seus processos.");
    h += "<div class='tabela-wrap'><table><caption>Pauta revelada</caption><thead><tr><th scope='col'>Posição / seq.</th><th scope='col'>Processo</th><th scope='col'>Relator</th><th scope='col'>Revisor</th><th scope='col'>3º membro</th></tr></thead><tbody>";
    st.processos.forEach(function (p) {
      h += "<tr><td>" + p.pos + " / " + p.seq + "</td><td>" + p.numero + "</td>";
      if (p.retirado) h += "<td colspan='3'><span class='etiqueta etiqueta-amarela'>Retirado de pauta</span> " + P.esc(p.retirado) + "</td>";
      else h += p.assentos.map(function (a) { return "<td>" + nomeAssento(a) + "</td>"; }).join("");
      h += "</tr>";
    });
    h += "</tbody></table></div>";
    h += rodapePasso(i, true, "Liberar processos aos membros");
    $("passo").innerHTML = h;
    ligarRodape(i, true, function () { st.liberado = true; aviso("Processos liberados aos membros às " + P.hora(P.agora()) + "."); concluir(i); });
  }

  /* 5. Julgamento: acompanhamento dos votos e proclamação */
  function apurar(p) {
    if (p.retirado) return { tipo: "retirado", texto: p.retirado };
    if (p.proclamado) return { tipo: "proclamado", texto: p.proclamado };
    var validos = p.assentos.filter(function (a) { return a.letra; });
    var assinados = p.votos.filter(function (v, k) { return v && p.assentos[k] && p.assentos[k].letra; });
    if (assinados.length < validos.length) return { tipo: "aguardando", texto: "Aguardando votos (" + assinados.length + " de " + validos.length + ")" };
    var cont = {};
    assinados.forEach(function (v) { cont[v] = (cont[v] || 0) + 1; });
    var maior = Object.keys(cont).sort(function (a, b) { return cont[b] - cont[a]; })[0];
    var n = assinados.length;
    if (n >= VOTOS_MINIMOS) {
      if (cont[maior] > n / 2) return { tipo: "pronto", resultado: maior, forma: cont[maior] === n ? "unanimidade" : "maioria (" + cont[maior] + " a " + (n - cont[maior]) + ")" };
      return { tipo: "sem-maioria", texto: "Sem maioria: nenhum resultado teve mais da metade dos votos. Não há voto de qualidade nem desempate." };
    }
    if (EXCECAO.permitida && n >= EXCECAO.minimo && (!EXCECAO.exigePresidenteOuVice || presidenteOuVicePresente())) {
      if (cont[maior] === n) return { tipo: "pronto", resultado: maior, forma: "exceção de 2 votos concordes (art. 6º §3)" };
      return { tipo: "sem-maioria", texto: "Apenas 2 votos e eles divergem: o processo volta à pauta." };
    }
    return { tipo: "sem-maioria", texto: "Votos insuficientes para decidir." };
  }

  function celulaVoto(p, k) {
    var a = p.assentos[k];
    if (!a) return "<td>—</td>";
    if (!a.letra) return "<td>" + nomeAssento(a) + "</td>";
    var v = p.votos[k];
    return "<td>" + P.esc(pos(a.letra).nome) + " (" + a.letra + ")" + (a.substitui ? " <span class='etiqueta etiqueta-amarela'>substitui " + a.substitui + "</span>" : "") + "<br>" +
      (v ? "<span class='voto-ok'>Assinado: " + P.esc(RESULTADOS[v]) + "</span>" : "<span class='voto-pend'>" + (k > 0 && !p.votos[k - 1] ? "Aguarda o voto anterior" : "Aguardando") + "</span>") + "</td>";
  }

  function passoJulgamento(i) {
    var h = cabecalho(i, "Acompanhe os votos. Cada voto tem o mesmo peso. O resultado é apurado pelo sistema a partir dos votos assinados; você proclama.");
    h += "<div class='acoes nao-imprimir' style='margin-top:0'><button type='button' class='btn' id='simular'>Simular a próxima rodada de votos (moderador)</button>" +
      "<span class='dica'>Os votos chegam em sequência: relator, depois revisor, depois 3º membro.</span></div>";
    h += "<div class='tabela-wrap'><table><caption>Votação</caption><thead><tr><th scope='col'>Processo</th><th scope='col'>Relator</th><th scope='col'>Revisor</th><th scope='col'>3º membro</th><th scope='col'>Apuração</th></tr></thead><tbody>";
    var prontos = 0, pendentes = 0;
    st.processos.forEach(function (p, idx) {
      var ap = apurar(p);
      h += "<tr><th scope='row'>" + p.numero + "<br><span class='dica'>" + p.pos + " / " + p.seq + "</span></th>";
      if (p.retirado && !p.assentos.length) h += "<td colspan='3'>—</td>";
      else h += celulaVoto(p, 0) + celulaVoto(p, 1) + celulaVoto(p, 2);
      h += "<td>";
      if (ap.tipo === "pronto") {
        prontos++;
        h += "<strong>" + P.esc(RESULTADOS[ap.resultado]) + "</strong><br><span class='dica'>" + P.esc(ap.forma) + "</span><br>" +
          "<button type='button' class='btn btn-primario' data-proclamar='" + idx + "'>Proclamar <span class='sr-only'>resultado do processo " + p.numero + "</span></button>";
      } else if (ap.tipo === "sem-maioria") {
        h += "<span class='etiqueta etiqueta-vermelha'>Não pode ser proclamado</span><br>" + P.esc(ap.texto) +
          "<br><button type='button' class='btn' data-retirar='" + idx + "'>Retirar de pauta <span class='sr-only'>o processo " + p.numero + "</span></button>";
      } else if (ap.tipo === "proclamado") {
        h += "<span class='etiqueta etiqueta-verde'>Proclamado</span><br>" + P.esc(ap.texto);
      } else if (ap.tipo === "retirado") {
        h += "<span class='etiqueta etiqueta-amarela'>Retirado de pauta</span><br>" + P.esc(ap.texto);
      } else { pendentes++; h += P.esc(ap.texto); }
      h += "</td></tr>";
    });
    h += "</tbody></table></div>";
    var abertos = st.processos.filter(function (p) { var t = apurar(p).tipo; return t !== "proclamado" && t !== "retirado"; }).length;
    h += "<p>" + abertos + " processo(s) ainda sem desfecho nesta sessão.</p>";
    h += rodapePasso(i, true, "Concluir julgamentos e seguir");
    if (abertos && !st.feito[i]) h += "<p class='dica'>Se seguir agora, os processos sem desfecho serão retirados de pauta no encerramento e voltam na próxima sessão.</p>";
    $("passo").innerHTML = h;
    $("simular").addEventListener("click", function () { simularRodada(); passoJulgamento(i); P.focar($("simular")); });
    Array.prototype.forEach.call(document.querySelectorAll("[data-proclamar]"), function (b) {
      b.addEventListener("click", function () {
        var p = st.processos[+b.getAttribute("data-proclamar")];
        var ap = apurar(p);
        p.proclamado = RESULTADOS[ap.resultado] + " (" + ap.forma + "), às " + P.hora(P.agora());
        aviso("Resultado do processo " + p.numero + " proclamado: " + RESULTADOS[ap.resultado] + ". O acórdão será gerado para assinatura.");
        passoJulgamento(i);
        P.focar($("t-passo"));
      });
    });
    Array.prototype.forEach.call(document.querySelectorAll("[data-retirar]"), function (b) {
      b.addEventListener("click", function () {
        var p = st.processos[+b.getAttribute("data-retirar")];
        p.retirado = "Sem maioria (D-37): volta à pauta da próxima sessão, mesma posição.";
        aviso("Processo " + p.numero + " retirado de pauta. O motivo fica registrado nos autos.", "alerta");
        passoJulgamento(i);
        P.focar($("t-passo"));
      });
    });
    ligarRodape(i, true);
  }

  function simularRodada() {
    st.processos.forEach(function (p) {
      if (p.retirado || p.proclamado) return;
      for (var k = 0; k < p.assentos.length; k++) {
        if (!p.assentos[k].letra) continue;
        if (!p.votos[k]) { p.votos[k] = p.script[k]; break; }
      }
    });
    aviso("Nova rodada de votos simulada.", "info");
  }

  /* 6. Proposições (opcional) */
  function passoProposicoes(i) {
    var h = cabecalho(i, "Registre as proposições dos membros, se houver. O texto vai para a ata.");
    h += "<div class='campo'><label for='prop'>Proposições</label><textarea id='prop'>" + P.esc(st.proposicoes) + "</textarea></div>";
    h += rodapePasso(i, true, "Registrar proposições");
    $("passo").innerHTML = h;
    $("prop").addEventListener("input", function () { st.proposicoes = this.value; });
    ligarRodape(i, true);
  }

  /* 7. Encerramento */
  function passoEncerramento(i) {
    var proc = st.processos.filter(function (p) { return p.proclamado; }).length;
    var ret = st.processos.filter(function (p) { return p.retirado; }).length;
    var sem = st.processos.length - proc - ret;
    var h = cabecalho(i);
    if (!st.encerrada) {
      h += "<dl class='dados'><dt>Proclamados</dt><dd>" + proc + "</dd><dt>Retirados de pauta</dt><dd>" + ret + "</dd><dt>Sem desfecho</dt><dd>" + sem + (sem ? " (serão retirados de pauta ao encerrar e voltam na próxima sessão)" : "") + "</dd></dl>";
      h += rodapePasso(i, true, "Encerrar sessão");
      $("passo").innerHTML = h;
      ligarRodape(i, true, function () {
        st.processos.forEach(function (p) { if (!p.proclamado && !p.retirado) p.retirado = "Sessão encerrada sem julgamento: volta à pauta da próxima sessão."; });
        st.encerrada = true;
        aviso("Sessão encerrada às " + P.hora(P.agora()) + ". A minuta da ata foi enviada à secretaria para conferência e assinatura.");
        concluir(i);
      });
    } else {
      h += "<p class='msg msg-sucesso'>Sessão encerrada. " + proc + " resultado(s) proclamado(s); " + (ret) + " processo(s) retirado(s) de pauta.</p>";
      $("passo").innerHTML = h;
    }
  }

  renderRoteiro();
  renderPasso();
})();
