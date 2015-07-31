package pneumaticCraft.common.block.tubes;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.item.IPressurizable;
import pneumaticCraft.api.tileentity.IAirHandler;
import pneumaticCraft.api.tileentity.IPneumaticMachine;
import pneumaticCraft.client.model.IBaseModel;
import pneumaticCraft.client.model.tubemodules.ModelCharging;
import pneumaticCraft.common.util.IOHelper;
import pneumaticCraft.common.util.TileEntityCache;
import pneumaticCraft.lib.Names;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;

public class ModuleCharging extends TubeModule{
    private final IBaseModel model = new ModelCharging();
    private TileEntityCache connectedInventory;

    @Override
    public String getType(){
        return Names.MODULE_CHARGING;
    }

    @Override
    public IBaseModel getModel(){
        return model;
    }

    @Override
    protected EnumGuiId getGuiId(){
        return null;
    }

    @Override
    public void update(){
        super.update();
        IInventory inv = getConnectedInventory();
        if(inv != null) {
            int[] accessibleSlots = IOHelper.getAccessibleSlotsForInventory(inv, dir.getOpposite());
            for(int i = 0; i < (upgraded ? 10 : 1) * PneumaticValues.CHARGING_STATION_CHARGE_RATE; i++) {
                boolean charged = false;
                for(int slot : accessibleSlots) {
                    ItemStack chargedItem = inv.getStackInSlot(slot);
                    if(chargedItem != null && chargedItem.getItem() instanceof IPressurizable) {
                        IPressurizable chargingItem = (IPressurizable)chargedItem.getItem();
                        IAirHandler airHandler = ((IPneumaticMachine)pressureTube).getAirHandler();

                        if(chargingItem.getPressure(chargedItem) > airHandler.getPressure(ForgeDirection.UNKNOWN) + 0.01F && chargingItem.getPressure(chargedItem) > 0F) {
                            chargingItem.addAir(chargedItem, -1);
                            airHandler.addAir(1, ForgeDirection.UNKNOWN);
                            charged = true;
                        } else if(chargingItem.getPressure(chargedItem) < airHandler.getPressure(ForgeDirection.UNKNOWN) - 0.01F && chargingItem.getPressure(chargedItem) < chargingItem.maxPressure(chargedItem)) {// if there is pressure, and the item isn't fully charged yet..
                            chargingItem.addAir(chargedItem, 1);
                            airHandler.addAir(-1, ForgeDirection.UNKNOWN);
                            charged = true;
                        }
                    }
                }
                if(!charged) break;
            }
        }
    }

    @Override
    public void onNeighborTileUpdate(){
        connectedInventory = null;
    }

    private IInventory getConnectedInventory(){
        if(connectedInventory == null) {
            connectedInventory = new TileEntityCache(pressureTube.world(), pressureTube.x() + dir.offsetX, pressureTube.y() + dir.offsetY, pressureTube.z() + dir.offsetZ);
        }
        return connectedInventory.getTileEntity() instanceof IInventory ? (IInventory)connectedInventory.getTileEntity() : null;
    }
}
