package academy.cashbackmerge.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import academy.cashbackmerge.model.Cashback;
import academy.cashbackmerge.support.CampaignJson;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("Загрузка конфига кампании")
class CampaignConfigLoaderTest {

    @Test
    @DisplayName("Пример из config/ загружается целиком, поле _note пропускается")
    void exampleConfigIsLoaded() {
        var config = CampaignConfigLoader.load(Path.of("config", "campaign.example.json"));

        assertThat(config.movesLimit()).isEqualTo(100);
        assertThat(config.spawnRules()).extracting(SpawnRule::cashback).containsExactly(Cashback.of(1), Cashback.of(2));
        assertThat(config.rewardTable()).hasSize(5);
        assertThat(config.rewardTable().getLast()).isEqualTo(new RewardTier(Cashback.of(32), new BigDecimal("2000")));
    }

    @Test
    @DisplayName("0.7 + 0.2 + 0.1 — ровно 1, хотя в double это не так")
    void probabilitiesAreSummedExactly() {
        assertThat(0.7 + 0.2 + 0.1).as("демонстрация: double теряет точность").isNotEqualTo(1.0);

        var config = CampaignConfigLoader.parse(CampaignJson.with("10", """
                [
                  { "cashback": "1", "probability": "0.7" },
                  { "cashback": "2", "probability": "0.2" },
                  { "cashback": "4", "probability": "0.1" }
                ]"""));

        assertThat(config.spawnRules()).hasSize(3);
    }

    @Test
    @DisplayName("Вероятность числом без кавычек отклоняется — она бы прочиталась как double")
    void probabilityAsJsonNumberIsRejected() {
        var json = CampaignJson.with("100", """
                [ { "cashback": "1", "probability": 1.0 } ]""");

        assertThat(errorsOf(json))
                .singleElement()
                .asString()
                .startsWith("spawnRules[0].probability: значение нужно записать строкой");
    }

    @Test
    @DisplayName("Дробный movesLimit не обрезается до целого молча")
    void fractionalMovesLimitIsRejected() {
        var json = CampaignJson.with("1.9", CampaignJson.DEFAULT_SPAWN_RULES);

        assertThat(errorsOf(json)).singleElement().asString().startsWith("movesLimit: значение неверного типа");
    }

    @Test
    @DisplayName("Нулевая вероятность отклоняется, даже если сумма равна 1")
    void zeroProbabilityIsRejected() {
        var json = CampaignJson.with("100", """
                [
                  { "cashback": "1", "probability": "1" },
                  { "cashback": "2", "probability": "0" }
                ]""");

        assertThat(errorsOf(json)).containsExactly("spawnRules[1].probability: должна быть больше 0");
    }

    @Test
    @DisplayName("Вероятность больше 1 и нечисловой кэшбэк — ошибки разбора с путём к полю")
    void malformedValuesAreReportedWithPath() {
        var json = CampaignJson.with("100", """
                [
                  { "cashback": "abc", "probability": "0.5" },
                  { "cashback": "-2",  "probability": "1.5" }
                ]""");

        assertThat(errorsOf(json))
                .containsExactly(
                        "spawnRules[0].cashback: ожидалось число, получено 'abc'",
                        "spawnRules[1].cashback: кэшбэк должен быть больше 0, получено -2",
                        "spawnRules[1].probability: ожидалось число от 0 до 1, получено 1.5");
    }

    @Test
    @DisplayName("Повторяющийся кэшбэк в правилах появления отклоняется")
    void duplicateSpawnCashbackIsRejected() {
        var json = CampaignJson.with("100", """
                [
                  { "cashback": "1",   "probability": "0.5" },
                  { "cashback": "1.0", "probability": "0.5" }
                ]""");

        assertThat(errorsOf(json)).containsExactly("spawnRules[1].cashback: правило для 1% уже задано");
    }

    @Test
    @DisplayName("Все отсутствующие обязательные поля перечисляются разом")
    void allMissingFieldsAreReported() {
        assertThat(errorsOf("{}"))
                .containsExactly(
                        "movesLimit: обязательное поле не задано",
                        "spawnRules: обязательное поле не задано",
                        "rewardTable: обязательное поле не задано",
                        "maxRewardCashback: обязательное поле не задано",
                        "maxRewardAllowedRate: обязательное поле не задано");
    }

    @Test
    @DisplayName("Опечатка в имени поля — ошибка, а не тихо пропущенное значение")
    void unknownFieldIsRejected() {
        var json = CampaignJson.valid().replace("\"movesLimit\"", "\"moveLimit\"");

        assertThat(errorsOf(json)).singleElement().asString().startsWith("moveLimit: неизвестное поле");
    }

    @Test
    @DisplayName("maxRewardCashback должен быть в таблице наград, награды не отрицательны")
    void rewardTableIsValidated() {
        var json = CampaignJson.valid()
                .replace("\"maxRewardCashback\": \"32\"", "\"maxRewardCashback\": \"64\"")
                .replace("\"reward\": \"50\"", "\"reward\": \"-50\"");

        assertThat(errorsOf(json))
                .containsExactly(
                        "rewardTable[1].reward: не может быть отрицательной, получено -50",
                        "maxRewardCashback: плитки 64% нет в rewardTable");
    }

    @Test
    @DisplayName("Поддерживается только поле 4x4")
    void onlyStandardBoardSizeIsSupported() {
        var json = CampaignJson.valid().replaceFirst("\\{", "{ \"boardSize\": 5,");

        assertThat(errorsOf(json)).containsExactly("boardSize: поддерживается только поле 4x4, получено 5");
    }

    @Test
    @DisplayName("Сломанный JSON — сообщение с номером строки")
    void malformedJsonIsReported() {
        assertThat(errorsOf("{ \"movesLimit\": 100,, }"))
                .singleElement()
                .asString()
                .startsWith("некорректный JSON (строка 1, столбец");
    }

    @Test
    @DisplayName("Несуществующий файл — понятная ошибка")
    void missingFileIsReported(@TempDir Path directory) {
        var path = directory.resolve("nope.json");

        var error = catchThrowableOfType(InvalidConfigException.class, () -> CampaignConfigLoader.load(path));

        assertThat(error.errors()).containsExactly("файл конфига не найден: " + path);
    }

    private static List<String> errorsOf(String json) {
        var error = catchThrowableOfType(InvalidConfigException.class, () -> CampaignConfigLoader.parse(json));
        assertThat(error).as("конфиг должен быть отклонён:%n%s", json).isNotNull();
        return error.errors();
    }
}
