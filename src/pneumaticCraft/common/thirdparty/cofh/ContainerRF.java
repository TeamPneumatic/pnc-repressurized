package pneumaticCraft.common.thirdparty.cofh;

import java.util.List;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import pneumaticCraft.common.inventory.Container4UpgradeSlots;
import pneumaticCraft.common.tileentity.TileEntityPneumaticBase;
import cofh.api.tileentity.IEnergyInfo;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ContainerRF extends Container4UpgradeSlots<TileEntityPneumaticBase>{
    public int energy = -1;
    public final IEnergyInfo energyHandler;

    public ContainerRF(InventoryPlayer inventoryPlayer, TileEntityPneumaticBase te){
        super(inventoryPlayer, te);
        energyHandler = (IEnergyInfo)te;
    }

    @Override
    public void detectAndSendChanges(){
        super.detectAndSendChanges();
        if(energy != energyHandler.getInfoEnergyStored()) {
            energy = energyHandler.getInfoEnergyStored();
            for(ICrafting crafter : (List<ICrafting>)crafters) {
                crafter.sendProgressBarUpdate(this, 0, energy);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int value){
        super.updateProgressBar(id, value);
        switch(id){
            case 0:
                energy = value;
                break;
        }
    }
}
