package academy.cashbackmerge.engine;

import academy.cashbackmerge.config.SpawnRule;
import academy.cashbackmerge.model.Cashback;
import java.math.BigDecimal;
import java.util.List;
import java.util.random.RandomGenerator;

public final class SpawnDistribution {

    private final List<Cashback> outcomes;
    private final long[] cumulativeWeights;
    private final long totalWeight;

    public SpawnDistribution(List<SpawnRule> rules) {
        if (rules.isEmpty()) {
            throw new IllegalArgumentException("нужно хотя бы одно правило появления плитки");
        }
        int scale = rules.stream()
                .mapToInt(rule -> rule.probability().value().scale())
                .max()
                .orElseThrow();
        scale = Math.max(scale, 0);

        this.outcomes = rules.stream().map(SpawnRule::cashback).toList();
        this.cumulativeWeights = new long[rules.size()];
        long sum = 0;
        for (int i = 0; i < rules.size(); i++) {
            sum += rules.get(i).probability().value().movePointRight(scale).longValueExact();
            cumulativeWeights[i] = sum;
        }
        if (BigDecimal.valueOf(sum).compareTo(BigDecimal.ONE.movePointRight(scale)) != 0) {
            throw new IllegalArgumentException("сумма вероятностей должна быть ровно 1");
        }
        this.totalWeight = sum;
    }

    public Cashback sample(RandomGenerator random) {
        long point = random.nextLong(totalWeight);
        for (int i = 0; i < cumulativeWeights.length; i++) {
            if (point < cumulativeWeights[i]) {
                return outcomes.get(i);
            }
        }
        throw new IllegalStateException("точка %d за пределами суммы весов %d".formatted(point, totalWeight));
    }
}
