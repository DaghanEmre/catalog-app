#!/usr/bin/env bash
# start-local.sh
# English: Starts the local development environment for Catalog App
# - Ensures .env exists (portable copy method)
# - Starts PostgreSQL via docker compose with readiness check
# - Starts backend (Gradle bootRun) with PID tracking and graceful shutdown
# - Starts frontend (npm start) with PID tracking and graceful shutdown
# - Provides health checks and helpful logging
#
# Usage: ./start-local.sh
# To stop: kill $(cat logs/backend.pid) && kill $(cat logs/frontend.pid)
# Or: ctrl+c to gracefully shutdown

set -euo pipefail
ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT_DIR"

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[start-local]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[start-local]${NC} ✅ $1"
}

log_warn() {
    echo -e "${YELLOW}[start-local]${NC} ⚠️  $1"
}

log_error() {
    echo -e "${RED}[start-local]${NC} ❌ $1"
}

# Ensure logs directory exists
mkdir -p "$ROOT_DIR/logs"

# Cleanup function: called on exit or ctrl+c
cleanup() {
    log_info "Shutting down..."

    # Kill backend if PID file exists
    if [ -f "$ROOT_DIR/logs/backend.pid" ]; then
        BACKEND_PID=$(cat "$ROOT_DIR/logs/backend.pid")
        if kill -0 "$BACKEND_PID" 2>/dev/null; then
            log_info "Stopping backend (PID: $BACKEND_PID)..."
            kill "$BACKEND_PID" 2>/dev/null || true
            sleep 2
            # Force kill if still running
            kill -9 "$BACKEND_PID" 2>/dev/null || true
        fi
        rm -f "$ROOT_DIR/logs/backend.pid"
    fi

    # Kill frontend if PID file exists
    if [ -f "$ROOT_DIR/logs/frontend.pid" ]; then
        FRONTEND_PID=$(cat "$ROOT_DIR/logs/frontend.pid")
        if kill -0 "$FRONTEND_PID" 2>/dev/null; then
            log_info "Stopping frontend (PID: $FRONTEND_PID)..."
            kill "$FRONTEND_PID" 2>/dev/null || true
            sleep 1
            # Force kill if still running
            kill -9 "$FRONTEND_PID" 2>/dev/null || true
        fi
        rm -f "$ROOT_DIR/logs/frontend.pid"
    fi

    # Stop Docker Compose (graceful)
    if command -v docker-compose &> /dev/null || command -v docker &> /dev/null; then
        log_info "Stopping PostgreSQL..."
        cd "$ROOT_DIR/backend"
        docker-compose down 2>/dev/null || true
        cd "$ROOT_DIR"
    fi

    log_success "Shutdown complete"
}

# Register cleanup on exit and Ctrl+C
trap cleanup EXIT INT TERM

# ============================================================================
# Step 1: Ensure .env exists (portable approach)
# ============================================================================
if [ ! -f "$ROOT_DIR/backend/.env" ]; then
    if [ -f "$ROOT_DIR/backend/.env.example" ]; then
        log_info "Copying backend/.env.example → backend/.env"
        cp "$ROOT_DIR/backend/.env.example" "$ROOT_DIR/backend/.env"
        log_success ".env created from template"
    else
        log_warn "No .env.example found, skipping .env copy"
    fi
else
    log_info ".env already exists, skipping copy"
fi

# ============================================================================
# Step 2: Start PostgreSQL with readiness check
# ============================================================================
log_info "Starting PostgreSQL via docker compose..."
cd "$ROOT_DIR/backend"

# Check if container is already running
if docker-compose ps catalog_postgres 2>/dev/null | grep -q "Up"; then
    log_warn "PostgreSQL container already running, skipping docker-compose up"
else
    docker-compose up -d
    log_success "Docker Compose started"
fi

# Wait for PostgreSQL to be ready (pg_isready)
log_info "Waiting for PostgreSQL to accept connections..."
MAX_RETRIES=30
RETRY_COUNT=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if docker-compose exec -T postgres pg_isready -U catalog -d catalogdb 2>/dev/null | grep -q "accepting"; then
        log_success "PostgreSQL is ready"
        break
    fi
    RETRY_COUNT=$((RETRY_COUNT + 1))
    echo -n "."
    sleep 1
done

if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
    log_error "PostgreSQL failed to become ready after ${MAX_RETRIES}s"
    log_error "Check logs: docker-compose logs postgres"
    exit 1
fi

# ============================================================================
# Step 3: Start Backend (Gradle bootRun)
# ============================================================================
log_info "Starting backend (./gradlew bootRun)..."
log_info "Logs will be written to: $ROOT_DIR/logs/backend.log"

# Kill previous backend process if it exists
if [ -f "$ROOT_DIR/logs/backend.pid" ]; then
    OLD_BACKEND_PID=$(cat "$ROOT_DIR/logs/backend.pid" 2>/dev/null || echo "")
    if [ -n "$OLD_BACKEND_PID" ] && kill -0 "$OLD_BACKEND_PID" 2>/dev/null; then
        log_warn "Killing previous backend process (PID: $OLD_BACKEND_PID)"
        kill "$OLD_BACKEND_PID" 2>/dev/null || true
        sleep 1
    fi
fi

# Start backend in background
nohup ./gradlew bootRun > "$ROOT_DIR/logs/backend.log" 2>&1 &
BACKEND_PID=$!
echo "$BACKEND_PID" > "$ROOT_DIR/logs/backend.pid"
log_success "Backend started with PID: $BACKEND_PID"

