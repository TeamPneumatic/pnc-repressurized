package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticBase;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

public class ContainerEnergy extends Container4UpgradeSlots<TileEntityPneumaticBase> {

    public ContainerEnergy(InventoryPlayer inventoryPlayer, TileEntityPneumaticBase te) {
        super(inventoryPlayer, te);
        if (!te.hasCapability(CapabilityEnergy.ENERGY, null)) {
            throw new IllegalStateException("tile entity must support CapabilityEnergy.ENERGY on face null!");
        }
        try {
            IEnergyStorage energyStorage = te.getCapability(CapabilityEnergy.ENERGY, null);
            addSyncedField(new SyncedField.SyncedInt(energyStorage, EnergyStorage.class.getDeclaredField("energy")));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
