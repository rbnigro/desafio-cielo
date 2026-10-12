"""Independent acceptance checks for the Flash Booking backend."""

from __future__ import annotations

import argparse
import json
import os
import re
import shutil
import subprocess
import sys
import xml.etree.ElementTree as ET
from dataclasses import asdict, dataclass
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
BACKEND = ROOT / "Codigo" / "backend"
IGNORED_PARTS = {".git", "target", "node_modules", "__pycache__"}
REQUIRED_FILES = (
    "README.md",
    "API_PAYLOADS.md",
    "IMPLEMENTATION.md",
    "docs/ARCHITECTURE.md",
    "docs/DATA_MODEL.md",
    "Dockerfile",
    "compose.yaml",
    "pom.xml",
    "src/main/resources/db/migration/V1__create_events_and_reservations.sql",
)
REQUIRED_ROUTES = (
    "POST /events",
    "GET /events/{id}",
    "POST /events/{id}/reservations",
    "GET /reservations/{id}",
    "DELETE /reservations/{id}",
)
REQUIRED_TEST_CASES = (
    "createsEventAndReturnsAvailability",
    "reservationIsIdempotentAndRejectsChangedPayloadForSameKey",
    "cancellationRestoresCapacityOnlyOnce",
    "expirationRestoresCapacityAndUpdatesReservationStatus",
    "doesNotOversellUnderConcurrentReservations",
    "returnsStructuredNotFoundErrors",
)
SECRET_PATTERNS = (
    re.compile(r"(?i)-----BEGIN (?:RSA|EC|OPENSSH|PRIVATE) KEY-----"),
    re.compile(
        r"""(?im)^[ \t]*[\w.-]*(?:password|passwd|secret|api[_-]?key|token)[ \t]*[:=][ \t]*["']?(?!\$\{)(?!<)[A-Za-z0-9/+_=.-]{8,}"""
    ),
)


@dataclass
class Check:
    check_id: str
    status: str
    detail: str
    command: str | None = None


def add(checks: list[Check], check_id: str, status: str, detail: str, command: str | None = None) -> None:
    checks.append(Check(check_id, status, detail, command))


def run_command(command: list[str], cwd: Path, env: dict[str, str] | None = None) -> tuple[int, str]:
    if sys.platform == "win32" and command[0] in {"mvn", "npm"}:
        command[0] += ".cmd"
    try:
        result = subprocess.run(
            command, cwd=cwd, env=env, capture_output=True, text=True, timeout=300
        )
    except FileNotFoundError as error:
        return 127, str(error)
    except subprocess.TimeoutExpired as error:
        return 124, f"Command exceeded the 300-second timeout: {error}"
    output = (result.stdout + "\n" + result.stderr).strip()
    return result.returncode, output[-5000:]


def project_files() -> list[Path]:
    return [
        path
        for path in BACKEND.rglob("*")
        if path.is_file() and not (IGNORED_PARTS & set(path.parts))
    ]


def check_required_artifacts(checks: list[Check]) -> None:
    missing = [name for name in REQUIRED_FILES if not (BACKEND / name).is_file()]
    if missing:
        add(checks, "00-DELIVERY-ARTIFACTS", "fail", "Missing: " + ", ".join(missing))
        return
    readme = (BACKEND / "README.md").read_text(encoding="utf-8", errors="replace").lower()
    required_readme_terms = ("docker compose", "mvn test", "trade-off", "evolu")
    absent_terms = [term for term in required_readme_terms if term not in readme]
    if absent_terms:
        add(
            checks,
            "00-DELIVERY-ARTIFACTS",
            "fail",
            "README missing required instructions/decision topics: " + ", ".join(absent_terms),
        )
        return
    add(checks, "00-DELIVERY-ARTIFACTS", "pass", "README, API, architecture, data model, migration and container files found")


def check_api_contract(checks: list[Check]) -> None:
    contract_path = BACKEND / "API_PAYLOADS.md"
    if not contract_path.is_file():
        return
    contract = contract_path.read_text(encoding="utf-8", errors="replace")
    missing = [route for route in REQUIRED_ROUTES if route not in contract]
    if missing:
        add(checks, "01-API-CONTRACT", "fail", "Missing required routes: " + ", ".join(missing))
    else:
        add(checks, "01-API-CONTRACT", "pass", "All five required routes documented")


