package academy.cashbackmerge.report;

import static academy.cashbackmerge.report.Formats.percent;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.simulation.ConstraintStatus;
import academy.cashbackmerge.simulation.SimulationReport;
import academy.cashbackmerge.simulation.SimulationReport.RewardCount;
import java.util.stream.Collectors;

public final class MarkdownReport {

    private MarkdownReport() {}

    public static String render(SimulationReport report) {
        var out = new StringBuilder();
        out.append("# Отчёт симуляции Cashback Merge\n\n");
        out.append(statusLine(report)).append("\n\n");
        appendParameters(out, report);
        appendMaxTiles(out, report);
        appendRewards(out, report);
        appendScore(out, report);
        appendMaxReward(out, report);
        out.append("""
                > Это эмпирический результат для конкретного конфига, seed, числа игроков и стратегии, а не \
                математическая гарантия. Доля максимальной награды — оценка методом Монте-Карло: по закону больших \
                чисел она приближается к настоящей вероятности с ростом числа игроков, а погрешность убывает как \
                `1/√n`.
                """);
        return out.toString();
    }

    private static String statusLine(SimulationReport report) {
        var comparison = report.status() == ConstraintStatus.OK ? "не больше" : "больше";
        return "**Статус: %s.** Максимальную награду получили %s игроков (%d из %d) — %s допустимых %s."
                .formatted(
                        report.status(),
                        percent(report.maxRewardRate()),
                        report.maxRewardPlayers(),
                        report.players(),
                        comparison,
                        percent(report.config().maxRewardAllowedRate().value()));
    }

    private static void appendParameters(StringBuilder out, SimulationReport report) {
        CampaignConfig config = report.config();
        var spawnRules = config.spawnRules().stream()
                .map(rule -> rule.cashback() + " — " + rule.probability())
                .collect(Collectors.joining("; "));
        var rewardTable = config.rewardTable().stream()
                .map(tier -> tier.cashback() + " — " + tier.reward().toPlainString())
                .collect(Collectors.joining("; "));
        out.append("## Параметры\n\n");
        out.append("| Параметр | Значение |\n|---|---|\n");
        row(out, "Конфиг", "`" + report.configSource() + "`");
        row(out, "Игроков", String.valueOf(report.players()));
        row(out, "Стратегия", "`" + report.strategy() + "`");
        row(out, "Seed", String.valueOf(report.seed()));
        row(out, "Поле", "%dx%d".formatted(config.boardSize(), config.boardSize()));
        row(out, "Лимит ходов", String.valueOf(config.movesLimit()));
        row(out, "Появление плиток", spawnRules);
        row(out, "Таблица наград", rewardTable);
        row(out, "Максимальная награда", "от плитки " + config.maxRewardCashback());
        row(
                out,
                "Допустимая доля максимальной награды",
                percent(config.maxRewardAllowedRate().value()));
        out.append('\n');
    }

    private static void appendMaxTiles(StringBuilder out, SimulationReport report) {
        out.append("## Распределение максимальных плиток\n\n");
        out.append("| Максимальная плитка | Игроков | Доля |\n|---:|---:|---:|\n");
        report.maxTiles().forEach((tile, count) -> row(out, tile.toString(), count, percent(report.rate(count))));
        out.append('\n');
    }

    private static void appendRewards(StringBuilder out, SimulationReport report) {
        out.append("## Распределение наград\n\n");
        out.append("| Tier | Награда | Игроков | Доля |\n|---:|---:|---:|---:|\n");
        for (RewardCount reward : report.rewards()) {
            var rewardTier = reward.tier();
            var tier =
                    rewardTier == null ? "без награды" : rewardTier.cashback().toString();
            row(out, tier, reward.reward().toPlainString(), reward.players(), percent(report.rate(reward.players())));
        }
        out.append('\n');
    }

    private static void appendScore(StringBuilder out, SimulationReport report) {
        out.append("## Score\n\n");
        out.append("| Средний | Максимальный |\n|---:|---:|\n");
        row(out, report.averageScore().toPlainString(), String.valueOf(report.maxScore()));
        out.append('\n');
    }

    private static void appendMaxReward(StringBuilder out, SimulationReport report) {
        out.append("## Доля игроков с максимальной наградой\n\n");
        out.append("| Показатель | Значение |\n|---|---:|\n");
        row(out, "Игроков с максимальной наградой", "%d из %d".formatted(report.maxRewardPlayers(), report.players()));
        row(out, "actualMaxRewardRate", percent(report.maxRewardRate()));
        row(out, "Погрешность оценки, 95%", "±" + percent(report.maxRewardRateMargin95()));
        row(
                out,
                "maxRewardAllowedRate",
                percent(report.config().maxRewardAllowedRate().value()));
        row(out, "Статус", "**" + report.status() + "**");
        out.append('\n');
    }

    private static void row(StringBuilder out, Object... cells) {
        out.append('|');
        for (var cell : cells) {
            out.append(' ').append(cell).append(" |");
        }
        out.append('\n');
    }
}
