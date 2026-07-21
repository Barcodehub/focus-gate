# FocusGate

App personal para Android que obliga a estudiar inglés en AnkiDroid antes de
poder abrir apps de entretenimiento (Instagram, TikTok, YouTube, etc).
Uso exclusivamente personal, sin root, sin modificar AnkiDroid.

## Cómo probarlo

1. Abre el proyecto en Android Studio (Hedgehog o más reciente). Al
   sincronizar, Android Studio genera automáticamente el
   `gradle-wrapper.jar` (no se incluye en el zip porque requiere descarga
   desde internet).
2. Instala la app en tu dispositivo real (un `AccessibilityService` con
   overlay no tiene mucho sentido en emulador, pero también funciona ahí
   si quieres probar antes).
3. Abre FocusGate. En la pestaña **Inicio** verás dos tarjetas de permisos:
   - **Servicio de accesibilidad**: actívalo desde el botón "Activar
     ahora" (te lleva a Ajustes > Accesibilidad).
   - **Mostrar sobre otras apps**: actívalo igual (te lleva al ajuste de
     overlay específico de esta app).
4. Ve a la pestaña **Apps**, toca "+", busca Instagram/TikTok/YouTube (o
   cualquier otra), y define cuántos puntos cuesta abrirla.
5. Ve a la pestaña **Config** y ajusta los puntos por Again/Hard/Good/Easy
   si quieres cambiar los valores por defecto (0/2/5/8) y los minutos de
   la sesión de desbloqueo (por defecto 2).
6. Abre AnkiDroid y responde tarjetas: verás tu saldo subir en la pestaña
   **Inicio**.
7. Intenta abrir una app protegida: si tienes puntos suficientes se
   descuentan automáticamente y la app abre normal; si no, aparece la
   pantalla de bloqueo con la opción de ir a estudiar o volver al inicio.

## Arquitectura

```
domain/        modelos + interfaces de repositorio + casos de uso (sin Android)
data/          Room (entidades, DAOs) + implementaciones de repositorio
accessibility/ AccessibilityService + controlador del overlay (WindowManager+Compose)
system/        utilidades de plataforma (permisos, listado de apps instaladas)
ui/            pantallas Compose + ViewModels (uno por pantalla)
di/            módulos de Hilt
```

Ver los comentarios en `GateAccessibilityService.kt`, `OverlayController.kt`
y `PointsTransactionDao.kt` (gasto atómico con `@Transaction`) para el
razonamiento detallado de cada decisión.

### Por qué es fácil agregar Estadísticas / Rachas / Logros

Cada movimiento de puntos (ganado por Anki o gastado al desbloquear una
app) se guarda como una fila en `points_transactions` con `timestamp`,
`delta`, `reason` y `source` (`ANKI_ANSWER` / `APP_UNLOCK` /
`MANUAL_ADJUSTMENT`). Esa tabla ya es la fuente de verdad para cualquier
análisis futuro:

- **Estadísticas**: un `ObserveDailyPointsUseCase` que agrupe
  `points_transactions` por día usando el `timestamp`.
- **Rachas**: un caso de uso que recorra los días con al menos una
  transacción `ANKI_ANSWER` y calcule días consecutivos.
- **Logros**: reglas declarativas evaluadas contra el mismo historial
  (p. ej. "100 respuestas Easy seguidas", "7 días de racha").

Ninguno de estos requiere tocar el esquema de Room existente ni el
`AccessibilityService`; solo se añaden nuevos casos de uso y pantallas que
leen `ObservePointsHistoryUseCase`.

## Permisos y por qué

| Permiso | Para qué |
|---|---|
| Accessibility Service | Detectar clics en AnkiDroid y cambios de ventana en primer plano de otras apps. Se concede manualmente en Ajustes (Android no permite hacerlo por código). |
| `SYSTEM_ALERT_WINDOW` ("Mostrar sobre otras apps") | Dibujar la pantalla de bloqueo por encima de la app protegida. |
| `<queries>` con intent MAIN/LAUNCHER (no `QUERY_ALL_PACKAGES`) | Listar las apps instaladas para el selector de "apps a proteger", sin pedir el permiso sensible de listar *todos* los paquetes. |

## Limitaciones conocidas

- La heurística de detección de botones de Anki (`ease1`..`ease4` + texto
  "Again/Hard/Good/Easy" en inglés/español) puede necesitar ajuste si tu
  versión de AnkiDroid cambia esos identificadores. El POC anterior incluía
  un volcado de árbol de depuración; se puede reincorporar aquí de la misma
  forma si hace falta.
- El servicio de accesibilidad recibe eventos de **todas** las apps (no
  solo AnkiDroid) porque la lista de apps protegidas es configurable; esto
  implica algo más de consumo de batería que una restricción fija por
  `packageNames`.
- El overlay requiere que el usuario haya concedido manualmente el
  permiso "Mostrar sobre otras apps"; si lo revoca, el bloqueo deja de
  mostrarse (aunque el descuento de puntos/registro de Anki sigue
  funcionando).
- Para máxima fiabilidad en segundo plano, se recomienda además excluir la
  app de la optimización de batería del fabricante (esto varía mucho
  entre marcas y no se automatiza en este POC).
