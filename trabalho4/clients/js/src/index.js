const readline = require('node:readline/promises');
const { stdin: input, stdout: output } = require('node:process');

const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080/api';
const PUBLISHER_BASE_URL = process.env.PUBLISHER_BASE_URL || 'http://localhost:8090/api/publisher';

async function request(url, options = {}) {
  const response = await fetch(url, {
    method: options.method || 'GET',
    headers: options.body ? { 'Content-Type': 'application/json' } : {},
    body: options.body ? JSON.stringify(options.body) : undefined,
  });

  const data = await response.json();
  if (!response.ok) {
    throw new Error(JSON.stringify(data));
  }
  return data;
}

async function main() {
  const rl = readline.createInterface({ input, output });

  try {
    while (true) {
      console.log('==========================================');
      console.log('Cliente JavaScript - Trabalho 4');
      console.log('1. Listar mensagens processadas');
      console.log('2. Total de mensagens');
      console.log('3. Publicar lote de teste');
      console.log('0. Sair');

      const option = (await rl.question('Opção: ')).trim();

      if (option === '1') {
        const messages = await request(`${API_BASE_URL}/mensagens`);
        console.log(JSON.stringify(messages, null, 2));
      } else if (option === '2') {
        const total = await request(`${API_BASE_URL}/mensagens/total`);
        console.log(JSON.stringify(total, null, 2));
      } else if (option === '3') {
        const count = await rl.question('Quantidade (default 10): ');
        const published = await request(`${PUBLISHER_BASE_URL}/teste?quantidade=${encodeURIComponent(count || '10')}`, {
          method: 'POST',
        });
        console.log(JSON.stringify(published, null, 2));
      } else if (option === '0') {
        break;
      } else {
        console.log('Opção inválida.');
      }
    }
  } finally {
    rl.close();
  }
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});const readline = require('node:readline/promises');
const { stdin: input, stdout: output } = require('node:process');

const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080/api';
const PUBLISHER_BASE_URL = process.env.PUBLISHER_BASE_URL || 'http://localhost:8082/api';

async function request(url, options = {}) {
  const response = await fetch(url, {
    method: options.method || 'GET',
    headers: options.body ? { 'Content-Type': 'application/json' } : {},
    body: options.body ? JSON.stringify(options.body) : undefined,
  });

  const text = await response.text();
  const payload = text ? JSON.parse(text) : null;

  if (!response.ok) {
    throw new Error(`${response.status} ${response.statusText}: ${text}`);
  }

  return payload;
}

function show(label, value) {
  console.log(`\n${label}:`);
  console.log(JSON.stringify(value, null, 2));
  console.log('');
}

async function listarMensagens() {
  const result = await request(`${API_BASE_URL}/mensagens`);
  show('Mensagens processadas', result);
}

async function listarEstatisticas() {
  const result = await request(`${API_BASE_URL}/estatisticas`);
  show('Estatísticas', result);
}

async function publicarTeste() {
  const result = await request(`${PUBLISHER_BASE_URL}/publicar/teste?quantidade=10`, { method: 'POST' });
  show('Publicação disparada', result);
}

async function main() {
  const rl = readline.createInterface({ input, output });

  try {
    while (true) {
      console.log('==========================================');
      console.log('Cliente JavaScript - Trabalho 4');
      console.log(`API: ${API_BASE_URL}`);
      console.log('1. Listar mensagens processadas');
      console.log('2. Ver estatísticas');
      console.log('3. Disparar massa de teste');
      console.log('0. Sair');

      let option;
      try {
        option = (await rl.question('Opção: ')).trim();
      } catch (error) {
        if (error && error.code === 'ERR_USE_AFTER_CLOSE') {
          break;
        }
        throw error;
      }

      try {
        if (option === '1') await listarMensagens();
        else if (option === '2') await listarEstatisticas();
        else if (option === '3') await publicarTeste();
        else if (option === '0') break;
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
