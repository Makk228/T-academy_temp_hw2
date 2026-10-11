package academy.cashbackmerge.support;

import academy.cashbackmerge.engine.TileSpawner;
import academy.cashbackmerge.model.Board;
import academy.cashbackmerge.model.Cashback;
import academy.cashbackmerge.model.Position;
import java.util.HashMap;

public final class TestBoards {

    private TestBoards() {}

    public static Board of(String... rows) {
        var tiles = new HashMap<Position, Cashback>();
        for (int row = 0; row < rows.length; row++) {
            var cells = rows[row].split(",");
            for (int column = 0; column < cells.length; column++) {
                var cell = cells[column].strip();
                if (!cell.equals(".")) {
                    tiles.put(new Position(row, column), Cashback.parse(cell));
                }
            }
        }
        return Board.of(Board.STANDARD_SIZE, tiles);
    }

    public static String firstRow(Board board) {
        return board.toString().substring(1, board.toString().indexOf(']'));
    }

    public static TileSpawner lastEmptyCell() {
        return board -> board.withTile(board.emptyCells().getLast(), Cashback.of(1));
    }

    public static TileSpawner mustNotSpawn() {
        return board -> {
            throw new AssertionError("плитка не должна появляться, поле: " + board);
        };
    }
}
