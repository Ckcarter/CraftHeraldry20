package com.example.craftheraldry.client.screen;

import com.example.craftheraldry.CraftHeraldry;
import com.example.craftheraldry.common.network.ModNetwork;
import com.example.craftheraldry.common.network.ScrollUpdatePacket;
import com.example.craftheraldry.common.util.CrestData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;
import java.util.Random;

public class CrestEditorScreen extends Screen {

    private static final ResourceLocation SHEET0 = new ResourceLocation(CraftHeraldry.MODID, "textures/icons/icon_sheet_0.png");
    private static final ResourceLocation SHEET1 = new ResourceLocation(CraftHeraldry.MODID, "textures/icons/icon_sheet_1.png");

    private EditBox color1Box;
    private EditBox color2Box;
    
    private RGBSlider c1R, c1G, c1B;
    private RGBSlider c2R, c2G, c2B;
    private boolean syncing = false;
private int icon = -1;
    private int page = 0;

    private CrestData working = CrestData.defaultBlank();

    public CrestEditorScreen(CrestData initial) {
        super(Component.translatable("screen.craftheraldry.crest_editor"));
        if (initial != null) {
            this.working = initial.copy();
            this.icon = initial.icon();
        }
    }

    @Override
    protected void init() {
        Layout l = layout();

        // --- Hex inputs ---
        color1Box = new EditBox(this.font, l.cx - 110, l.colorY, 90, 20, Component.empty());
        color2Box = new EditBox(this.font, l.cx + 20,  l.colorY, 90, 20, Component.empty());
        color1Box.setMaxLength(7); // allow optional leading '#'
        color2Box.setMaxLength(7);

        color1Box.setValue(hex(working.color1()));
        color2Box.setValue(hex(working.color2()));

        color1Box.setResponder(s -> onHexChanged(true, s));
        color2Box.setResponder(s -> onHexChanged(false, s));

        addRenderableWidget(color1Box);
        addRenderableWidget(color2Box);

        // --- RGB sliders (scale each channel 0..255) ---
        int sliderW = 200;
        int sliderH = 14;
        int sliderGap = 2;

        int leftX  = l.cx - sliderW - 6;
        int rightX = l.cx + 6;

        c1R = addRenderableWidget(new RGBSlider(leftX,  l.sliderY + (sliderH + sliderGap) * 0, sliderW, sliderH, "R", () -> getChannel(working.color1(), 16), v -> setChannel(true, 16, v)));
        c1G = addRenderableWidget(new RGBSlider(leftX,  l.sliderY + (sliderH + sliderGap) * 1, sliderW, sliderH, "G", () -> getChannel(working.color1(), 8),  v -> setChannel(true, 8,  v)));
        c1B = addRenderableWidget(new RGBSlider(leftX,  l.sliderY + (sliderH + sliderGap) * 2, sliderW, sliderH, "B", () -> getChannel(working.color1(), 0),  v -> setChannel(true, 0,  v)));

        c2R = addRenderableWidget(new RGBSlider(rightX, l.sliderY + (sliderH + sliderGap) * 0, sliderW, sliderH, "R", () -> getChannel(working.color2(), 16), v -> setChannel(false, 16, v)));
        c2G = addRenderableWidget(new RGBSlider(rightX, l.sliderY + (sliderH + sliderGap) * 1, sliderW, sliderH, "G", () -> getChannel(working.color2(), 8),  v -> setChannel(false, 8,  v)));
        c2B = addRenderableWidget(new RGBSlider(rightX, l.sliderY + (sliderH + sliderGap) * 2, sliderW, sliderH, "B", () -> getChannel(working.color2(), 0),  v -> setChannel(false, 0,  v)));

        // Ensure slider positions match the current hex values.
        syncSlidersFromColors();

        // --- Buttons ---
        addRenderableWidget(Button.builder(Component.translatable("screen.craftheraldry.random"), b -> {
            Random r = new Random();
            working = working.withColors(r.nextInt(0x1000000), r.nextInt(0x1000000));
            syncing = true;
            color1Box.setValue(hex(working.color1()));
            color2Box.setValue(hex(working.color2()));
            syncing = false;
            syncSlidersFromColors();
        }).bounds(l.cx - 110, l.buttonsY, 90, 20).build());

        addRenderableWidget(Button.builder(Component.literal("<"), b -> page = Math.max(0, page - 1))
            .bounds(l.cx + 20, l.buttonsY, 20, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), b -> page = Math.min(31, page + 1))
            .bounds(l.cx + 90, l.buttonsY, 20, 20).build());

