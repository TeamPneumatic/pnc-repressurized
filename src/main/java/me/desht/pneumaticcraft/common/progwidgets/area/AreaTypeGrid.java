package me.desht.pneumaticcraft.common.progwidgets.area;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.function.Consumer;

public class AreaTypeGrid extends AreaType{

    public static final String ID = "grid";
    private int interval;
    
    public AreaTypeGrid(){
        super(ID);
    }

    @Override
    public void addArea(Consumer<BlockPos> areaAdder, BlockPos p1, BlockPos p2, int minX, int minY, int minZ, int maxX, int maxY, int maxZ){
        if (p1.equals(p2) || interval <= 0) {
            areaAdder.accept(p1);
        } else {
            for (int x = p1.getX(); p1.getX() < p2.getX() ? x <= p2.getX() : x >= p2.getX(); x += (p1.getX() < p2.getX() ? 1 : -1) * interval) {
                for (int y = p1.getY(); p1.getY() < p2.getY() ? y <= p2.getY() : y >= p2.getY(); y += (p1.getY() < p2.getY() ? 1 : -1) * interval) {
                    for (int z = p1.getZ(); p1.getZ() < p2.getZ() ? z <= p2.getZ() : z >= p2.getZ(); z += (p1.getZ() < p2.getZ() ? 1 : -1) * interval) {
                        areaAdder.accept(new BlockPos(x, y, z));
                    }
                }
            }
        }
    }
    
    @Override
    public void addUIWidgets(List<AreaTypeWidget> widgets){
        super.addUIWidgets(widgets);
        widgets.add(new AreaTypeWidgetInteger("gui.progWidget.area.type.grid.interval", () -> interval, interval -> this.interval = interval));
    }
    
    @Override
    public void writeToNBT(CompoundNBT tag){
        super.writeToNBT(tag);
        tag.putInt("interval", interval);
    }
    
    @Override
    public void readFromNBT(CompoundNBT tag){
        super.readFromNBT(tag);
        interval = tag.getInt("interval");
    }
}
