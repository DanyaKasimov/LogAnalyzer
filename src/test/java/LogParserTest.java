import backend.academy.config.Config;
import backend.academy.dto.LogRecord;
import backend.academy.utils.LogParser;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class LogParserTest {

    @Test
    public void testParseLine() {
        String logLine =
            "93.180.71.3 - - [17/May/2015:08:05:11 +0000] \"GET /downloads/product_1 HTTP/1.1\" 404 340 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"";

        LogRecord record = LogParser.parseLine(logLine);
        assertNotNull(record);

        assertEquals("93.180.71.3", record.remoteAddr());

        assertEquals("-", record.remoteUser());

        LocalDateTime expectedTimestamp = LocalDateTime.parse("17/May/2015:08:05:11 +0000", Config.LOG_FORMATTER);
        assertEquals(expectedTimestamp, record.timestamp());

        assertEquals("GET /downloads/product_1 HTTP/1.1", record.request());
        assertEquals(404, record.status());
        assertEquals(340, record.bodyBytesSent());
        assertEquals("Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)", record.agent());
    }
}

