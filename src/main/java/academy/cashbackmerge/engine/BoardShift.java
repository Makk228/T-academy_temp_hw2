package academy.cashbackmerge.engine;

import academy.cashbackmerge.model.Board;
import academy.cashbackmerge.model.Cashback;
import academy.cashbackmerge.model.Position;
import java.util.HashMap;
import java.util.Optional;

public final class BoardShift {

    private BoardShift() {}

    public record Shifted(Board board, long score, boolean changed) {}

    public static Shifted shift(Board board, Direction direction) {
        var tiles = new HashMap<Position, Cashback>();
        long score = 0;
        for (int index = 0; index < board.size(); index++) {
            var cells = direction.line(index, board.size());
            var lineTiles =
                    cells.stream().map(board::tileAt).flatMap(Optional::stream).toList();

            var merged = LineMerger.merge(lineTiles);
            for (int k = 0; k < merged.tiles().size(); k++) {
                tiles.put(cells.get(k), merged.tiles().get(k));
            }
            score += merged.score();
        }
        var shifted = Board.of(board.size(), tiles);
        return new Shifted(shifted, score, !shifted.equals(board));
    }
}
