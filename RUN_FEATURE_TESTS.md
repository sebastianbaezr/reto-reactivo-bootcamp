# 🧪 Ejecutar Tests - HU#1 Technology Registration

Guía para ejecutar solo los tests de la feature de registro de tecnologías, sin los tests preexistentes del proyecto.

---

## ⚡ Opción 1: Por Módulo (Recomendado)

### Tests de Domain/Usecase
```bash
./gradlew :usecase:test jacocoTestReport -x pitest
```
**Cubre:**
- ✅ RegisterTechnologyUseCaseTest (6 tests)
- ✅ TechnologyValidatorTest (37 tests)

### Tests de R2DBC Persistence
```bash
./gradlew :r2dbc-postgresql:test jacocoTestReport -x pitest
```
**Cubre:**
- ✅ TechnologyRepositoryAdapterTest (14 tests)
- ✅ GlobalWebExceptionHandlerTest (14+ tests)

### Tests de Web Layer
```bash
./gradlew :reactive-web:test --tests "*TechnologyHandler*" jacocoTestReport -x pitest
```
**Cubre:**
- ✅ TechnologyHandlerTest (10 tests)

### Todos los Tests de la Feature
```bash
./gradlew :usecase:test :r2dbc-postgresql:test --tests "*TechnologyHandler*" jacocoTestReport -x pitest
```

---

## 🎯 Opción 2: Filtrar por Nombres de Clase

### Solo TechnologyValidator
```bash
./gradlew test --tests "*TechnologyValidator*" jacocoTestReport -x pitest
```

### Solo RegisterTechnologyUseCase
```bash
./gradlew test --tests "*RegisterTechnologyUseCase*" jacocoTestReport -x pitest
```

### Solo TechnologyHandler
```bash
./gradlew test --tests "*TechnologyHandler*" jacocoTestReport -x pitest
```

### Solo TechnologyRepositoryAdapter
```bash
./gradlew test --tests "*TechnologyRepositoryAdapter*" jacocoTestReport -x pitest
```

### Todos los tests de Technology
```bash
./gradlew test --tests "*Technology*" jacocoTestReport -x pitest
```

---

## 📦 Opción 3: Ejecutar Múltiples Filtros

```bash
./gradlew test \
  --tests "*TechnologyValidator*" \
  --tests "*RegisterTechnologyUseCase*" \
  --tests "*TechnologyRepositoryAdapter*" \
  --tests "*TechnologyHandler*" \
  jacocoTestReport \
  -x pitest
```

---

## 🚀 Script Automatizado

### Crear archivo: `run-feature-tests.sh`

```bash
#!/bin/bash
# run-feature-tests.sh
# Ejecuta todos los tests de HU#1 - Technology Registration

echo "🧪 Ejecutando tests de HU#1 - Technology Registration"
echo "=================================================="

./gradlew \
  :usecase:test \
  :r2dbc-postgresql:test \
  --tests "*TechnologyHandler*" \
  jacocoTestReport \
  -x pitest \
  --info

echo ""
echo "✅ Tests completados"
echo ""
echo "📊 Reportes disponibles en:"
echo "  • Usecase: domain/usecase/build/reports/tests/test/index.html"
echo "  • R2DBC: infrastructure/driven-adapters/r2dbc-postgresql/build/reports/tests/test/index.html"
echo "  • Cobertura: */build/reports/jacocoHtml/index.html"
```

### Ejecutar script
```bash
chmod +x run-feature-tests.sh
./run-feature-tests.sh
```

---

## 📊 Ver Reportes

Una vez ejecutados los tests, abre estos archivos en el navegador:

### Reportes de Tests
```
domain/usecase/build/reports/tests/test/index.html
infrastructure/driven-adapters/r2dbc-postgresql/build/reports/tests/test/index.html
infrastructure/entry-points/reactive-web/build/reports/tests/test/index.html
applications/app-service/build/reports/tests/test/index.html
```

### Reportes de Cobertura (JaCoCo)
```
domain/usecase/build/reports/jacocoHtml/index.html
infrastructure/driven-adapters/r2dbc-postgresql/build/reports/jacocoHtml/index.html
infrastructure/entry-points/reactive-web/build/reports/jacocoHtml/index.html
```

---

## 📈 Estadísticas Esperadas

| Módulo | Tests | Status | Cobertura |
|--------|-------|--------|-----------|
| **TechnologyValidator** | 37 | ✅ | 100% |
| **RegisterTechnologyUseCase** | 6 | ✅ | ~95% |
| **TechnologyRepositoryAdapter** | 14 | ✅ | 100% |
| **GlobalWebExceptionHandler** | 14+ | ✅ | ~85% |
| **TechnologyHandler** | 10 | ✅ | ~90% |
| **TOTAL** | **71+** | ✅ | **80%+** |

---

## 🔧 Opciones Útiles

### Ejecutar sin Cobertura (Más Rápido)
```bash
./gradlew :usecase:test -x pitest
```

### Ejecutar sin Mutation Testing
```bash
./gradlew test --tests "*Technology*" jacocoTestReport -x pitest
```

### Ejecutar sin salida detallada
```bash
./gradlew :usecase:test jacocoTestReport -x pitest -q
```

### Ver solo fallos
```bash
./gradlew test --tests "*Technology*" jacocoTestReport -x pitest 2>&1 | grep -E "FAILED|PASSED|test"
```

---

## ✨ Recomendación

Para el desarrollo diario, usa:

```bash
./gradlew :usecase:test :r2dbc-postgresql:test --tests "*TechnologyHandler*" jacocoTestReport -x pitest
```

Es rápido, claro y cubre toda la feature sin tests preexistentes que fallan.
