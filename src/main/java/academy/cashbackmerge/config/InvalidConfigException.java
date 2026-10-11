package academy.cashbackmerge.config;

import java.util.List;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;

public final class InvalidConfigException extends RuntimeException {

    private final List<String> errors;

    public InvalidConfigException(List<String> errors) {
        this(errors, null);
    }

    public InvalidConfigException(String error, Throwable cause) {
        this(List.of(error), cause);
    }

    private InvalidConfigException(List<String> errors, @Nullable Throwable cause) {
        super(format(errors), cause);
        this.errors = List.copyOf(errors);
    }

    public List<String> errors() {
        return errors;
    }

    private static String format(List<String> errors) {
        return errors.stream()
                .map(error -> "  - " + error)
                .collect(Collectors.joining("\n", "Конфиг кампании некорректен:\n", ""));
    }
}