# ============================================================================
# Step 4: Wait for Backend to be ready
# ============================================================================
log_info "Waiting for backend to start (max 60s)..."
BACKEND_MAX_RETRIES=60
BACKEND_RETRY_COUNT=0

while [ $BACKEND_RETRY_COUNT -lt $BACKEND_MAX_RETRIES ]; do
    if curl -sS -f http://localhost:8080/actuator/health -o /dev/null 2>&1; then
        log_success "Backend is ready"
        break
    fi
    BACKEND_RETRY_COUNT=$((BACKEND_RETRY_COUNT + 1))
    echo -n "."
    sleep 1
done

if [ $BACKEND_RETRY_COUNT -eq $BACKEND_MAX_RETRIES ]; then
    log_warn "Backend failed to become ready after ${BACKEND_MAX_RETRIES}s"
    log_info "Backend logs: tail -f $ROOT_DIR/logs/backend.log"
else
    log_success "Backend startup verified"
fi

# ============================================================================
# Step 5: Start Frontend (npm install + npm start)
# ============================================================================
if [ -f "$ROOT_DIR/frontend/package.json" ]; then
    log_info "Installing frontend dependencies (npm ci)..."
    cd "$ROOT_DIR/frontend"

    # Use npm ci if package-lock.json exists, otherwise npm install
    if [ -f "$ROOT_DIR/frontend/package-lock.json" ]; then
        npm ci --no-audit --no-fund --silent 2>&1 | tail -1 || log_warn "npm ci completed with warnings"
    else
        npm install --no-audit --no-fund --silent 2>&1 | tail -1 || log_warn "npm install completed with warnings"
    fi

    log_success "Frontend dependencies installed"

    log_info "Starting frontend (npm start)..."
    log_info "Logs will be written to: $ROOT_DIR/logs/frontend.log"

    # Kill previous frontend process if it exists
    if [ -f "$ROOT_DIR/logs/frontend.pid" ]; then
        OLD_FRONTEND_PID=$(cat "$ROOT_DIR/logs/frontend.pid" 2>/dev/null || echo "")
        if [ -n "$OLD_FRONTEND_PID" ] && kill -0 "$OLD_FRONTEND_PID" 2>/dev/null; then
            log_warn "Killing previous frontend process (PID: $OLD_FRONTEND_PID)"
            kill "$OLD_FRONTEND_PID" 2>/dev/null || true
            sleep 1
        fi
    fi

    # Start frontend in background
    nohup npm start > "$ROOT_DIR/logs/frontend.log" 2>&1 &
    FRONTEND_PID=$!
    echo "$FRONTEND_PID" > "$ROOT_DIR/logs/frontend.pid"
    log_success "Frontend started with PID: $FRONTEND_PID"

    # Wait a bit for Angular dev server to start
    log_info "Waiting for frontend dev server to start..."
    sleep 5

else
    log_warn "frontend/package.json not found, skipping frontend startup"
fi

# ============================================================================
# Step 6: Health Checks and Summary
# ============================================================================
log_info "Performing health checks..."

# PostgreSQL
if docker ps --filter "name=catalog_postgres" --format "{{.Names}}" 2>/dev/null | grep -q catalog_postgres; then
    POSTGRES_STATUS=$(docker-compose ps catalog_postgres 2>/dev/null | grep catalog_postgres | awk '{print $NF}')
    log_success "PostgreSQL: $POSTGRES_STATUS"
else
    log_warn "PostgreSQL container not found"
fi

# Backend health check
if curl -sS http://localhost:8080/actuator/health > "$ROOT_DIR/logs/actuator_health.json" 2>&1; then
    HEALTH_STATUS=$(grep -o '"status":"[^"]*"' "$ROOT_DIR/logs/actuator_health.json" | cut -d'"' -f4)
    log_success "Backend Actuator: $HEALTH_STATUS"
else
    log_warn "Backend health check failed (might still be starting)"
fi

# Summary
log_info "═══════════════════════════════════════════════════════════════"
log_success "Local development environment is ready!"
log_info "═══════════════════════════════════════════════════════════════"
echo ""
echo -e "${GREEN}Web Services:${NC}"
echo -e "  Backend:      ${BLUE}http://localhost:8080${NC}"
echo -e "  Frontend:     ${BLUE}http://localhost:4200${NC}"
echo -e "  Database:     ${BLUE}postgresql://localhost:5432/catalogdb${NC}"
echo ""
echo -e "${GREEN}Log Files:${NC}"
echo -e "  Backend:      ${BLUE}tail -f $ROOT_DIR/logs/backend.log${NC}"
echo -e "  Frontend:     ${BLUE}tail -f $ROOT_DIR/logs/frontend.log${NC}"
echo ""
echo -e "${GREEN}API Documentation:${NC}"
echo -e "  Swagger UI:   ${BLUE}http://localhost:8080/swagger-ui.html${NC}"
echo -e "  API Docs:     ${BLUE}http://localhost:8080/v3/api-docs${NC}"
echo ""
echo -e "${GREEN}Default Credentials:${NC}"
echo -e "  Admin:        ${BLUE}admin / admin123${NC}"
echo -e "  User:         ${BLUE}user / user123${NC}"
echo ""
echo -e "${GREEN}Process Management:${NC}"
echo -e "  Backend PID:  ${BLUE}$(cat $ROOT_DIR/logs/backend.pid 2>/dev/null || echo 'N/A')${NC}"
echo -e "  Frontend PID: ${BLUE}$(cat $ROOT_DIR/logs/frontend.pid 2>/dev/null || echo 'N/A')${NC}"
echo ""
echo -e "${YELLOW}To stop services: press Ctrl+C${NC}"
echo ""

