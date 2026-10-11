package academy.cashbackmerge.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import org.jspecify.annotations.Nullable;

public final class Board {

    public static final int STANDARD_SIZE = 4;

    private static final int MIN_SIZE = 2;

    private final int size;
    private final Map<Position, Cashback> tiles;

    private Board(int size, Map<Position, Cashback> tiles) {
        this.size = size;
        this.tiles = Map.copyOf(tiles);
    }

    public static Board empty() {
        return empty(STANDARD_SIZE);
    }

    public static Board empty(int size) {
        if (size < MIN_SIZE) {
            throw new IllegalArgumentException(
                    "размер поля должен быть не меньше %d, получено %d".formatted(MIN_SIZE, size));
        }
        return new Board(size, Map.of());
    }

    public int size() {
        return size;
    }

    public Optional<Cashback> tileAt(Position position) {
        requireInBounds(position);
        return Optional.ofNullable(tiles.get(position));
    }

    public boolean isEmptyAt(Position position) {
        return tileAt(position).isEmpty();
    }

    public Board withTile(Position position, Cashback tile) {
        if (!isEmptyAt(position)) {
            throw new IllegalStateException("клетка %s уже занята плиткой %s".formatted(position, tiles.get(position)));
        }
        var updated = new HashMap<>(tiles);
        updated.put(position, tile);
        return new Board(size, updated);
    }

    public List<Position> positions() {
        var result = new ArrayList<Position>(size * size);
        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                result.add(new Position(row, column));
            }
        }
        return List.copyOf(result);
    }

    public List<Position> emptyCells() {
        return positions().stream().filter(p -> !tiles.containsKey(p)).toList();
    }

    public int tileCount() {
        return tiles.size();
    }

    public Map<Position, Cashback> tiles() {
        return tiles;
    }

    private void requireInBounds(Position position) {
        if (position.row() >= size || position.column() >= size) {
            throw new IndexOutOfBoundsException("клетка %s за границами поля %dx%d".formatted(position, size, size));
        }
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return other instanceof Board board && size == board.size && tiles.equals(board.tiles);
    }

    @Override
    public int hashCode() {
        return 31 * size + tiles.hashCode();
    }

    @Override
    public String toString() {
        var rows = new StringJoiner("/");
        for (int row = 0; row < size; row++) {
            var cells = new StringJoiner(",", "[", "]");
            for (int column = 0; column < size; column++) {
                var tile = tiles.get(new Position(row, column));
                cells.add(tile == null ? "." : tile.toPlainString());
            }
            rows.add(cells.toString());
        }
        return rows.toString();
    }
}
