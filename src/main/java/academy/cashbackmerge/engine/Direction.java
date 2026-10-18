package academy.cashbackmerge.engine;

import academy.cashbackmerge.model.Position;
import java.util.List;
import java.util.stream.IntStream;

public enum Direction {
    UP {
        @Override
        Position cell(int index, int step, int size) {
            return new Position(step, index);
        }
    },
    DOWN {
        @Override
        Position cell(int index, int step, int size) {
            return new Position(size - 1 - step, index);
        }
    },
    LEFT {
        @Override
        Position cell(int index, int step, int size) {
            return new Position(index, step);
        }
    },
    RIGHT {
        @Override
        Position cell(int index, int step, int size) {
            return new Position(index, size - 1 - step);
        }
    };

    public List<Position> line(int index, int size) {
        return IntStream.range(0, size)
                .mapToObj(step -> cell(index, step, size))
                .toList();
    }

    abstract Position cell(int index, int step, int size);
}
