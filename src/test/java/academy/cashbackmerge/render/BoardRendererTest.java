package academy.cashbackmerge.render;

import static org.assertj.core.api.Assertions.assertThat;

import academy.cashbackmerge.model.Board;
import academy.cashbackmerge.model.Cashback;
import academy.cashbackmerge.model.GameState;
import academy.cashbackmerge.model.Position;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Текстовый вывод поля")
class BoardRendererTest {

    @Test
    @DisplayName("Поле с плитками и строкой состояния")
    void rendersStateWithBoard() {
        var board = Board.empty()
                .withTile(new Position(0, 0), Cashback.of(1))
                .withTile(new Position(1, 3), Cashback.of(32));

        var text = BoardRenderer.render(GameState.initial(board, 100));

        assertThat(text).isEqualTo("""
                Ходы: 0 из 100 (осталось 100), score: 0
                +-----+-----+-----+-----+
                |  1% |  .  |  .  |  .  |
                +-----+-----+-----+-----+
                |  .  |  .  |  .  | 32% |
                +-----+-----+-----+-----+
                |  .  |  .  |  .  |  .  |
                +-----+-----+-----+-----+
                |  .  |  .  |  .  |  .  |
                +-----+-----+-----+-----+
                """);
    }

    @Test
    @DisplayName("Клетки расширяются под длинную подпись")
    void cellsGrowForLongLabels() {
        var board = Board.empty().withTile(new Position(0, 0), Cashback.of(1024));

        var firstLine = BoardRenderer.render(board).lines().findFirst().orElseThrow();

        assertThat(firstLine).isEqualTo("+-------+-------+-------+-------+");
    }
}
