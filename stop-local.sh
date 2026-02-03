#!/usr/bin/env bash
# stop-local.sh
# English: Gracefully stops the local development environment for Catalog App
# - Stops backend process (via PID file)
# - Stops frontend process (via PID file)
# - Stops Docker Compose PostgreSQL
# - Cleans up PID files
#
# Usage: ./stop-local.sh
# Or use: kill $(cat logs/backend.pid) && kill $(cat logs/frontend.pid)

set -euo pipefail
ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[stop-local]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[stop-local]${NC} ✅ $1"
}

log_warn() {
    echo -e "${YELLOW}[stop-local]${NC} ⚠️  $1"
}

log_error() {
    echo -e "${RED}[stop-local]${NC} ❌ $1"
}

log_info "Stopping local development environment..."

# Stop backend
if [ -f "$ROOT_DIR/logs/backend.pid" ]; then
    BACKEND_PID=$(cat "$ROOT_DIR/logs/backend.pid" 2>/dev/null || echo "")
    if [ -n "$BACKEND_PID" ]; then
        if kill -0 "$BACKEND_PID" 2>/dev/null; then
            log_info "Stopping backend (PID: $BACKEND_PID)..."
            kill "$BACKEND_PID" 2>/dev/null || true

            # Wait for graceful shutdown (up to 10 seconds)
            WAIT_COUNT=0
            while kill -0 "$BACKEND_PID" 2>/dev/null && [ $WAIT_COUNT -lt 10 ]; do
                echo -n "."
                sleep 1
                WAIT_COUNT=$((WAIT_COUNT + 1))
            done

            # Force kill if still running
            if kill -0 "$BACKEND_PID" 2>/dev/null; then
                log_warn "Forcing kill on backend (PID: $BACKEND_PID)"
                kill -9 "$BACKEND_PID" 2>/dev/null || true
            else
                log_success "Backend stopped gracefully"
            fi
        else
            log_warn "Backend PID $BACKEND_PID not found (process already stopped)"
        fi
        rm -f "$ROOT_DIR/logs/backend.pid"
    else
        log_warn "Backend PID file is empty"
    fi
else
    log_warn "Backend PID file not found (backend may not have started)"
fi

# Stop frontend
if [ -f "$ROOT_DIR/logs/frontend.pid" ]; then
    FRONTEND_PID=$(cat "$ROOT_DIR/logs/frontend.pid" 2>/dev/null || echo "")
    if [ -n "$FRONTEND_PID" ]; then
        if kill -0 "$FRONTEND_PID" 2>/dev/null; then
            log_info "Stopping frontend (PID: $FRONTEND_PID)..."
            kill "$FRONTEND_PID" 2>/dev/null || true

            # Wait for graceful shutdown (up to 5 seconds)
            WAIT_COUNT=0
            while kill -0 "$FRONTEND_PID" 2>/dev/null && [ $WAIT_COUNT -lt 5 ]; do
                echo -n "."
                sleep 1
                WAIT_COUNT=$((WAIT_COUNT + 1))
            done

            # Force kill if still running
            if kill -0 "$FRONTEND_PID" 2>/dev/null; then
                log_warn "Forcing kill on frontend (PID: $FRONTEND_PID)"
                kill -9 "$FRONTEND_PID" 2>/dev/null || true
            else
                log_success "Frontend stopped gracefully"
            fi
        else
            log_warn "Frontend PID $FRONTEND_PID not found (process already stopped)"
        fi
        rm -f "$ROOT_DIR/logs/frontend.pid"
    else
        log_warn "Frontend PID file is empty"
    fi
else
    log_warn "Frontend PID file not found (frontend may not have started)"
fi

# Stop Docker Compose
if [ -d "$ROOT_DIR/backend" ]; then
    log_info "Stopping PostgreSQL (docker-compose down)..."
    cd "$ROOT_DIR/backend"

    if docker-compose down 2>/dev/null; then
        log_success "PostgreSQL stopped"
    else
        log_warn "Docker Compose not available or already stopped"
    fi

    cd "$ROOT_DIR"
fi

log_info "═══════════════════════════════════════════════════════════════"
log_success "Local development environment stopped"
log_info "═══════════════════════════════════════════════════════════════"
echo ""
log_info "To restart, run: ./start-local.sh"
echo ""
