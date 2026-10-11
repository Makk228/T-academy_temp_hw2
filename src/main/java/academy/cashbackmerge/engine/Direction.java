package academy.cashbackmerge.engine;

import academy.cashbackmerge.model.Position;
import java.util.ArrayList;
import java.util.List;

public enum Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT;

    public List<Position> line(int index, int size) {
        var cells = new ArrayList<Position>(size);
        for (int step = 0; step < size; step++) {
            int reversed = size - 1 - step;
            cells.add(
                    switch (this) {
                        case LEFT -> new Position(index, step);
                        case RIGHT -> new Position(index, reversed);
                        case UP -> new Position(step, index);
                        case DOWN -> new Position(reversed, index);
                    });
        }
        return List.copyOf(cells);
    }
}
