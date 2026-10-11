package academy.cashbackmerge.engine;

import static org.assertj.core.api.Assertions.assertThat;

import academy.cashbackmerge.model.Cashback;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Слияние одной линии")
class LineMergerTest {

    @ParameterizedTest(name = "[{0}] -> [{1}], score {2}")
    @CsvSource(
            delimiter = '|',
            value = {
                "''          | ''      | 0",
                "1           | 1       | 0",
                "1,2         | 1,2     | 0",
                "2,2,2,2     | 4,4     | 8",
                "4,4,8       | 8,8     | 8",
                "2,2,2       | 4,2     | 4",
                "1,2,2,1     | 1,4,1   | 4",
                "16,16       | 32      | 32",
                "32,32       | 64      | 64",
            })
    @DisplayName("Сдвиг к началу и слияние соседних одинаковых")
    void mergesAdjacentEqualTiles(String input, String expected, long score) {
        var merged = LineMerger.merge(tiles(input));

        assertThat(merged.tiles()).isEqualTo(tiles(expected));
        assertThat(merged.score()).isEqualTo(score);
    }

    private static List<Cashback> tiles(String text) {
        if (text.isBlank()) {
            return List.of();
        }
        return Arrays.stream(text.split(",")).map(Cashback::parse).toList();
    }
}
