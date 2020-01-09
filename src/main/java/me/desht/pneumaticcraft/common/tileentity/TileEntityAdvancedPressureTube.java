package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.lib.PneumaticValues;

public class TileEntityAdvancedPressureTube extends TileEntityPressureTube {
    public TileEntityAdvancedPressureTube() {
        super(ModTileEntities.ADVANCED_PRESSURE_TUBE, PneumaticValues.DANGER_PRESSURE_ADVANCED_PRESSURE_TUBE, PneumaticValues.MAX_PRESSURE_ADVANCED_PRESSURE_TUBE, PneumaticValues.VOLUME_ADVANCED_PRESSURE_TUBE, 0);
    }
}
