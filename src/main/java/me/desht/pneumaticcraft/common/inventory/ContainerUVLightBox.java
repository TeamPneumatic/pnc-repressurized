package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemEmptyPCB;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUVLightBox;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class ContainerUVLightBox extends ContainerPneumaticBase<TileEntityUVLightBox> {

    public ContainerUVLightBox(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public ContainerUVLightBox(int windowId, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainers.UV_LIGHT_BOX.get(), windowId, playerInventory, pos);

        addSlot(new SlotPCB(te.getPrimaryInventory(), 0, 11, 22));
        addSlot(new SlotOutput(te.getOutputInventory(), 0, 49, 22));

        // add upgrade slots
        for (int i = 0; i < 4; i++) {
            addSlot(new SlotUpgrade(te, i, 98 + i * 18, 90));
        }

        addPlayerSlots(playerInventory, 114);
    }

    private class SlotPCB extends SlotItemHandler {
        SlotPCB(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean isItemValid(@Nonnull ItemStack stack) {
            return stack.getItem() == ModItems.EMPTY_PCB.get() && ItemEmptyPCB.getEtchProgress(stack) == 0;
        }

        @Override
        public int getSlotStackLimit() {
            return 1;
        }
    }
}
