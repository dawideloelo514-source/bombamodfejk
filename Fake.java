package pl.d3szcz;

import java.util.*;

/** Generator losowych (fałszywych) danych. Nic nie jest nigdzie wysyłane. */
public final class Fake {
    private static final Random R = new Random();
    private static final String[] ISPS = {
            "NAT", "Multimedia Polska S.A.", "Orange Polska S.A.", "Play (P4 Sp. z o.o.)",
            "Netia S.A.", "Vectra S.A.", "T-Mobile Polska S.A.", "Inea S.A.",
            "Naukowa i Akademicka Siec Komputerowa", "UPC Polska"
    };
    private static final Map<String, List<Result>> CACHE = new HashMap<>();

    public record Result(int count, String ip, String isp, boolean online) {}

    public static String ip() {
        int a;
        do { a = 1 + R.nextInt(223); } while (a == 10 || a == 100 || a == 127 || a == 169 || a == 172 || a == 192 || a == 198);
        return a + "." + R.nextInt(256) + "." + R.nextInt(256) + "." + (1 + R.nextInt(254));
    }

    /** 0-3 wyników (czasem brak, czasem 2, czasem 3), zapamiętane per nick. */
    public static List<Result> results(String nick) {
        return CACHE.computeIfAbsent(nick, k -> {
            int n = R.nextInt(4);
            List<Result> list = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                list.add(new Result(1 + R.nextInt(60), ip(), ISPS[R.nextInt(ISPS.length)], R.nextBoolean()));
            }
            list.sort((x, y) -> Integer.compare(y.count(), x.count()));
            return list;
        });
    }

    private Fake() {}
}
