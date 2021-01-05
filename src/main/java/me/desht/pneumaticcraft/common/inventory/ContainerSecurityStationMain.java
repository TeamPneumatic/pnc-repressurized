package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.client.gui.GuiSecurityStationInventory;
import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.item.ItemNetworkComponent;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;

import java.util.function.Predicate;

public class ContainerSecurityStationMain extends ContainerPneumaticBase<TileEntitySecurityStation> {

    public ContainerSecurityStationMain(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public ContainerSecurityStationMain(int windowId, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainers.SECURITY_STATION_MAIN.get(), windowId, playerInventory, pos);

        //add the network slots
        for (int i = 0; i < TileEntitySecurityStation.INV_ROWS; i++) {
            for (int j = 0; j < TileEntitySecurityStation.INV_COLS; j++) {
                addSlot(new SlotSecurityNode(te.getPrimaryInventory(), stack -> stack.getItem() instanceof ItemNetworkComponent, j + i * 5, 17 + j * 18, 19 + i * 18));
            }
        }

        addUpgradeSlots(123, 59);

        addPlayerSlots(playerInventory, 157);
    }

    private class SlotSecurityNode extends SlotItemSpecific {
        SlotSecurityNode(IItemHandler handler, Predicate<ItemStack> itemAllowed, int index, int x, int y) {
            super(handler, itemAllowed, index, x, y);
        }

        @Override
        public void onSlotChanged() {
            super.onSlotChanged();

            if (te.getWorld().isRemote) GuiSecurityStationInventory.reinitConnectionRendering();
        }
    }
}
