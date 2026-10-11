package academy.cashbackmerge.engine;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.model.Board;
import academy.cashbackmerge.model.GameState;

public final class GameFactory {

    public static final int INITIAL_TILES = 2;

    private GameFactory() {}

    public static GameState newGame(CampaignConfig config, TileSpawner spawner) {
        var board = Board.empty(config.boardSize());
        for (int i = 0; i < INITIAL_TILES; i++) {
            board = spawner.spawn(board);
        }
        return GameState.initial(board, config.movesLimit());
    }
}
