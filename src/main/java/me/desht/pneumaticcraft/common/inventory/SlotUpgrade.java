package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import me.desht.pneumaticcraft.common.util.upgrade.ApplicableUpgradesDB;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class SlotUpgrade extends SlotItemHandler {
    private final TileEntityBase te;

    SlotUpgrade(TileEntityBase te, int index, int xPosition, int yPosition) {
        super(te.getUpgradeHandler(), index, xPosition, yPosition);
        this.te = te;
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return getItemHandler().isItemValid(getSlotIndex(), stack);
    }

    @Override
    public void onSlotChanged() {
        te.getUpgradeCache().invalidate();
    }

    @Override
    public int getItemStackLimit(@Nonnull ItemStack stack) {
        return ApplicableUpgradesDB.getInstance().getMaxUpgrades(te, EnumUpgrade.from(stack));
    }
}
