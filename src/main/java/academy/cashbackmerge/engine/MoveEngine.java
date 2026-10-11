package academy.cashbackmerge.engine;

import academy.cashbackmerge.model.Board;
import academy.cashbackmerge.model.GameState;
import academy.cashbackmerge.model.Position;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class MoveEngine {

    private final TileSpawner spawner;

    public MoveEngine(TileSpawner spawner) {
        this.spawner = Objects.requireNonNull(spawner, "spawner");
    }

    public MoveResult applyMove(GameState state, Direction direction) {
        if (state.isFinished()) {
            throw new IllegalStateException(
                    "партия окончена: сделано %d ходов из %d".formatted(state.movesUsed(), state.movesLimit()));
        }
        var shifted = BoardShift.shift(state.board(), direction);
        if (!shifted.changed()) {
            return MoveResult.unchanged(state, direction);
        }

        var spawned = spawner.spawn(shifted.board());
        var spawnedAt = findSingleNewTile(shifted.board(), spawned);
        var next = new GameState(spawned, state.movesLimit(), state.movesUsed() + 1, state.score() + shifted.score());
        return new MoveResult(next, direction, true, shifted.score(), spawnedAt);
    }

    public static List<Direction> availableMoves(Board board) {
        return Arrays.stream(Direction.values())
                .filter(direction -> BoardShift.shift(board, direction).changed())
                .toList();
    }

    public static boolean isGameOver(GameState state) {
        return state.isFinished() || availableMoves(state.board()).isEmpty();
    }

    private static Position findSingleNewTile(Board before, Board after) {
        var added = after.positions().stream()
                .filter(before::isEmptyAt)
                .filter(position -> !after.isEmptyAt(position))
                .toList();
        boolean othersKept = before.tiles().entrySet().stream()
                .allMatch(entry -> after.tileAt(entry.getKey()).equals(Optional.of(entry.getValue())));
        if (added.size() != 1 || !othersKept) {
            throw new IllegalStateException(
                    "после хода должна появиться ровно одна плитка, а поле стало %s".formatted(after));
        }
        return added.getFirst();
    }
}
