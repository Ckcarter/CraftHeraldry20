package com.example.craftheraldry.common.item;

import com.example.craftheraldry.common.util.CrestData;
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
import net.minecraftforge.fml.DistExecutor;

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

    // Shift + Right Click -> Open the crest editor (client-side).
    if (player.isShiftKeyDown()) {
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientOnly.openEditor(stack));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    // Right Click -> Toggle / mount the cosmetic cape (server-side).
    if (!level.isClientSide) {
        CrestData crest = getCrest(stack);
        // If scroll has no valid crest, treat as "remove cape"
        boolean hasValid = crest != null && crest.icon() >= 0;

        if (com.example.craftheraldry.common.util.CapeData.hasCape(player)) {
            com.example.craftheraldry.common.util.CapeData.clearCape(player);
            com.example.craftheraldry.common.network.ModNetwork.CHANNEL.send(
                    net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    new com.example.craftheraldry.common.network.CapeSyncPacket(player.getUUID(), false, null)
            );
            player.displayClientMessage(Component.translatable("message.craftheraldry.cape_removed"), true);
        } else if (hasValid) {
            com.example.craftheraldry.common.util.CapeData.setCape(player, crest);
            com.example.craftheraldry.common.network.ModNetwork.CHANNEL.send(
                    net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    new com.example.craftheraldry.common.network.CapeSyncPacket(player.getUUID(), crest)
            );
            player.displayClientMessage(Component.translatable("message.craftheraldry.cape_equipped"), true);
        } else {
            player.displayClientMessage(Component.translatable("message.craftheraldry.cape_no_crest"), true);
        }
    }

    return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
}

    /**
     * Client-only code lives in this nested class so dedicated servers never try to load
     * client classes like Minecraft / screens.
     */
    @OnlyIn(Dist.CLIENT)
    private static final class ClientOnly {
        private static void openEditor(ItemStack stack) {
            CrestData crest = getCrest(stack);
            net.minecraft.client.Minecraft.getInstance().setScreen(
                    new com.example.craftheraldry.client.screen.CrestEditorScreen(crest)
            );
        }
    }
}

