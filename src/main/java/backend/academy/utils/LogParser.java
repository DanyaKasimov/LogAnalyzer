package backend.academy.utils;

import backend.academy.config.Config;
import backend.academy.dto.LogRecord;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LogParser {

    private static final Integer REMOTE_ADDRESS = 1;

    private static final Integer REMOTE_USER = 2;

    private static final Integer TIMESTAMP = 3;

    private static final Integer REQUEST = 4;

    private static final Integer STATUS = 5;

    private static final Integer BODY_BITES = 6;

    private static final Integer AGENT = 7;

    public static LogRecord parseLine(String line) {
        Matcher matcher = Config.LOG_PATTERN.matcher(line);
        if (matcher.find()) {
            String remoteAddr = matcher.group(REMOTE_ADDRESS);
            String remoteUser = matcher.group(REMOTE_USER);
            LocalDateTime timestamp = LocalDateTime.parse(matcher.group(TIMESTAMP), Config.LOG_FORMATTER);
            String request = matcher.group(REQUEST);
            int status = Integer.parseInt(matcher.group(STATUS));
            int bodyBytesSent = Integer.parseInt(matcher.group(BODY_BITES));
            String agent = matcher.group(AGENT);
            return new LogRecord(remoteAddr, remoteUser, timestamp, request, status, bodyBytesSent, agent);
        }
        return null;
    }
}