def check_test_coverage_artifacts(checks: list[Check]) -> None:
    h2_test = BACKEND / "src" / "test" / "java" / "com" / "cielo" / "flashbooking" / "FlashBookingIntegrationTest.java"
    mysql_test = BACKEND / "src" / "test" / "java" / "com" / "cielo" / "flashbooking" / "MySqlConcurrencyIntegrationTest.java"
    if not h2_test.is_file() or not mysql_test.is_file():
        add(checks, "01-TEST-CASES", "fail", "H2 contract/concurrency test and MySQL Testcontainers test are required")
        return
    source = h2_test.read_text(encoding="utf-8", errors="replace")
    missing = [name for name in REQUIRED_TEST_CASES if name not in source]
    if missing:
        add(checks, "01-TEST-CASES", "fail", "Missing acceptance tests: " + ", ".join(missing))
        return
    add(checks, "01-TEST-CASES", "pass", "API, error, idempotency, cancellation, expiry and concurrency tests found")


def check_stack(checks: list[Check]) -> None:
    pom_path = BACKEND / "pom.xml"
    properties_path = BACKEND / "src" / "main" / "resources" / "application.properties"
    try:
        pom = ET.parse(pom_path).getroot()
        ns = {"m": "http://maven.apache.org/POM/4.0.0"}
        java_version = pom.findtext("m:properties/m:java.version", namespaces=ns)
        dependencies = {
            (
                dependency.findtext("m:groupId", namespaces=ns),
                dependency.findtext("m:artifactId", namespaces=ns),
            )
            for dependency in pom.findall("m:dependencies/m:dependency", ns)
        }
    except (OSError, ET.ParseError) as error:
        add(checks, "02-TECH-STACK", "fail", f"Cannot read Maven project: {error}")
        return
    props = properties_path.read_text(encoding="utf-8", errors="replace") if properties_path.is_file() else ""
    required_dependencies = {
        ("org.springframework.boot", "spring-boot-starter-web"),
        ("org.springframework.boot", "spring-boot-starter-data-jpa"),
        ("com.mysql", "mysql-connector-j"),
        ("org.flywaydb", "flyway-mysql"),
    }
    missing = sorted(f"{group}:{artifact}" for group, artifact in required_dependencies - dependencies)
    if java_version != "21" or missing or "jdbc:mysql:" not in props:
        details = []
        if java_version != "21":
            details.append(f"expected Java 21, found {java_version!r}")
        if missing:
            details.append("missing dependencies: " + ", ".join(missing))
        if "jdbc:mysql:" not in props:
            details.append("MySQL datasource URL missing")
        add(checks, "02-TECH-STACK", "fail", "; ".join(details))
        return
    source = "\n".join(
        path.read_text(encoding="utf-8", errors="replace")
        for path in project_files()
        if path.suffix == ".java" and "src" in path.parts
    )
    if "lombok" in source.lower():
        add(checks, "02-TECH-STACK", "fail", "Lombok usage found; implementation must use explicit Java code")
        return
    add(checks, "02-TECH-STACK", "pass", "Java 21, Spring MVC/JPA and MySQL/Flyway configured")


def check_container_setup(checks: list[Check]) -> None:
    compose_path = BACKEND / "compose.yaml"
    dockerfile_path = BACKEND / "Dockerfile"
    if not compose_path.is_file() or not dockerfile_path.is_file():
        add(checks, "03-CONTAINER-SETUP", "fail", "Dockerfile and Compose configuration are required")
        return
    compose = compose_path.read_text(encoding="utf-8", errors="replace")
    dockerfile = dockerfile_path.read_text(encoding="utf-8", errors="replace")
    if not all(marker in compose for marker in ("mysql:8.4", "build: .", "mysql_data", "service_healthy")):
        add(checks, "03-CONTAINER-SETUP", "fail", "Compose must define API, persistent MySQL and startup health dependency")
        return
    if "eclipse-temurin:21-jre" not in dockerfile or "USER 10001" not in dockerfile:
        add(checks, "03-CONTAINER-SETUP", "fail", "Runtime image must use Java 21 and a non-root user")
        return
    docker = shutil.which("docker")
    if docker is None:
        add(checks, "03-CONTAINER-SETUP", "warn", "Static container files look valid; Docker Compose CLI unavailable")
        return
    env = os.environ.copy()
    env.setdefault("DB_PASSWORD", "harness-local-placeholder")
    env.setdefault("MYSQL_ROOT_PASSWORD", "harness-root-placeholder")
    code, output = run_command(["docker", "compose", "config", "--quiet"], BACKEND, env)
    add(
        checks,
        "03-CONTAINER-SETUP",
        "pass" if code == 0 else "fail",
        output or "Docker Compose configuration is valid",
        "docker compose config --quiet",
    )