        int saveY = Math.min(this.height - 30, l.gridY + l.gridH + 10);
        addRenderableWidget(Button.builder(Component.translatable("screen.craftheraldry.save"), b -> {
            int c1 = parseColor(color1Box.getValue(), working.color1());
            int c2 = parseColor(color2Box.getValue(), working.color2());
            CrestData updated = new CrestData(c1, c2, (short) icon);
            ModNetwork.CHANNEL.sendToServer(new ScrollUpdatePacket(updated));
            this.onClose();
        }).bounds(l.cx - 45, saveY, 90, 20).build());
    }


    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gg);

        Layout l = layout();

        gg.drawCenteredString(this.font, Component.translatable("Heraldry Crest Editor"), l.cx, l.top, 0xFFFFFF);

        gg.drawString(this.font, Component.translatable("Background Color"), l.cx - 110, l.colorY - 10, 0xFFFFFF);
        gg.drawString(this.font, Component.translatable("Crest Color"), l.cx + 20,  l.colorY - 10, 0xFFFFFF);

        gg.drawString(this.font, Component.translatable("screen.craftheraldry.page").append(": " + (page + 1)), l.cx + 45, l.buttonsY + 6, 0xFFFFFF);

        // Icon grid
        int iconsPerPage = l.cols * l.rows;
        int startIcon = page * iconsPerPage;

        for (int i = 0; i < iconsPerPage; i++) {
            int ic = startIcon + i;
            if (ic >= 2048) break;

            int col = i % l.cols;
            int row = i / l.cols;

            int x = l.gridX + col * (l.cell + l.gap);
            int y = l.gridY + row * (l.cell + l.gap);

            drawIcon(gg, SHEET0, x, y, ic, working.color1(), l.cell);
            drawIcon(gg, SHEET1, x, y, ic, working.color2(), l.cell);

            if (ic == icon) {
                gg.fill(x - 1, y - 1, x + l.cell + 1, y, 0xFFFFFFFF);
                gg.fill(x - 1, y + l.cell, x + l.cell + 1, y + l.cell + 1, 0xFFFFFFFF);
                gg.fill(x - 1, y, x, y + l.cell, 0xFFFFFFFF);
                gg.fill(x + l.cell, y, x + l.cell + 1, y + l.cell, 0xFFFFFFFF);
            }
        }

        super.render(gg, mouseX, mouseY, partialTick);
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;

        Layout l = layout();

        int iconsPerPage = l.cols * l.rows;
        int startIcon = page * iconsPerPage;

        if (mouseX >= l.gridX && mouseX < l.gridX + l.gridW &&
            mouseY >= l.gridY && mouseY < l.gridY + l.gridH) {

            int relX = (int) mouseX - l.gridX;
            int relY = (int) mouseY - l.gridY;

            int col = relX / (l.cell + l.gap);
            int row = relY / (l.cell + l.gap);

            if (col >= 0 && col < l.cols && row >= 0 && row < l.rows) {
                int i = row * l.cols + col;
                int ic = startIcon + i;
                if (ic >= 0 && ic < 2048) {
                    icon = ic;
                    return true;
                }
            }
        }

        return false;
    }


    private static void drawIcon(GuiGraphics gg, ResourceLocation tex, int x, int y, int iconIndex, int color, int size) {
        int col = iconIndex % 32;
        int row = iconIndex / 32;
        int u = col * 64;
        int v = row * 64;

        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        gg.setColor(r, g, b, 1f);
        gg.blit(tex, x, y, size, size, u, v, 64, 64, 2048, 4096);
        gg.setColor(1f, 1f, 1f, 1f);
    }

    
    // ---------- Layout ----------
    private Layout layout() {
        int cx = this.width / 2;

        int cols = 8, rows = 6;
        int cell = 20; // smaller so the whole UI fits on typical screens
        int gap = 2;

        int gridW = cols * cell + (cols - 1) * gap;
        int gridH = rows * cell + (rows - 1) * gap;

        int sliderH = 14, sliderGap = 2;
        int sliderBlockH = 3 * sliderH + 2 * sliderGap;

        // Estimate total height needed and center vertically if possible.
        int estimated = 12 /*title*/ + 18 /*spacer*/ + 20 /*hex*/ + 4 + sliderBlockH + 6 + 20 /*buttons*/ + 8 + gridH + 34 /*save + padding*/;
        int top = Math.max(4, (this.height - estimated) / 2);

        int colorY = top + 18;
        int sliderY = colorY + 24;
        int buttonsY = sliderY + sliderBlockH + 6;
        int gridY = buttonsY + 28;

        int gridX = cx - (gridW / 2);

        return new Layout(cx, top, colorY, sliderY, buttonsY, gridX, gridY, cols, rows, cell, gap, gridW, gridH);
    }

    private static final class Layout {
        final int cx, top;
        final int colorY, sliderY, buttonsY;
        final int gridX, gridY;
        final int cols, rows, cell, gap;
        final int gridW, gridH;

        Layout(int cx, int top, int colorY, int sliderY, int buttonsY,
               int gridX, int gridY, int cols, int rows, int cell, int gap, int gridW, int gridH) {
            this.cx = cx;
            this.top = top;
            this.colorY = colorY;
            this.sliderY = sliderY;
            this.buttonsY = buttonsY;
            this.gridX = gridX;
            this.gridY = gridY;
            this.cols = cols;
            this.rows = rows;
            this.cell = cell;
            this.gap = gap;
            this.gridW = gridW;
            this.gridH = gridH;
        }
    }

    // ---------- Color helpers ----------
    private static int getChannel(int color, int shift) {
        return (color >> shift) & 0xFF;
    }

    private void setChannel(boolean color1, int shift, int value) {
        value = Math.max(0, Math.min(255, value));

        int c1 = parseColor(color1Box.getValue(), working.color1());
        int c2 = parseColor(color2Box.getValue(), working.color2());

        if (color1) {
            c1 = (c1 & ~(0xFF << shift)) | ((value & 0xFF) << shift);
        } else {
            c2 = (c2 & ~(0xFF << shift)) | ((value & 0xFF) << shift);
        }

        working = working.withColors(c1, c2);

        // Push updated hex text without feeding back into responders
        syncing = true;
        color1Box.setValue(hex(working.color1()));
        color2Box.setValue(hex(working.color2()));
        syncing = false;

        syncSlidersFromColors();
    }

    private void onHexChanged(boolean isColor1, String text) {
        if (syncing) return;

        int c1 = parseColor(color1Box.getValue(), working.color1());
        int c2 = parseColor(color2Box.getValue(), working.color2());

        // Update working colors so preview updates even before saving.
        working = working.withColors(c1, c2);

        // If the user entered a valid full hex, sync sliders.
        String t = text.trim();
        if (t.startsWith("#")) t = t.substring(1);
        if (t.length() == 6) {
            syncSlidersFromColors();
        }
    }

    private void syncSlidersFromColors() {
        if (c1R == null) return;

        syncing = true;
        c1R.setFromInt(getChannel(working.color1(), 16));
        c1G.setFromInt(getChannel(working.color1(), 8));
        c1B.setFromInt(getChannel(working.color1(), 0));

        c2R.setFromInt(getChannel(working.color2(), 16));
        c2G.setFromInt(getChannel(working.color2(), 8));
        c2B.setFromInt(getChannel(working.color2(), 0));
        syncing = false;
    }

    // ---------- Slider ----------
    private final class RGBSlider extends net.minecraft.client.gui.components.AbstractSliderButton {
        private final String channelLabel;
        private final java.util.function.IntSupplier getter;
        private final java.util.function.IntConsumer setter;

        RGBSlider(int x, int y, int w, int h, String channelLabel,
                  java.util.function.IntSupplier getter,
                  java.util.function.IntConsumer setter) {
            super(x, y, w, h, Component.empty(), 0.0D);
            this.channelLabel = channelLabel;
            this.getter = getter;
            this.setter = setter;
            setFromInt(getter.getAsInt());
            this.updateMessage();
        }

        void setFromInt(int value255) {
            value255 = Math.max(0, Math.min(255, value255));
            this.value = value255 / 255.0D;
            this.updateMessage();
        }

        private int asInt() {
            return (int) Math.round(this.value * 255.0D);
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal(channelLabel + ": " + asInt()));
        }

        @Override
        protected void applyValue() {
            if (syncing) return;
            setter.accept(asInt());
        }
    }

private static int parseColor(String text, int fallback) {
        try {
            String t = text.trim().toLowerCase(Locale.ROOT);
            if (t.startsWith("#")) t = t.substring(1);
            if (t.length() != 6) return fallback;
            return Integer.parseInt(t, 16) & 0xFFFFFF;
        } catch (Exception e) {
            return fallback;
        }
    }

    private static String hex(int color) {
        return String.format("%06X", color & 0xFFFFFF);
    }
}
