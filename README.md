Taller Metodológico SQA — Serenity Screenplay + Cucumber

Automatización UI con Serenity BDD (Screenplay), Cucumber, cobertura con JaCoCo y análisis en SonarCloud. Incluye script para exportar métricas a CSV y PDF.

Requisitos:

	* Java 17 (el proyecto usa toolchain en Gradle).
	* Chrome o Edge (para generar el PDF del reporte).
	* SonarCloud (opcional, si corres análisis local): token personal.

Comandos clave:

1) Pruebas + cobertura + reporte Serenity

	* .\gradlew clean test jacocoTestReport

	* Serenity: target/site/serenity/index.html
	* JaCoCo: build/reports/jacoco/test/jacocoTestReport.html

2) Análisis local con SonarCloud (análisis automático desactivado)

	Configura el token:

	Temporal (solo esta sesión PowerShell):

	* $env:SONAR_TOKEN = "<TOKEN>"

	Lanza análisis:

	* .\gradlew clean test jacocoTestReport sonar "-Dsonar.token=$env:SONAR_TOKEN"

3) Exportar métricas Sonar a CSV y PDF
	
	* .\scripts\sonar-export.ps1

	Genera sonar_metrics.csv y sonar_report.pdf en la raíz del proyecto.

Estructura mínima:

	src/main/java | resources
	test/java     | resources (features)
	scripts/sonar-export.ps1
	build.gradle
	gradle.properties

Notas importantes:

* Si usas Análisis Automático en SonarCloud (por GitHub), evita ejecutar sonar local o desactiva el automático.
* El coverage en SonarCloud usa build/reports/jacoco/test/jacocoTestReport.xml.
* No subas tokens al repo.

Problemas comunes:

* No aparece coverage en Sonar: verifica que el XML de JaCoCo exista y que el análisis se ejecute después de test jacocoTestReport.
* PDF no se genera: confirma que Chrome/Edge esté instalado y accesible.
* Java version: usa JDK 17 (el toolchain ya lo fija).
