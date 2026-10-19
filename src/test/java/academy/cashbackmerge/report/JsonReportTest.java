package academy.cashbackmerge.report;

import static org.assertj.core.api.Assertions.assertThat;

import academy.cashbackmerge.config.CampaignConfigLoader;
import academy.cashbackmerge.simulation.RandomStrategy;
import academy.cashbackmerge.simulation.Simulator;
import academy.cashbackmerge.support.CampaignJson;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("JSON-отчёт и формат по расширению")
class JsonReportTest {

    @Test
    @DisplayName("JSON-отчёт читается обратно, доли — строками без потери точности")
    void jsonReportIsReadable() throws Exception {
        var report = Simulator.run(
                CampaignConfigLoader.parse(CampaignJson.valid()), "test.json", new RandomStrategy(), 120, 5);

        var text = JsonReport.render(report);
        var json = new ObjectMapper().readTree(text);

        assertThat(text).as("переносы строк одинаковые на любой ОС").doesNotContain("\r");
        assertThat(text.indexOf("\"cashback\" : \"1\""))
                .as("порядок полей фиксирован")
                .isLessThan(text.indexOf("\"probability\""));

        assertThat(json.get("status").asText()).isEqualTo(report.status().name());
        assertThat(json.at("/simulation/players").asInt()).isEqualTo(120);
        assertThat(json.at("/simulation/seed").asLong()).isEqualTo(5);
        assertThat(json.at("/config/maxRewardAllowedRate").asText()).isEqualTo("0.2");
        assertThat(json.at("/maxReward/actualRate").isTextual()).isTrue();
        assertThat(json.at("/maxReward/actualRate").asText())
                .isEqualTo(report.maxRewardRate().toPlainString());
        assertThat(json.get("maxTileDistribution")).hasSize(report.maxTiles().size());
        assertThat(json.get("rewardDistribution")).hasSize(report.rewards().size());
    }

    @Test
    @DisplayName("Формат выбирается по расширению файла")
    void formatIsChosenByExtension() {
        assertThat(ReportFormat.forFile(Path.of("out", "report.JSON"))).isEqualTo(ReportFormat.JSON);
        assertThat(ReportFormat.forFile(Path.of("report.md"))).isEqualTo(ReportFormat.MARKDOWN);
        assertThat(ReportFormat.forFile(Path.of("report"))).isEqualTo(ReportFormat.MARKDOWN);
    }
}
