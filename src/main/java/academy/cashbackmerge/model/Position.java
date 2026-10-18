package academy.cashbackmerge.model;

public record Position(int row, int column) {

    public Position {
        if (row < 0 || column < 0) {
            throw new IllegalArgumentException(
                    "координаты не могут быть отрицательными: (%d, %d)".formatted(row, column));
        }
    }

    @Override
    public String toString() {
        return "(%d, %d)".formatted(row, column);
    }
}