def check_domain_scope(checks: list[Check]) -> None:
    files = project_files()
    frontend_files = [
        path for path in files if path.suffix.lower() in {".ts", ".tsx", ".html", ".css", ".jsx"}
    ]
    old_domain_files = [
        path for path in files if path.suffix == ".java" and "paciente" in path.name.lower()
    ]
    if frontend_files or old_domain_files:
        details = [str(path.relative_to(BACKEND)) for path in frontend_files + old_domain_files]
        add(checks, "04-PROJECT-SCOPE", "fail", "Out-of-scope files remain: " + ", ".join(details))
    else:
        add(checks, "04-PROJECT-SCOPE", "pass", "Backend-only Flash Booking scope; no frontend or patient CRUD sources")


def check_secrets(checks: list[Check]) -> None:
    findings: list[str] = []
    allowed_suffixes = {".java", ".properties", ".sql", ".yaml", ".yml", ".xml"}
    for path in project_files():
        if path.name == ".env.example" or (path.suffix.lower() not in allowed_suffixes and path.name != "Dockerfile"):
            continue
        if path.stat().st_size > 1_000_000:
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        if any(pattern.search(text) for pattern in SECRET_PATTERNS):
            findings.append(str(path.relative_to(BACKEND)))
    add(
        checks,
        "05-SECRET-SCAN",
        "fail" if findings else "pass",
        "Possible hard-coded secrets: " + ", ".join(findings) if findings else "No hard-coded secret patterns found",
    )


def check_automated_tests(checks: list[Check], skip: bool) -> None:
    if skip:
        add(checks, "06-BACKEND-TESTS", "skip", "Tests were explicitly skipped")
        return
    if not BACKEND.is_dir():
        add(checks, "06-BACKEND-TESTS", "fail", f"Backend directory not found: {BACKEND}")
        return
    code, output = run_command(["mvn", "-q", "test"], BACKEND)
    if code == 127:
        add(checks, "06-BACKEND-TESTS", "warn", "Maven unavailable; automated tests were not executed", "mvn -q test")
    else:
        add(checks, "06-BACKEND-TESTS", "pass" if code == 0 else "fail", output or "Maven tests completed", "mvn -q test")
        if code == 0:
            mysql_report = (
                BACKEND
                / "target"
                / "surefire-reports"
                / "TEST-com.cielo.flashbooking.MySqlConcurrencyIntegrationTest.xml"
            )
            try:
                report = ET.parse(mysql_report).getroot()
                tests = int(report.attrib.get("tests", "0"))
                skipped = int(report.attrib.get("skipped", "0"))
            except (OSError, ET.ParseError, ValueError) as error:
                add(checks, "07-MYSQL-INTEGRATION", "fail", f"MySQL Testcontainers result missing/invalid: {error}")
                return
            if tests < 1:
                add(checks, "07-MYSQL-INTEGRATION", "fail", "MySQL concurrency integration test did not run")
            elif skipped:
                add(checks, "07-MYSQL-INTEGRATION", "warn", "MySQL concurrency test skipped; Docker/MySQL behavior remains unverified")
            else:
                add(checks, "07-MYSQL-INTEGRATION", "pass", "MySQL 8.4 concurrency integration test ran")


def main() -> int:
    parser = argparse.ArgumentParser(description="Run independent Flash Booking acceptance checks")
    parser.add_argument("--skip-tests", action="store_true", help="Skip Maven tests; required checks remain unverified")
    parser.add_argument("--report", type=Path, help="Write the JSON report to this path")
    args = parser.parse_args()

    checks: list[Check] = []
    check_required_artifacts(checks)
    check_api_contract(checks)
    check_test_coverage_artifacts(checks)
    check_stack(checks)
    check_container_setup(checks)
    check_domain_scope(checks)
    check_secrets(checks)
    check_automated_tests(checks, args.skip_tests)

    counts = {status: sum(check.status == status for check in checks) for status in ("pass", "warn", "fail", "skip")}
    report = {
        "harness": "flash-booking-acceptance",
        "generated_at": datetime.now(timezone.utc).isoformat(),
        "root": str(ROOT),
        "decision": "blocked" if counts["fail"] or counts["skip"] else (
            "approved_with_remarks" if counts["warn"] else "approved"
        ),
        "counts": counts,
        "checks": [asdict(check) for check in checks],
    }
    print(json.dumps(report, ensure_ascii=False, indent=2))
    if args.report:
        args.report.parent.mkdir(parents=True, exist_ok=True)
        args.report.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    return 1 if counts["fail"] or counts["skip"] else 0


if __name__ == "__main__":
    sys.exit(main())
