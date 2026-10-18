package academy.cashbackmerge.engine;

import academy.cashbackmerge.model.Cashback;
import java.util.ArrayList;
import java.util.List;

public final class LineMerger {

    private LineMerger() {}

    public record Merged(List<Cashback> tiles, long score) {

        public Merged {
            tiles = List.copyOf(tiles);
        }
    }

    public static Merged merge(List<Cashback> tiles) {
        var result = new ArrayList<Cashback>(tiles.size());
        long score = 0;
        int i = 0;
        while (i < tiles.size()) {
            var current = tiles.get(i);
            boolean mergesWithNext = i + 1 < tiles.size() && current.equals(tiles.get(i + 1));
            if (mergesWithNext) {
                var merged = current.doubled();
                result.add(merged);
                score += merged.percent();
                i += 2;
            } else {
                result.add(current);
                i++;
            }
        }
        return new Merged(result, score);
    }
}
