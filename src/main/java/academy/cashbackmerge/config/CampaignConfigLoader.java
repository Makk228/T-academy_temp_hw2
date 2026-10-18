package academy.cashbackmerge.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.LogicalType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

public final class CampaignConfigLoader {

    private static final JsonMapper MAPPER = JsonMapper.builder()
            .disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT)
            .withCoercionConfig(
                    LogicalType.Textual,
                    config -> config.setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                            .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail))
            .build();

    private CampaignConfigLoader() {}

    public static CampaignConfig load(Path path) {
        try {
            return parse(Files.readString(path));
        } catch (NoSuchFileException e) {
            throw new InvalidConfigException("файл конфига не найден: " + path, e);
        } catch (IOException e) {
            throw new InvalidConfigException("не удалось прочитать файл конфига %s: %s".formatted(path, e), e);
        }
    }

    public static CampaignConfig parse(String json) {
        RawCampaignConfig raw;
        try {
            raw = MAPPER.readValue(json, RawCampaignConfig.class);
        } catch (JsonProcessingException e) {
            throw new InvalidConfigException(JsonErrors.describe(e), e);
        }
        if (raw == null) {
            throw new InvalidConfigException(List.of("конфиг пуст: ожидался JSON-объект"));
        }
        return RawConfigConverter.convert(raw);
    }
}
