package academy.cashbackmerge.simulation;

import academy.cashbackmerge.engine.Direction;
import academy.cashbackmerge.model.GameState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.random.RandomGenerator;

public final class RandomStrategy implements Strategy {

    public static final String NAME = "random";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public List<Direction> preferences(GameState state, RandomGenerator random) {
        var moves = new ArrayList<>(List.of(Direction.values()));
        Collections.shuffle(moves, random);
        return moves;
    }
}
