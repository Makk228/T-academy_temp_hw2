package academy.cashbackmerge.simulation;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public final class Strategies {

    private static final Map<String, Strategy> BY_NAME = byName(new RandomStrategy());

    private Strategies() {}

    public static Strategy byName(String name) {
        var strategy = BY_NAME.get(name);
        if (strategy == null) {
            throw new IllegalArgumentException(
                    "неизвестная стратегия '%s', доступны: %s".formatted(name, String.join(", ", names())));
        }
        return strategy;
    }

    public static Set<String> names() {
        return BY_NAME.keySet();
    }

    private static Map<String, Strategy> byName(Strategy... strategies) {
        var result = new TreeMap<String, Strategy>();
        for (var strategy : strategies) {
            result.put(strategy.name(), strategy);
        }
        return Collections.unmodifiableSortedMap(result);
    }
}
