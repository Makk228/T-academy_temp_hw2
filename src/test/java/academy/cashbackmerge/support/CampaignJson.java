package academy.cashbackmerge.support;

public final class CampaignJson {

    public static final String DEFAULT_SPAWN_RULES = """
            [
              { "cashback": "1", "probability": "0.85" },
              { "cashback": "2", "probability": "0.15" }
            ]""";

    private CampaignJson() {}

    public static String valid() {
        return with("100", DEFAULT_SPAWN_RULES);
    }

    public static String with(String movesLimit, String spawnRules) {
        return """
                {
                  "movesLimit": %s,
                  "spawnRules": %s,
                  "rewardTable": [
                    { "cashback": "2",  "reward": "0" },
                    { "cashback": "4",  "reward": "50" },
                    { "cashback": "8",  "reward": "150" },
                    { "cashback": "16", "reward": "500" },
                    { "cashback": "32", "reward": "2000" }
                  ],
                  "maxRewardCashback": "32",
                  "maxRewardAllowedRate": "0.20"
                }
                """.formatted(movesLimit, spawnRules);
    }
}
