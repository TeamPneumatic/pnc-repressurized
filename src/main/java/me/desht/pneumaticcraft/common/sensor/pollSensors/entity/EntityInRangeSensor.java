package me.desht.pneumaticcraft.common.sensor.pollSensors.entity;

import me.desht.pneumaticcraft.common.util.EntityFilter;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class EntityInRangeSensor extends EntityPollSensor {

    private EntityFilter filter;

    @Override
    public String getSensorPath() {
        return "Within Range";
    }

    @Override
    public boolean needsTextBox() {
        return true;
    }

    @Override
    public boolean isEntityFilter() {
        return true;
    }

    @Override
    public int getRedstoneValue(List<Entity> entities, String textboxText) {
        if (filter == null) {
            filter = new EntityFilter(textboxText);
        }

        int entitiesFound = textboxText.isEmpty() ?
                entities.size() :
                (int) entities.stream().filter(entity -> filter.test(entity)).count();
        return Math.min(15, entitiesFound);
    }

    @Override
    public Class<? extends Entity> getEntityTracked() {
        return Entity.class;
    }

    @Override
    public void getAdditionalInfo(List<ITextComponent> info) {
        info.add(xlate("pneumaticcraft.gui.entityFilter"));
    }

    @Override
    public void notifyTextChange(String newText) {
        filter = new EntityFilter(newText);
    }
}
