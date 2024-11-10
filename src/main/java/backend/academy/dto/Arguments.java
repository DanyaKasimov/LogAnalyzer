package backend.academy.dto;

import backend.academy.config.Config;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@SuppressWarnings("ModifiedControlVariable")
public class Arguments {
    private String path;

    private LocalDateTime from;

    private LocalDateTime to;

    private String format;

    private String filterField;

    private String filterValue;

    private String order;

    public static Arguments parse(String[] args) {
        Arguments arguments = new Arguments();
        for (int i = 0; i < args.length; i++) {
            switch (args[i].toLowerCase()) {
                case "--path":
                    arguments.path = args[++i];
                    break;
                case "--from":
                    arguments.from = LocalDateTime.parse(parseISO(args[++i]), Config.LOG_FORMATTER);
                    break;
                case "--to":
                    arguments.to = LocalDateTime.parse(parseISO(args[++i]), Config.LOG_FORMATTER);
                    break;
                case "--format":
                    arguments.format = args[++i];
                    break;
                case "--filter-field":
                    arguments.filterField = args[++i];
                    break;
                case "--filter-value":
                    arguments.filterValue = args[++i];
                    break;
                case "--order":
                    arguments.order = args[++i];
                    break;
                default:
                    break;
            }
        }
        return arguments;
    }

    private static String parseISO(String isoDateTime) {
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(isoDateTime, Config.ISO_FORMATTER);
        return offsetDateTime.format(Config.LOG_FORMATTER);
    }

    @Override
    public String toString() {
        return "path = " + path + '\n'
            + "from = " + from + '\n'
            + "to = " + to + '\n'
            + "format = " + format + '\n'
            + "filterField = " + filterField + '\n'
            + "filterValue = " + filterValue + '\n'
            + "order = " + order + '\n';
    }
}
