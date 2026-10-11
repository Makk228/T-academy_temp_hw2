package academy.cashbackmerge.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

final class JsonErrors {

    private JsonErrors() {}

    static String describe(JsonProcessingException e) {
        if (e instanceof UnrecognizedPropertyException unknown) {
            return "%s: неизвестное поле. Допустимые поля: %s".formatted(path(unknown), unknown.getKnownPropertyIds());
        }
        if (e instanceof MismatchedInputException mismatch) {
            return describeMismatch(mismatch);
        }
        var location = e.getLocation();
        var where = location == null
                ? ""
                : " (строка %d, столбец %d)".formatted(location.getLineNr(), location.getColumnNr());
        return "некорректный JSON%s: %s".formatted(where, e.getOriginalMessage());
    }

    private static String describeMismatch(MismatchedInputException e) {
        if (e.getPath().isEmpty()) {
            return "конфиг пуст или не является JSON-объектом";
        }
        if (e.getTargetType() == String.class) {
            return "%s: значение нужно записать строкой в кавычках, например \"0.85\" — так оно читается без потери точности"
                    .formatted(path(e));
        }
        return "%s: значение неверного типа (%s)".formatted(path(e), firstLine(e.getOriginalMessage()));
    }

    private static String path(JsonMappingException e) {
        var result = new StringBuilder();
        for (var reference : e.getPath()) {
            var field = reference.getFieldName();
            if (field != null) {
                result.append(result.isEmpty() ? "" : ".").append(field);
            } else {
                result.append('[').append(reference.getIndex()).append(']');
            }
        }
        return result.toString();
    }

    private static String firstLine(String message) {
        var end = message.indexOf('\n');
        return end < 0 ? message : message.substring(0, end);
    }
}
