// Verificação automática dos protótipos: percorre as três jornadas e roda axe-core
// (WCAG 2.0/2.1 níveis A e AA) em cada estado de tela. Não faz parte do produto.
// Uso (fora do repositório, numa pasta temporária):
//   npm i axe-core@4 playwright@1.56.1
//   PLAYWRIGHT_BROWSERS_PATH=/opt/pw-browsers node axe-prototipos.js <caminho>/docs/prototipos
const { chromium } = require("playwright");
const fs = require("fs");
const path = require("path");
const AXE = fs.readFileSync(require.resolve("axe-core/axe.min.js"), "utf8");
const base = "file://" + path.resolve(process.argv[2]) + "/";
const resultados = [];
const errosConsole = [];

async function axe(page, nome) {
  await page.addScriptTag({ content: AXE });
  const r = await page.evaluate(async () => {
    const res = await window.axe.run(document, { runOnly: { type: "tag", values: ["wcag2a", "wcag2aa", "wcag21a", "wcag21aa"] } });
    return res.violations.map(v => ({ id: v.id, impact: v.impact, n: v.nodes.length, alvo: v.nodes.slice(0, 3).map(n => n.target.join(" ")), resumo: v.nodes[0] && v.nodes[0].failureSummary }));
  });
  resultados.push({ tela: nome, violacoes: r });
  console.log((r.length ? "FALHA " : "ok    ") + nome + (r.length ? " " + JSON.stringify(r, null, 1) : ""));
}

