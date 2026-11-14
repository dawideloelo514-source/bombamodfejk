package pl.d3szcz;

import net.minecraft.client.gui.DrawContext;

public final class Ui {
    public static final int BG = 0xF00E0E12, SIDE = 0xFF0B0B0E, CARD = 0xFF17171D, BORDER = 0xFF2A2A33;
    public static final int GOLD = 0xFFF5C518, GREEN = 0xFF3DDC84, RED = 0xFFE5484D, GRAY = 0xFF8A8A94,
            WHITE = 0xFFFFFFFF, BLUE = 0xFF4A9EFF, DARK = 0xFF1A1400;

    private static final String[] ICON = {
            "..XXXX..", ".XXXXXX.", "XX.XX.XX", "XXXXXXXX",
            "XXXXXXXX", "XXXXXXXX", "XX.XX.XX", "X..XX..X"
    };

    /** Ikona (pikselowy duszek). Podmień na własną teksturę jeśli chcesz. */
    public static void icon(DrawContext c, int x, int y, int px, int color) {
        for (int r = 0; r < 8; r++)
            for (int col = 0; col < 8; col++)
                if (ICON[r].charAt(col) == 'X') c.fill(x + col * px, y + r * px, x + (col + 1) * px, y + (r + 1) * px, color);
    }

    public static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private Ui() {}
}
