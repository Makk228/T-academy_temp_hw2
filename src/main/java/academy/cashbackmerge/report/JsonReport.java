package academy.cashbackmerge.report;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.simulation.SimulationReport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

public final class JsonReport {

    private static final ObjectWriter WRITER = JsonMapper.builder().build().writer(prettyPrinter());

    private JsonReport() {}

    public static String render(SimulationReport report) {
        var json = object(
                "status", report.status().name(),
                "config", config(report),
                "simulation", object("players", report.players(), "strategy", report.strategy(), "seed", report.seed()),
                "maxTileDistribution", maxTiles(report),
                "rewardDistribution", rewards(report),
                "score", object("average", report.averageScore().toPlainString(), "max", report.maxScore()),
                "maxReward", maxReward(report));
        try {
            return WRITER.writeValueAsString(json) + "\n";
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException("не удалось построить JSON-отчёт", e);
        }
    }

    private static Map<String, @Nullable Object> config(SimulationReport report) {
        CampaignConfig config = report.config();
        var spawnRules = config.spawnRules().stream()
                .map(rule -> object(
                        "cashback", rule.cashback().toPlainString(),
                        "probability", rule.probability().toString()))
                .toList();
        var rewardTable = config.rewardTable().stream()
                .map(tier -> object(
                        "cashback", tier.cashback().toPlainString(),
                        "reward", tier.reward().toPlainString()))
                .toList();
        return object(
                "source", report.configSource(),
                "boardSize", config.boardSize(),
                "movesLimit", config.movesLimit(),
                "spawnRules", spawnRules,
                "rewardTable", rewardTable,
                "maxRewardCashback", config.maxRewardCashback().toPlainString(),
                "maxRewardAllowedRate", config.maxRewardAllowedRate().toString());
    }

    private static List<Map<String, @Nullable Object>> maxTiles(SimulationReport report) {
        return report.maxTiles().entrySet().stream()
                .map(entry -> object(
                        "cashback", entry.getKey().toPlainString(),
                        "players", entry.getValue(),
                        "rate", report.rate(entry.getValue()).toPlainString()))
                .toList();
    }

    private static List<Map<String, @Nullable Object>> rewards(SimulationReport report) {
        return report.rewards().stream()
                .map(reward -> {
                    var tier = reward.tier();
                    return object(
                            "tier", tier == null ? null : tier.cashback().toPlainString(),
                            "reward", reward.reward().toPlainString(),
                            "players", reward.players(),
                            "rate", report.rate(reward.players()).toPlainString());
                })
                .toList();
    }

    private static Map<String, @Nullable Object> maxReward(SimulationReport report) {
        return object(
                "cashback", report.config().maxRewardCashback().toPlainString(),
                "players", report.maxRewardPlayers(),
                "actualRate", report.maxRewardRate().toPlainString(),
                "margin95", report.maxRewardRateMargin95().toPlainString(),
                "allowedRate", report.config().maxRewardAllowedRate().toString());
    }

    private static Map<String, @Nullable Object> object(@Nullable Object... keysAndValues) {
        var result = new LinkedHashMap<String, @Nullable Object>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            result.put(String.valueOf(keysAndValues[i]), keysAndValues[i + 1]);
        }
        return result;
    }

    private static DefaultPrettyPrinter prettyPrinter() {
        var indenter = new DefaultIndenter("  ", "\n");
        var printer = new DefaultPrettyPrinter().withObjectIndenter(indenter);
        printer.indentArraysWith(indenter);
        return printer;
    }
}
