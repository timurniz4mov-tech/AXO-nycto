# Эхо Пустоты (Echo of the Hollow) — Forge 1.20.1

## Как собрать
1. Скачай Forge MDK 1.20.1 (47.x): https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html
2. Распакуй MDK, удали `src/main/java/com/example` и `src/main/resources/META-INF/mods.toml`.
3. Скопируй папку `src` из этого архива в MDK (с заменой).
4. В `gradle.properties` MDK поставь `mod_id=hollowecho`.
5. Java 17. Запуск: `gradlew runClient` (тест) или `gradlew build` -> jar в `build/libs`.

## Как играть
- При первом входе: тьма, сердцебиение, титры, дневник и Компас эха.
- ПКМ компасом — направление на Маяк. Или `/locate structure hollowecho:forgotten_lighthouse`.
- Хранитель Нил: ПКМ по нему. Квест: 3 аметиста -> 8 светящейся пыли -> Око Края -> финал.
- В сундуке маяка есть часть нужных предметов.
- Яйцо призыва: творческая вкладка «Эхо Пустоты».
