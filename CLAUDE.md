# Prueba técnica Android

App Android de búsqueda + detalle. API: iTunes Search API
(https://itunes.apple.com/search?term=...&media=music), sin API key.

## Stack
- Kotlin, Jetpack Compose (todas las pantallas), Navigation Compose.
- Coroutines + StateFlow. Retrofit + OkHttp + kotlinx.serialization (o Moshi).
- Sin Hilt: inyección manual simple en el módulo `app`.
- Formato de código con ktfmt (plugin `com.ncorti.ktfmt.gradle`, tareas `ktfmtCheck` / `ktfmtFormat`).
- Pantallas con alta accesibilidad (TalkBack, tamaño de fuente, contraste y navegación por teclado):
    - Texto accesible en iconos e imágenes con significado (`contentDescription`), `null` en los decorativos.
    - Áreas táctiles de al menos 48dp x 48dp.
    - Contraste de texto mínimo 4.5:1 y sin transmitir información solo con color.
    - Textos en `sp` y layouts que no se rompen con fuente al 200%.
    - Títulos con `heading()` y cada fila de la lista agrupada como un único elemento (`mergeDescendants`).
    - Estados de carga, vacío y error anunciados a TalkBack (`liveRegion`).
    - TextField con `label` visible y `ImeAction.Search`.
    - Strings en `strings.xml`.

## Requisitos funcionales
- Pantalla de búsqueda (TextField + LazyColumn) y pantalla de detalle (mín. 3 campos).
- Una búsqueda nueva cancela la anterior; nunca se muestran resultados antiguos.
- Caché en memoria por query con TTL de 3 minutos. Con caché válida no hay llamada de red; si expira, red y se actualiza la caché.
- Reloj inyectable en la caché para poder testear la expiración.
- Tests: Domain con repositorio fake; Data (hit, miss y expiración de caché); ViewModel (cancelación).
- README breve: API elegida, cómo ejecutar, decisiones (caché, cancelación, arquitectura, accesibilidad).

## Arquitectura de módulos
Los módulos `domain`, `data` y `presentation` viven en la raíz, al mismo nivel que `app` (no dentro de `app`).

```
/
├── app            # Android application. Composition root: DI manual, Activity y Application. Sin UI
├── domain         # Kotlin/JVM puro. Modelos, contratos y casos de uso. Sin dependencias Android
├── data           # Caché, API (Retrofit), DTOs, mappers e implementación del repositorio
└── presentation   # Android library (Compose). ViewModels, pantallas, UI state, tema y navegación
```

Regla de dependencias (no romperla):
- `presentation` -> `domain`
- `data` -> `domain`
- `app` -> `presentation`, `data`, `domain`
- `presentation` NUNCA depende de `data`; `domain` NUNCA depende de nadie.

Declarar los módulos en `settings.gradle.kts` y centralizar versiones en `gradle/libs.versions.toml`.
`domain` y `data` son librerías JVM si no necesitan APIs de Android (tests más rápidos).

## Flujo Git (obligatorio)
Ramas: `main` (la gestiona solo el usuario), `develop` (integración) y una rama por cada paso.

1. Nunca commitear ni hacer push directo a `main` ni a `develop`. Nunca `push --force` ni `--no-verify`.
2. Cada paso del plan se hace en una rama nueva desde `develop` actualizado, con nombre descriptivo:
   `feature/<modulo>-<que-hace>`, `chore/<que>`, `docs/<que>`, `ci/<que>` (kebab-case).
3. Commits pequeños y cerrados: cada commit compila y deja los tests en verde. Un commit = una unidad con sentido
   (un modelo, un contrato, una clase con sus tests). Mínimo 2 commits por rama de código.
4. Mensajes con Conventional Commits: `tipo(ámbito): descripción en imperativo`, máx. 72 caracteres.
   Tipos: `feat`, `fix`, `test`, `refactor`, `chore`, `docs`, `ci`. Ámbitos: `domain`, `data`, `presentation`, `app`, `build`.
   Ejemplo: `feat(data): add in-memory search cache with TTL`.
5. Al terminar la rama: `./gradlew ktfmtCheck test` en verde -> `/code-review` -> `git push -u origin <rama>` ->
   `gh pr create --base develop` rellenando la plantilla `.github/pull_request_template.md`.
6. Se fusiona la PR en `develop` con merge commit (`gh pr merge --merge --delete-branch`) para conservar el historial
   de commits de la rama. Después, `git checkout develop && git pull`.
7. La PR de `develop` a `main` la abre y la fusiona el usuario. Claude no toca `main`.

## Forma de trabajar
- Haz UN solo paso del plan por petición. Al terminar (PR fusionada), resume en 2-3 líneas y PARA hasta que el usuario pida el siguiente.
- Antes de cada paso no trivial usa `/ecc:plan`. Para código usa el workflow `tdd-workflow`. Antes de la PR usa `/code-review`.
- No adelantes trabajo de pasos posteriores ni mezcles cambios no relacionados en una rama.
- Si algo falla o no está claro, pregunta antes de improvisar. Nunca capturar `CancellationException` sin relanzarla.

## Plan de pasos (en este orden)
0. **Preparación** (manual del usuario): repo vacío en GitHub, `gh auth login`, proyecto Android Studio (Empty Activity, Compose) en la carpeta.
   Claude: commit inicial en `main` (`chore: initial Android project`), crear y publicar `develop`.
1. `chore/gitignore-and-pr-template`: `.gitignore` Android y plantilla de PR (descripción, tipo de cambio, checklist de tests y accesibilidad).
2. `chore/modularize-project`: crear `domain`, `data` y `presentation`, un commit por módulo, y un commit final que cablea las dependencias. Verifica `./gradlew assembleDebug`.
3. `chore/ktfmt`: añadir ktfmt y formatear el proyecto.
4. `feature/domain-track-model`: modelo `Track`.
5. `feature/domain-search-usecase`: contrato `SearchRepository`, repositorio fake de test y `SearchTracksUseCase` con tests.
6. `feature/data-search-cache`: abstracción de reloj + caché en memoria con TTL + tests de hit, miss y expiración.
7. `feature/data-itunes-api`: DTOs, mapper a `Track` y `ITunesApi` (Retrofit) con tests del mapper.
8. `feature/data-search-repository`: `SearchRepositoryImpl` (caché primero, red después) + tests (con caché válida no llama a red; expirada, refresca).
9. `feature/presentation-search-viewmodel`: `SearchUiState` y `SearchViewModel` con cancelación de la búsqueda anterior + tests (una búsqueda cancelada no emite resultados).
10. `feature/presentation-search-screen`: `SearchScreen` con la checklist de accesibilidad + tests de UI de semantics.
11. `feature/presentation-detail-screen`: `DetailScreen` (mín. 3 campos) con accesibilidad.
12. `feature/app-navigation-and-di`: DI manual en `app` y navegación búsqueda -> detalle (la navegación vive en `presentation`).
13. `ci/run-tests-on-pr`: GitHub Actions que ejecuta `ktfmtCheck` y `test` en PRs hacia `develop`.
14. `docs/readme`: README breve (API, cómo ejecutar, decisiones de caché, cancelación, arquitectura y accesibilidad).
15. **Cierre**: Claude se detiene. El usuario abre la PR `develop` -> `main`.

## Variaciones respecto a la primera versión
Para que el código no sea idéntico al de la v1 (no copiar nada de ella; el código se escribe desde cero):
- `SearchResult` como `sealed interface` (Success / Empty / Error) en `domain` en lugar de lanzar excepciones.
- Interfaz `TimeProvider` propia en lugar de `System.currentTimeMillis()` directo.
- `debounce(300 ms)` + `flatMapLatest` en el ViewModel como mecanismo de cancelación, en lugar de guardar un `Job` a mano.
- `SearchUiState` como `sealed interface` (Idle / Loading / Content / Empty / Error) con un único `StateFlow`.

## Criterios
- Código claro y simple, sin features extra.
- Un paso = una rama = una PR a `develop`. Historial legible y con commits pequeños.
- Revisar la accesibilidad con la checklist del Stack antes de dar por terminada cada pantalla.