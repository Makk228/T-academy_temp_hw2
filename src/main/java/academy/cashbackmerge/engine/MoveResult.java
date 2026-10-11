package academy.cashbackmerge.engine;

import academy.cashbackmerge.model.GameState;
import academy.cashbackmerge.model.Position;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

public record MoveResult(
        GameState state,
        Direction direction,
        boolean changed,
        long scoreGained,
        @Nullable Position spawnedAt) {

    public MoveResult {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(direction, "direction");
        if (changed != (spawnedAt != null)) {
            throw new IllegalArgumentException("новая плитка появляется тогда и только тогда, когда ход изменил поле");
        }
    }

    static MoveResult unchanged(GameState state, Direction direction) {
        return new MoveResult(state, direction, false, 0, null);
    }
}
