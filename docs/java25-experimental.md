# Branch Experimental: Java 25 + Spring Boot 3.3.x

## Objetivo

Explorar las últimas versiones de Java y Spring Boot para mantener FinGuard a la vanguardia tecnológica, sin comprometer la estabilidad de la rama productiva.

## Estado

Exploratorio. No productivo.

## Cambios principales

- **Java 25**: uso de características modernas del lenguaje (variables locales en patrones, mejoras en strings, etc.).
- **Spring Boot 3.3.x**: mejoras en observabilidad, soporte a Virtual Threads mejorado, actualizaciones de dependencias.
- **Maven Compiler Plugin**: configurado con `<release>25</release>` para garantizar compatibilidad.

## Cómo trabajar con esta rama

```bash
# Clonar el repositorio
git clone <repo-url>
cd finguard

# Crear la rama experimental (si no existe)
git checkout -b java-25-spring-3.3

# Usar el POM experimental
cp pom-experimental.xml pom.xml

# Compilar y probar
mvn clean verify

# Compilar imagen Docker
docker build -t finguard:experimental .
```

## Riesgos y consideraciones

- Spring Boot 3.3.x puede tener cambios de API menores que requieran ajustes en el código.
- Java 25 es una versión no-LTS; no se recomienda para producción hasta que sea LTS o se evalúe su madurez.
- Algunas dependencias (ej. AWS SDK, Spring Cloud) pueden requerir actualizaciones adicionales.

## Plan de migración

1. Evaluar compilación y tests en Java 25/Spring Boot 3.3.x.
2. Resolver breaking changes.
3. Ejecutar pruebas de integración y carga.
4. Cuando Spring Boot 3.3.x sea LTS o se considere estable, promover a rama principal.
