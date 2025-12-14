package com.example.craftheraldry.client;

import com.example.craftheraldry.ClientIcons;
import com.example.craftheraldry.CrestData;
import com.example.craftheraldry.ItemCrestUtil;
import com.example.craftheraldry.Network;
import com.example.craftheraldry.PacketChangeCrest;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class CrestCreatorScreen extends Screen {

    private CrestIconList list;
    public CrestData currentCrest = CrestData.defaultCrest();

    private final List<Integer> viewableCrests = new ArrayList<>();

    private ColorSlider[] bgSliders;
    private ColorSlider[] fgSliders;

    private EditBox searchField;
    private final ItemStack editingStack;

    public CrestCreatorScreen(ItemStack stack, net.minecraft.core.BlockPos editingPos) {
        super(Component.literal("Heraldic Editor"));
        this.editingStack = stack == null ? ItemStack.EMPTY : stack.copy();
        this.editingPos = editingPos;

        if (!this.editingStack.isEmpty()) {
            CrestData temp = ItemCrestUtil.readCrest(this.editingStack);
            if (temp != null) {
                currentCrest = temp;
                if (currentCrest.icon < 0) {
                    currentCrest.icon = 0;
                    currentCrest.backgroundColor = 0x000000;
                }
            }
        }
    }

    @Override
    protected void init() {
        ClientIcons.loadIfNeeded();

        if (this.minecraft != null && this.minecraft.player != null) {
            var p = this.minecraft.player;
            if (ItemStack.isSameItemSameTags(p.getOffhandItem(), this.editingStack)) this.editingHand = net.minecraft.world.InteractionHand.OFF_HAND;
            else this.editingHand = net.minecraft.world.InteractionHand.MAIN_HAND;
        }

        this.list = new CrestIconList(this.minecraft, this, 250, this.height, 32, this.height - 32, 36);

        // Background (like 1.7.10 "color1")
        bgSliders = new ColorSlider[] {
                new ColorSlider(0, 258, 90, 100, 20, Component.literal("Red: "), Channel.RED, true),
                new ColorSlider(1, 258, 112, 100, 20, Component.literal("Green: "), Channel.GREEN, true),
                new ColorSlider(2, 258, 134, 100, 20, Component.literal("Blue: "), Channel.BLUE, true)
        };

        // Foreground (like 1.7.10 "color2")
        fgSliders = new ColorSlider[] {
                new ColorSlider(3, 258, 200, 100, 20, Component.literal("Red: "), Channel.RED, false),
                new ColorSlider(4, 258, 222, 100, 20, Component.literal("Green: "), Channel.GREEN, false),
                new ColorSlider(5, 258, 244, 100, 20, Component.literal("Blue: "), Channel.BLUE, false)
        };

        for (var s : bgSliders) addRenderableWidget(s);
        for (var s : fgSliders) addRenderableWidget(s);

        addRenderableWidget(Button.builder(Component.literal("Random"), b -> randomAll())
                .pos(15, this.height - 27).size(60, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> done())
                .pos(this.width / 2 - 80, this.height - 27).size(200, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Swap"), b -> swap())
                .pos(this.width - 159, this.height / 2 + 20).size(60, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Invert"), b -> invert())
                .pos(this.width - 96, this.height / 2 + 20).size(60, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Random Colors"), b -> randomColors())
                .pos(258, 270).size(100, 20).build());

        searchField = new EditBox(this.font, 85, this.height - 27, 140, 20, Component.literal("Search"));
        searchField.setFocused(true);
        searchField.setCanLoseFocus(false);
        searchField.setResponder(s -> filterViewableCrests());
        addRenderableWidget(searchField);

        filterViewableCrests();
        updateSlidersFromCrest();
    }

    private void done() {
        // Send to server
        if (editingPos != null) {
            Network.CHANNEL.sendToServer(new com.example.craftheraldry.PacketChangeCrestBlock(currentCrest, editingPos));
        } else {
            Network.CHANNEL.sendToServer(new com.example.craftheraldry.PacketChangeCrestItem(currentCrest, editingHand));
        }

        // Optionally write to the edited stack client-side too
        if (!editingStack.isEmpty()) {
            ItemCrestUtil.writeCrest(editingStack, currentCrest);
        }

        this.minecraft.setScreen(null);
    }

    private void randomAll() {
        Random rand = new Random();
        currentCrest.backgroundColor = rand.nextInt(0x1000000);
        currentCrest.foregroundColor = rand.nextInt(0x1000000);
        currentCrest.icon = rand.nextInt(ClientIcons.iconCount());
        updateSlidersFromCrest();
        rebuildList();
    }

    private void randomColors() {
        Random rand = new Random();
        currentCrest.backgroundColor = rand.nextInt(0x1000000);
        currentCrest.foregroundColor = rand.nextInt(0x1000000);
        updateSlidersFromCrest();
    }

    private void swap() {
        int a = currentCrest.backgroundColor;
        currentCrest.backgroundColor = currentCrest.foregroundColor;
        currentCrest.foregroundColor = a;
        updateSlidersFromCrest();
    }

    private void invert() {
        currentCrest.backgroundColor = 0xFFFFFF - (currentCrest.backgroundColor & 0xFFFFFF);
        currentCrest.foregroundColor = 0xFFFFFF - (currentCrest.foregroundColor & 0xFFFFFF);
        updateSlidersFromCrest();
    }

    @Override
    public void tick() {
        super.tick();
        searchField.tick();

        // Pull slider values into current crest each tick (similar to updateScreen in 1.7.10)
        currentCrest.backgroundColor = rgbFromSliders(bgSliders);
        currentCrest.foregroundColor = rgbFromSliders(fgSliders);
    }

    private int rgbFromSliders(ColorSlider[] sliders) {
        int r = (int) (sliders[0].value * 255.0);
        int g = (int) (sliders[1].value * 255.0);
        int b = (int) (sliders[2].value * 255.0);
        r = Mth.clamp(r, 0, 255);
        g = Mth.clamp(g, 0, 255);
        b = Mth.clamp(b, 0, 255);
        return (r << 16) | (g << 8) | b;
    }

    private void updateSlidersFromCrest() {
        setSlidersFromRgb(bgSliders, currentCrest.backgroundColor);
        setSlidersFromRgb(fgSliders, currentCrest.foregroundColor);
    }

    private void setSlidersFromRgb(ColorSlider[] sliders, int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        sliders[0].setValue(r / 255.0);
        sliders[1].setValue(g / 255.0);
        sliders[2].setValue(b / 255.0);
    }

    private void filterViewableCrests() {
        viewableCrests.clear();
        String q = searchField.getValue() == null ? "" : searchField.getValue().toLowerCase(Locale.ROOT);

        for (int i = 0; i < ClientIcons.iconCount(); i++) {
            if (ClientIcons.ICON_NAMES.get(i).toLowerCase(Locale.ROOT).contains(q)) {
                viewableCrests.add(i);
            }
        }
        rebuildList();
    }

    private void rebuildList() {
        if (list == null) return;
        list.rebuild(viewableCrests);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Let EditBox handle typing first
        if (searchField.keyPressed(keyCode, scanCode, modifiers) || searchField.canConsumeInput()) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        renderBackground(gfx);

        list.render(gfx, mouseX, mouseY, partialTick);

        // Title and preview panel
        gfx.drawCenteredString(this.font, "Heraldic Editor", this.width / 2, 8, 0xFFFFFF);

        String iconName = "Unknown";
        if (currentCrest.icon >= 0 && currentCrest.icon < ClientIcons.iconCount()) {
            iconName = ClientIcons.nameOrFallback(currentCrest.icon);
        }
        gfx.drawString(this.font, "Current Icon: " + iconName + " (#" + (currentCrest.icon + 1) + ")", 8, 32, 0xFFFFFF, true);

        gfx.drawString(this.font, "Preview:", (this.width - 136) / 2, 48, 0xFFFFFF, true);

        HeraldryRender.renderCrestInGui(gfx, currentCrest, px, py, 64);

        gfx.drawString(this.font, "Background Color", 265, 76, 0xFFFFFF, true);
        gfx.drawString(this.font, "Foreground Color", 265, 186, 0xFFFFFF, true);

        super.render(gfx, mouseX, mouseY, partialTick);
    }


public int getCurrentIconIndex() {
    return currentCrest.icon;
}

public void setCurrentIconIndex(int idx) {
    currentCrest.icon = Mth.clamp(idx, 0, ClientIcons.iconCount() - 1);
}

    // -------------------- Sliders --------------------

    private enum Channel { RED, GREEN, BLUE }

    private class ColorSlider extends AbstractSliderButton {
        private final Channel channel;
        private final boolean background;
        private final Component labelPrefix;

        public ColorSlider(int id, int x, int y, int width, int height, Component labelPrefix, Channel channel, boolean background) {
            super(x, y, width, height, Component.empty(), 0.0);
            this.labelPrefix = labelPrefix;
            this.channel = channel;
            this.background = background;
            updateMessage();
        }

        public void setValue(double v) {
            this.value = Mth.clamp(v, 0.0, 1.0);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            int pct = (int) Math.round(this.value * 255.0);
            this.setMessage(Component.literal(labelPrefix.getString() + pct));
        }

        @Override
        protected void applyValue() {
            // Value is read in tick() from slider.value; nothing needed here.
        }
    }
}
