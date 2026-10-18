package academy.cashbackmerge.model;

import java.util.Objects;

public record GameState(Board board, int movesLimit, int movesUsed, long score) {

    public GameState {
        Objects.requireNonNull(board, "board");
        if (movesLimit <= 0) {
            throw new IllegalArgumentException("movesLimit должен быть больше 0, получено " + movesLimit);
        }
        if (movesUsed < 0 || movesUsed > movesLimit) {
            throw new IllegalArgumentException(
                    "movesUsed должен быть от 0 до %d, получено %d".formatted(movesLimit, movesUsed));
        }
        if (score < 0) {
            throw new IllegalArgumentException("score не может быть отрицательным, получено " + score);
        }
    }

    public static GameState initial(Board board, int movesLimit) {
        return new GameState(board, movesLimit, 0, 0);
    }

    public int movesLeft() {
        return movesLimit - movesUsed;
    }

    public boolean isFinished() {
        return movesUsed == movesLimit;
    }
}
