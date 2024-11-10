# Анализатор лог-файлов

### Путь (--path)

* URL

  Программа поддерживает ввод как одного url, так и нескольких, используя символ "|" для разграничения.
  <br>
  <br>
  Пример:

      --path https://logs/2024/nginx_logs
      --path https://logs/2024/nginx_logs|https://logs/2023/nginx_logs

  P.S. В качестве теста использовался URL:
  https://raw.githubusercontent.com/elastic/examples/master/Common%20Data%20Formats/nginx_logs/nginx_logs
  <br>
  <br>
* File

  Программа поддерживает несколько форматов ввода пути.
  <br>
  <br>
  Пример:

      --path logs/2023/app.log   (Будет прочитан один конкретный файл)
      --path logs/2023/*         (Будут прочитаны все файлы, находящиеся по пути "logs/2023")
      --path logs/**/access.log  (Будyт прочитаны все файлы, имеющие название "access.log")

### Временные промежутки (--from / --to)

Программа поддерживает ввод даты начала (--from) и даты конца (--to) в формате ISO8601.
<br>
<br>
Пример:

      --from 2015-05-17T00:00:00+00:00
      --to 2015-05-17T23:59:59+00:00

### Формат вывода (--format)

Программа поддерживает два формата вывода:

* Markdown
* Adoc

Пример:

    --format markdown
    --format adoc

### Фильтрация (--filter-field / --filter-value)

Программа поддерживает фильтрация по 3 полям:

* agent
* method (С этим полем поддерживает сортировка по возрастанию и убыванию (asc/desc))
* status

Пример:

    --filter-field agent --filter-value "Mozilla"
    --filter-field method --filter-value "GET" --order asc 

### Дополнительные характеристики

Реализованы две дополнительные характеристики:

* Топ-15 дней по количеству запросов
* Топ-15 IP-адресов по количеству запросов

### Полный пример запуска программы

    java
    LogAnalyzer
    --path
    logs/**/access.log
    --from
    2015-05-17T00:00:00+00:00
    --to
    2015-05-17T23:59:59+00:00
    --format
    markdown
    --filter-field
    method
    --filter-value
    "GET"
    --order
    desc
