#!/usr/bin/env bash
# Sobe o SafeField AI sem Docker: API (Java 21) + interface web (Node 18+).
# Uso: ./iniciar.sh   e depois abra http://localhost:5173
set -e
cd "$(dirname "$0")"

command -v java >/dev/null || { echo "Java 21 não encontrado. Instale o JDK 21 ou use: docker compose up --build"; exit 1; }
command -v npm >/dev/null || { echo "Node.js não encontrado. Instale o Node 18+ ou use: docker compose up --build"; exit 1; }

echo "==> Instalando dependências da interface (primeira vez pode demorar)..."
(cd frontend && npm install --no-audit --no-fund)

echo "==> Subindo a API em http://localhost:8080 (log em backend/api.log)..."
(cd backend && ./mvnw -q spring-boot:run > api.log 2>&1) &
API_PID=$!
trap 'echo; echo "Encerrando..."; kill $API_PID 2>/dev/null; exit 0' INT TERM

echo -n "==> Aguardando a API e os dados de demonstração"
for _ in $(seq 1 120); do
  if grep -q "Dados de demonstração da Metaindústria criados" backend/api.log 2>/dev/null; then break; fi
  if ! kill -0 $API_PID 2>/dev/null; then echo; echo "A API falhou. Veja backend/api.log"; exit 1; fi
  echo -n "."; sleep 2
done
echo " pronto!"

echo "==> Interface em http://localhost:5173  (Ctrl+C encerra tudo)"
cd frontend && npx vite --port 5173
