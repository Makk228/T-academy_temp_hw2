package academy.cashbackmerge.engine;

import academy.cashbackmerge.config.SpawnRule;
import academy.cashbackmerge.model.Board;
import java.util.List;
import java.util.Objects;
import java.util.SplittableRandom;
import java.util.random.RandomGenerator;

public final class RandomTileSpawner implements TileSpawner {

    private final SpawnDistribution distribution;
    private final RandomGenerator random;

    public RandomTileSpawner(List<SpawnRule> rules, RandomGenerator random) {
        this.distribution = new SpawnDistribution(rules);
        this.random = Objects.requireNonNull(random, "random");
    }

    public static RandomTileSpawner seeded(List<SpawnRule> rules, long seed) {
        return new RandomTileSpawner(rules, new SplittableRandom(seed));
    }

    @Override
    public Board spawn(Board board) {
        var emptyCells = board.emptyCells();
        if (emptyCells.isEmpty()) {
            throw new IllegalStateException("на поле нет свободных клеток для новой плитки");
        }
        var position = emptyCells.get(random.nextInt(emptyCells.size()));
        return board.withTile(position, distribution.sample(random));
    }
}
