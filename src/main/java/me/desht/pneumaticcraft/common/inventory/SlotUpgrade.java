package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class SlotUpgrade extends SlotItemHandler {

    private final TileEntityBase te;

    public SlotUpgrade(TileEntityBase inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn.getUpgradesInventory(), index, xPosition, yPosition);
        te = inventoryIn;
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return te.getApplicableUpgrades().contains(stack.getItem());
    }
}
