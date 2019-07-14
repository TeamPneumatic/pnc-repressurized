package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.DroneEntityBase;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetEntityRightClick extends ProgWidget implements IAreaProvider, IEntityProvider {

    private EntityFilterPair entityFilters;

    @Override
    public void addErrors(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (getConnectedParameters()[0] == null) {
            curInfo.add(xlate("gui.progWidget.area.error.noArea"));
        }
        EntityFilterPair.addErrors(this, curInfo);
    }

    @Override
    public boolean hasStepInput() {
        return true;
    }

    @Override
    public Class<? extends IProgWidget> returnType() {
        return null;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters() {
        return new Class[]{ProgWidgetArea.class, ProgWidgetString.class};
    }

    @Override
    public String getWidgetString() {
        return "entityRightClick";
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.YELLOW;
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.EASY;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_ENTITY_RIGHT_CLICK;
    }

    @Override
    public Goal getWidgetAI(IDroneBase drone, IProgWidget progWidget) {
        return new DroneEntityBase<IEntityProvider, LivingEntity>(drone, (IEntityProvider) progWidget) {
            private final List<Entity> visitedEntities = new ArrayList<>();

            @Override
            protected boolean isEntityValid(Entity entity) {
                return entity instanceof LivingEntity && !visitedEntities.contains(entity);
            }

            @Override
            protected boolean doAction() {
                visitedEntities.add(targetedEntity);
                boolean activated = false;
                ItemStack stack = drone.getInv().getStackInSlot(0);
                if (stack.getItem().itemInteractionForEntity(stack, drone.getFakePlayer(), targetedEntity, Hand.MAIN_HAND)) {
                    activated = true;
                }
                if (!activated && targetedEntity instanceof AgeableEntity && ((AgeableEntity) targetedEntity).processInteract(drone.getFakePlayer(), Hand.MAIN_HAND)) {
                    activated = true;
                }
                return false;//return activated; <-- will right click as long as it's sucessfully activated.
            }

        };
    }

    @Override
    public List<Entity> getValidEntities(World world) {
        if (entityFilters == null) {
            entityFilters = new EntityFilterPair(this);
        }
        return entityFilters.getValidEntities(world);
    }

    @Override
    public boolean isEntityValid(Entity entity) {
        if (entityFilters == null) {
            entityFilters = new EntityFilterPair(this);
        }
        return entityFilters.isEntityValid(entity);
    }

    @Override
    public void getArea(Set<BlockPos> area) {
        ProgWidgetEntityAttack.getArea(area, (ProgWidgetArea) getConnectedParameters()[0], (ProgWidgetArea) getConnectedParameters()[2]);
    }
}
