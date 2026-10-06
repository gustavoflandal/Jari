import { cleanup, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it } from 'vitest';
import { App } from './App';
import { mensagens } from './mensagens/pt-BR';

afterEach(cleanup);

describe('Portal do recorrente (esqueleto PT-01)', () => {
  it('PT01_renderiza_titulo_do_portal', () => {
    render(<App />);
    const titulo = screen.getByRole('heading', { level: 1 });
    expect(titulo.textContent).toBe(mensagens.titulo);
  });

  it('PT01_tem_regiao_principal_para_leitores_de_tela', () => {
    render(<App />);
    expect(screen.getByRole('main')).toBeDefined();
  });
});
