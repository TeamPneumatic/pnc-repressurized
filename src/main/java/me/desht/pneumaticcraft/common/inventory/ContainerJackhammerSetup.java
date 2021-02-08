package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemDrillBit;
import me.desht.pneumaticcraft.common.item.ItemJackHammer;
import me.desht.pneumaticcraft.common.item.ItemJackHammer.DigMode;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class ContainerJackhammerSetup extends ContainerPneumaticBase<TileEntityBase> {
    private final ItemJackHammer.DrillBitHandler drillBitHandler;
    private final ItemJackHammer.EnchantmentHandler enchantmentHandler;
    private final Hand hand;

    public ContainerJackhammerSetup(int windowId, PlayerInventory invPlayer, PacketBuffer buffer) {
        this(windowId, invPlayer, getHand(buffer));
    }

    public ContainerJackhammerSetup(int windowId, PlayerInventory invPlayer, Hand hand) {
        super(ModContainers.JACKHAMMER_SETUP.get(), windowId, invPlayer);
        this.hand = hand;

        drillBitHandler = ItemJackHammer.getDrillBitHandler(invPlayer.player.getHeldItem(hand));
        if (drillBitHandler != null) {
            addSlot(new SlotDrillBit(drillBitHandler, 0, 128, 19));
        }

        enchantmentHandler = ItemJackHammer.getEnchantmentHandler(invPlayer.player.getHeldItem(hand));
        if (enchantmentHandler != null) {
            addSlot(new SlotEnchantmentHandler(enchantmentHandler, 0, 96, 19));
        }

        addPlayerSlots(invPlayer, 100);
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn) {
        super.onContainerClosed(playerIn);

        drillBitHandler.save();
        enchantmentHandler.save();
    }

    @Override
    public boolean canInteractWith(PlayerEntity player) {
        return player.getHeldItem(hand).getItem() == ModItems.JACKHAMMER.get();
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayerEntity player) {
        ItemStack hammerStack = player.getHeldItem(hand);
        if (tag.startsWith("digmode:") && hammerStack.getItem() instanceof ItemJackHammer) {
            try {
                ItemDrillBit.DrillBitType bitType = ((ItemJackHammer) hammerStack.getItem()).getDrillBit(hammerStack);
                DigMode dt = DigMode.valueOf(tag.substring(8));
                if (dt.getBitType().getTier() <= bitType.getTier() || dt == DigMode.MODE_1X1) {
                    ItemJackHammer.setDigMode(hammerStack, dt);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public Hand getHand() {
        return hand;
    }

    private static class SlotDrillBit extends SlotItemHandler {
        public SlotDrillBit(ItemJackHammer.DrillBitHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean isItemValid(@Nonnull ItemStack stack) {
            return stack.getItem() instanceof ItemDrillBit;
        }

        @Override
        public void onSlotChanged() {
            ((ItemJackHammer.DrillBitHandler) getItemHandler()).save();
        }

        @Override
        public void onSlotChange(@Nonnull ItemStack oldStackIn, @Nonnull ItemStack newStackIn) {
            ((ItemJackHammer.DrillBitHandler) getItemHandler()).save();
        }
    }

    private static class SlotEnchantmentHandler extends SlotItemHandler {
        public SlotEnchantmentHandler(ItemJackHammer.EnchantmentHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean isItemValid(@Nonnull ItemStack stack) {
            return ItemJackHammer.EnchantmentHandler.validateBook(stack);
        }

        @Override
        public void onSlotChange(@Nonnull ItemStack oldStackIn, @Nonnull ItemStack newStackIn) {
            ((ItemJackHammer.EnchantmentHandler) getItemHandler()).save();
        }

        @Override
        public void onSlotChanged() {
            ((ItemJackHammer.EnchantmentHandler) getItemHandler()).save();
        }
    }
}
