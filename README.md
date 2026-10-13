# FocusAnki

App para Android que te obliga a estudiar en **AnkiDroid** antes de poder abrir
las apps que te distraen (Instagram, TikTok, YouTube, etc.). Cada respuesta en
Anki te da puntos y cada app protegida cuesta puntos para abrirla.

Hecha por **Brayan Barco** · Licencia MIT · Sin root, sin modificar AnkiDroid,
sin internet.

## Cómo funciona

1. Respondes tarjetas en AnkiDroid: **Again / Hard / Good / Easy** suman puntos
   (por defecto 0 / 2 / 5 / 8).
2. Eliges qué apps proteger y cuántos puntos cuesta abrir cada una.
3. Al abrir una app protegida:
   - con saldo suficiente, se descuenta el costo y tienes una **sesión**
     (2 minutos por defecto). Al terminar, si sigues dentro de la app, se
     vuelve a cobrar o se bloquea;
   - sin saldo, aparece una pantalla de bloqueo con acceso directo a AnkiDroid.

## Instalación

Descarga el APK desde el repositorio de releases:
<https://github.com/Barcodehub/FocusAnki-releases/releases>

1. Instala el APK (permite "instalar apps desconocidas" si Android lo pide).
2. Abre FocusAnki y, en **Inicio**, concede dos permisos:
   - **Servicio de accesibilidad** (Ajustes > Accesibilidad). En Android 13+,
     al instalar fuera de la tienda aparece bloqueado: abre
     *Ajustes > Apps > FocusAnki > ⋮ > Permitir ajustes restringidos* y vuelve
     a intentarlo.
   - **Mostrar sobre otras apps**.
3. En **Apps** toca "+" y elige las apps a proteger (AnkiDroid y FocusAnki no
   se pueden proteger).
4. En **Config** ajusta los puntos por botón (0–1000) y los minutos de sesión
   (1–240).
5. Para que el bloqueo sea fiable, excluye FocusAnki de la optimización de
   batería de tu fabricante.

## Compilar desde el código

Requisitos: JDK 17+ y Android SDK (API 34). El wrapper de Gradle va incluido.

```bash
./gradlew :app:testDebugUnitTest   # tests unitarios
./gradlew :app:assembleDebug       # APK debug (con panel de diagnóstico)
./gradlew :app:assembleRelease     # APK minificado con R8
```

Para firmar el release con tu propia llave, crea `keystore.properties` en la
raíz (está en `.gitignore`):

```properties
storeFile=C:/ruta/a/tu.jks
storePassword=...
keyAlias=...
keyPassword=...
```

Las llaves (`*.jks`, `*.keystore`) nunca deben subirse al repositorio.

## Arquitectura

```
domain/        modelos + interfaces de repositorio + casos de uso (sin Android)
data/          Room (entidades, DAOs) + implementaciones de repositorio
accessibility/ AccessibilityService, overlay de bloqueo y mapeo de botones de Anki
system/        utilidades de plataforma (permisos, listado de apps instaladas)
ui/            pantallas Compose + ViewModels (uno por pantalla)
di/            módulos de Hilt
```

Kotlin, Jetpack Compose, Room, Hilt y corrutinas. Cada movimiento de puntos se
guarda en `points_transactions` con `timestamp`, `delta`, `reason` y `source`
(`ANKI_ANSWER`, `APP_UNLOCK`, ...), por lo que estadísticas, rachas o logros se
pueden añadir después sin cambiar el esquema. El gasto es atómico
(`@Transaction` en `PointsTransactionDao`).

## Permisos

| Permiso | Para qué |
|---|---|
| Servicio de accesibilidad | Detectar las respuestas en AnkiDroid y qué app está en primer plano. Android exige concederlo manualmente. |
| `SYSTEM_ALERT_WINDOW` ("Mostrar sobre otras apps") | Dibujar la pantalla de bloqueo sobre la app protegida. |
| `<queries>` MAIN/LAUNCHER (no `QUERY_ALL_PACKAGES`) | Listar las apps abribles para el selector de apps a proteger. |

No hay permiso `INTERNET`: la app no puede enviar datos a ningún sitio.

## Privacidad

- Todo es local. `allowBackup=false` y las reglas de extracción excluyen la
  base de datos de copias de seguridad y transferencias entre dispositivos.
- El servicio de accesibilidad recibe eventos de todas las apps (clics y
  cambios de ventana) porque las apps protegidas son configurables. El código
  solo actúa sobre AnkiDroid y las apps protegidas, y en **release** no registra
  ni guarda nada de lo que ocurre (logs y panel de diagnóstico existen solo en
  debug).

## Modelo de amenazas y límites

FocusAnki es una herramienta de **autocontrol**, no un control parental: quien
tiene el teléfono puede desactivar el servicio, desinstalar la app o editar los
costos. Lo que sí evita son el uso accidental y los atajos fáciles:

- Entradas validadas (costos ≥ 1, límites en puntos, minutos y textos).
- Los botones de Anki se detectan por id (`ease1`..`ease4`) o texto exacto; un
  mazo llamado "Easy…" o un texto que solo empiece con "Good" no suma puntos.
- Bajar la barra de notificaciones o abrir el teclado no quita el bloqueo.
- Si AnkiDroid no está instalado, "Estudiar" lleva al inicio en vez de
  destapar la app protegida.

Limitaciones conocidas:

- Nada impide pulsar botones de respuesta en Anki sin estudiar (AnkiDroid no
  expone una señal verificable).
- El sistema puede matar el servicio en segundo plano en algunos fabricantes.
- Un servicio de accesibilidad que bloquea otras apps no cumple las políticas
  de Google Play; se distribuye como APK.
- Si una versión futura de AnkiDroid cambia los ids de los botones, el mapeo
  habrá que ajustarlo (`AnkiButtonMapper`).

## Licencia

MIT © 2026 Brayan Alexander Barco. Ver [LICENSE](LICENSE).
