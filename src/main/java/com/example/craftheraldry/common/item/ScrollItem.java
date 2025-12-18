package com.example.craftheraldry.common.item;

import com.example.craftheraldry.client.screen.CrestEditorScreen;
import com.example.craftheraldry.common.util.CrestData;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ScrollItem extends Item {

    public static final String TAG_CREST = "crest";

    public ScrollItem(Properties props) {
        super(props);
    }

    public static CrestData getCrest(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_CREST)) {
            return CrestData.fromTag(tag.getCompound(TAG_CREST));
        }
        return CrestData.defaultBlank();
    }

    public static void setCrest(ItemStack stack, CrestData crest) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.put(TAG_CREST, crest.toTag());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                mountCap(player, hand, stack);
            }
            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
        }
        if (level.isClientSide) openEditor(stack);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private static void mountCap(Player player, InteractionHand hand, ItemStack stack) {
        ItemStack previousHead = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack capStack = stack.split(1);

        CrestData crest = getCrest(capStack);
        player.getPersistentData().put(CrestData.NBT_KEY, crest.toTag());

        player.setItemSlot(EquipmentSlot.HEAD, capStack);

        if (!previousHead.isEmpty()) {
            ItemStack handStack = player.getItemInHand(hand);
            if (handStack.isEmpty()) {
                player.setItemInHand(hand, previousHead);
            } else if (!player.addItem(previousHead)) {
                player.drop(previousHead, false);
            }
        }

        player.displayClientMessage(Component.translatable("message.craftheraldry.cap_equipped"), true);
    }


    @OnlyIn(Dist.CLIENT)
    private static void openEditor(ItemStack stack) {
        CrestData crest = getCrest(stack);
        Minecraft.getInstance().setScreen(new CrestEditorScreen(crest));
    }
}
