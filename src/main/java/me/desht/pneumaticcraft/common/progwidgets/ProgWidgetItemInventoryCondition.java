package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.DroneAIBlockCondition;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProgWidgetItemInventoryCondition extends ProgWidgetCondition {

    public ProgWidgetItemInventoryCondition() {
        super(ModProgWidgets.CONDITION_ITEM_INVENTORY);
    }

    @Override
    public List<ProgWidgetType> getParameters() {
        return ImmutableList.of(ModProgWidgets.AREA, ModProgWidgets.ITEM_FILTER, ModProgWidgets.TEXT);
    }

    @Override
    protected DroneAIBlockCondition getEvaluator(IDroneBase drone, IProgWidget widget) {
        return new DroneAIBlockCondition(drone, (ProgWidgetAreaItemBase) widget) {

            @Override
            protected boolean evaluate(BlockPos pos) {
                TileEntity te = drone.world().getTileEntity(pos);

                boolean[] sides = ((ISidedWidget) progWidget).getSides();

                // item handlers won't typically override hashCode/equals, but this should be OK: we just
                // want a set of distinct item handler objects, which Object#hashCode() should give us
                Set<IItemHandler> handlers = new HashSet<>();
                for (int sideIdx = 0; sideIdx < sides.length; sideIdx++) {
                    if (sides[sideIdx]) {
                        IOHelper.getInventoryForTE(te, Direction.byIndex(sideIdx)).ifPresent(handlers::add);
                    }
                }

                int count = 0;
                for (IItemHandler handler : handlers) {
                    for (int i = 0; i < handler.getSlots(); i++) {
                        ItemStack stack = handler.getStackInSlot(i);
                        if (progWidget.isItemValidForFilters(stack)) {
                            count += stack.getCount();
                        }
                    }
                }
                return ((ICondition) progWidget).getOperator().evaluate(count, ((ICondition) progWidget).getRequiredCount());
            }

        };
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_ITEM_INVENTORY;
    }

}
