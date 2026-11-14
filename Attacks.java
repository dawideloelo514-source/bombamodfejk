package pl.d3szcz;

import java.util.*;

/** Fałszywe "ataki" – tylko licznik 60 sekund. */
public final class Attacks {
    public static final long DURATION_MS = 60_000L;
    public record Attack(String name, String ip, long start) {
        public long remainingMs() { return Math.max(0, DURATION_MS - (System.currentTimeMillis() - start)); }
    }
    private static final List<Attack> ACTIVE = new ArrayList<>();

    public static void start(String name, String ip) {
        tick();
        for (Attack a : ACTIVE) if (a.ip().equals(ip)) return;
        ACTIVE.add(new Attack(name, ip, System.currentTimeMillis()));
    }

    public static List<Attack> tick() {
        ACTIVE.removeIf(a -> a.remainingMs() <= 0);
        return ACTIVE;
    }

    private Attacks() {}
}
