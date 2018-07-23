package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.client.model.module.ModelCharging;
import me.desht.pneumaticcraft.client.model.module.ModelModuleBase;
import me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;
import me.desht.pneumaticcraft.common.util.TileEntityCache;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.lib.PneumaticValues;
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
    protected EnumGuiId getGuiId() {
        return null;
    }

    @Override
    public Class<? extends ModelModuleBase> getModelClass() {
        return ModelCharging.class;
    }

    @Override
    public void update() {
        super.update();
        if (connectedInventory != null && !connectedInventory.getTileEntity().getWorld().isRemote) {
            IItemHandler handler = getConnectedInventory();
            if (handler != null) {
                int airToTransfer = PneumaticValues.CHARGING_STATION_CHARGE_RATE * (upgraded ? 10 : 1);
                IAirHandler airHandler = pressureTube.getAirHandler(null);
                int airInTube = (int) airHandler.getPressure() * airHandler.getVolume();

                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    ItemStack chargedItem = handler.getStackInSlot(slot);
                    if (chargedItem.getItem() instanceof IPressurizable) {
                        IPressurizable chargingItem = (IPressurizable) chargedItem.getItem();
                        float itemPressure = chargingItem.getPressure(chargedItem);
                        float itemVolume = chargingItem.getVolume(chargedItem);
                        float delta = Math.abs(airHandler.getPressure() - itemPressure) / 2.0F;
                        int airInItem = (int) (itemPressure * itemVolume);
                        if (chargingItem.getPressure(chargedItem) > airHandler.getPressure() + 0.01F && chargingItem.getPressure(chargedItem) > 0F) {
                            // move air from item to charger (tube)
                            int airToMove = Math.min(Math.min(airToTransfer, airInItem), (int) (delta * airHandler.getVolume()));
                            chargingItem.addAir(chargedItem, -airToMove);
                            airHandler.addAir(airToMove);
                            airInTube += airToMove;
                        } else if (chargingItem.getPressure(chargedItem) < airHandler.getPressure() - 0.01F && chargingItem.getPressure(chargedItem) < chargingItem.maxPressure(chargedItem)) {
                            // move air from charger (tube) to item
                            int maxAirInItem = (int) (chargingItem.maxPressure(chargedItem) * itemVolume);
                            int airToMove = Math.min(Math.min(airToTransfer, airInTube), maxAirInItem - airInItem);
                            airToMove = Math.min((int) (delta * itemVolume), airToMove);
                            chargingItem.addAir(chargedItem, airToMove);
                            airHandler.addAir(-airToMove);
                        }
                    }
                }
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
