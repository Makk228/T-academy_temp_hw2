package academy.cashbackmerge.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Кэшбэк и состояние партии")
class ValueObjectsTest {

    @Test
    @DisplayName("Кэшбэк сравнивается по величине, а не по записи числа")
    void cashbackIsNormalized() {
        assertThat(Cashback.parse("1.0")).isEqualTo(Cashback.parse(" 1 ")).isEqualTo(Cashback.of(1));
        assertThat(Cashback.parse("16").toString()).isEqualTo("16%");
        assertThat(Cashback.of(2)).isGreaterThan(Cashback.of(1));
    }

    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(strings = {"0", "-1", "abc", ""})
    @DisplayName("Кэшбэк должен быть положительным числом")
    void invalidCashbackIsRejected(String text) {
        assertThatThrownBy(() -> Cashback.parse(text)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Начальное состояние: ни одного хода, ноль очков, партия не окончена")
    void initialState() {
        var state = GameState.initial(Board.empty(), 10);

        assertThat(state.movesUsed()).isZero();
        assertThat(state.movesLeft()).isEqualTo(10);
        assertThat(state.score()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(state.isFinished()).isFalse();
    }

    @Test
    @DisplayName("Состояние с нарушенным лимитом ходов создать нельзя")
    void invalidStateIsRejected() {
        var board = Board.empty();

        assertThatThrownBy(() -> GameState.initial(board, 0)).hasMessageContaining("movesLimit");
        assertThatThrownBy(() -> new GameState(board, 5, 6, BigDecimal.ZERO)).hasMessageContaining("movesUsed");
        assertThatThrownBy(() -> new GameState(board, 5, 0, BigDecimal.ONE.negate()))
                .hasMessageContaining("score");
    }
}
