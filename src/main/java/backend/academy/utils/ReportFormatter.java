package backend.academy.utils;

import backend.academy.config.Config;
import backend.academy.dto.Arguments;
import backend.academy.dto.LogStatistics;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
@SuppressWarnings("MagicNumber")
public class ReportFormatter {

    private static final String ADOC_BORDER_1 = "|===\n";

    private static final String ADOC_BORDER_2 = "|===\n\n";

    private static final String FORMAT_40 = "%-40s";

    private static final String FORMAT_10 = "%-10d";

    private static final String LINE = "|------------------------------------------|--------------|\n";

    public static String format(LogStatistics stats, Arguments arguments) {
        if (arguments.format() == null) {
            return formatMarkdown(stats, arguments);
        }
        return switch (arguments.format().toUpperCase()) {
            case Config.MARKDOWN -> formatMarkdown(stats, arguments);
            case Config.ADOC -> formatADoc(stats, arguments);
            default -> throw new IllegalArgumentException("Неподдерживаемый формат: " + arguments.format());
        };
    }

    private static String formatMarkdown(LogStatistics stats, Arguments arguments) {
        StringBuilder report = new StringBuilder();

        report.append("#### Общая информация\n\n");
        report.append("| Метрика                 | Значение          \n");
        report.append("|-------------------------|-------------------\n");
        report.append(String.format("| Файл(-ы)                | %s \n",
            stats.filesNames().toString().replace("[", "").replace("]", "")));
        report.append(String.format("| Начальная дата          | %s \n", getDate(arguments.from())));
        report.append(String.format("| Конечная дата           | %s \n", getDate(arguments.to())));
        report.append(String.format("| Количество запросов     | %d \n", stats.totalRequests()));
        report.append(String.format("| Средний размер ответа   | %.2f b \n", stats.avgResponseSize()));
        report.append(String.format("| 95-й персентиль размера | %.2f b \n", stats.responseSizePercentile95()));
        report.append("\n\n");

        report.append("#### Запрашиваемые ресурсы\n\n");
        report.append("| Ресурс                                   | Количество        |\n");
        report.append("|------------------------------------------|-------------------|\n");
        report.append(buildTable(stats.resources(), null, arguments.order(), FORMAT_40, "%-15d", true));
        report.append("\n");

        report.append("#### Коды ответа\n\n");
        report.append("| Код | Имя                    | Количество   |\n");
        report.append("|-----|------------------------|--------------|\n");
        stats.statuses().forEach((code, count) -> {
            report.append(String.format("| %-3d | %-22s | %-10s   |\n", code, getStatusMessage(code), count));
        });
        report.append("\n");

        report.append("#### Топ-15 IP-адресов по количеству запросов \n\n");
        report.append("| Адрес                                    | Количество   |\n");
        report.append(LINE);
        report.append(buildTable(stats.ipAddresses(), Config.LIMIT_DEFAULT, Config.DESC, FORMAT_40, FORMAT_10, true));
        report.append("\n");

        report.append("#### Топ-15 дней по количеству запросов \n\n");
        report.append("| Дата                                     | Количество   |\n");
        report.append(LINE);
        report.append(
            buildTable(stats.countRequestsPerDay(), Config.LIMIT_DEFAULT, Config.DESC, FORMAT_40, FORMAT_10, true));

        return report.toString();
    }

    private static String formatADoc(LogStatistics stats, Arguments arguments) {
        StringBuilder report = new StringBuilder();

        report.append("== Общая информация\n\n");
        report.append(ADOC_BORDER_1);
        report.append("| Метрика                 | Значение\n");
        report.append(String.format("| Файл(-ы)                | %s\n",
            stats.filesNames().toString().replace("[", "").replace("]", "")));
        report.append(String.format("| Начальная дата          | %s\n", getDate(arguments.from())));
        report.append(String.format("| Конечная дата           | %s\n", getDate(arguments.to())));
        report.append(String.format("| Количество запросов     | %d\n", stats.totalRequests()));
        report.append(String.format("| Средний размер ответа   | %.2f b\n", stats.avgResponseSize()));
        report.append(String.format("| 95-й перцентиль размера | %.2f b\n", stats.responseSizePercentile95()));
        report.append(ADOC_BORDER_2);

        report.append("== Запрашиваемые ресурсы\n\n");
        report.append(ADOC_BORDER_1);
        report.append("| Ресурс                                   | Количество\n");
        report.append(buildTable(stats.resources(), null, arguments.order(), FORMAT_40, FORMAT_10, false));
        report.append(ADOC_BORDER_2);

        report.append("== Коды ответа\n\n");
        report.append(ADOC_BORDER_1);
        report.append("| Код | Имя                    | Количество\n");
        stats.statuses().forEach((code, count) -> {
            report.append(String.format("| %-3d | %-22s | %d\n", code, getStatusMessage(code), count));
        });
        report.append(ADOC_BORDER_2);

        report.append("== Топ-15 IP-адресов по количеству запросов \n\n");
        report.append(ADOC_BORDER_1);
        report.append("| Адрес                                    | Количество   \n");
        report.append(buildTable(stats.ipAddresses(), Config.LIMIT_DEFAULT, Config.DESC, FORMAT_40, FORMAT_10, false));
        report.append(ADOC_BORDER_2);

        report.append("== Топ-15 дней по количеству запросов \n\n");
        report.append(ADOC_BORDER_1);
        report.append("| Дата                                     | Количество   \n");
        report.append(
            buildTable(stats.countRequestsPerDay(), Config.LIMIT_DEFAULT, Config.DESC, FORMAT_40, FORMAT_10, false));
        report.append(ADOC_BORDER_1);

        return report.toString();
    }

    private static <K> String buildTable(
        Map<K, Long> map,
        Long limit,
        String order,
        String col1,
        String col2,
        boolean isMarkdown
    ) {
        StringBuilder sb = new StringBuilder();
        Comparator<? super Map.Entry<K, Long>> sortedMap =
            order != null && order.equalsIgnoreCase(Config.ASC) ? Map.Entry.comparingByValue()
                : Map.Entry.<K, Long>comparingByValue().reversed();

        long finalLimit = limit == null ? Integer.MAX_VALUE : limit;

        String format = "| " + col1 + " | " + col2;
        format = isMarkdown ? format + "   |" : format;
        String finalFormat = format + "\n";

        map.entrySet().stream()
            .sorted(sortedMap)
            .limit(finalLimit)
            .forEach(entry -> sb.append(String.format(finalFormat, entry.getKey(), entry.getValue())));
        return sb.toString();
    }

    private static String getDate(LocalDateTime date) {
        return date != null ? date.toString() : " - ";
    }

    private static String getStatusMessage(int code) {
        return switch (code) {
            case 200 -> "OK";
            case 206 -> "Partial Content";
            case 403 -> "Forbidden";
            case 304 -> "Not Modified";
            case 404 -> "Not Found";
            case 416 -> "Range Not Satisfiable";
            case 500 -> "Internal Server Error";
            default -> "Unknown";
        };
    }
}
