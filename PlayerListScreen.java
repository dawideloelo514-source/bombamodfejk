package pl.d3szcz;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

public class PlayerListScreen extends Screen {
    private static final String[] MENU = {"Gracze", "Atak", "Historia", "Ustawienia", "Motywy", "Profil"};
    private static final int CARD_H = 34, SIDE_W = 100;
    private final Map<String, String> shownIps = new HashMap<>();
    private int scroll = 0;

    public PlayerListScreen() { super(Text.literal("D3SZCZ")); }

    @Override public boolean shouldPause() { return false; }

    private int pw() { return Math.min(width - 20, 580); }
    private int ph() { return Math.min(height - 20, 260); }
    private int px() { return (width - pw()) / 2; }
    private int py() { return (height - ph()) / 2; }
    private int cx() { return px() + SIDE_W + 10; }
    private int cy() { return py() + 34; }
    private int cw() { return pw() - SIDE_W - 20; }
    private int ch() { return ph() - 34 - 22; }

    private record Card(AbstractClientPlayerEntity p, int x, int y, int w) {}

    private List<Card> layout() {
        List<Card> out = new ArrayList<>();
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return out;
        var me = mc.player;
        List<AbstractClientPlayerEntity> list = mc.world.getPlayers().stream()
                .filter(p -> p != me && p.distanceTo(me) <= 50f)
                .sorted(Comparator.comparingDouble(p -> p.distanceTo(me))).toList();
        int w = (cw() - 10) / 2;
        for (int i = 0; i < list.size(); i++) {
            int x = cx() + (i % 2) * (w + 10);
            int y = cy() + (i / 2) * (CARD_H + 8) - scroll;
            out.add(new Card(list.get(i), x, y, w));
        }
        return out;
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        c.fill(0, 0, width, height, 0x90000000);
        int px = px(), py = py(), pw = pw(), ph = ph();
        c.fill(px, py, px + pw, py + ph, Ui.BG);
        c.drawBorder(px, py, pw, ph, Ui.BORDER);
        c.fill(px, py, px + SIDE_W, py + ph, Ui.SIDE);

        // logo
        Ui.icon(c, px + 8, py + 8, 2, Ui.GOLD);
        c.drawText(textRenderer, Text.literal("D3SZCZ").formatted(Formatting.BOLD), px + 30, py + 12, Ui.WHITE, false);

        // menu
        for (int i = 0; i < MENU.length; i++) {
            int y = py + 36 + i * 24;
            boolean active = i == 0;
            if (active) {
                c.fill(px + 6, y, px + SIDE_W - 6, y + 20, 0x30F5C518);
                c.drawBorder(px + 6, y, SIDE_W - 12, 20, Ui.GOLD);
            }
            c.drawText(textRenderer, MENU[i], px + 14, y + 6, active ? Ui.GOLD : Ui.GRAY, false);
        }

        c.drawText(textRenderer, Text.literal("Gracze").formatted(Formatting.BOLD), cx(), py + 12, Ui.WHITE, false);

        List<Card> cards = layout();
        c.enableScissor(cx(), cy(), cx() + cw(), cy() + ch());
        for (Card k : cards) {
            c.fill(k.x(), k.y(), k.x() + k.w(), k.y() + CARD_H, Ui.CARD);
            c.drawBorder(k.x(), k.y(), k.w(), CARD_H, Ui.in(mx, my, k.x(), k.y(), k.w(), CARD_H) ? Ui.GOLD : Ui.BORDER);
            PlayerSkinDrawer.draw(c, k.p().getSkinTextures(), k.x() + 5, k.y() + 5, 24);
            String name = k.p().getGameProfile().getName();
            c.drawText(textRenderer, Text.literal(name).formatted(Formatting.BOLD), k.x() + 34, k.y() + 6, Ui.WHITE, false);
            String ip = shownIps.get(name);
            c.drawText(textRenderer, ip != null ? ip : "klik = losuj IP", k.x() + 34, k.y() + 20,
                    ip != null ? Ui.GOLD : Ui.GRAY, false);
            String dist = Math.round(k.p().distanceTo(client.player)) + "m";
            int dw = textRenderer.getWidth(dist) + 8;
            c.fill(k.x() + k.w() - dw - 5, k.y() + 5, k.x() + k.w() - 5, k.y() + 17, 0x40E5484D);
            c.drawText(textRenderer, dist, k.x() + k.w() - dw - 1, k.y() + 7, Ui.GOLD, false);
        }
        c.disableScissor();

        if (cards.isEmpty())
            c.drawCenteredTextWithShadow(textRenderer, "Brak graczy w promieniu 50m", cx() + cw() / 2, cy() + 40, Ui.GRAY);

        c.drawText(textRenderer, "LPM - losuj IP   |   ŚPM (scroll) - panel ataku   |   kółko - przewijanie",
                cx(), py + ph - 14, Ui.GRAY, false);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (Ui.in(mx, my, cx(), cy(), cw(), ch())) {
            for (Card k : layout()) {
                if (Ui.in(mx, my, k.x(), k.y(), k.w(), CARD_H)) {
                    String name = k.p().getGameProfile().getName();
                    if (button == 0) { shownIps.put(name, Fake.ip()); return true; }
                    if (button == 2) { client.setScreen(new TargetScreen(name, this)); return true; }
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        int rows = (layout().size() + 1) / 2;
        int max = Math.max(0, rows * (CARD_H + 8) - ch());
        scroll = Math.max(0, Math.min(max, scroll - (int) (v * 20)));
        return true;
    }
}
