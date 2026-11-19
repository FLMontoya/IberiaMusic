# IberiaMusic

App Android de búsqueda de música con pantalla de detalle, hecha con Kotlin y Jetpack Compose.

## API elegida

[iTunes Search API](https://itunes.apple.com/search?term=queen&media=music): pública y sin API key.
Se consulta `search?term=...&media=music&entity=song&limit=50`, es decir, solo canciones y un
máximo de 50 resultados. El detalle muestra nombre, artista, álbum, género, duración y fecha de
lanzamiento; los campos que iTunes no devuelve se omiten.

## Cómo ejecutar

Requisitos: Android Studio reciente (o JDK 25 para la línea de comandos) y conexión a internet.

```bash
./gradlew assembleDebug        # compila la app
./gradlew installDebug         # la instala en un dispositivo o emulador (API 26+)
./gradlew ktfmtCheck test      # formato y tests unitarios (lo mismo que ejecuta el CI)
./gradlew ktfmtFormat          # aplica el formato
```

En Windows usa `gradlew.bat` en lugar de `./gradlew`.

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
- La clave es el texto de la query ya recortado, sin pasar a minúsculas: "Queen" y "queen" son
  entradas distintas.
- La caché vive en `AppContainer`, así que sobrevive a cambios de configuración y de pantalla,
  pero no al cierre del proceso.

### Cancelación

- `SearchViewModel` encadena `debounce(300 ms)`, `distinctUntilChanged` y `flatMapLatest`. Una
  query nueva cancela la búsqueda en curso, y los resultados de una búsqueda cancelada nunca se
  emiten.
- No se guarda ningún `Job` a mano. `CancellationException` siempre se relanza, nunca se captura.
- Durante los 300 ms de debounce todavía se muestra el resultado de la búsqueda anterior; solo se
  descarta cuando llega una query nueva.

### Accesibilidad

- Textos en `sp` y pantallas con scroll, comprobadas con la fuente al 200 %.
- Áreas táctiles de al menos 48 dp.
- Títulos con `heading()` y cada fila de la lista (y cada campo del detalle) agrupada con
  `mergeDescendants`, de modo que TalkBack la lee como un único elemento.
- Estados de carga, vacío y error anunciados con `liveRegion`.
- Campo de búsqueda con `label` visible y `ImeAction.Search`.
- Todos los textos en `strings.xml`.
- Pendiente: el contraste no se mide en los tests y TalkBack y la navegación por teclado no se han
  probado en un dispositivo.

### Otras

- El detalle no muestra la carátula: haría falta una librería de imágenes y no se pedía.
- La canción elegida se guarda con `rememberSaveable`, así que el detalle se restaura tras la
  muerte del proceso; el texto de búsqueda no.

## Tests

Se ejecutan en JVM con `./gradlew test`:

- `domain`: caso de uso con repositorio fake.
- `data`: caché (acierto, fallo y expiración), mapper y repositorio (con caché válida no llama a
  la red; con caché expirada, refresca).
- `presentation`: ViewModel (cancelación), y semantics de las pantallas con Robolectric.
- `app`: navegación búsqueda → detalle y guardado de la canción seleccionada.

## Flujo de trabajo

Una rama por paso desde `develop`, commits pequeños con Conventional Commits y una PR a `develop`
por rama. El CI ejecuta `ktfmtCheck` y `test` en cada PR hacia `develop`.
