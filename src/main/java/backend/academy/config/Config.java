package backend.academy.config;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Config {
    public static final String MARKDOWN = "MARKDOWN";

    public static final String ADOC = "ADOC";

    public static final String ASC = "asc";

    public static final String DESC = "desc";

    public static final Long LIMIT_DEFAULT = 15L;

    public static final Pattern LOG_PATTERN = Pattern.compile(
        "^(\\S+) - (\\S+) \\[(.*?)] \"(.*?)\" (\\d{3}) (\\d+) \"-\" \"(.*?)\""
    );

    public static final String DIR_PATH = "src/main/java/backend/academy/";

    public static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    public static final DateTimeFormatter LOG_FORMATTER =
        DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);

    public static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("dd/MMM/yyyy", Locale.ENGLISH);
}
