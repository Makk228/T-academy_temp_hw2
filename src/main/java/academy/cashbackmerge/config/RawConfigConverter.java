package academy.cashbackmerge.config;

import static java.util.Objects.requireNonNull;

import academy.cashbackmerge.config.RawCampaignConfig.RawRewardTier;
import academy.cashbackmerge.config.RawCampaignConfig.RawSpawnRule;
import academy.cashbackmerge.model.Board;
import academy.cashbackmerge.model.Cashback;
import academy.cashbackmerge.util.Decimals;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

final class RawConfigConverter {

    private final List<String> errors = new ArrayList<>();

    private RawConfigConverter() {}

    static CampaignConfig convert(RawCampaignConfig raw) {
        return new RawConfigConverter().toConfig(raw);
    }

    private CampaignConfig toConfig(RawCampaignConfig raw) {
        var rawBoardSize = raw.boardSize();
        int boardSize = rawBoardSize == null ? Board.STANDARD_SIZE : rawBoardSize;
        var movesLimit = required("movesLimit", raw.movesLimit());
        var spawnRules = spawnRules(raw.spawnRules());
        var rewardTable = rewardTable(raw.rewardTable());
        var maxRewardCashback = parse("maxRewardCashback", raw.maxRewardCashback(), Cashback::parse);
        var maxRewardAllowedRate = parse("maxRewardAllowedRate", raw.maxRewardAllowedRate(), Probability::parse);

        if (!errors.isEmpty()) {
            throw new InvalidConfigException(errors);
        }
        return new CampaignConfig(
                boardSize,
                requireNonNull(movesLimit),
                requireNonNull(spawnRules),
                requireNonNull(rewardTable),
                requireNonNull(maxRewardCashback),
                requireNonNull(maxRewardAllowedRate));
    }

    private @Nullable List<SpawnRule> spawnRules(@Nullable List<@Nullable RawSpawnRule> raw) {
        var rows = required("spawnRules", raw);
        if (rows == null) {
            return null;
        }
        var result = new ArrayList<SpawnRule>();
        for (int i = 0; i < rows.size(); i++) {
            var path = "spawnRules[%d]".formatted(i);
            var row = required(path, rows.get(i));
            if (row == null) {
                continue;
            }
            var cashback = parse(path + ".cashback", row.cashback(), Cashback::parse);
            var probability = parse(path + ".probability", row.probability(), Probability::parse);
            if (cashback != null && probability != null) {
                result.add(new SpawnRule(cashback, probability));
            }
        }
        return result;
    }

    private @Nullable List<RewardTier> rewardTable(@Nullable List<@Nullable RawRewardTier> raw) {
        var rows = required("rewardTable", raw);
        if (rows == null) {
            return null;
        }
        var result = new ArrayList<RewardTier>();
        for (int i = 0; i < rows.size(); i++) {
            var path = "rewardTable[%d]".formatted(i);
            var row = required(path, rows.get(i));
            if (row == null) {
                continue;
            }
            var cashback = parse(path + ".cashback", row.cashback(), Cashback::parse);
            BigDecimal reward = parse(path + ".reward", row.reward(), Decimals::parse);
            if (cashback != null && reward != null) {
                result.add(new RewardTier(cashback, reward));
            }
        }
        return result;
    }

    private <T> @Nullable T parse(String path, @Nullable String text, Function<String, T> parser) {
        var value = required(path, text);
        if (value == null) {
            return null;
        }
        try {
            return parser.apply(value);
        } catch (IllegalArgumentException e) {
            errors.add(path + ": " + e.getMessage());
            return null;
        }
    }

    private <T> @Nullable T required(String path, @Nullable T value) {
        if (value == null) {
            errors.add(path + ": обязательное поле не задано");
        }
        return value;
    }
}
