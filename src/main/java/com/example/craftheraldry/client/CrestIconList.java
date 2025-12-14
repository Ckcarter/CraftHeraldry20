package com.example.craftheraldry.client;

import com.example.craftheraldry.ClientIcons;
import com.example.craftheraldry.CrestData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * 1.20.1 Forge port of the old 1.7.10 GuiSlot-based crest list.
 *
 * - Uses ObjectSelectionList instead of GuiSlot
 * - Renders a small crest preview and icon name + (#)
 * - Click selects the icon index in the parent screen
 *
 * Notes:
 * - The original mod used HeraldryRender.renderCrest(...) to draw the real crest icon.
 *   This port uses placeholder swatches; wire your real renderer in renderCrestPreview().
 */
public class CrestIconList extends ObjectSelectionList<CrestIconList.Entry> {

    private final CrestCreatorScreen parent;

    public CrestIconList(Minecraft mc, CrestCreatorScreen parent, int width, int height, int y0, int y1, int itemHeight) {
        super(mc, width, height, y0, y1, itemHeight);
        this.parent = parent;
        this.setLeftPos(8);
    }

    @Override
    public int getRowWidth() {
        return 230;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.getLeft() + this.getRowWidth() + 6;
    }

    @Override
    protected void renderBackground(GuiGraphics gfx) {
        // The old GuiCrestList drew its own background only when in-world.
        // Here we keep it subtle and let the parent screen draw the main background.
        // You can draw custom panels here if you want.
    }

    @Override
    protected void renderDecorations(GuiGraphics gfx, int mouseX, int mouseY) {
        // Draw a darker strip behind the scrollbar (roughly matching the old code's right-side bar)
        int x0 = this.getScrollbarPosition();
        int x1 = x0 + 6;
        int y0 = this.y0;
        int y1 = this.y1;
        gfx.fill(x0, y0, x1, y1, 0xFF000000);
        super.renderDecorations(gfx, mouseX, mouseY);
    }

    public void rebuild(java.util.List<Integer> viewable) {
        this.clearEntries();
        for (int idx : viewable) {
            this.addEntry(new Entry(idx));
        }
    }

    public class Entry extends ObjectSelectionList.Entry<Entry> {
        private final int iconIndex;

        public Entry(int iconIndex) {
            this.iconIndex = iconIndex;
        }

        @Override
        public Component getNarration() {
            return Component.literal(ClientIcons.ICON_NAMES.get(iconIndex));
        }

        @Override
        public void render(GuiGraphics gfx, int idx, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovered, float partialTick) {

            Font font = Minecraft.getInstance().font;

            // Background highlight (selected/hovered)
            boolean selected = parent.getCurrentIconIndex() == iconIndex;
            if (selected) {
                gfx.fill(left, top, left + width, top + height, 0x55FFFFFF);
            } else if (hovered) {
                gfx.fill(left, top, left + width, top + height, 0x22FFFFFF);
            }

            // Build a temporary crest like the old drawSlot did
            CrestData crest = new CrestData(0x000000, 0xFFFFFF, iconIndex);

            // "Render crest" preview area
            int previewX = left + 4;
            int previewY = top + 4;
            renderCrestPreview(gfx, crest, previewX, previewY);

            // Icon name
            String name = ClientIcons.nameOrFallback(iconIndex);
            gfx.drawString(font, name, left + 44, top + 8, 0xFFFFFF, true);

            // (#N)
            String num = "(#" + (iconIndex + 1) + ")";
            gfx.drawString(font, num, left + 44, top + 20, 0x888888, false);
        }

        private void renderCrestPreview(GuiGraphics gfx, CrestData crest, int x, int y) {
    // Real crest render (64x64 -> scaled down)
    int size = 32;
    HeraldryRender.renderCrestInGui(gfx, crest, x, y, size);
}

@Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            parent.setCurrentIconIndex(iconIndex);
            CrestIconList.this.setSelected(this);
            return true;
        }
    }
}