(async () => {
  const browser = await chromium.launch();
  const mob = await browser.newContext({ viewport: { width: 390, height: 844 }, isMobile: true, hasTouch: true });
  const desk = await browser.newContext({ viewport: { width: 1366, height: 900 } });
  const watch = (page, nome) => {
    page.on("pageerror", e => errosConsole.push(nome + ": " + e.message));
    page.on("console", m => { if (m.type() === "error" && !/CORS policy|ERR_FAILED/.test(m.text())) errosConsole.push(nome + ": " + m.text()); });
  };

  // ---- Índice
  let pg = await desk.newPage(); watch(pg, "indice");
  await pg.goto(base + "index.html"); await axe(pg, "indice");

  // ---- Portal (celular)
  pg = await mob.newPage(); watch(pg, "portal");
  await pg.goto(base + "portal/index.html?moderador=1");
  await axe(pg, "portal: inicio");
  await pg.click("text=Buscar");
  await pg.waitForSelector("#busca-erro:not(.oculto)");
  await axe(pg, "portal: inicio com erro");
  await pg.fill("#busca", "abc1d23"); await pg.click("text=Buscar");
  await pg.waitForSelector("#tela-aits:not(.oculto)"); await axe(pg, "portal: lista de autuacoes");
  await pg.click("[data-ait='0']");
  await pg.waitForSelector("#tela-passo1:not(.oculto)"); await axe(pg, "portal: passo 1");
  const prazo = await pg.textContent("#p1-pecas .prazo"); console.log("   prazo exibido:", prazo.trim());
  await pg.click("#form-peca button[type=submit]");
  await pg.waitForSelector("#tela-passo2:not(.oculto)");
  await pg.click("#form-texto button[type=submit]");
  await axe(pg, "portal: passo 2 com erro");
  await pg.fill("#texto", "O radar estava sem verificação válida na data da autuação, peço cancelamento.");
  await pg.check("#rede-ruim");
  await pg.click("#anexo-exemplo");
  await pg.waitForTimeout(400);
  await axe(pg, "portal: passo 2 enviando");
  await pg.waitForSelector("#anexos .etiqueta-verde", { timeout: 10000 });
  await pg.click("#form-texto button[type=submit]");
  await pg.waitForSelector("#tela-passo3:not(.oculto)"); await axe(pg, "portal: passo 3");
  await pg.click("text=Está certo, continuar");
  await pg.waitForSelector("#tela-passo4:not(.oculto)");
  await pg.click("#form-assinar button[type=submit]"); await axe(pg, "portal: passo 4 com erro");
  await pg.check("#declaro"); await pg.click("#form-assinar button[type=submit]");
  await pg.waitForSelector("#tela-recibo:not(.oculto)"); await axe(pg, "portal: recibo");
  console.log("   recibo:", (await pg.textContent(".recibo-numero")));
  await pg.click("#ver-processo");
  await pg.waitForSelector("#tela-processo:not(.oculto)"); await axe(pg, "portal: linha do tempo");
  await pg.click("text=Voltar para meus processos");
  await pg.waitForSelector("#tela-meus:not(.oculto)"); await axe(pg, "portal: meus processos");
  await pg.click("[data-proc='1']");
  await pg.waitForFunction(() => document.getElementById("proc-num").textContent === "2026.000777");
 
  const txt = await pg.textContent("#tela-processo"); console.log("   distribuido:", txt.replace(/\s+/g, " ").slice(0, 120));
  const proibidos = (txt.match(/.{30}(\bjunta\b|posi[cç][aã]o|relator|julgador).{30}/i)||[""])[0]; console.log("   trecho:", proibidos);
  console.log("   sigilo (sem junta/posicao/relator antes da sessao):", proibidos ? "VIOLADO" : "ok");
  // Varredura de pagamento em todo o HTML/JS do portal
  const fonte = fs.readFileSync(path.join(process.argv[2], "portal/index.html"), "utf8") + fs.readFileSync(path.join(process.argv[2], "portal/portal.js"), "utf8");
  // Invariante 7: toda menção a pagamento deve ser só para dizer que ele NÃO é exigido.
  console.log("   mencoes a pagamento (revisar: devem apenas dizer que nao e exigido):", (fonte.match(/.{0,40}\b(pagar|pagou|pagamento|comprovante|boleto|dep[oó]sito|guia)\b.{0,40}/gi) || []));
  const sw = await pg.evaluate(() => document.documentElement.scrollWidth); console.log("   largura (sem rolagem horizontal se <= 390):", sw);
  // 2a instancia
  await pg.goto(base + "portal/index.html");
  await pg.fill("#busca", "PROTO-000456"); await pg.click("text=Buscar");
  await pg.waitForSelector("#tela-passo1:not(.oculto)"); await axe(pg, "portal: passo 1 (NA, duas pecas)");

  // ---- Relator (desktop)
  pg = await desk.newPage(); watch(pg, "relator");
  await pg.goto(base + "relator/index.html?moderador=1");
  await axe(pg, "relator: processo 1");
  console.log("   botao de download existe:", await pg.$$eval("button, a", els => els.some(e => /baixar|download|imprimir/i.test(e.textContent))));
  await pg.click("#btn-assinar");
  await axe(pg, "relator: erros de validacao");
  for (let k = 0; k < 3; k++) {
    await pg.click("#modelo"); await pg.selectOption("#modelo", "padrao");
    for (let e = 0; e < 3; e++) await pg.check("#ex" + e + "-a");
    await pg.check(k === 2 ? "#resultado-1" : "#resultado-3");
    await pg.selectOption("#dispositivo", { index: 1 });
    await pg.click("#btn-assinar");
    await pg.waitForFunction(n => document.querySelectorAll(".fila li.feito").length === n, k + 1);
  }
  await axe(pg, "relator: revisor");
  await pg.check("#rev-d"); await axe(pg, "relator: revisor divergindo");
  await pg.check("#rev-a"); await pg.click("#btn-assinar");
  await pg.waitForSelector("#tela-fim:not(.oculto)"); await axe(pg, "relator: fim");
  console.log("   moderador:", (await pg.textContent("#painel-moderador")).replace(/\s+/g, " "));

  // ---- Relator no celular (sem rolagem horizontal)
  pg = await mob.newPage(); watch(pg, "relator-mob");
  await pg.goto(base + "relator/index.html");
  console.log("   relator largura celular:", await pg.evaluate(() => document.documentElement.scrollWidth));

  // ---- Presidente
  pg = await desk.newPage(); watch(pg, "presidente");
  await pg.goto(base + "presidente/index.html");
  await axe(pg, "presidente: abertura sem quorum");
  await pg.click("#btn-concluir", { force: true });
  console.log("   abriu sem quorum?", await pg.$eval("#roteiro button[data-passo='0']", b => b.className.includes("feito")));
  await pg.check("#pres-C"); await pg.check("#pres-E"); await pg.check("#pres-B"); await pg.check("#pres-D");
  // F ausente para exercitar substituição; B presente
  await axe(pg, "presidente: abertura com quorum");
  await pg.click("#btn-concluir");
  await axe(pg, "presidente: ata anterior");
  await pg.click("#btn-pular");
  await axe(pg, "presidente: turmas");
  await pg.click("#btn-concluir");
  await axe(pg, "presidente: pauta revelada");
  await pg.click("#btn-concluir");
  for (let k = 0; k < 3; k++) await pg.click("#simular");
  await axe(pg, "presidente: julgamento");
  const apur = await pg.$$eval("tbody tr", trs => trs.map(t => t.cells[0].innerText.split("\n")[0] + " → " + t.cells[t.cells.length - 1].innerText.replace(/\s+/g, " ")));
  apur.forEach(a => console.log("   ", a));
  await pg.click("[data-proclamar]");
  await pg.click("#btn-concluir");
  await pg.click("#btn-pular");
  await axe(pg, "presidente: encerramento");
  await pg.click("#btn-concluir");
  console.log("   final:", (await pg.textContent("#passo")).replace(/\s+/g, " ").slice(0, 160));

  await browser.close();
  console.log("\nErros de console:", errosConsole.length ? errosConsole : "nenhum");
  const total = resultados.reduce((s, r) => s + r.violacoes.length, 0);
  console.log("Telas/estados verificados:", resultados.length, "· violações:", total);
  if (total) process.exitCode = 1;
})().catch(e => { console.error(e); process.exit(1); });
