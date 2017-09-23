package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.api.drone.IBlockInteractHandler;
import me.desht.pneumaticcraft.common.progwidgets.ICountWidget;
import me.desht.pneumaticcraft.common.progwidgets.ISidedWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetAreaItemBase;
import net.minecraft.util.math.BlockPos;

public abstract class DroneAIImExBase extends DroneAIBlockInteraction implements IBlockInteractHandler {
    private int transportCount;

    protected DroneAIImExBase(IDroneBase drone, ProgWidgetAreaItemBase widget) {
        super(drone, widget);
        transportCount = ((ICountWidget) widget).getCount();
    }

    @Override
    public boolean shouldExecute() {
        boolean countReached = transportCount <= 0;
        transportCount = ((ICountWidget) widget).getCount();
        return !(countReached && useCount()) && super.shouldExecute();
    }

    @Override
    public void decreaseCount(int count) {
        transportCount -= count;
    }

    @Override
    public int getRemainingCount() {
        return transportCount;
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double distToBlock) {
        return !useCount() || transportCount > 0;
    }

    @Override
    public boolean[] getSides() {
        return ((ISidedWidget) widget).getSides();
    }

    @Override
    public boolean useCount() {
        return ((ICountWidget) widget).useCount();
    }

}
