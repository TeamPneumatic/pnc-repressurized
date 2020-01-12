package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.common.item.ItemTubeModule;

public class ModuleSafetyValve extends TubeModuleRedstoneReceiving {

    public ModuleSafetyValve(ItemTubeModule item) {
        super(item);
    }

    @Override
    public void update() {
        super.update();
        if (!pressureTube.getWorld().isRemote) {
            pressureTube.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY).ifPresent(h -> {
               if (h.getPressure() > getThreshold()) {
                   pressureTube.forceLeak(dir);
               }
            });
        }
    }
}
