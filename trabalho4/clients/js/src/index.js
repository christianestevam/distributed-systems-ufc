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
});

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
