package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.common.util.TileEntityCache;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ModuleCharging extends TubeModule {
    private TileEntityCache connectedInventory;

    @Override
    public String getType() {
        return Names.MODULE_CHARGING;
    }

    @Override
    public String getModelName() {
        return "charging";
    }

    @Override
    protected EnumGuiId getGuiId() {
        return null;
    }

    @Override
    public void update() {
        super.update();
        IItemHandler handler = getConnectedInventory();
        if (handler != null) {
            for (int i = 0; i < (upgraded ? 10 : 1) * PneumaticValues.CHARGING_STATION_CHARGE_RATE; i++) {
                boolean charged = false;
                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    ItemStack chargedItem = handler.getStackInSlot(slot);
                    if (chargedItem.getItem() instanceof IPressurizable) {
                        IPressurizable chargingItem = (IPressurizable) chargedItem.getItem();
                        IAirHandler airHandler = pressureTube.getAirHandler(null);
                        if (chargingItem.getPressure(chargedItem) > airHandler.getPressure() + 0.01F && chargingItem.getPressure(chargedItem) > 0F) {
                            chargingItem.addAir(chargedItem, -1);
                            airHandler.addAir(1);
                            charged = true;
                        } else if (chargingItem.getPressure(chargedItem) < airHandler.getPressure() - 0.01F && chargingItem.getPressure(chargedItem) < chargingItem.maxPressure(chargedItem)) {// if there is pressure, and the item isn't fully charged yet..
                            chargingItem.addAir(chargedItem, 1);
                            airHandler.addAir(-1);
                            charged = true;
                        }
                    }
                }
                if (!charged) break;
            }
        }
    }

    @Override
    public void onNeighborTileUpdate() {
        connectedInventory = null;
    }

    private IItemHandler getConnectedInventory() {
        if (connectedInventory == null) {
            connectedInventory = new TileEntityCache(pressureTube.world(), pressureTube.pos().offset(dir));
        }
        TileEntity te = connectedInventory.getTileEntity();
        return te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite()) ?
                te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite()) :
                null;
    }
}
