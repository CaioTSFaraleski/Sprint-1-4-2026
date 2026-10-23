@echo off
REM Sobe o SafeField AI sem Docker no Windows: API (Java 21) + interface web (Node 18+).
REM Uso: iniciar.cmd   e depois abra http://localhost:5173
cd /d "%~dp0"

where java >nul 2>nul || (echo Java 21 nao encontrado. Instale o JDK 21 ou use: docker compose up --build & exit /b 1)
where npm >nul 2>nul || (echo Node.js nao encontrado. Instale o Node 18+ ou use: docker compose up --build & exit /b 1)

echo ==^> Subindo a API em uma nova janela (http://localhost:8080)...
start "SafeField AI - API" cmd /k "cd /d %~dp0backend && mvnw.cmd spring-boot:run"

echo ==^> Instalando dependencias da interface...
cd frontend
call npm install --no-audit --no-fund

echo ==^> Aguarde a janela da API mostrar "Dados de demonstracao ... criados" e abra http://localhost:5173
call npx vite --port 5173
