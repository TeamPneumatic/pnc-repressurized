package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.client.model.module.ModelModuleBase;
import me.desht.pneumaticcraft.client.model.module.ModelSafetyValve;
import me.desht.pneumaticcraft.lib.Names;

public class ModuleSafetyValve extends TubeModuleRedstoneReceiving {

    @Override
    public void update() {
        super.update();
        if (!pressureTube.world().isRemote) {
            if (pressureTube.getAirHandler(null).getPressure() > getThreshold()) {
                pressureTube.getAirHandler(null).airLeak(dir);
            }
        }
    }

    @Override
    public String getType() {
        return Names.MODULE_SAFETY_VALVE;
    }

    @Override
    public Class<? extends ModelModuleBase> getModelClass() {
        return ModelSafetyValve.class;
    }
}
