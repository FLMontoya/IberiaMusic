## Descripción

<!-- Qué hace esta PR y por qué. Enlaza el paso del plan si aplica. -->

## Tipo de cambio

- [ ] `feat`: nueva funcionalidad
- [ ] `fix`: corrección de un error
- [ ] `refactor`: cambio interno sin alterar el comportamiento
- [ ] `test`: añade o mejora tests
- [ ] `chore` / `ci` / `build`: configuración, herramientas o CI
- [ ] `docs`: documentación

## Módulos afectados

- [ ] `app`
- [ ] `domain`
- [ ] `data`
- [ ] `presentation`
- [ ] Ninguno (configuración o documentación)

## Checklist de tests

- [ ] Los tests nuevos o modificados cubren el comportamiento añadido.
- [ ] `./gradlew ktfmtCheck test` pasa en verde.
- [ ] Cada commit compila y deja los tests en verde.
- [ ] No hay tests ignorados ni comentados.
- [ ] No aplica (cambio sin código ni tests).

## Checklist de accesibilidad

<!-- Solo si la PR toca pantallas o componentes de UI. Si no, marca "No aplica". -->

- [ ] `contentDescription` en iconos e imágenes con significado, `null` en los decorativos.
- [ ] Áreas táctiles de al menos 48dp x 48dp.
- [ ] Contraste de texto mínimo 4.5:1 y sin transmitir información solo con color.
- [ ] Textos en `sp` y layout sin roturas con la fuente al 200%.
- [ ] Títulos con `heading()` y cada fila de lista agrupada con `mergeDescendants`.
- [ ] Estados de carga, vacío y error anunciados con `liveRegion`.
- [ ] `TextField` con `label` visible y `ImeAction.Search`.
- [ ] Textos en `strings.xml`.
- [ ] Probado con TalkBack y navegación por teclado.
- [ ] No aplica (sin cambios de UI).

## Notas para quien revisa

<!-- Decisiones, limitaciones o puntos donde quieres que se fije la revisión. -->
