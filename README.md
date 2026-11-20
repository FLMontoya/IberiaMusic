# IberiaMusic

[![CI](https://github.com/FLMontoya/IberiaMusic/actions/workflows/ci.yml/badge.svg)](https://github.com/FLMontoya/IberiaMusic/actions/workflows/ci.yml)
![Kotlin 2.3.21](https://img.shields.io/badge/Kotlin-2.3.21-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)
![minSdk 26](https://img.shields.io/badge/minSdk-26-3DDC84?logo=android&logoColor=white)
![Formato ktfmt](https://img.shields.io/badge/formato-ktfmt-blue)

App Android de búsqueda de música con pantalla de detalle, hecha con Kotlin y Jetpack Compose.

## Características

- Búsqueda de canciones con debounce de 300 ms; una búsqueda nueva cancela la anterior.
- Caché en memoria por query con TTL de 3 minutos y reloj inyectable.
- Pantalla de detalle con carátula y los campos que devuelve iTunes.
- Estados de carga, vacío y error, con accesibilidad cuidada (TalkBack, fuente al 200 %, contraste y
  teclado).
- Arquitectura en cuatro módulos (`app`, `presentation`, `domain`, `data`) con inyección manual,
  sin Hilt.

## Tecnologías

| Área | Tecnología | Versión |
|---|---|---|
| Lenguaje | Kotlin | 2.3.21 |
| Build | Android Gradle Plugin | 9.5.1 |
| UI | Jetpack Compose (BOM) y Material 3 | 2026.09.00 |
| Navegación | Navigation Compose | 2.10.2 |
| Asincronía | Coroutines y `StateFlow` | 1.11.0 |
| Red | Retrofit y OkHttp | 3.0.0 y 5.5.1 |
| Serialización | kotlinx.serialization | 1.13.0 |
| Imágenes | Coil | 3.6.3 |
| Formato | ktfmt (`com.ncorti.ktfmt.gradle`) | 0.28.0 |
| Tests | JUnit 4, coroutines-test, MockWebServer y Robolectric | 4.13.2, 1.11.0, 5.5.1 y 4.18.1 |

Android: `minSdk` 26, `targetSdk` y `compileSdk` 37. Las versiones están centralizadas en
`gradle/libs.versions.toml`.

## API elegida

[iTunes Search API](https://itunes.apple.com/search?term=queen&media=music): pública y sin API key.
Se consulta `search?term=...&media=music&entity=song&limit=50`, es decir, solo canciones y un
máximo de 50 resultados. El detalle muestra nombre, artista, álbum, género, duración y fecha de
lanzamiento, junto con la carátula; los campos que iTunes no devuelve se omiten.

## Cómo ejecutar

Requisitos: Android Studio reciente (o JDK 25 para la línea de comandos) y conexión a internet.

```bash
./gradlew assembleDebug        # compila la app
./gradlew installDebug         # la instala en un dispositivo o emulador (API 26+)
./gradlew ktfmtCheck test      # formato y tests unitarios (lo mismo que ejecuta el CI)
./gradlew ktfmtFormat          # aplica el formato
```

En Windows usa `gradlew.bat` en lugar de `./gradlew`.

Pasos desde cero:

1. Clona el repositorio: `git clone https://github.com/FLMontoya/IberiaMusic.git`.
2. Ábrelo en Android Studio y deja que sincronice Gradle, o usa la línea de comandos.
3. Arranca un emulador (API 26 o superior) o conecta un dispositivo y ejecuta `installDebug`.

El Gradle daemon pide JDK 25 (`gradle/gradle-daemon-jvm.properties`), que el CI también usa.

## Arquitectura

Cuatro módulos en la raíz, con esta regla de dependencias:

```
app ──▶ presentation ──▶ domain ◀── data ◀── app
```

| Módulo         | Tipo            | Contenido                                                          |
|----------------|-----------------|--------------------------------------------------------------------|
| `domain`       | Kotlin/JVM      | `Track`, `SearchResult`, `SearchRepository`, `SearchTracksUseCase` |
| `data`         | Kotlin/JVM      | API Retrofit, DTOs, mappers, caché en memoria, repositorio         |
| `presentation` | Android library | `SearchViewModel`, `SearchUiState`, pantallas Compose y tema       |
| `app`          | Application     | Inyección manual de dependencias y navegación                      |

`app` depende de los otros tres; `presentation` y `data` solo dependen de `domain`; `domain` no
depende de nadie y `presentation` nunca depende de `data`. No se usa Hilt: el `AppContainer` de
`app` construye el grafo a mano, una vez por proceso.

Los fallos de red no se lanzan como excepciones: el repositorio devuelve un `SearchResult`
(`Success`, `Empty` o `Error`) y la UI lo traduce a un `SearchUiState` (`Idle`, `Loading`,
`Content`, `Empty` o `Error`) expuesto en un único `StateFlow`.

## Decisiones

### Caché

- Caché en memoria por query, con TTL de 3 minutos contado desde que se guarda la entrada.
- Con caché válida no hay llamada de red. Si la entrada ha expirado, se consulta la red y se
  actualiza la caché. Los errores no se cachean.
- El reloj es una interfaz propia (`TimeProvider`) inyectada en la caché, de modo que los tests
  simulan el paso del tiempo sin esperas.
- La clave es la query sin espacios en los extremos y en minúsculas: "Queen", " queen " y "QUEEN"
  comparten entrada.
- Los resultados se deduplican por `id`, porque la lista usa el `id` como clave y debe ser única.
- La caché vive en `AppContainer`, así que sobrevive a cambios de configuración y de pantalla,
  pero no al cierre del proceso.

### Cancelación

- `SearchViewModel` usa `flatMapLatest`: cada cambio de texto emite `Loading` al instante, espera
  300 ms (el debounce) y busca, todo dentro del flujo que `flatMapLatest` cancela. Una query nueva
  cancela la búsqueda en curso, y los resultados de una búsqueda cancelada nunca se emiten.
- El debounce está dentro de `flatMapLatest` (un `delay`) y no como operador `debounce` previo: con
  el operador, durante los 300 ms seguía visible la lista de la query anterior junto al texto nuevo.
- La acción de búsqueda del teclado salta el debounce y también reintenta tras un error.
- No se guarda ningún `Job` a mano. `CancellationException` siempre se relanza, nunca se captura.

### Accesibilidad

- Textos en `sp` y pantallas con scroll, comprobadas con la fuente al 200 %.
- Áreas táctiles de al menos 48 dp, también el botón de borrar la búsqueda.
- Las carátulas de la lista son decorativas (`contentDescription = null`): la fila ya dice el
  título y el artista. La del detalle sí lleva descripción ("Carátula de …").
- El error se comunica con icono y texto, no solo con color.
- Títulos con `heading()` y cada fila de la lista (y cada campo del detalle) agrupada con
  `mergeDescendants`, de modo que TalkBack la lee como un único elemento.
- Estados de carga, vacío y error anunciados con `liveRegion`.
- Campo de búsqueda con `label` visible y `ImeAction.Search`.
- Todos los textos en `strings.xml`.
- Verificado en un emulador (Pixel 6, API 37): tamaño de fuente al 200 %, tema oscuro,
  TalkBack y navegación por teclado (ver [Capturas](#capturas)).
- No verificado: que TalkBack anuncie en voz alta los cambios de estado (`liveRegion`; una captura
  solo muestra el foco), el Accessibility Scanner, ni el foco de TalkBack sobre la carátula y los
  campos del detalle (el teclado solo enfoca elementos con acción).

### Otras

- Las carátulas se cargan con [Coil](https://coil-kt.github.io/coil/). La lista usa la miniatura de
  100 px y el detalle pide la de 600 px cambiando el tamaño en la URL de iTunes.
- Siguiendo las guías de Material 3: color dinámico (según el fondo de pantalla) en Android 12 o
  posterior, con la paleta estática como respaldo; ancho máximo de contenido de 840 dp en pantallas
  grandes; y *back* predictivo activado en el manifiesto. Los ratios de contraste de abajo son los de
  la paleta estática: con color dinámico dependen del fondo de pantalla.
- El `NavHost` aplica `imePadding`, para que el teclado no tape el final de la lista, y navega con
  `launchSingleTop`, para que un doble toque no apile dos detalles.
- La canción elegida se guarda con `rememberSaveable`, así que el detalle se restaura tras la
  muerte del proceso; el texto de búsqueda no.

## Tests

Se ejecutan en JVM con `./gradlew test`:

- `domain`: caso de uso con repositorio fake.
- `data`: caché (acierto, fallo y expiración), mapper, repositorio (con caché válida no llama a
  la red; con caché expirada, refresca) y contrato de `ITunesApi` contra un `MockWebServer`.
- `presentation`: ViewModel (cancelación), y semantics de las pantallas con Robolectric.
- `app`: navegación búsqueda → detalle y guardado de la canción seleccionada.

## Capturas

Tomadas en un emulador Pixel 6 (API 37, en español) con la app y la API reales de iTunes.

### Flujo principal

| 1. Inicio | 2. Resultados | 3. Detalle |
|---|---|---|
| <img src="docs/screenshots/01-inicial.png" width="240" alt="Pantalla de búsqueda vacía con el mensaje «Escribe para empezar a buscar»"> | <img src="docs/screenshots/02-resultados.png" width="240" alt="Lista de resultados para «queen» con carátula, título y artista"> | <img src="docs/screenshots/03-detalle.png" width="240" alt="Detalle de Bohemian Rhapsody con carátula y cinco campos"> |

### Estados vacío y error

| 4. Sin resultados | 5. Sin conexión |
|---|---|
| <img src="docs/screenshots/04-vacio.png" width="240" alt="Mensaje «No hay resultados para zzqqxx9917kk»"> | <img src="docs/screenshots/05-error.png" width="240" alt="Mensaje de error en rojo con icono de aviso"> |

El mensaje de vacío cita lo buscado. El de error lleva icono y texto, y la tecla de búsqueda del
teclado reintenta la búsqueda al recuperar la conexión.

### Fuente al 200 % y tema oscuro

| 6. Lista al 200 % | 7. Detalle al 200 % | 8. Detalle al 200 % (scroll) |
|---|---|---|
| <img src="docs/screenshots/06-fuente200-lista.png" width="240" alt="Lista con la fuente al 200 %"> | <img src="docs/screenshots/07-fuente200-detalle.png" width="240" alt="Detalle con la fuente al 200 %"> | <img src="docs/screenshots/08-fuente200-detalle-scroll.png" width="240" alt="Detalle al 200 % desplazado hasta el último campo"> |

| 9. Lista (oscuro) | 10. Detalle (oscuro) |
|---|---|
| <img src="docs/screenshots/09-oscuro-lista.png" width="240" alt="Lista en tema oscuro"> | <img src="docs/screenshots/10-oscuro-detalle.png" width="240" alt="Detalle en tema oscuro"> |

Ratios de contraste medidos sobre los píxeles de las capturas (fórmula WCAG; el mínimo para texto
es 4,5:1):

| Elemento | Tema | Texto | Fondo | Ratio |
|---|---|---|---|---|
| Título de la fila | Claro | `#1D1B20` | `#FEF7FF` | 16,2:1 |
| Subtítulo y etiqueta de campo | Claro | `#49454F` | `#FEF7FF` | 8,9:1 |
| Mensaje de error | Claro | `#B3261E` | `#FEF7FF` | 6,2:1 |
| Título de la fila | Oscuro | `#E6E0E9` | `#141218` | 14,3:1 |
| Subtítulo y etiqueta de campo | Oscuro | `#CAC4D0` | `#141218` | 10,9:1 |

El mensaje de error en el tema oscuro no se midió.

### TalkBack

Con TalkBack activado; el recuadro verde es su foco, es decir, lo que leería. Para moverse se usó
el teclado (`Tab`).

| 11. Título | 12. Campo | 13. Borrar |
|---|---|---|
| <img src="docs/screenshots/11-talkback-titulo.png" width="240" alt="Foco de TalkBack en el título «Buscar música»"> | <img src="docs/screenshots/12-talkback-campo.png" width="240" alt="Foco de TalkBack en el campo de búsqueda con su etiqueta"> | <img src="docs/screenshots/13-talkback-borrar.png" width="240" alt="Foco de TalkBack en el botón de borrar la búsqueda"> |
| Encabezado (`heading()`). | Etiqueta visible; el teclado muestra la tecla de búsqueda. | Botón de 48 dp con descripción. |

| 14. Fila | 15. Fila siguiente | 16. Detalle |
|---|---|---|
| <img src="docs/screenshots/14-talkback-fila.png" width="240" alt="Foco de TalkBack sobre una fila completa"> | <img src="docs/screenshots/15-talkback-fila-siguiente.png" width="240" alt="Foco de TalkBack sobre la fila siguiente"> | <img src="docs/screenshots/16-talkback-detalle-volver.png" width="240" alt="Foco de TalkBack en el botón Volver del detalle"> |
| Carátula, título y artista forman un único elemento. | El siguiente foco salta a la fila contigua. | Al abrir el detalle, el foco empieza en «Volver». |

## Flujo de trabajo

Una rama por paso desde `develop`, commits pequeños con Conventional Commits y una PR a `develop`
por rama. El CI ejecuta `ktfmtCheck` y `test` en cada PR hacia `develop`.

Las PR usan la plantilla `.github/pull_request_template.md`, que incluye una lista de comprobación
de tests y de accesibilidad.

## Licencia

El repositorio no incluye un archivo de licencia.
