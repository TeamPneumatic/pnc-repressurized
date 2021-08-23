package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.item.ItemSpawnerCore;
import me.desht.pneumaticcraft.common.tileentity.TileEntityVacuumTrap;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class ContainerVacuumTrap extends ContainerPneumaticBase<TileEntityVacuumTrap> {
    public ContainerVacuumTrap(int windowId, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(windowId, playerInventory, getTilePos(buffer));
    }

    public ContainerVacuumTrap(int windowId, PlayerInventory invPlayer, BlockPos pos) {
        super(ModContainers.VACUUM_TRAP.get(), windowId, invPlayer, pos);

        addSlot(new SlotSpawnerCore(te.getPrimaryInventory(), 0, 62, 38));

        addUpgradeSlots(8, 29);

        addPlayerSlots(invPlayer, 84);
    }

    public static class SlotSpawnerCore extends SlotItemHandler {
        public SlotSpawnerCore(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(@Nonnull ItemStack stack) {
            return stack.getItem() instanceof ItemSpawnerCore;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
