package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class ContainerChargingStation extends ContainerPneumaticBase<TileEntityChargingStation> {

    public ContainerChargingStation(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public ContainerChargingStation(int i, PlayerInventory inventoryPlayer, BlockPos pos) {
        super(ModContainers.CHARGING_STATION.get(), i, inventoryPlayer, pos);

        addSlot(new SlotItemHandler(te.getPrimaryInventory(), 0, 91, 39) {
            @Override
            public int getSlotStackLimit() {
                return 1;
            }
        });

        addUpgradeSlots(42, 29);

        addArmorSlots(inventoryPlayer, 8, 19);

        addPlayerSlots(inventoryPlayer, 95);
    }

    @Override
    @Nonnull
    public ItemStack transferStackInSlot(PlayerEntity player, int slot) {
        Slot srcSlot = inventorySlots.get(slot);
        if (srcSlot == null || !srcSlot.getHasStack()) {
            return ItemStack.EMPTY;
        }
        ItemStack srcStack = srcSlot.getStack().copy();
        ItemStack copyOfSrcStack = srcStack.copy();

        if (slot == 0 && srcStack.getItem() instanceof ArmorItem) {
            // chargeable slot - move to armor if appropriate, player inv otherwise
            if (!mergeItemStack(srcStack, 5, 9, false)
                    && !mergeItemStack(srcStack, playerSlotsStart, playerSlotsStart + 36, false))
                return ItemStack.EMPTY;
        } else if (slot >= 5 && slot < 9 && srcStack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).isPresent()) {
            // armor slots - try to move to the charging slot if possible
            if (!mergeItemStack(srcStack, 0, 1, false)
                    && !mergeItemStack(srcStack, playerSlotsStart, playerSlotsStart + 36, false))
                return ItemStack.EMPTY;
        } else if (slot < playerSlotsStart) {
            if (!mergeItemStack(srcStack, playerSlotsStart, playerSlotsStart + 36, false))
                return ItemStack.EMPTY;
        } else {
            if (!mergeItemStack(srcStack, 0, playerSlotsStart, false))
                return ItemStack.EMPTY;
        }

        srcSlot.putStack(srcStack);
        srcSlot.onSlotChange(srcStack, copyOfSrcStack);
        srcSlot.onTake(player, srcStack);

        return copyOfSrcStack;
    }
}
