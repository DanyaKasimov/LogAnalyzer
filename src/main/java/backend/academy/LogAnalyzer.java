package backend.academy;

import backend.academy.config.Config;
import backend.academy.dto.Arguments;
import backend.academy.dto.LogRecord;
import backend.academy.dto.LogStatistics;
import backend.academy.exceptions.FileNotFoundException;
import backend.academy.exceptions.NoFoundDataException;
import backend.academy.exceptions.ResourseNotFoundException;
import backend.academy.utils.LogParser;
import backend.academy.utils.ReportFormatter;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings({"RegexpSinglelineJava", "MagicNumber"})
public class LogAnalyzer {

    public void run(String[] args) {
        try {
            if (args == null || args.length == 0) {
                throw new IllegalArgumentException("Аргументы отсутствуют.");
            }
            Arguments arguments = Arguments.parse(args);
            LogStatistics stats = loadLogs(arguments);
            String report = ReportFormatter.format(stats, arguments);
            System.out.println(report);
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    public LogStatistics loadLogs(Arguments arguments) {
        if (arguments.path().startsWith("http") || arguments.path().startsWith("https")) {
            return loadLogsFromUrl(arguments);
        } else {
            return loadLogsFromFile(arguments);
        }
    }

    public LogStatistics loadLogsFromUrl(Arguments arguments) {
        String[] urls = arguments.path().split("\\|");
        List<String> urlsNames = Arrays.asList(urls);

        try (Stream<String> lines = urlsNames.stream()
            .map(this::createURL)
            .flatMap(this::readUrl)) {
            return getStats(lines, arguments).filesNames(urlsNames);
        }
    }

    private URL createURL(String urlPath) {
        try {
            return new URL(urlPath);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Неверный формат URL: " + urlPath, e);
        }
    }

    private Stream<String> readUrl(URL url) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            return reader.lines().onClose(() -> closeReader(reader, url));
        } catch (IOException e) {
            throw new ResourseNotFoundException("Ресурс по адресу: " + url + " не доступен.");
        }
    }

    private void closeReader(BufferedReader reader, URL url) {
        try {
            reader.close();
        } catch (IOException e) {
            throw new UncheckedIOException("Ошибка при закрытии ресурса: " + url, e);
        }
    }

    public LogStatistics loadLogsFromFile(Arguments arguments) {
        String path = Config.DIR_PATH;
        String file = "";
        List<String> filesNames;
        Stream<String> lines;

        if (arguments.path().contains("**")) {
            path += arguments.path().substring(0, arguments.path().indexOf("**") - 1);
            file = arguments.path().substring(arguments.path().indexOf("**") + 3);
        } else {
            path += arguments.path().replace("/*", "");
        }

        File folder = new File(path);

        if (!folder.exists()) {
            throw new FileNotFoundException("Файл(-ы) по пути " + arguments.path() + " не найден(-ы).");
        }

        if (arguments.path().endsWith("/*") || arguments.path().contains("**")) {
            List<File> files = findLogFiles(folder, file);
            filesNames = files.stream().map(File::getName).toList();
            lines = files.stream().flatMap(this::readFile);
        } else {
            filesNames = List.of(folder.getName());
            lines = readFile(folder);
        }

        try (lines) {
            return getStats(lines, arguments).filesNames(filesNames);
        }
    }

    private List<File> findLogFiles(File folder, String fileName) {
        List<File> logFiles = new ArrayList<>();
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    logFiles.addAll(findLogFiles(file, fileName));
                } else if (file.isFile()) {
                    if (fileName.isBlank() || fileName.equals(file.getName())) {
                        logFiles.add(file);
                    }
                }
            }
        }

        return logFiles;
    }

    private Stream<String> readFile(File file) {
        try {
            return Files.lines(file.toPath());
        } catch (IOException e) {
            throw new UncheckedIOException("Ошибка при чтении файла: " + file.getPath(), e);
        }
    }

    public LogStatistics getStats(Stream<String> lines, Arguments arguments) {
        LogStatistics stats = new LogStatistics();
        List<Integer> sizes = new ArrayList<>();
        AtomicLong totalSize = new AtomicLong(0L);
        AtomicInteger recordCount = new AtomicInteger(0);
        List<String> filesNames = new ArrayList<>();

        lines.map(LogParser::parseLine)
            .filter(Objects::nonNull)
            .map(logRecord -> applyFilters(logRecord, arguments))
            .filter(Objects::nonNull)
            .forEach(logRecord -> {
                int size = logRecord.bodyBytesSent();
                sizes.add(size);
                totalSize.addAndGet(size);
                recordCount.incrementAndGet();
                updateStatistics(stats, logRecord);
            });

        if (recordCount.get() == 0) {
            throw new NoFoundDataException("Записи с параметрами: " + '\n' + arguments.toString() + " не найдены.");
        }

        stats.avgResponseSize((double) totalSize.get() / recordCount.get());
        stats.responseSizePercentile95(calculatePercentile95(sizes));
        stats.filesNames(filesNames);
        return stats;
    }

    private double calculatePercentile95(List<Integer> sizes) {
        Collections.sort(sizes);
        int index = (int) Math.ceil(0.95 * sizes.size()) - 1;
        return sizes.get(index);
    }

    private void updateStatistics(LogStatistics stats, LogRecord logRecord) {
        stats.totalRequests(stats.totalRequests() + 1);
        stats.resources().merge(logRecord.request(), 1L, Long::sum);
        stats.statuses().merge(logRecord.status(), 1L, Long::sum);
        stats.ipAddresses().merge(logRecord.remoteAddr(), 1L, Long::sum);
        stats.countRequestsPerDay().merge(logRecord.timestamp().format(Config.DATE_FORMATTER), 1L, Long::sum);
    }

    private LogRecord applyFilters(LogRecord logRecord, Arguments arguments) {
        LogRecord recordResponse = logRecord;
        if (logRecord != null) {
            if (arguments.from() != null || arguments.to() != null) {
                recordResponse = filterByDate(logRecord, arguments.from(), arguments.to());
            }
            if (arguments.filterField() != null && arguments.filterValue() != null) {
                recordResponse = filterByField(logRecord, arguments.filterField(), arguments.filterValue());
            }
        }
        return recordResponse;
    }

    public LogRecord filterByDate(LogRecord logRecord, LocalDateTime from, LocalDateTime to) {
        boolean isAfterFrom = from == null || logRecord.timestamp().isAfter(from);
        boolean isBeforeTo = to == null || logRecord.timestamp().isBefore(to);
        return (isAfterFrom && isBeforeTo) ? logRecord : null;
    }

    public LogRecord filterByField(LogRecord logRecord, String field, String valuePattern) {
        Pattern pattern = Pattern.compile(valuePattern);
        if (logRecord.getFieldValue(field) != null
            && pattern.matcher(Objects.requireNonNull(logRecord.getFieldValue(field))).find()) {
            return logRecord;
        }
        return null;
    }

}
