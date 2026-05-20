#!/usr/bin/env bash
# =============================================================================
# release.sh — Script de release automatizado para speech-ai-hexagonal
#
# Uso:
#   ./release.sh <versão> "<título>" "<descrição>"
#
# Exemplos:
#   ./release.sh 1.0.0 "Fase 1: Base hexagonal + transcrição local" "Arquitetura hexagonal completa"
#   ./release.sh 2.1.0 "Fase 2 item 1: RedisConfig" "Setup Redis + RedisTemplate"
# =============================================================================

set -euo pipefail

# ─── Cores ────────────────────────────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'

info()    { echo -e "${BLUE}[INFO]${NC} $*"; }
success() { echo -e "${GREEN}[OK]${NC} $*"; }
warn()    { echo -e "${YELLOW}[WARN]${NC} $*"; }
error()   { echo -e "${RED}[ERRO]${NC} $*"; exit 1; }

# ─── Validação argumentos ─────────────────────────────────────────────────────
if [ $# -lt 2 ]; then
  echo "Uso: $0 <versão> \"<título>\" [\"<notas extras>\"]"
  exit 1
fi

VERSION="$1"; TITLE="$2"; EXTRA_NOTES="${3:-}"; TAG="v${VERSION}"

# ─── Validar versão semântica ─────────────────────────────────────────────────
if ! echo "$VERSION" | grep -qE '^[0-9]+\.[0-9]+\.[0-9]+$'; then
  error "Versão inválida: '$VERSION'. Use o formato MAJOR.MINOR.PATCH"
fi

info "Iniciando release ${TAG} — ${TITLE}"

# ─── Validar repositório git ──────────────────────────────────────────────────
if ! git rev-parse --git-dir > /dev/null 2>&1; then error "Não é um repositório git."; fi

# ─── Validar branch main ──────────────────────────────────────────────────────
BRANCH=$(git rev-parse --abbrev-ref HEAD)

[ "$BRANCH" != "main" ] \
 && error "Release só pode ser executada na branch main."

# ─── Validar sincronização ────────────────────────────────────────────────────
git fetch origin

LOCAL=$(git rev-parse HEAD)
REMOTE=$(git rev-parse origin/main)

[ "$LOCAL" != "$REMOTE" ] \
  && error "Sua branch local não está sincronizada com origin/main."

# ─── Validar tag duplicada ────────────────────────────────────────────────────
if git tag -l | grep -q "^${TAG}$"; then error "Tag ${TAG} já existe."; fi

# ─── Validar arquivos pendentes ───────────────────────────────────────────────
if ! git diff --quiet || ! git diff --cached --quiet; then
  warn "Há arquivos modificados ou staged. Deseja continuar mesmo assim? (s/N)"

  read -r confirm

  [[ "$confirm" != "s" && "$confirm" != "S" ]] && error "Release cancelada."
fi

# ─── Validar build.gradle ─────────────────────────────────────────────────────
BUILD_FILE="build.gradle"

[ ! -f "$BUILD_FILE" ] && error "build.gradle não encontrado."

# ─── Atualizar versão ─────────────────────────────────────────────────────────
CURRENT_VERSION=$(grep "^version = " "$BUILD_FILE" | sed "s/version = '//;s/'//")

info "Versão atual: ${CURRENT_VERSION} → Nova versão: ${VERSION}"

# Compatível Linux/macOS
sed -i.bak "s/^version = '.*'/version = '${VERSION}'/" "$BUILD_FILE"
rm -f "${BUILD_FILE}.bak"

NEW_VERSION=$(grep "^version = " "$BUILD_FILE" | sed "s/version = '//;s/'//")

[ "$NEW_VERSION" != "$VERSION" ] && error "Falha ao atualizar versão no build.gradle"

success "build.gradle atualizado"

# ─── Executar build ───────────────────────────────────────────────────────────
info "Executando build + testes..."
./gradlew clean build --no-daemon
success "Build executado com sucesso"

# ─── Commit bump de versão ────────────────────────────────────────────────────
git add build.gradle

if ! git diff --cached --quiet; then
  git commit -m "chore(release): bump version para ${VERSION} — ${TITLE}"
  success "Commit criado"
fi

# ─── Push branch ──────────────────────────────────────────────────────────────
info "Push da branch ${BRANCH}..."
git push --follow-tags origin "$BRANCH"
success "Push concluído"

# ─── Gerar changelog ──────────────────────────────────────────────────────────
LAST_TAG=$(git describe --tags --abbrev=0 2>/dev/null || echo "")

if [ -n "$LAST_TAG" ]; then
  CHANGELOG=$(git log "${LAST_TAG}..HEAD" --oneline --no-decorate --invert-grep --grep="chore(release)" 2>/dev/null || echo "Sem commits anteriores")
else
  CHANGELOG=$(git log --oneline --no-decorate 2>/dev/null | head -20)
fi

echo ""
info "Commits desta release:"
echo "$CHANGELOG" | sed 's/^/  /'
echo ""

# ─── Criar mensagem da tag ────────────────────────────────────────────────────
TAG_MESSAGE="${TAG} — ${TITLE}

Commits inclusos:
$(echo "$CHANGELOG" | head -20)

${EXTRA_NOTES:+Notas:
 ${EXTRA_NOTES}

}Stack: Java 25 · Spring Boot 4.0.6 · Speaches/Whisper · Docker Compose · RabbitMQ · Redis · Ollama · Observabilidade (Micrometer/Zipkin)
"

# ─── Criar tag anotada ────────────────────────────────────────────────────────
info "Criando tag anotada ${TAG}..."
git tag -a "$TAG" -m "$TAG_MESSAGE"
success "Tag criada"

# ─── Confirmar release ─────────────────────────────────────────────────────────
warn "Confirmar release ${TAG}? (s/N)"
read -r confirm

[[ "$confirm" != "s" && "$confirm" != "S" ]] \
  && error "Release cancelada."

# ─── Push tag ─────────────────────────────────────────────────────────────────
info "Push da tag ${TAG}..."
git push origin "$TAG"
success "Push da tag concluído"

# ─── GitHub CLI ───────────────────────────────────────────────────────────────
if command -v gh &> /dev/null; then

  gh auth status > /dev/null 2>&1 \
    || error "gh CLI não autenticado."

  RELEASE_NOTES="## ${TITLE}

### Commits desta release

\`\`\`
${CHANGELOG}
\`\`\`

${EXTRA_NOTES:+### Notas adicionais

${EXTRA_NOTES}

}
---
*Release gerada automaticamente por \`release.sh\`*
" 

  info "Criando GitHub Release..."

  gh release create "$TAG" --title "${TAG} — ${TITLE}" --notes "$RELEASE_NOTES"

  success "GitHub Release criada"

else
  warn "gh CLI não encontrado. Crie a release manualmente."
fi

# ─── Resumo final ─────────────────────────────────────────────────────────────
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

success "Release ${TAG} concluída com sucesso!"
echo ""
echo " Tag: ${TAG}"
echo " Branch: ${BRANCH}"
echo " Versão: ${VERSION}"
echo " Título: ${TITLE}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

info "Próximo passo: iniciar desenvolvimento da próxima versão."
