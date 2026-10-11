package academy.cashbackmerge.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import academy.cashbackmerge.config.CampaignConfigLoader;
import academy.cashbackmerge.config.Probability;
import academy.cashbackmerge.config.SpawnRule;
import academy.cashbackmerge.model.Board;
import academy.cashbackmerge.model.Cashback;
import academy.cashbackmerge.model.GameState;
import academy.cashbackmerge.support.CampaignJson;
import java.util.List;
import java.util.SplittableRandom;
import java.util.stream.LongStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Начальное состояние и появление плиток")
class GameFactoryTest {

    private static final List<SpawnRule> ONLY_TWOS = List.of(new SpawnRule(Cashback.of(2), Probability.parse("1")));

    @Test
    @DisplayName("Новая партия: две плитки из правил появления, лимит из конфига")
    void newGameHasTwoTiles() {
        var config = CampaignConfigLoader.parse(CampaignJson.valid());

        var state = GameFactory.newGame(config, RandomTileSpawner.seeded(config.spawnRules(), 7));

        assertThat(state.board().tileCount()).isEqualTo(GameFactory.INITIAL_TILES);
        assertThat(state.board().tiles().values())
                .allMatch(tile -> tile.equals(Cashback.of(1)) || tile.equals(Cashback.of(2)));
        assertThat(state.movesLimit()).isEqualTo(100);
        assertThat(state.movesUsed()).isZero();
    }

    @Test
    @DisplayName("Разные seed дают разные начальные поля")
    void differentSeedsGiveDifferentBoards() {
        var config = CampaignConfigLoader.parse(CampaignJson.valid());

        var boards = LongStream.range(0, 20)
                .mapToObj(seed -> GameFactory.newGame(config, RandomTileSpawner.seeded(config.spawnRules(), seed)))
                .map(GameState::board)
                .distinct()
                .count();

        assertThat(boards).isGreaterThan(1);
    }

    @Test
    @DisplayName("Плитка ставится только в свободную клетку, на полном поле — ошибка")
    void spawnFillsOnlyEmptyCells() {
        var spawner = new RandomTileSpawner(ONLY_TWOS, new SplittableRandom(1));
        var board = Board.empty();
        for (int i = 0; i < 16; i++) {
            board = spawner.spawn(board);
        }

        assertThat(board.tileCount()).isEqualTo(16);
        assertThat(board.tiles().values()).containsOnly(Cashback.of(2));
        var full = board;
        assertThatThrownBy(() -> spawner.spawn(full)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Правило с вероятностью 1 выпадает всегда")
    void certainRuleAlwaysWins() {
        var distribution = new SpawnDistribution(ONLY_TWOS);
        var random = new SplittableRandom(3);

        for (int i = 0; i < 1000; i++) {
            assertThat(distribution.sample(random)).isEqualTo(Cashback.of(2));
        }
    }
}
