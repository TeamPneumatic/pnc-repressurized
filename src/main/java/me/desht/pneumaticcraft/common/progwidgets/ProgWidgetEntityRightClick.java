package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.DroneEntityBase;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetEntityRightClick extends ProgWidget implements IAreaProvider, IEntityProvider {

    private EntityFilterPair<ProgWidgetEntityRightClick> entityFilters;

    public ProgWidgetEntityRightClick() {
        super(ModProgWidgets.ENTITY_RIGHT_CLICK);
    }

    @Override
    public void addErrors(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (getConnectedParameters()[0] == null) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.area.error.noArea"));
        }
        EntityFilterPair.addErrors(this, curInfo);
    }

    @Override
    public boolean hasStepInput() {
        return true;
    }

    @Override
    public ProgWidgetType<?> returnType() {
        return null;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.AREA, ModProgWidgets.TEXT);
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
            private final Set<Entity> visitedEntities = new HashSet<>();

            @Override
            protected boolean isEntityValid(Entity entity) {
                return entity instanceof LivingEntity && !visitedEntities.contains(entity);
            }

            @Override
            protected boolean doAction() {
                visitedEntities.add(targetedEntity);
                ItemStack stack = drone.getInv().getStackInSlot(0);
                PlayerEntity fakePlayer = drone.getFakePlayer();
                if (stack.getItem().itemInteractionForEntity(stack, fakePlayer, targetedEntity, Hand.MAIN_HAND)
                        || targetedEntity.processInitialInteract(fakePlayer, Hand.MAIN_HAND)) {
                    // fake player's inventory has probably been modified in some way
                    // copy items back to drone inventory, dropping on the ground any items that don't fit
                    for (int i = 0; i < fakePlayer.inventory.mainInventory.size(); i++) {
                        ItemStack fakePlayerStack = fakePlayer.inventory.mainInventory.get(i);
                        if (i < drone.getInv().getSlots()) {
                            drone.getInv().setStackInSlot(i, fakePlayerStack);
                        } else if (!fakePlayerStack.isEmpty()) {
                            drone.dropItem(fakePlayerStack);
                            fakePlayer.inventory.mainInventory.set(i, ItemStack.EMPTY);
                        }
                    }
                }
                return false; // always returning false, to indicate we're done trying
            }
        };
    }

    @Override
    public List<Entity> getValidEntities(World world) {
        if (entityFilters == null) {
            entityFilters = new EntityFilterPair<>(this);
        }
        return entityFilters.getValidEntities(world);
    }

    @Override
    public boolean isEntityValid(Entity entity) {
        if (entityFilters == null) {
            entityFilters = new EntityFilterPair<>(this);
        }
        return entityFilters.isEntityValid(entity);
    }

    @Override
    public void getArea(Set<BlockPos> area) {
        ProgWidgetEntityAttack.getArea(area, (ProgWidgetArea) getConnectedParameters()[0], (ProgWidgetArea) getConnectedParameters()[2]);
    }
}
