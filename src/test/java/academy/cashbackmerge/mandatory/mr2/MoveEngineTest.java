package academy.cashbackmerge.mandatory.mr2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import academy.cashbackmerge.config.Probability;
import academy.cashbackmerge.config.SpawnRule;
import academy.cashbackmerge.engine.Direction;
import academy.cashbackmerge.engine.MoveEngine;
import academy.cashbackmerge.engine.RandomTileSpawner;
import academy.cashbackmerge.model.Cashback;
import academy.cashbackmerge.model.GameState;
import academy.cashbackmerge.model.Position;
import academy.cashbackmerge.support.TestBoards;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Обязательные тесты: движок ходов — что считается ходом и что после него происходит. */
@DisplayName("MR2. Движок ходов")
class MoveEngineTest {

    private static final List<SpawnRule> RULES = List.of(
            new SpawnRule(Cashback.of(1), Probability.parse("0.85")),
            new SpawnRule(Cashback.of(2), Probability.parse("0.15")));

    @Test
    @DisplayName("Невалидный ход не меняет поле, не тратит ход и не создаёт плитку")
    void invalidMoveChangesNothing() {
        var state = GameState.initial(TestBoards.of("1,.,.,."), 10);
        var engine = new MoveEngine(TestBoards.mustNotSpawn());

        var result = engine.applyMove(state, Direction.LEFT);

        assertThat(result.changed()).isFalse();
        assertThat(result.state()).isEqualTo(state);
        assertThat(result.state().movesUsed()).isZero();
        assertThat(result.state().board().tileCount()).isEqualTo(1);
        assertThat(result.spawnedAt()).isNull();
    }

    @Test
    @DisplayName("После валидного хода появляется ровно одна новая плитка")
    void validMoveSpawnsExactlyOneTile() {
        var state = GameState.initial(TestBoards.of(".,.,.,1"), 10);
        var engine = new MoveEngine(RandomTileSpawner.seeded(RULES, 42));

        var result = engine.applyMove(state, Direction.LEFT);

        var board = result.state().board();
        assertThat(result.changed()).isTrue();
        assertThat(board.tileCount()).isEqualTo(2);
        assertThat(board.tileAt(new Position(0, 0))).as("сдвинутая плитка").contains(Cashback.of(1));
        assertThat(result.spawnedAt()).isNotNull().isNotEqualTo(new Position(0, 0));
        assertThat(board.tileAt(result.spawnedAt())).isPresent();
        assertThat(result.state().movesUsed()).isEqualTo(1);
    }

    @Test
    @DisplayName("После N валидных ходов игра завершена")
    void gameEndsAfterMovesLimit() {
        int limit = 5;
        var engine = new MoveEngine(RandomTileSpawner.seeded(RULES, 7));
        var state = GameState.initial(TestBoards.of("1,.,.,.", ".,.,.,.", ".,.,2,."), limit);

        int validMoves = 0;
        while (!state.isFinished()) {
            var direction = MoveEngine.availableMoves(state.board()).getFirst();
            var result = engine.applyMove(state, direction);
            assertThat(result.changed()).isTrue();
            state = result.state();
            validMoves++;
        }

        assertThat(validMoves).isEqualTo(limit);
        assertThat(state.movesLeft()).isZero();
        assertThat(MoveEngine.isGameOver(state)).isTrue();
        var finished = state;
        assertThatThrownBy(() -> engine.applyMove(finished, Direction.UP))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("партия окончена");
    }

    @Test
    @DisplayName("Достижение плитки 32% не заканчивает игру — доигрываем до лимита ходов")
    void reachingMaxTileDoesNotEndGame() {
        var state = GameState.initial(TestBoards.of("16,16,.,."), 10);
        var engine = new MoveEngine(TestBoards.lastEmptyCell());

        var result = engine.applyMove(state, Direction.LEFT);

        assertThat(result.state().board().tileAt(new Position(0, 0))).contains(Cashback.of(32));
        assertThat(result.state().isFinished()).isFalse();
        assertThat(MoveEngine.isGameOver(result.state())).isFalse();
        assertThat(result.state().movesLeft()).isEqualTo(9);

        var next = engine.applyMove(result.state(), Direction.DOWN);
        assertThat(next.changed()).as("после 32% можно ходить дальше").isTrue();
    }

    @Test
    @DisplayName("Score считается по правилам и растёт только при слияниях")
    void scoreIsCalculatedOnMerges() {
        var state = GameState.initial(TestBoards.of("2,2,4,4"), 10);
        var engine = new MoveEngine(TestBoards.lastEmptyCell());

        var merged = engine.applyMove(state, Direction.LEFT);

        assertThat(merged.scoreGained()).as("4 + 8").isEqualTo(12);
        assertThat(merged.state().score()).isEqualTo(12);

        var slidOnly = engine.applyMove(merged.state(), Direction.UP);

        assertThat(slidOnly.changed()).isTrue();
        assertThat(slidOnly.scoreGained()).as("сдвиг без слияний").isZero();
        assertThat(slidOnly.state().score()).isEqualTo(12);
    }
}
