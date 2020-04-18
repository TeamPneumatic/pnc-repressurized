package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.network.SyncedField;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammableController;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerProgrammableController extends ContainerPneumaticBase<TileEntityProgrammableController> {

    public ContainerProgrammableController(InventoryPlayer inventoryPlayer, final TileEntityProgrammableController te) {
        super(te);

        addSlotToContainer(new SlotItemHandler(te.getPrimaryInventory(), 0, 89, 36));

        addUpgradeSlots(39, 29);

        addPlayerSlots(inventoryPlayer, 84);

        try {
            IEnergyStorage energyStorage = te.getCapability(CapabilityEnergy.ENERGY, null);
            addSyncedField(new SyncedField.SyncedInt(energyStorage, EnergyStorage.class.getDeclaredField("energy")));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
