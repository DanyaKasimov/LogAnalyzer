package backend.academy.dto;

import java.time.LocalDateTime;

public record LogRecord(String remoteAddr,
                        String remoteUser,
                        LocalDateTime timestamp,
                        String request,
                        int status,
                        int bodyBytesSent,
                        String agent) {

    public String getFieldValue(String field) {
        return switch (field.toLowerCase()) {
            case "agent" -> agent;
            case "method" -> request.split(" ")[0];
            case "status" -> String.valueOf(status);
            default -> null;
        };
    }
}
