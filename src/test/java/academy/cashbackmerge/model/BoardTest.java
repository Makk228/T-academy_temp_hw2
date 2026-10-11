package academy.cashbackmerge.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Поле")
class BoardTest {

    @Test
    @DisplayName("Пустое поле 4x4: 16 свободных клеток построчно")
    void emptyBoardHasAllCellsFree() {
        var board = Board.empty();

        assertThat(board.size()).isEqualTo(4);
        assertThat(board.tileCount()).isZero();
        assertThat(board.emptyCells())
                .hasSize(16)
                .startsWith(new Position(0, 0), new Position(0, 1))
                .endsWith(new Position(3, 3));
    }

    @Test
    @DisplayName("withTile возвращает новое поле, старое не меняется")
    void withTileDoesNotMutate() {
        var empty = Board.empty();
        var position = new Position(1, 2);

        var board = empty.withTile(position, Cashback.of(2));

        assertThat(board.tileAt(position)).contains(Cashback.of(2));
        assertThat(board.emptyCells()).hasSize(15).doesNotContain(position);
        assertThat(empty.tileCount()).isZero();
        assertThat(board.toString()).isEqualTo("[.,.,.,.]/[.,.,2,.]/[.,.,.,.]/[.,.,.,.]");
    }

    @Test
    @DisplayName("В клетке не больше одной плитки")
    void cellHoldsAtMostOneTile() {
        var board = Board.empty().withTile(new Position(0, 0), Cashback.of(1));

        assertThatThrownBy(() -> board.withTile(new Position(0, 0), Cashback.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("клетка (0, 0) уже занята плиткой 1%");
    }

    @Test
    @DisplayName("Координаты за границами поля отклоняются")
    void outOfBoundsPositionIsRejected() {
        var board = Board.empty();

        assertThatThrownBy(() -> board.withTile(new Position(4, 0), Cashback.of(1)))
                .isInstanceOf(IndexOutOfBoundsException.class)
                .hasMessage("клетка (4, 0) за границами поля 4x4");
        assertThatThrownBy(() -> new Position(-1, 0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Поля с одинаковыми плитками равны")
    void boardsWithSameTilesAreEqual() {
        var first =
                Board.empty().withTile(new Position(0, 0), Cashback.of(1)).withTile(new Position(3, 3), Cashback.of(2));
        var second =
                Board.empty().withTile(new Position(3, 3), Cashback.of(2)).withTile(new Position(0, 0), Cashback.of(1));

        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
        assertThat(first).isNotEqualTo(Board.empty());
    }
}
