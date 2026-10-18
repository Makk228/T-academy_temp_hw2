package academy.cashbackmerge.engine;

import academy.cashbackmerge.model.Board;

@FunctionalInterface
public interface TileSpawner {

    Board spawn(Board board);
}
