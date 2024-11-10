import backend.academy.LogAnalyzer;
import backend.academy.dto.Arguments;
import backend.academy.dto.LogRecord;
import backend.academy.dto.LogStatistics;
import backend.academy.exceptions.NoFoundDataException;
import backend.academy.utils.LogParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LogAnalyzerTest {

    private LogAnalyzer analyzer;

    @BeforeEach
    public void setUp() {
        analyzer = new LogAnalyzer();
    }

    @Test
    public void testLoadLogsFromFile_LogsDoubleStar() {
        Arguments arguments = new Arguments();
        arguments.path("logs/test/**/test.log");
        LogStatistics stats = analyzer.loadLogsFromFile(arguments);

        List<String> filesNames = stats.filesNames();

        assertEquals(2, filesNames.size());

        assertEquals(21L, stats.totalRequests());

    }

    @Test
    public void testLoadLogsFromFile_EndsStar() {
        Arguments arguments = new Arguments();
        arguments.path("logs/test/2023/*");

        LogStatistics stats = analyzer.loadLogsFromFile(arguments);

        List<String> filesNames = stats.filesNames();
        assertEquals(2, filesNames.size());
        assertEquals("test_2.log", filesNames.get(0));
        assertEquals("test.log", filesNames.get(1));
        assertEquals(23L, stats.totalRequests());
    }

    @Test
    public void testLoadLogsFromFile_FullPath() {
        Arguments arguments = new Arguments();
        arguments.path("logs/test/2023/test.log");

        LogStatistics stats = analyzer.loadLogsFromFile(arguments);

        List<String> filesNames = stats.filesNames();
        assertEquals(1, filesNames.size());
        assertEquals("test.log", filesNames.get(0));

        assertEquals(12L, stats.totalRequests());
    }

    @Test
    public void testLoadLogsFromURL() {
        Arguments arguments = new Arguments();
        arguments.path(
            "https://raw.githubusercontent.com/elastic/examples/master/Common%20Data%20Formats/nginx_logs/nginx_logs");

        LogStatistics stats = analyzer.loadLogsFromUrl(arguments);

        List<String> filesNames = stats.filesNames();
        assertEquals(1, filesNames.size());
        assertEquals(51449, stats.totalRequests());
    }

    @Test
    public void testLoadLogsFromURL_TwoUrls() {
        Arguments arguments = new Arguments();
        arguments.path(
            "https://raw.githubusercontent.com/elastic/examples/master/Common%20Data%20Formats/nginx_logs/nginx_logs" +
                "|" +
                "https://raw.githubusercontent.com/elastic/examples/master/Common%20Data%20Formats/nginx_logs/nginx_logs");

        LogStatistics stats = analyzer.loadLogsFromUrl(arguments);

        List<String> filesNames = stats.filesNames();
        assertEquals(2, filesNames.size());
        assertEquals(51449 * 2, stats.totalRequests());
    }

    @Test
    public void testFilterByDate_From() {
        String logLineValid =
            "93.180.71.3 - - [17/May/2015:08:05:11 +0000] \"GET /downloads/product_1 HTTP/1.1\" 404 340 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"";
        String logLineNotValid =
            "93.180.71.3 - - [16/Feb/2015:08:05:11 +0000] \"GET /downloads/product_1 HTTP/1.1\" 404 340 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"";

        Arguments arguments =
            Arguments.parse(new String[] {"--from", "2015-05-17T00:00:00+00:00"});

        LogRecord recordValid = LogParser.parseLine(logLineValid);

        recordValid = analyzer.filterByDate(recordValid, arguments.from(), arguments.to());

        assertNotNull(recordValid);

        LogRecord recordNotValid = LogParser.parseLine(logLineNotValid);
        recordNotValid = analyzer.filterByDate(recordNotValid, arguments.from(), arguments.to());

        assertNull(recordNotValid);

    }

    @Test
    public void testFilterByDate_To() {
        String logLineValid =
            "93.180.71.3 - - [17/May/2015:08:05:11 +0000] \"GET /downloads/product_1 HTTP/1.1\" 404 340 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"";
        String logLineNotValid =
            "93.180.71.3 - - [20/May/2015:08:05:11 +0000] \"GET /downloads/product_1 HTTP/1.1\" 404 340 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"";

        Arguments arguments =
            Arguments.parse(new String[] {"--to", "2015-05-17T23:59:59+00:00"});

        LogRecord recordValid = LogParser.parseLine(logLineValid);

        recordValid = analyzer.filterByDate(recordValid, arguments.from(), arguments.to());

        assertNotNull(recordValid);

        LogRecord recordNotValid = LogParser.parseLine(logLineNotValid);
        recordNotValid = analyzer.filterByDate(recordNotValid, arguments.from(), arguments.to());

        assertNull(recordNotValid);
    }

    @Test
    public void testFilterByDate_FromAndTo() {
        String logLineValid =
            "93.180.71.3 - - [17/May/2015:08:05:11 +0000] \"GET /downloads/product_1 HTTP/1.1\" 404 340 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"";
        String logLineNotValidTo =
            "93.180.71.3 - - [20/May/2015:08:05:11 +0000] \"GET /downloads/product_1 HTTP/1.1\" 404 340 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"";
        String logLineNotValidFrom =
            "93.180.71.3 - - [11/May/2015:08:05:11 +0000] \"GET /downloads/product_1 HTTP/1.1\" 404 340 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"";

        Arguments arguments =
            Arguments.parse(new String[] {"--from", "2015-05-17T00:00:00+00:00", "--to", "2015-05-17T23:59:59+00:00"});

        LogRecord recordValid = LogParser.parseLine(logLineValid);

        recordValid = analyzer.filterByDate(recordValid, arguments.from(), arguments.to());

        assertNotNull(recordValid);

        LogRecord recordNotValidFrom = LogParser.parseLine(logLineNotValidFrom);
        recordNotValidFrom = analyzer.filterByDate(recordNotValidFrom, arguments.from(), arguments.to());

        assertNull(recordNotValidFrom);

        LogRecord recordNotValidTo = LogParser.parseLine(logLineNotValidTo);
        recordNotValidTo = analyzer.filterByDate(recordNotValidTo, arguments.from(), arguments.to());

        assertNull(recordNotValidTo);
    }

    @Test
    public void testGetStats_CorrectCalculations() {
        List<String> logLines = Arrays.asList(
            "188.138.60.101 - - [16/May/2015:08:05:25 +0000] \"GET /downloads/product_2 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.9.7.9)\"\n",
            "93.180.71.3 - - [17/May/2015:08:05:11 +0000] \"GET /downloads/product_1 HTTP/1.1\" 404 340 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"\n",
            "46.4.66.76 - - [17/May/2015:08:05:02 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 100 \"-\" \"Debian APT-HTTP/1.3 (1.0.1ubuntu2)\"\n",
            "62.75.198.179 - - [17/May/2015:08:05:06 +0000] \"GET /downloads/product_2 HTTP/1.1\" 200 490 \"-\" \"Debian APT-HTTP/1.3 (0.9.7.9)\"\n",
            "62.75.198.179 - - [17/May/2015:08:05:55 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.16)\"\n"
        );

        Arguments arguments = new Arguments();

        LogStatistics stats = analyzer.getStats(logLines.stream(), arguments);

        assertEquals(5, stats.totalRequests());

        double expectedAvgSize = (340 + 490 + 100) / 5.0;
        assertEquals(expectedAvgSize, stats.avgResponseSize(), 0.001);

        List<Integer> sizes = Arrays.asList(340, 490, 100);
        Collections.sort(sizes);
        int index = (int) Math.ceil(0.95 * sizes.size()) - 1;
        double expectedPercentile95 = sizes.get(index);
        assertEquals(expectedPercentile95, stats.responseSizePercentile95(), 0.001);

        Map<String, Long> expectedResources = new HashMap<>();
        expectedResources.put("GET /downloads/product_1 HTTP/1.1", 3L);
        expectedResources.put("GET /downloads/product_2 HTTP/1.1", 2L);
        assertEquals(expectedResources, stats.resources());

        Map<Integer, Long> expectedStatuses = new HashMap<>();
        expectedStatuses.put(304, 3L);
        expectedStatuses.put(404, 1L);
        expectedStatuses.put(200, 1L);
        assertEquals(expectedStatuses, stats.statuses());

        Map<String, Long> expectedIPs = new HashMap<>();
        expectedIPs.put("188.138.60.101", 1L);
        expectedIPs.put("93.180.71.3", 1L);
        expectedIPs.put("46.4.66.76", 1L);
        expectedIPs.put("62.75.198.179", 2L);
        assertEquals(expectedIPs, stats.ipAddresses());

        Map<String, Long> expectedRequestsPerDay = new HashMap<>();
        expectedRequestsPerDay.put("17/May/2015", 4L);
        expectedRequestsPerDay.put("16/May/2015", 1L);
        assertEquals(expectedRequestsPerDay, stats.countRequestsPerDay());
    }

    @Test
    public void testGetStats_NoDataFound() {
        List<String> logLines = new ArrayList<>();
        Arguments arguments = new Arguments();
        assertThrows(NoFoundDataException.class, () -> {
            analyzer.getStats(logLines.stream(), arguments);
        });
    }

}
