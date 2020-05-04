package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ProgWidgetDroneConditionEntity extends ProgWidgetDroneCondition implements IEntityProvider {

    private EntityFilterPair<ProgWidgetDroneConditionEntity> entityFilters;

    public ProgWidgetDroneConditionEntity() {
        super(ModProgWidgets.DRONE_CONDITION_ENTITY);
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.TEXT, ModProgWidgets.TEXT);
    }

    @Override
    public void addErrors(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);

        EntityFilterPair.addErrors(this, curInfo);
    }

    @Override
    public int getEntityFilterPosition() {
        return 0;
    }

    @Override
    protected int getCount(IDroneBase d, IProgWidget widget) {
        EntityDrone drone = (EntityDrone) d;
        int count = 0;
        for (Entity e : drone.getPassengers()) {
            if (((IEntityProvider) widget).isEntityValid(e)) count++;
        }
        return count;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_DRONE_ENTITY;
    }

    @Override
    public List<Entity> getValidEntities(World world) {
        return new ArrayList<>();
    }

    @Override
    public boolean isEntityValid(Entity entity) {
        if (entityFilters == null) {
            entityFilters = new EntityFilterPair<>(this);
        }
        return entityFilters.isEntityValid(entity);
    }

}
