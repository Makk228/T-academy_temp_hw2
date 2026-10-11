package academy.cashbackmerge.model;

import java.math.BigDecimal;
import java.util.Objects;

public record GameState(Board board, int movesLimit, int movesUsed, BigDecimal score) {

    public GameState {
        Objects.requireNonNull(board, "board");
        Objects.requireNonNull(score, "score");
        if (movesLimit <= 0) {
            throw new IllegalArgumentException("movesLimit должен быть больше 0, получено " + movesLimit);
        }
        if (movesUsed < 0 || movesUsed > movesLimit) {
            throw new IllegalArgumentException(
                    "movesUsed должен быть от 0 до %d, получено %d".formatted(movesLimit, movesUsed));
        }
        if (score.signum() < 0) {
            throw new IllegalArgumentException("score не может быть отрицательным, получено " + score.toPlainString());
        }
        score = score.stripTrailingZeros();
    }

    public static GameState initial(Board board, int movesLimit) {
        return new GameState(board, movesLimit, 0, BigDecimal.ZERO);
    }

    public int movesLeft() {
        return movesLimit - movesUsed;
    }

    public boolean isFinished() {
        return movesUsed == movesLimit;
    }
}
