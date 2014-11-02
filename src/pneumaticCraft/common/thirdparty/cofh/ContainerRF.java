package pneumaticCraft.common.thirdparty.cofh;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.common.inventory.Container4UpgradeSlots;
import pneumaticCraft.common.tileentity.IGUIButtonSensitive;
import pneumaticCraft.common.tileentity.IRedstoneControl;
import cofh.api.tileentity.IEnergyInfo;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ContainerRF extends Container4UpgradeSlots{
    public int energy = -1;
    public int redstoneMode = -1;
    public int airPerTick = -1;
    public int rfPerTick = -1;
    public final IEnergyInfo energyHandler;

    public ContainerRF(InventoryPlayer inventoryPlayer, TileEntity te){
        super(inventoryPlayer, (IInventory)te);
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
        if(redstoneMode != ((IRedstoneControl)energyHandler).getRedstoneMode()) {
            redstoneMode = ((IRedstoneControl)energyHandler).getRedstoneMode();
            for(ICrafting crafter : (List<ICrafting>)crafters) {
                crafter.sendProgressBarUpdate(this, 1, redstoneMode);
            }
        }
        if(rfPerTick != ((IRFConverter)energyHandler).getRFRate()) {
            rfPerTick = ((IRFConverter)energyHandler).getRFRate();
            for(ICrafting crafter : (List<ICrafting>)crafters) {
                crafter.sendProgressBarUpdate(this, 2, rfPerTick);
            }
        }
        if(airPerTick != ((IRFConverter)energyHandler).getAirRate()) {
            airPerTick = ((IRFConverter)energyHandler).getAirRate();
            for(ICrafting crafter : (List<ICrafting>)crafters) {
                crafter.sendProgressBarUpdate(this, 3, airPerTick);
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
            case 1:
                redstoneMode = value;
                while(((IRedstoneControl)energyHandler).getRedstoneMode() != redstoneMode) {
                    ((IGUIButtonSensitive)energyHandler).handleGUIButtonPress(0, Minecraft.getMinecraft().thePlayer);
                }
                break;
            case 2:
                rfPerTick = value;
                break;
            case 3:
                airPerTick = value;
                break;
        }
    }
}
