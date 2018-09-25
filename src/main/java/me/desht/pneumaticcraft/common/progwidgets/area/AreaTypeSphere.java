package me.desht.pneumaticcraft.common.progwidgets.area;

import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.function.Consumer;

public class AreaTypeSphere extends AreaType{

    public static final String ID = "sphere";
    
    private EnumSphereType sphereType = EnumSphereType.FILLED;
    
    private enum EnumSphereType{
        FILLED("filled"), HOLLOW("hollow");
        
        private final String name;
        
        EnumSphereType(String name){
            this.name = "gui.progWidget.area.type.sphere.sphereType." + name;
        }
        
        @Override
        public String toString(){
            return I18n.format(name);
        }
    }
    
    public AreaTypeSphere(){
        super(ID);
    }

    @Override
    public void addArea(Consumer<BlockPos> areaAdder, BlockPos p1, BlockPos p2, int minX, int minY, int minZ, int maxX, int maxY, int maxZ){
        double radius = PneumaticCraftUtils.distBetween(p1, p2);
        double radiusSq = radius * radius;
        double innerRadius = sphereType == EnumSphereType.HOLLOW ? radius - 1 : 0;
        double innerRadiusSq = innerRadius * innerRadius;
        minX = (int) (p1.getX() - radius - 1);
        minY = (int) (p1.getY() - radius - 1);
        minZ = (int) (p1.getZ() - radius - 1);
        maxX = (int) (p1.getX() + radius + 1);
        maxY = (int) (p1.getY() + radius + 1);
        maxZ = (int) (p1.getZ() + radius + 1);
        for (int x = minX; x <= maxX; x++) {
            for (int y = Math.max(0, minY); y <= maxY && y < 256; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    double centerDistSq = PneumaticCraftUtils.distBetweenSq(p1, x + 0.5, y + 0.5, z + 0.5);
                    if (centerDistSq <= radiusSq && centerDistSq >= innerRadiusSq) { //Only add blocks between a certain radius
                        areaAdder.accept(new BlockPos(x, y, z));
                    }
                }
            }
        }
    }
    
    @Override
    public void addUIWidgets(List<AreaTypeWidget> widgets){
        super.addUIWidgets(widgets);
        widgets.add(new AreaTypeWidgetEnum<>("gui.progWidget.area.type.sphere.sphereType", EnumSphereType.class, () -> sphereType, sphereType -> this.sphereType = sphereType));
    }
    
    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setByte("sphereType", (byte)sphereType.ordinal());
    }
    
    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        sphereType = EnumSphereType.values()[tag.getByte("sphereType")];
    }
}
