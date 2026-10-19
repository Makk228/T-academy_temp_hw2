package academy.cashbackmerge.simulation;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.engine.GameFactory;
import academy.cashbackmerge.engine.MoveEngine;
import academy.cashbackmerge.engine.RandomTileSpawner;
import academy.cashbackmerge.model.GameState;
import java.util.Optional;
import java.util.SplittableRandom;

public final class GameRunner {

    private GameRunner() {}

    public static GameState play(CampaignConfig config, Strategy strategy, SplittableRandom random) {
        var spawner = new RandomTileSpawner(config.spawnRules(), random.split());
        var strategyRandom = random.split();
        var engine = new MoveEngine(spawner);

        var state = GameFactory.newGame(config, spawner);
        while (!state.isFinished()) {
            var next = makeMove(engine, state, strategy, strategyRandom);
            if (next.isEmpty()) {
                break;
            }
            state = next.get();
        }
        return state;
    }

    private static Optional<GameState> makeMove(
            MoveEngine engine, GameState state, Strategy strategy, SplittableRandom strategyRandom) {
        for (var direction : strategy.preferences(state, strategyRandom)) {
            var result = engine.applyMove(state, direction);
            if (result.changed()) {
                return Optional.of(result.state());
            }
        }
        return Optional.empty();
    }
}
