package me.desht.pneumaticcraft.common.progwidgets.area;

import java.util.function.Consumer;

import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetArea.EnumAreaType;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AreaTypeLine extends AreaType{

    public static final String ID = "line";
    
    public AreaTypeLine(){
        super(ID);
    }

    @Override
    public void addArea(Consumer<BlockPos> areaAdder, BlockPos p1, BlockPos p2, int minX, int minY, int minZ, int maxX, int maxY, int maxZ){
        Vec3d lineVec = new Vec3d(p2.getX() - p1.getX(), p2.getY() - p1.getY(), p2.getZ() - p1.getZ()).normalize();
        lineVec = new Vec3d(lineVec.x / 10, lineVec.y / 10, lineVec.z / 10);
        double curX = p1.getX() + 0.5;
        double curY = p1.getY() + 0.5;
        double curZ = p1.getZ() + 0.5;
        double totalDistance = 0;
        double maxDistance = PneumaticCraftUtils.distBetween(p1, p2);
        while (totalDistance <= maxDistance) {
            totalDistance += 0.1;
            curX += lineVec.x;
            curY += lineVec.y;
            curZ += lineVec.z;
            if (curY >= 0 && curY < 256) {
                BlockPos pos = new BlockPos((int) curX, (int) curY, (int) curZ);
                areaAdder.accept(pos);
            }
        }
    }
}
