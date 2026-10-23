#!/usr/bin/env python3
"""
Simulador externo do edge gateway (câmeras/sensores) do SafeField AI.

Mostra a integração RF05 do jeito que um gateway real faria: publica detecções em
POST /api/v1/deteccoes autenticando com o cabeçalho X-Gateway-Key, sem login de usuário.
Usa só a biblioteca padrão do Python 3.

Exemplos:
  python3 gateway/simulador_gateway.py                 # uma detecção aleatória a cada 5 s
  python3 gateway/simulador_gateway.py --intervalo 2 --falha 0.5
  python3 gateway/simulador_gateway.py --uma --operador 4 --area 5 --epis BOTA,OCULOS
"""
import argparse
import json
import random
import time
import urllib.error
import urllib.request

TIPOS = ["CAPACETE", "OCULOS", "LUVAS", "PROTETOR_AURICULAR", "BOTA", "COLETE",
         "MASCARA_SOLDA", "RESPIRADOR", "AVENTAL", "CINTO"]


def chamar(api, metodo, caminho, corpo=None, token=None, chave=None):
    cabecalhos = {"Content-Type": "application/json", "Accept": "application/json"}
    if token:
        cabecalhos["Authorization"] = f"Bearer {token}"
    if chave:
        cabecalhos["X-Gateway-Key"] = chave
    dados = json.dumps(corpo).encode() if corpo is not None else None
    req = urllib.request.Request(api + caminho, data=dados, headers=cabecalhos, method=metodo)
    with urllib.request.urlopen(req, timeout=10) as resp:
        return json.loads(resp.read().decode() or "null")


def carregar_cadastro(api, email, senha):
    """O gateway real teria esse cadastro localmente; aqui ele é lido da API com um usuário supervisor."""
    token = chamar(api, "POST", "/api/v1/auth/login", {"email": email, "senha": senha})["token"]
    colaboradores = [c for c in chamar(api, "GET", "/api/v1/colaboradores", token=token) if c["areaId"]]
    areas = {a["id"]: a for a in chamar(api, "GET", "/api/v1/areas", token=token)}
    return colaboradores, areas


def main():
    p = argparse.ArgumentParser(description="Simulador externo do edge gateway do SafeField AI")
    p.add_argument("--api", default="http://localhost:8080")
    p.add_argument("--chave", default="gateway-dev-key", help="valor de safefield.gateway.api-key")
    p.add_argument("--intervalo", type=float, default=5.0, help="segundos entre detecções")
    p.add_argument("--falha", type=float, default=0.3, help="probabilidade de faltar um EPI (0 a 1)")
    p.add_argument("--uma", action="store_true", help="envia uma única detecção e sai")
    p.add_argument("--operador", type=int)
    p.add_argument("--area", type=int)
    p.add_argument("--epis", help="lista de EPIs detectados, separados por vírgula")
    p.add_argument("--usuario", default="supervisor@metaindustria.com.br")
    p.add_argument("--senha", default="safefield123")
    args = p.parse_args()

    if args.operador and args.area and args.epis is not None:
        detectados = [e.strip().upper() for e in args.epis.split(",") if e.strip()]
        r = chamar(args.api, "POST", "/api/v1/deteccoes",
                   {"operadorId": args.operador, "areaId": args.area, "episDetectados": detectados,
                    "confianca": 0.93, "origem": "GATEWAY", "dispositivo": "GW-EXTERNO-01"}, chave=args.chave)
        print(json.dumps(r, ensure_ascii=False, indent=2))
        return

    colaboradores, areas = carregar_cadastro(args.api, args.usuario, args.senha)
    print(f"Gateway conectado a {args.api}: {len(colaboradores)} colaboradores em {len(areas)} áreas.")
    while True:
        c = random.choice(colaboradores)
        area = areas[c["areaId"]]
        exigidos = sorted({r["tipoEpi"] for r in area["regras"]})
        detectados = list(exigidos)
        if detectados and random.random() < args.falha:
            detectados.remove(random.choice(detectados))
        corpo = {"operadorId": c["id"], "areaId": area["id"], "episDetectados": detectados,
                 "confianca": round(random.uniform(0.82, 0.98), 3), "origem": "GATEWAY",
                 "dispositivo": f"GW-{area['nome'][:3].upper()}-0{random.randint(1, 3)}"}
        try:
            r = chamar(args.api, "POST", "/api/v1/deteccoes", corpo, chave=args.chave)
            situacao = "conforme" if r["conforme"] else f"NÃO CONFORME -> {r['ocorrenciaCodigo']}"
            print(f"{time.strftime('%H:%M:%S')}  {c['nome']:<22} {area['nome']:<20} {situacao}")
        except urllib.error.HTTPError as e:
            print("Erro da API:", e.code, e.read().decode()[:200])
        if args.uma:
            break
        time.sleep(args.intervalo)


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\nGateway encerrado.")
