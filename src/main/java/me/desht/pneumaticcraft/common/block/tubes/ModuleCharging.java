package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.common.item.ItemTubeModule;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ModuleCharging extends TubeModule {
    private TileEntity neighbourTE = null;

    public ModuleCharging(ItemTubeModule itemTubeModule) {
        super(itemTubeModule);
    }

    @Override
    public void update() {
        super.update();

        if (pressureTube.getWorld().isRemote || (pressureTube.getWorld().getGameTime() & 0x7) != 0) return;

        getConnectedInventory().ifPresent(itemHandler -> {
            // times 8 because we only run every 8 ticks
            int airToTransfer = 8 * PneumaticValues.CHARGING_STATION_CHARGE_RATE * (upgraded ? 10 : 1);
            pressureTube.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY).ifPresent(airHandler -> {
                for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
                    ItemStack chargedItem = itemHandler.getStackInSlot(slot);
                    if (chargedItem.isEmpty()) continue;
                    chargedItem.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).ifPresent(airHandlerItem -> {
                        float itemPressure = airHandlerItem.getPressure();
                        float itemVolume = airHandlerItem.getVolume();
                        float delta = Math.abs(airHandler.getPressure() - itemPressure) / 2.0F;
                        int airInItem = airHandlerItem.getAir();
                        if (itemPressure > airHandler.getPressure() + 0.01F && itemPressure > 0F) {
                            // move air from item to charger (tube)
                            int airToMove = Math.min(Math.min(airToTransfer, airInItem), (int) (delta * airHandler.getVolume()));
                            airHandlerItem.addAir(-airToMove / chargedItem.getCount());
                            airHandler.addAir(airToMove);
                        } else if (itemPressure < airHandler.getPressure() - 0.01F && itemPressure < airHandlerItem.maxPressure()) {
                            // move air from charger (tube) to item
                            int maxAirInItem = (int) (airHandlerItem.maxPressure() * itemVolume);
                            int airToMove = Math.min(Math.min(airToTransfer, airHandler.getAir()), maxAirInItem - airInItem);
                            airToMove = Math.min((int) (delta * itemVolume), airToMove);
                            airHandlerItem.addAir(airToMove / chargedItem.getCount());
                            airHandler.addAir(-airToMove);
                        }
                    });
                }
            });
        });
    }

    private LazyOptional<IItemHandler> getConnectedInventory() {
        if (neighbourTE == null || neighbourTE.isRemoved()) {
            neighbourTE = pressureTube.getWorld().getTileEntity(pressureTube.getPos().offset(dir));
        }
        return neighbourTE == null ? LazyOptional.empty() : neighbourTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite());
    }
}
