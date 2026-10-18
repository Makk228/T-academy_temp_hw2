package academy.cashbackmerge.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.config.CampaignConfigLoader;
import academy.cashbackmerge.model.Cashback;
import academy.cashbackmerge.model.GameState;
import academy.cashbackmerge.model.Position;
import academy.cashbackmerge.support.CampaignJson;
import academy.cashbackmerge.support.TestBoards;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Движок ходов: граничные случаи")
class MoveEngineRulesTest {

    @Test
    @DisplayName("Сдвиг не меняет исходное поле")
    void shiftDoesNotMutateBoard() {
        var board = TestBoards.of("1,1,.,.");

        BoardShift.shift(board, Direction.LEFT);

        assertThat(board).hasToString("[1,1,.,.]/[.,.,.,.]/[.,.,.,.]/[.,.,.,.]");
    }

    @Test
    @DisplayName("Линия читается от стены, к которой едут плитки")
    void directionLinesStartAtTargetWall() {
        assertThat(Direction.LEFT.line(1, 4))
                .containsExactly(new Position(1, 0), new Position(1, 1), new Position(1, 2), new Position(1, 3));
        assertThat(Direction.RIGHT.line(1, 4))
                .containsExactly(new Position(1, 3), new Position(1, 2), new Position(1, 1), new Position(1, 0));
        assertThat(Direction.UP.line(2, 4))
                .containsExactly(new Position(0, 2), new Position(1, 2), new Position(2, 2), new Position(3, 2));
        assertThat(Direction.DOWN.line(2, 4))
                .containsExactly(new Position(3, 2), new Position(2, 2), new Position(1, 2), new Position(0, 2));
    }

    @Test
    @DisplayName("Заблокированное поле: ходов нет, партия окончена до лимита")
    void blockedBoardEndsGame() {
        var board = TestBoards.of("1,2,1,2", "2,1,2,1", "1,2,1,2", "2,1,2,1");
        var state = GameState.initial(board, 100);

        assertThat(MoveEngine.availableMoves(board)).isEmpty();
        assertThat(MoveEngine.isGameOver(state)).isTrue();
        assertThat(state.isFinished()).as("лимит ещё не исчерпан").isFalse();
    }

    @Test
    @DisplayName("Доступны только ходы, которые что-то меняют")
    void availableMovesExcludeNoOps() {
        var board = TestBoards.of("1,.,.,.");

        assertThat(MoveEngine.availableMoves(board)).containsExactly(Direction.DOWN, Direction.RIGHT);
    }

    @Test
    @DisplayName("Генератор, поставивший больше одной плитки, — ошибка, а не тихо испорченная партия")
    void spawnerMustAddExactlyOneTile() {
        TileSpawner twoTiles = board ->
                board.withTile(new Position(3, 3), Cashback.of(1)).withTile(new Position(3, 2), Cashback.of(1));
        var engine = new MoveEngine(twoTiles);
        var state = GameState.initial(TestBoards.of(".,1,.,."), 10);

        assertThatThrownBy(() -> engine.applyMove(state, Direction.LEFT))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageStartingWith("после хода должна появиться ровно одна плитка");
    }

    @Test
    @DisplayName("Одинаковый seed и одинаковые ходы — одинаковая партия целиком")
    void wholeGameIsReproducibleBySeed() {
        var config = CampaignConfigLoader.parse(CampaignJson.valid());

        var first = play(config, 42);
        var second = play(config, 42);

        assertThat(second).isEqualTo(first);
    }

    private static List<GameState> play(CampaignConfig config, long seed) {
        var spawner = RandomTileSpawner.seeded(config.spawnRules(), seed);
        var engine = new MoveEngine(spawner);
        var state = GameFactory.newGame(config, spawner);
        var history = new ArrayList<GameState>();
        var directions = Direction.values();
        for (int i = 0; i < 30 && !MoveEngine.isGameOver(state); i++) {
            state = engine.applyMove(state, directions[i % directions.length]).state();
            history.add(state);
        }
        return history;
    }
}
