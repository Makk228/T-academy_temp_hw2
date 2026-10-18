package academy.cashbackmerge.render;

import academy.cashbackmerge.model.Board;
import academy.cashbackmerge.model.Cashback;
import academy.cashbackmerge.model.GameState;
import academy.cashbackmerge.model.Position;

public final class BoardRenderer {

    private static final String EMPTY_CELL = ".";
    private static final int MIN_LABEL_WIDTH = 3;

    private BoardRenderer() {}

    public static String render(Board board) {
        int labelWidth = board.tiles().values().stream()
                .mapToInt(tile -> label(tile).length())
                .max()
                .orElse(0);
        int cellWidth = Math.max(labelWidth, MIN_LABEL_WIDTH) + 2;
        var border = "+" + ("-".repeat(cellWidth) + "+").repeat(board.size()) + "\n";

        var result = new StringBuilder(border);
        for (int row = 0; row < board.size(); row++) {
            result.append('|');
            for (int column = 0; column < board.size(); column++) {
                var text = board.tileAt(new Position(row, column))
                        .map(BoardRenderer::label)
                        .orElse(EMPTY_CELL);
                result.append(center(text, cellWidth)).append('|');
            }
            result.append('\n').append(border);
        }
        return result.toString();
    }

    public static String render(GameState state) {
        var header = "Ходы: %d из %d (осталось %d), score: %d"
                .formatted(state.movesUsed(), state.movesLimit(), state.movesLeft(), state.score());
        return header + "\n" + render(state.board());
    }

    private static String label(Cashback tile) {
        return tile.toString();
    }

    private static String center(String text, int width) {
        int left = (width - text.length() + 1) / 2;
        int right = width - text.length() - left;
        return " ".repeat(left) + text + " ".repeat(right);
    }
}
