package pneumaticCraft.common.thirdparty.cofh;

import net.minecraft.entity.player.InventoryPlayer;
import pneumaticCraft.common.inventory.Container4UpgradeSlots;
import pneumaticCraft.common.inventory.SyncedField;
import pneumaticCraft.common.tileentity.TileEntityPneumaticBase;
import cofh.api.energy.EnergyStorage;
import cofh.api.tileentity.IEnergyInfo;

public class ContainerRF extends Container4UpgradeSlots<TileEntityPneumaticBase>{
    public final IEnergyInfo energyHandler;

    public ContainerRF(InventoryPlayer inventoryPlayer, TileEntityPneumaticBase te){
        super(inventoryPlayer, te);
        energyHandler = (IEnergyInfo)te;
        try {
            addSyncedField(new SyncedField.SyncedInt(((IRFConverter)te).getEnergyStorage(), EnergyStorage.class.getDeclaredField("energy")));
        } catch(Throwable e) {
            e.printStackTrace();
        }
    }
}
