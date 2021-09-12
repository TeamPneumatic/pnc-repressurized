package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.api.drone.IBlockInteractHandler;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetInventoryBase;
import net.minecraft.util.math.BlockPos;

public abstract class DroneAIImExBase<W extends ProgWidgetInventoryBase>
        extends DroneAIBlockInteraction<W>
        implements IBlockInteractHandler {
    private int transportCount;

    protected DroneAIImExBase(IDroneBase drone, W widget) {
        super(drone, widget);
        transportCount = widget.getCount();
    }

    @Override
    public boolean canUse() {
        boolean countReached = transportCount <= 0;
        transportCount = progWidget.getCount();
        return !(countReached && useCount()) && super.canUse();
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
    protected boolean doBlockInteraction(BlockPos pos, double squareDistToBlock) {
        return !useCount() || transportCount > 0;
    }

    @Override
    public boolean[] getSides() {
        return progWidget.getSides();
    }

    @Override
    public boolean useCount() {
        return progWidget.useCount();
    }

}
