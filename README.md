# Vivo/iQOO 144 FPS Unlocker

Small Android app that unlocks 144 Hz globally on certain Vivo/iQOO (OriginOS) devices by writing a vendor system setting.

Русская версия ниже.

## How it works
- Writes the system key `gamecube_frame_interpolation_for_sr` to `1:1::72:144` when ON, and to `0:-1:0:0:0` when OFF.
- A small foreground service re-applies the value periodically to prevent the system from reverting it.

Important: This only removes a software cap. Actual 144 Hz depends on your device, game/app, and thermal/perf limits.

## Install & use
- Download APK from the repo releases: https://github.com/yergali07/vivofpsunlocker
- Open the app → Home tab → toggle ON/OFF.
- If prompted, allow "Modify system settings" for the app (needed to write `Settings.System`).

### Quick Settings tiles (Android 7.0+)
- 144 FPS tile — toggles the global 144 Hz unlock.
- FPS Overlay tile — toggles a small floating FPS bubble over all apps.
- Add them via QS editor: Pull down QS → Edit → drag tiles into the active area.

### FPS overlay
- Shows current FPS system rendering above all apps, draggable.
- Requires "Display over other apps" permission on first use.

## Permissions
- `android.permission.WRITE_SETTINGS` — required to change `Settings.System` values.
- `android.permission.SYSTEM_ALERT_WINDOW` — required for the floating overlay bubble.

## Compatibility
- Designed for Vivo/iQOO devices on OriginOS where the key exists and is honored by the firmware.
- May not work on other vendors/ROMs or after firmware updates.

## Development
```bash
git clone https://github.com/yergali07/vivofpsunlocker
./gradlew :app:assembleDebug
```
The app uses Jetpack Compose + Material 3 with dynamic color and a bottom navigation (Home, About).

---

## Русская версия

Небольшое Android‑приложение, которое разблокирует 144 Гц глобально на некоторых устройствах Vivo/iQOO (OriginOS) путём изменения вендорской системной настройки.

### Как это работает
- При включении пишет в ключ `gamecube_frame_interpolation_for_sr` значение `1:1::72:144`, при выключении — `0:-1:0:0:0`.
- Фоновая служба периодически обновляет значение, чтобы система его не сбрасывала.

Важно: Это снимает только программное ограничение. Реальные 144 Гц зависят от устройства, приложений и тепловых/производительных лимитов.

### Установка и использование
1. Скачайте APK со страницы релизов: https://github.com/yergali07/vivofpsunlocker
2. Откройте приложение → вкладка Home → включите переключатель.
3. Если появится запрос, предоставьте приложению доступ "Изменение системных настроек".

#### Плитки в шторке быстрых настроек (Android 7.0+)
- Плитка «144 FPS» — включает/выключает глобальную разблокировку 144 Гц.
- Плитка «FPS Overlay» — включает/выключает плавающий индикатор FPS поверх всех приложений.
- Добавление: откройте шторку QS → «Изменить» → перетащите плитки в активную область.

#### Плавающий индикатор FPS
- Показывает текущий FPS системы поверх всех приложений, можно перетаскивать.
- При первом запуске потребуется разрешение «Отображение поверх других приложений».

### Разрешения
- `android.permission.WRITE_SETTINGS` — изменение значений в `Settings.System`.
- `android.permission.SYSTEM_ALERT_WINDOW` — для плавающего индикатора FPS.

### Совместимость
- Приложение рассчитано на устройства Vivo/iQOO c OriginOS, где присутствует и обрабатывается указанный ключ.
- На других устройствах/прошивках может не работать.

---

## License

MIT License

Copyright (c) 2025 yergali07 and contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
