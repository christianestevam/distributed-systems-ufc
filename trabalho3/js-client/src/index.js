const readline = require('node:readline/promises');
const { stdin: input, stdout: output } = require('node:process');

const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080/api/v1';

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: options.method || 'GET',
    headers: options.body ? { 'Content-Type': 'application/json' } : {},
    body: options.body ? JSON.stringify(options.body) : undefined,
  });

  const contentType = response.headers.get('content-type') || '';
  let payload = null;

  if (contentType.includes('application/json')) {
    payload = await response.json();
  } else {
    payload = await response.text();
  }

  if (!response.ok) {
    const details = typeof payload === 'string' ? payload : JSON.stringify(payload, null, 2);
    throw new Error(`${response.status} ${response.statusText}: ${details}`);
  }

  return payload;
}

function printJson(label, value) {
  console.log(`\n${label}:`);
  console.log(JSON.stringify(value, null, 2));
  console.log('');
}

async function criarInvestidor(rl) {
  const investidorId = await rl.question('Investidor ID: ');
  const nome = await rl.question('Nome: ');
  const cpf = await rl.question('CPF: ');
  const email = await rl.question('E-mail: ');
  const telefone = await rl.question('Telefone: ');

  const resultado = await request('/investidores', {
    method: 'POST',
    body: { investidorId, nome, cpf, email, telefone },
  });
  printJson('Investidor criado', resultado);
}

async function obterInvestidor(rl) {
  const investidorId = await rl.question('Investidor ID: ');
  const resultado = await request(`/investidores/${encodeURIComponent(investidorId)}`);
  printJson('Investidor encontrado', resultado);
}

async function criarOrdem(rl) {
  const investidorId = await rl.question('Investidor ID: ');
  const ordemId = await rl.question('Ordem ID: ');
  const tipo = await rl.question('Tipo (COMPRA/VENDA): ');
  const ticker = await rl.question('Ticker: ');
  const quantidade = Number(await rl.question('Quantidade: '));
  const precoUnitario = Number(await rl.question('Preço unitário: '));

  const resultado = await request(`/investidores/${encodeURIComponent(investidorId)}/ordens`, {
    method: 'POST',
    body: { ordemId, tipo, ticker, quantidade, precoUnitario },
  });
  printJson('Ordem criada', resultado);
}

async function obterOrdens(rl) {
  const investidorId = await rl.question('Investidor ID: ');
  const resultado = await request(`/investidores/${encodeURIComponent(investidorId)}/ordens`);
  printJson('Ordens do investidor', resultado);
}

async function adicionarSaldo(rl) {
  const investidorId = await rl.question('Investidor ID: ');
  const valor = Number(await rl.question('Valor: '));

  const resultado = await request(`/investidores/${encodeURIComponent(investidorId)}/saldo`, {
    method: 'POST',
    body: { valor },
  });
  printJson('Saldo atualizado', resultado);
}

async function obterAtivo(rl) {
  const ticker = await rl.question('Ticker: ');
  const resultado = await request(`/ativos/${encodeURIComponent(ticker)}`);
  printJson('Ativo encontrado', resultado);
}

async function listarAtivos() {
  const resultado = await request('/ativos');
  printJson('Ativos disponíveis', resultado);
}

async function main() {
  const rl = readline.createInterface({ input, output });

  try {
    while (true) {
      console.log('==========================================');
      console.log('Cliente JavaScript - Trabalho 3');
      console.log(`API: ${API_BASE_URL}`);
      console.log('1. Criar investidor');
      console.log('2. Obter investidor');
      console.log('3. Criar ordem');
      console.log('4. Obter ordens do investidor');
      console.log('5. Adicionar saldo à carteira');
      console.log('6. Obter ativo');
      console.log('7. Listar ativos');
      console.log('0. Sair');

      const opcao = (await rl.question('Opção: ')).trim();

      try {
        if (opcao === '1') await criarInvestidor(rl);
        else if (opcao === '2') await obterInvestidor(rl);
        else if (opcao === '3') await criarOrdem(rl);
        else if (opcao === '4') await obterOrdens(rl);
        else if (opcao === '5') await adicionarSaldo(rl);
        else if (opcao === '6') await obterAtivo(rl);
        else if (opcao === '7') await listarAtivos();
        else if (opcao === '0') break;
        else console.log('Opção inválida.');
      } catch (error) {
        console.error(`Erro: ${error.message}`);
      }
    }
  } finally {
    rl.close();
  }
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});