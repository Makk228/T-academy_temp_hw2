package academy.cashbackmerge.mandatory.mr2;

import static org.assertj.core.api.Assertions.assertThat;

import academy.cashbackmerge.engine.BoardShift;
import academy.cashbackmerge.engine.Direction;
import academy.cashbackmerge.support.TestBoards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Обязательные тесты: правила слияния плиток. */
@DisplayName("MR2. Слияние плиток")
class MergeRulesTest {

    @ParameterizedTest
    @CsvSource({
        "'1,1,.,.', '2,.,.,.'",
        "'1,1,1,.', '2,1,.,.'",
        "'1,1,1,1', '2,2,.,.'",
        "'2,2,4,4', '4,8,.,.'",
    })
    @DisplayName("Сдвиг влево: {0} -> {1}")
    void rowShiftsLeftAndMerges(String before, String expected) {
        var board = TestBoards.of(before);

        var shifted = BoardShift.shift(board, Direction.LEFT);

        assertThat(TestBoards.firstRow(shifted.board())).isEqualTo(expected);
        assertThat(shifted.changed()).isTrue();
    }

    @Test
    @DisplayName("Одна плитка не сливается дважды за ход: [1,1,2,.] -> [2,2,.,.]")
    void tileDoesNotMergeTwiceInOneMove() {
        var board = TestBoards.of("1,1,2,.");

        var shifted = BoardShift.shift(board, Direction.LEFT);

        assertThat(TestBoards.firstRow(shifted.board())).isEqualTo("2,2,.,.");
        assertThat(shifted.score()).as("слилась только пара единиц").isEqualTo(2);
    }

    @Test
    @DisplayName("Слияние работает во всех четырёх направлениях")
    void mergeWorksInAllDirections() {
        var board = TestBoards.of("1,1,.,.", "1,.,.,.");

        assertThat(BoardShift.shift(board, Direction.LEFT).board())
                .hasToString("[2,.,.,.]/[1,.,.,.]/[.,.,.,.]/[.,.,.,.]");
        assertThat(BoardShift.shift(board, Direction.RIGHT).board())
                .hasToString("[.,.,.,2]/[.,.,.,1]/[.,.,.,.]/[.,.,.,.]");
        assertThat(BoardShift.shift(board, Direction.UP).board())
                .hasToString("[2,1,.,.]/[.,.,.,.]/[.,.,.,.]/[.,.,.,.]");
        assertThat(BoardShift.shift(board, Direction.DOWN).board())
                .hasToString("[.,.,.,.]/[.,.,.,.]/[.,.,.,.]/[2,1,.,.]");
    }
}
