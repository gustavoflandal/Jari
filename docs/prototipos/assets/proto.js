/*
 * SIREJ — utilitários comuns dos protótipos.
 * Sem dependências, funciona em file://. Nenhum dado sai do navegador.
 */
(function () {
  "use strict";

  var Proto = {};

  /* ---------- Datas (fuso fixo do protótipo: data simulada) ---------- */
  // "Hoje" fixo para que todas as sessões de teste vejam os mesmos prazos.
  Proto.HOJE = new Date(2026, 9, 6, 10, 0, 0); // 06/10/2026 10:00

  Proto.agora = function () {
    // Mantém a data simulada e usa o relógio real só para hora/minuto.
    var r = new Date();
    var d = new Date(Proto.HOJE.getTime());
    d.setHours(r.getHours(), r.getMinutes(), r.getSeconds());
    return d;
  };

  function dois(n) { return (n < 10 ? "0" : "") + n; }
  Proto.data = function (d) { return dois(d.getDate()) + "/" + dois(d.getMonth() + 1) + "/" + d.getFullYear(); };
  Proto.dataCurta = function (d) { return dois(d.getDate()) + "/" + dois(d.getMonth() + 1); };
  Proto.hora = function (d) { return dois(d.getHours()) + ":" + dois(d.getMinutes()); };
  Proto.dataHora = function (d) { return Proto.data(d) + " às " + Proto.hora(d) + ":" + dois(d.getSeconds()); };
  Proto.somaDias = function (d, n) { var x = new Date(d.getTime()); x.setDate(x.getDate() + n); return x; };

  /* ---------- SHA-256 (implementação pequena, para o recibo) ---------- */
  // crypto.subtle não existe em file:// em alguns navegadores; por isso a versão própria.
  Proto.sha256 = function (ascii) {
    function rr(v, a) { return (v >>> a) | (v << (32 - a)); }
    var maxWord = Math.pow(2, 32), result = "", words = [], len8 = ascii.length * 8;
    var hash = [], k = [], primeCounter = 0, isComposite = {};
    for (var candidate = 2; primeCounter < 64; candidate++) {
      if (!isComposite[candidate]) {
        for (var i = 0; i < 313; i += candidate) isComposite[i] = candidate;
        hash[primeCounter] = (Math.pow(candidate, 0.5) * maxWord) | 0;
        k[primeCounter++] = (Math.pow(candidate, 1 / 3) * maxWord) | 0;
      }
    }
    hash = hash.slice(0, 8);
    ascii += "\x80";
    while (ascii.length % 64 - 56) ascii += "\x00";
    for (i = 0; i < ascii.length; i++) {
      var j = ascii.charCodeAt(i);
      if (j >> 8) return null;
      words[i >> 2] |= j << ((3 - i) % 4) * 8;
    }
    words[words.length] = ((len8 / maxWord) | 0);
    words[words.length] = (len8);
    for (j = 0; j < words.length;) {
      var w = words.slice(j, j += 16), oldHash = hash;
      hash = hash.slice(0, 8);
      for (i = 0; i < 64; i++) {
        var w15 = w[i - 15], w2 = w[i - 2], a = hash[0], e = hash[4];
        var temp1 = hash[7] + (rr(e, 6) ^ rr(e, 11) ^ rr(e, 25)) + ((e & hash[5]) ^ ((~e) & hash[6])) + k[i] +
          (w[i] = (i < 16) ? w[i] : (w[i - 16] + (rr(w15, 7) ^ rr(w15, 18) ^ (w15 >>> 3)) + w[i - 7] + (rr(w2, 17) ^ rr(w2, 19) ^ (w2 >>> 10))) | 0);
        var temp2 = (rr(a, 2) ^ rr(a, 13) ^ rr(a, 22)) + ((a & hash[1]) ^ (a & hash[2]) ^ (hash[1] & hash[2]));
        hash = [(temp1 + temp2) | 0].concat(hash);
        hash[4] = (hash[4] + temp1) | 0;
      }
      for (i = 0; i < 8; i++) hash[i] = (hash[i] + oldHash[i]) | 0;
    }
    for (i = 0; i < 8; i++) for (j = 3; j + 1; j--) {
      var b = (hash[i] >> (j * 8)) & 255;
      result += ((b < 16) ? 0 : "") + b.toString(16);
    }
    return result;
  };
  Proto.hashTexto = function (s) {
    // Converte para UTF-8 em bytes "ascii" antes do hash.
    return Proto.sha256(unescape(encodeURIComponent(s)));
  };

  /* ---------- Avisos acessíveis ---------- */
  Proto.anunciar = function (texto) {
    var el = document.getElementById("anuncio");
    if (!el) {
      el = document.createElement("div");
      el.id = "anuncio"; el.className = "sr-only";
      el.setAttribute("role", "status"); el.setAttribute("aria-live", "polite");
      document.body.appendChild(el);
    }
    el.textContent = "";
    setTimeout(function () { el.textContent = texto; }, 50);
  };

  Proto.focar = function (el) {
    if (!el) return;
    if (!el.hasAttribute("tabindex")) el.setAttribute("tabindex", "-1");
    el.focus();
  };

  Proto.esc = function (s) {
    return String(s).replace(/[&<>"']/g, function (c) {
      return { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c];
    });
  };

  /* ---------- Painel do moderador (métricas do teste com usuários) ---------- */
  // Abra a tela com ?moderador=1 para ver cronômetro, contagem de cliques e marcos.
  // Nada é enviado a lugar nenhum; o moderador anota os números no achados.md.
  var met = { inicio: null, cliques: 0, marcos: [] };
  Proto.metricas = met;

  Proto.marco = function (nome) {
    var t = met.inicio ? Math.round((Date.now() - met.inicio) / 1000) : 0;
    met.marcos.push({ nome: nome, seg: t, cliques: met.cliques });
    desenharModerador();
  };

  function moderadorAtivo() {
    return /[?&]moderador=1/.test(location.search) || /moderador/.test(location.hash);
  }

  function desenharModerador() {
    var p = document.getElementById("painel-moderador");
    if (!p) return;
    var seg = met.inicio ? Math.round((Date.now() - met.inicio) / 1000) : 0;
    var html = "<strong>Moderador</strong> · " + Math.floor(seg / 60) + "min " + (seg % 60) + "s · " + met.cliques + " cliques";
    if (met.marcos.length) {
      html += "<ol style='margin:0.25rem 0 0.25rem 1rem;padding:0'>";
      met.marcos.slice(-3).forEach(function (m) {
        html += "<li>" + Proto.esc(m.nome) + ": " + m.seg + "s, " + m.cliques + " cliques</li>";
      });
      html += "</ol>";
    }
    html += " <button type='button' id='mod-zerar'>Zerar</button>";
    p.innerHTML = html;
    document.getElementById("mod-zerar").onclick = function (ev) {
      ev.stopPropagation();
      met.inicio = Date.now(); met.cliques = 0; met.marcos = []; desenharModerador();
    };
  }

  document.addEventListener("DOMContentLoaded", function () {
    met.inicio = Date.now();
    document.addEventListener("click", function (ev) {
      if (ev.target.closest && ev.target.closest("#painel-moderador")) return;
      if (ev.target.closest && ev.target.closest("button, a, input, select, label, [role=button]")) {
        met.cliques++;
        desenharModerador();
      }
    }, true);
    if (moderadorAtivo()) {
      var p = document.createElement("aside");
      p.id = "painel-moderador"; p.className = "moderador";
      p.setAttribute("aria-label", "Painel do moderador do teste");
      document.body.appendChild(p);
      document.body.classList.add("com-moderador");
      desenharModerador();
      setInterval(desenharModerador, 1000);
    }
  });

  window.Proto = Proto;
})();
