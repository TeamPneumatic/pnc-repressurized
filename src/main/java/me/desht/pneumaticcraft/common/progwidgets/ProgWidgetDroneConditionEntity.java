package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.ai.StringFilterEntitySelector;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class ProgWidgetDroneConditionEntity extends ProgWidgetDroneEvaluation implements IEntityProvider {

    @Override
    public Class<? extends IProgWidget>[] getParameters() {
        return new Class[]{ProgWidgetString.class, ProgWidgetString.class};
    }

    @Override
    public String getWidgetString() {
        return "droneConditionEntity";
    }

    @Override
    protected int getCount(IDroneBase d, IProgWidget widget) {
        EntityDrone drone = (EntityDrone) d;
        int count = 0;
        for (Entity e : drone.getPassengers()) {
            if (((IEntityProvider) widget).isEntityValid(e)) count++;
        }
        return count;
//        return drone.getPassengers().isEmpty() || !((IEntityProvider) widget).isEntityValid(drone.riddenByEntity) ? 0 : 1;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_DRONE_ENTITY;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer) {
        return null;
    }

    @Override
    public List<Entity> getValidEntities(World world) {
        return new ArrayList<Entity>();
    }

    @Override
    public boolean isEntityValid(Entity entity) {
        StringFilterEntitySelector whitelistFilter = ProgWidgetAreaItemBase.getEntityFilter((ProgWidgetString) getConnectedParameters()[0], true);
        StringFilterEntitySelector blacklistFilter = ProgWidgetAreaItemBase.getEntityFilter((ProgWidgetString) getConnectedParameters()[getParameters().length], false);
        return whitelistFilter.apply(entity) && !blacklistFilter.apply(entity);
    }

}
