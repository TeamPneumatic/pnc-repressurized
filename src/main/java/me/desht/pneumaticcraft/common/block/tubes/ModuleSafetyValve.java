package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.common.item.ItemTubeModule;

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
        return upgraded ? super.getThreshold() : getTube().dangerPressure - 0.1f;
    }
}
