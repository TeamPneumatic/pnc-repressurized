package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.common.item.ItemTubeModule;
import me.desht.pneumaticcraft.lib.PneumaticValues;

public class ModuleSafetyValve extends TubeModuleRedstoneReceiving {

    public ModuleSafetyValve(ItemTubeModule item) {
        super(item);
    }

    @Override
    public void update() {
        super.update();
        if (!pressureTube.getWorld().isRemote) {
            if (pressureTube.getPressure() > getThreshold()) {
                pressureTube.forceLeak(dir);
            }
        }
    }

    @Override
    public float getThreshold() {
        if (upgraded) return super.getThreshold();

        // 4.92 instead of 4.9 because if the system is being fed via regulator from a high pressure line,
        // then it will be at 4.9 bar, which would cause safety modules to leak unnecessarily...
        return getTube().dangerPressure == PneumaticValues.DANGER_PRESSURE_ADVANCED_PRESSURE_TUBE ? 19.9f : 4.92f;
    }
}
