package academy.cashbackmerge.config;

import academy.cashbackmerge.model.Board;
import academy.cashbackmerge.model.Cashback;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

final class CampaignConfigValidator {

    static final int MAX_PROBABILITY_SCALE = 18;

    private CampaignConfigValidator() {}

    static List<String> validate(
            int boardSize,
            int movesLimit,
            List<SpawnRule> spawnRules,
            List<RewardTier> rewardTable,
            Cashback maxRewardCashback) {
        var errors = new ArrayList<String>();
        if (boardSize != Board.STANDARD_SIZE) {
            errors.add("boardSize: поддерживается только поле %dx%d, получено %d"
                    .formatted(Board.STANDARD_SIZE, Board.STANDARD_SIZE, boardSize));
        }
        if (movesLimit <= 0) {
            errors.add("movesLimit: должен быть больше 0, получено " + movesLimit);
        }
        validateSpawnRules(spawnRules, errors);
        validateRewardTable(rewardTable, maxRewardCashback, errors);
        return errors;
    }

    private static void validateSpawnRules(List<SpawnRule> rules, List<String> errors) {
        if (rules.isEmpty()) {
            errors.add("spawnRules: нужно хотя бы одно правило появления плитки");
            return;
        }
        var seen = new HashSet<Cashback>();
        var sum = BigDecimal.ZERO;
        for (int i = 0; i < rules.size(); i++) {
            var rule = rules.get(i);
            var probability = rule.probability().value();
            if (rule.probability().isZero()) {
                errors.add("spawnRules[%d].probability: должна быть больше 0".formatted(i));
            }
            if (probability.scale() > MAX_PROBABILITY_SCALE) {
                errors.add("spawnRules[%d].probability: не больше %d знаков после запятой, получено %s"
                        .formatted(i, MAX_PROBABILITY_SCALE, probability.toPlainString()));
            }
            if (!seen.add(rule.cashback())) {
                errors.add("spawnRules[%d].cashback: правило для %s уже задано".formatted(i, rule.cashback()));
            }
            sum = sum.add(probability);
        }
        if (sum.compareTo(BigDecimal.ONE) != 0) {
            errors.add("spawnRules: сумма вероятностей должна быть ровно 1, получено " + sum.toPlainString());
        }
    }

    private static void validateRewardTable(List<RewardTier> table, Cashback maxRewardCashback, List<String> errors) {
        if (table.isEmpty()) {
            errors.add("rewardTable: нужна хотя бы одна строка таблицы наград");
            return;
        }
        var seen = new HashSet<Cashback>();
        for (int i = 0; i < table.size(); i++) {
            var tier = table.get(i);
            if (tier.reward().signum() < 0) {
                errors.add("rewardTable[%d].reward: не может быть отрицательной, получено %s"
                        .formatted(i, tier.reward().toPlainString()));
            }
            if (!seen.add(tier.cashback())) {
                errors.add("rewardTable[%d].cashback: награда для %s уже задана".formatted(i, tier.cashback()));
            }
        }
        if (!seen.contains(maxRewardCashback)) {
            errors.add("maxRewardCashback: плитки %s нет в rewardTable".formatted(maxRewardCashback));
        }
    }
}
