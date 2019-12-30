package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.util.ResourceLocation;

public class ModuleSafetyValve extends TubeModuleRedstoneReceiving {

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

    @Override
    public ResourceLocation getType() {
        return Names.MODULE_SAFETY_VALVE;
    }

}
