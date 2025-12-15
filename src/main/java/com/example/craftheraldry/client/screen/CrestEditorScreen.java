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
        int cx = this.width / 2;
        int top = 20;

        color1Box = new EditBox(this.font, cx - 110, top + 20, 90, 20, Component.empty());
        color2Box = new EditBox(this.font, cx + 20, top + 20, 90, 20, Component.empty());
        color1Box.setValue(hex(working.color1()));
        color2Box.setValue(hex(working.color2()));
        color1Box.setMaxLength(6);
        color2Box.setMaxLength(6);

        addRenderableWidget(color1Box);
        addRenderableWidget(color2Box);

        addRenderableWidget(Button.builder(Component.translatable("screen.craftheraldry.random"), b -> {
            Random r = new Random();
            working = working.withColors(r.nextInt(0x1000000), r.nextInt(0x1000000));
            color1Box.setValue(hex(working.color1()));
            color2Box.setValue(hex(working.color2()));
        }).bounds(cx - 110, top + 50, 90, 20).build());

        addRenderableWidget(Button.builder(Component.literal("<"), b -> page = Math.max(0, page - 1))
            .bounds(cx + 20, top + 50, 20, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), b -> page = Math.min(31, page + 1))
            .bounds(cx + 90, top + 50, 20, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("screen.craftheraldry.save"), b -> {
            int c1 = parseColor(color1Box.getValue(), working.color1());
            int c2 = parseColor(color2Box.getValue(), working.color2());
            CrestData updated = new CrestData(c1, c2, (short) icon);
            ModNetwork.CHANNEL.sendToServer(new ScrollUpdatePacket(updated));
            this.onClose();
        }).bounds(cx - 45, this.height - 30, 90, 20).build());
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gg);

        int cx = this.width / 2;
        int top = 20;

        gg.drawCenteredString(this.font, this.title, cx, top, 0xFFFFFF);
        gg.drawString(this.font, Component.literal("Color 1 (hex)"), cx - 110, top + 8, 0xAAAAAA);
        gg.drawString(this.font, Component.literal("Color 2 (hex)"), cx + 20, top + 8, 0xAAAAAA);

        super.render(gg, mouseX, mouseY, partialTick);

        int gridX = cx - 128;
        int gridY = top + 80;
        int cols = 8, rows = 6;
        int cell = 24;
        int iconsPerPage = cols * rows;
        int startIcon = page * iconsPerPage;

        gg.drawString(this.font, Component.translatable("screen.craftheraldry.page").append(": " + (page + 1)), cx + 45, top + 56, 0xFFFFFF);

        for (int i = 0; i < iconsPerPage; i++) {
            int ic = startIcon + i;
            if (ic >= 2048) break;
            int col = i % cols;
            int row = i / cols;
            int x = gridX + col * (cell + 2);
            int y = gridY + row * (cell + 2);

            drawIcon(gg, SHEET0, x, y, ic, working.color1(), cell);
            drawIcon(gg, SHEET1, x, y, ic, working.color2(), cell);

            if (ic == icon) {
                gg.fill(x - 1, y - 1, x + cell + 1, y, 0xFFFFFFFF);
                gg.fill(x - 1, y + cell, x + cell + 1, y + cell + 1, 0xFFFFFFFF);
                gg.fill(x - 1, y, x, y + cell, 0xFFFFFFFF);
                gg.fill(x + cell, y, x + cell + 1, y + cell, 0xFFFFFFFF);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;

        int cx = this.width / 2;
        int top = 20;
        int gridX = cx - 128;
        int gridY = top + 80;
        int cols = 8, rows = 6;
        int cell = 24;
        int iconsPerPage = cols * rows;
        int startIcon = page * iconsPerPage;

        if (mouseX >= gridX && mouseY >= gridY && mouseX < gridX + cols * (cell + 2) && mouseY < gridY + rows * (cell + 2)) {
            int relX = (int) (mouseX - gridX);
            int relY = (int) (mouseY - gridY);
            int c = relX / (cell + 2);
            int r = relY / (cell + 2);
            int idx = r * cols + c;
            int ic = startIcon + idx;
            if (ic < 2048) {
                icon = ic;
                working = working.withIcon((short) ic);
                return true;
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
