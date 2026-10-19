package academy.cashbackmerge.simulation;

import academy.cashbackmerge.engine.Direction;
import academy.cashbackmerge.model.GameState;
import java.util.List;
import java.util.random.RandomGenerator;

public interface Strategy {

    String name();

    List<Direction> preferences(GameState state, RandomGenerator random);
}
