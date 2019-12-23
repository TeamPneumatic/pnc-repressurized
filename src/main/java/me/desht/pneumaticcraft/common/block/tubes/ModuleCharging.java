package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.common.capabilities.CapabilityAirHandler;
import me.desht.pneumaticcraft.common.util.TileEntityCache;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ModuleCharging extends TubeModule {
    private TileEntityCache connectedInventory;
    private LazyOptional<IItemHandler> cachedHandler = LazyOptional.empty();

    @Override
    public ResourceLocation getType() {
        return Names.MODULE_CHARGING;
    }

    @Override
    public void update() {
        super.update();

        if (pressureTube.world().isRemote || (pressureTube.world().getGameTime() & 0x7) != 0) return;

        if (!cachedHandler.isPresent()) {
            cachedHandler = getConnectedInventory();
            if (cachedHandler.isPresent()) {
                cachedHandler.addListener(h -> cachedHandler = LazyOptional.empty());
            }
        }

        cachedHandler.ifPresent(itemHandler -> {
            // times 8 because we only run every 8 ticks
            int airToTransfer = 8 * PneumaticValues.CHARGING_STATION_CHARGE_RATE * (upgraded ? 10 : 1);
            IAirHandlerMachine airHandler = pressureTube.getAirHandler(null);
            int airInTube = (int) (airHandler.getPressure() * airHandler.getVolume());

            for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
                ItemStack chargedItem = itemHandler.getStackInSlot(slot);
                final int airInTube2 = airInTube;
                int toAdd = chargedItem.getCapability(CapabilityAirHandler.AIR_HANDLER_ITEM_CAPABILITY).map(h -> {
                    float itemPressure = h.getPressure();
                    float itemVolume = h.getVolume();
                    float delta = Math.abs(airHandler.getPressure() - itemPressure) / 2.0F;
                    int airInItem = (int) (itemPressure * itemVolume);
                    if (itemPressure > airHandler.getPressure() + 0.01F && itemPressure > 0F) {
                        // move air from item to charger (tube)
                        int airToMove = Math.min(Math.min(airToTransfer, airInItem), (int) (delta * airHandler.getVolume()));
                        h.addAir(-airToMove);
                        airHandler.addAir(airToMove);
                        return airToMove;
                    } else if (itemPressure < airHandler.getPressure() - 0.01F && itemPressure < h.maxPressure()) {
                        // move air from charger (tube) to item
                        int maxAirInItem = (int) (h.maxPressure() * itemVolume);
                        int airToMove = Math.min(Math.min(airToTransfer, airInTube2), maxAirInItem - airInItem);
                        airToMove = Math.min((int) (delta * itemVolume), airToMove);
                        h.addAir(airToMove);
                        airHandler.addAir(-airToMove);
                    }
                    return 0;
                }).orElse(0);
                airInTube += toAdd;
            }
        });
    }

    @Override
    public void onNeighborTileUpdate() {
        connectedInventory = null;
    }

    private LazyOptional<IItemHandler> getConnectedInventory() {
        if (connectedInventory == null) {
            connectedInventory = new TileEntityCache(pressureTube.world(), pressureTube.pos().offset(dir));
        }
        TileEntity te = connectedInventory.getTileEntity();
        return te == null ? LazyOptional.empty() : te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite());
    }
}
