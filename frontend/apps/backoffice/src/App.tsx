import { mensagens } from './mensagens/pt-BR';

export function App() {
  return (
    <main>
      <h1>{mensagens.titulo}</h1>
      <p>{mensagens.descricao}</p>
    </main>
  );
}
