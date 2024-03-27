package me.desht.pneumaticcraft.client.pneumatic_armor.entity_tracker;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IEntityTrackEntry;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class EntityTrackEntryPressurizable implements IEntityTrackEntry {
    @Override
    public boolean isApplicable(Entity entity) {
        return PNCCapabilities.getAirHandler(entity).isPresent();
    }

    @Override
    public void addInfo(Entity entity, List<Component> curInfo, boolean isLookingAtTarget) {
        float pressure = PNCCapabilities.getAirHandler(entity)
                .map(IAirHandler::getPressure)
                .orElseThrow(IllegalStateException::new);
        curInfo.add(xlate("pneumaticcraft.gui.tooltip.pressure", PneumaticCraftUtils.roundNumberTo(pressure, 1)));
    }
}
