# Приложение по экономике (Java, ООП)

Приложение показывает покупательную способность по выбранному году и списку продуктов на данных Росстата.

## Архитектура

- `app` — точка входа приложения.
- `ui` — графический интерфейс.
- `model` — доменные модели.
- `service` — orchestration/бизнес-логика загрузки.
- `loader` — источники данных (сайт, xlsx, txt).
- `parser` — общий парсер таблиц.
- `util` — утилиты (в т.ч. исправление кодировки).

## Диаграмма классов

```mermaid
classDiagram
    class EconomyRosstatApp
    class EconomyMainFrame
    class DataLoadService
    class LoadResult
    class DataSet
    class DataLoader
    class RosstatHtmlDataLoader
    class XlsxDataLoader
    class TextDataLoader
    class TableParser
    class TextFixer

    EconomyRosstatApp --> DataLoadService
    EconomyRosstatApp --> EconomyMainFrame
    DataLoadService --> DataLoader
    DataLoadService --> LoadResult
    LoadResult --> DataSet
    DataLoader <|.. RosstatHtmlDataLoader
    DataLoader <|.. XlsxDataLoader
    DataLoader <|.. TextDataLoader
    RosstatHtmlDataLoader --> TableParser
    RosstatHtmlDataLoader --> TextFixer
    XlsxDataLoader --> TableParser
    TextDataLoader --> TableParser
    EconomyMainFrame --> DataSet
```

## Запуск

```bash
javac app/EconomyRosstatApp.java model/*.java service/*.java loader/*.java parser/*.java util/*.java ui/*.java
java app.EconomyRosstatApp
```
