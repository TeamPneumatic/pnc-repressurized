package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.recipes.AmadronOffer.TradeType;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class ContainerAmadronAddTrade extends ContainerPneumaticBase<TileEntityBase> {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;

    private final ItemStackHandler inv = new ItemStackHandler(2);
    private final TradeType tradeType;

    ContainerAmadronAddTrade(int windowId, PlayerInventory playerInventory, TradeType tradeType) {
        super(ModContainerTypes.AMADRON_ADD_TRADE, windowId, playerInventory);

        this.tradeType = tradeType;
        addSlot(new SlotUntouchable(inv, INPUT_SLOT, 10, 90));
        addSlot(new SlotUntouchable(inv, OUTPUT_SLOT, 99, 90));
    }

    public ContainerAmadronAddTrade(int windowId, PlayerInventory invPlayer, PacketBuffer extraData) {
        this(windowId, invPlayer, TradeType.values()[extraData.readByte()]);
    }

    public void setStack(int index, @Nonnull ItemStack stack) {
        inv.setStackInSlot(index, stack);
    }

    @Nonnull
    public ItemStack getStack(int index) {
        return inv.getStackInSlot(index);
    }
    @Nonnull
    public ItemStack getInputStack() {
        return inv.getStackInSlot(INPUT_SLOT);
    }
    @Nonnull
    public ItemStack getOutputStack() {
        return inv.getStackInSlot(OUTPUT_SLOT);
    }

    @Override
    public boolean canInteractWith(PlayerEntity player) {
        return player.getHeldItemMainhand().getItem() == ModItems.AMADRON_TABLET;
    }

    @Override
    public void putStackInSlot(int slot, @Nonnull ItemStack stack) {
    }

    public TradeType getTradeType() {
        return tradeType;
    }
}
