package pl.d3szcz;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class TargetScreen extends Screen {
    private static final int CARD_H = 44, RIGHT_W = 120;
    private final String target;
    private final Screen parent;
    private final List<Fake.Result> results;

    public TargetScreen(String target, Screen parent) {
        super(Text.literal("D3SZCZ"));
        this.target = target;
        this.parent = parent;
        this.results = Fake.results(target);
    }

    @Override public boolean shouldPause() { return false; }
    @Override public void close() { client.setScreen(parent); }

    private int pw() { return Math.min(width - 20, 580); }
    private int ph() { return Math.min(height - 20, 260); }
    private int px() { return (width - pw()) / 2; }
    private int py() { return (height - ph()) / 2; }
    private int mainW() { return pw() - RIGHT_W - 8; }
    private int cardW() { return (mainW() - 24) / 2; }
    private int cardX(int i) { return px() + 8 + (i % 2) * (cardW() + 8); }
    private int cardY(int i) { return py() + 46 + (i / 2) * (CARD_H + 8); }
    private int backX() { return px() + mainW() - 82; }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        c.fill(0, 0, width, height, 0x90000000);
        int px = px(), py = py(), mw = mainW();

        // panel główny + prawy
        c.fill(px, py, px + mw, py + ph(), Ui.BG);
        c.drawBorder(px, py, mw, ph(), Ui.BORDER);
        int rx = px + mw + 8;
        c.fill(rx, py, px + pw(), py + ph(), Ui.BG);
        c.drawBorder(rx, py, RIGHT_W, ph(), Ui.BORDER);

        // nagłówek: głowa + nick
        AbstractClientPlayerEntity found = null;
        if (client.world != null)
            for (var p : client.world.getPlayers())
                if (p.getGameProfile().getName().equals(target)) { found = p; break; }
        if (found != null) PlayerSkinDrawer.draw(c, found.getSkinTextures(), px + 8, py + 8, 24);
        else Ui.icon(c, px + 8, py + 8, 3, Ui.GOLD);
        c.drawText(textRenderer, Text.literal(target).formatted(Formatting.BOLD), px + 38, py + 10, Ui.WHITE, false);
        c.drawText(textRenderer, "Baza wycieków danych • Przeszukiwanie innych baz.", px + 38, py + 22, Ui.GRAY, false);

        // przycisk powrót
        boolean hb = Ui.in(mx, my, backX(), py + 8, 74, 18);
        c.fill(backX(), py + 8, backX() + 74, py + 26, Ui.CARD);
        c.drawBorder(backX(), py + 8, 74, 18, hb ? Ui.GOLD : Ui.BORDER);
        c.drawText(textRenderer, "X  POWRÓT", backX() + 8, py + 13, Ui.GRAY, false);

        // wyniki
        if (results.isEmpty())
            c.drawCenteredTextWithShadow(textRenderer, "Brak wyników dla tego gracza", px + mw / 2, py + 100, Ui.GRAY);
        for (int i = 0; i < results.size(); i++) {
            Fake.Result r = results.get(i);
            int x = cardX(i), y = cardY(i), w = cardW();
            c.fill(x, y, x + w, y + CARD_H, Ui.CARD);
            c.drawBorder(x, y, w, CARD_H, Ui.BORDER);
            c.drawText(textRenderer, r.count() + " wystąpień", x + 6, y + 5, Ui.GRAY, false);
            String st = r.online() ? "ONLINE" : "OFFLINE";
            int col = r.online() ? Ui.GREEN : Ui.RED;
            c.drawText(textRenderer, Text.literal(st).formatted(Formatting.BOLD), x + w - textRenderer.getWidth(st) - 6, y + 5, col, false);
            c.drawText(textRenderer, Text.literal(r.ip()).formatted(Formatting.BOLD), x + 6, y + 17, Ui.WHITE, false);
            c.drawText(textRenderer, textRenderer.trimToWidth(r.isp(), w - 70), x + 6, y + 31, Ui.BLUE, false);
            int bx = x + w - 54, by = y + 22;
            boolean hv = Ui.in(mx, my, bx, by, 48, 16);
            c.fill(bx, by, bx + 48, by + 16, hv ? 0xFFFFD84D : Ui.GOLD);
            c.drawText(textRenderer, Text.literal("BOMBA").formatted(Formatting.BOLD), bx + 7, by + 4, Ui.DARK, false);
        }

        // aktywne ataki
        List<Attacks.Attack> atk = Attacks.tick();
        c.drawText(textRenderer, Text.literal("Aktywne Ataki").formatted(Formatting.BOLD), rx + 8, py + 10, Ui.WHITE, false);
        String cnt = String.valueOf(atk.size());
        c.fill(rx + RIGHT_W - 22, py + 7, rx + RIGHT_W - 6, py + 20, 0x40E5484D);
        c.drawText(textRenderer, cnt, rx + RIGHT_W - 14 - textRenderer.getWidth(cnt) / 2, py + 10, Ui.RED, false);
        for (int i = 0; i < atk.size(); i++) {
            Attacks.Attack a = atk.get(i);
            int y = py + 30 + i * 46;
            if (y + 42 > py + ph()) break;
            c.fill(rx + 6, y, rx + RIGHT_W - 6, y + 42, Ui.CARD);
            c.drawBorder(rx + 6, y, RIGHT_W - 12, 42, Ui.BORDER);
            c.drawText(textRenderer, Text.literal(a.name()).formatted(Formatting.BOLD), rx + 11, y + 5, Ui.WHITE, false);
            c.drawText(textRenderer, a.ip(), rx + 11, y + 16, Ui.GRAY, false);
            long sec = (a.remainingMs() + 999) / 1000;
            c.drawText(textRenderer, "Pozostało: " + sec + "s", rx + 11, y + 27, Ui.GRAY, false);
            int bw = RIGHT_W - 22;
            c.fill(rx + 11, y + 37, rx + 11 + bw, y + 39, 0xFF2A2A33);
            c.fill(rx + 11, y + 37, rx + 11 + (int) (bw * (a.remainingMs() / (double) Attacks.DURATION_MS)), y + 39, Ui.GOLD);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            if (Ui.in(mx, my, backX(), py() + 8, 74, 18)) { close(); return true; }
            for (int i = 0; i < results.size(); i++) {
                int bx = cardX(i) + cardW() - 54, by = cardY(i) + 22;
                if (Ui.in(mx, my, bx, by, 48, 16)) {
                    Attacks.start(target, results.get(i).ip());
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }
}
