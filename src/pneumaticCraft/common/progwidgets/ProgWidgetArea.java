package pneumaticCraft.common.progwidgets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.programmer.GuiProgWidgetArea;
import pneumaticCraft.common.PneumaticCraftUtils;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ProgWidgetArea extends ProgWidget implements IAreaProvider{
    public int x1, y1, z1, x2, y2, z2;
    public EnumAreaType type = EnumAreaType.FILL;

    public enum EnumAreaType{
        FILL("Filled"), FRAME("Frame"), WALL("Walls"), SPHERE("Sphere"), LINE("Line"), X_WALL("X-Wall"), Y_WALL(
                "Y-Wall"), Z_WALL("Z-Wall");

        private final String name;

        private EnumAreaType(String name){
            this.name = name;
        }

        @Override
        public String toString(){
            return name;
        }
    }

    @Override
    public void getTooltip(List<String> curTooltip){
        super.getTooltip(curTooltip);
        ChunkPosition[] areaPoints = getAreaPoints();
        if(areaPoints[0] != null) {
            if(areaPoints[1] != null) {
                curTooltip.add("Contains the points:");
                curTooltip.add("X1: " + areaPoints[0].chunkPosX + ", Y1: " + areaPoints[0].chunkPosY + ", Z1: " + areaPoints[0].chunkPosZ);
                curTooltip.add("X2: " + areaPoints[1].chunkPosX + ", Y2: " + areaPoints[1].chunkPosY + ", Z2: " + areaPoints[1].chunkPosZ);
            } else {
                curTooltip.add("Contains the point:");
                curTooltip.add("X1: " + areaPoints[0].chunkPosX + ", Y1: " + areaPoints[0].chunkPosY + ", Z1: " + areaPoints[0].chunkPosZ);
            }
        }

        curTooltip.add("Area type: " + type);
    }

    private ChunkPosition[] getAreaPoints(){
        ChunkPosition c1 = x1 != 0 || y1 != 0 || z1 != 0 ? new ChunkPosition(x1, y1, z1) : null;
        ChunkPosition c2 = x2 != 0 || y2 != 0 || z2 != 0 ? new ChunkPosition(x2, y2, z2) : null;
        if(c1 == null && c2 == null) {
            return new ChunkPosition[]{null, null};
        } else if(c1 == null) {
            return new ChunkPosition[]{c2, null};
        } else if(c2 == null) {
            return new ChunkPosition[]{c1, null};
        } else {
            return new ChunkPosition[]{c1, c2};
        }
    }

    @Override
    public boolean hasStepInput(){
        return false;
    }

    @Override
    public Class<? extends IProgWidget> returnType(){
        return ProgWidgetArea.class;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetArea.class};
    }

    @Override
    public String getWidgetString(){
        return "area";
    }

    @Override
    public String getLegacyString(){
        return "  area";
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_AREA;
    }

    @Override
    public Set<ChunkPosition> getArea(){
        Set<ChunkPosition> area = new HashSet<ChunkPosition>();
        ChunkPosition[] areaPoints = getAreaPoints();
        if(areaPoints[0] == null && areaPoints[1] == null) return area;

        int minX;
        int minY;
        int minZ;
        int maxX;
        int maxY;
        int maxZ;
        if(areaPoints[1] != null) {
            minX = Math.min(areaPoints[0].chunkPosX, areaPoints[1].chunkPosX);
            minY = Math.min(areaPoints[0].chunkPosY, areaPoints[1].chunkPosY);
            minZ = Math.min(areaPoints[0].chunkPosZ, areaPoints[1].chunkPosZ);
            maxX = Math.max(areaPoints[0].chunkPosX, areaPoints[1].chunkPosX);
            maxY = Math.max(areaPoints[0].chunkPosY, areaPoints[1].chunkPosY);
            maxZ = Math.max(areaPoints[0].chunkPosZ, areaPoints[1].chunkPosZ);
        } else {
            minX = maxX = areaPoints[0].chunkPosX;
            minY = maxY = areaPoints[0].chunkPosY;
            minZ = maxZ = areaPoints[0].chunkPosZ;
        }

        switch(type){
            case FILL:
                for(int x = minX; x <= maxX; x++) {
                    for(int y = maxY; y >= minY; y--) {
                        for(int z = minZ; z <= maxZ; z++) {
                            area.add(new ChunkPosition(x, y, z));
                        }
                    }
                }
                break;
            case FRAME:
                for(int x = minX; x <= maxX; x++) {
                    for(int y = minY; y <= maxY; y++) {
                        for(int z = minZ; z <= maxZ; z++) {
                            int axisRight = 0;
                            if(x == minX || x == maxX) axisRight++;
                            if(y == minY || y == maxY) axisRight++;
                            if(z == minZ || z == maxZ) axisRight++;
                            if(axisRight > 1) {
                                area.add(new ChunkPosition(x, y, z));
                            }
                        }
                    }
                }
                break;
            case WALL:
                for(int x = minX; x <= maxX; x++) {
                    for(int y = minY; y <= maxY; y++) {
                        for(int z = minZ; z <= maxZ; z++) {
                            if(x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ) {
                                area.add(new ChunkPosition(x, y, z));
                            }
                        }
                    }
                }
                break;
            case SPHERE:
                double radius = areaPoints[1] != null ? PneumaticCraftUtils.distBetween(areaPoints[0], areaPoints[1]) : 0;
                minX = (int)(areaPoints[0].chunkPosX - radius - 1);
                minY = (int)(areaPoints[0].chunkPosY - radius - 1);
                minZ = (int)(areaPoints[0].chunkPosZ - radius - 1);
                maxX = (int)(areaPoints[0].chunkPosX + radius + 1);
                maxY = (int)(areaPoints[0].chunkPosY + radius + 1);
                maxZ = (int)(areaPoints[0].chunkPosZ + radius + 1);
                for(int x = minX; x <= maxX; x++) {
                    for(int y = minY; y <= maxY; y++) {
                        for(int z = minZ; z <= maxZ; z++) {
                            if(PneumaticCraftUtils.distBetween(areaPoints[0], x + 0.5, y + 0.5, z + 0.5) <= radius) {
                                area.add(new ChunkPosition(x, y, z));
                            }
                        }
                    }
                }
                break;
            case LINE:
                if(areaPoints[1] != null) {
                    Vec3 lineVec = Vec3.createVectorHelper(areaPoints[1].chunkPosX - areaPoints[0].chunkPosX, areaPoints[1].chunkPosY - areaPoints[0].chunkPosY, areaPoints[1].chunkPosZ - areaPoints[0].chunkPosZ).normalize();
                    lineVec.xCoord /= 10;
                    lineVec.yCoord /= 10;
                    lineVec.zCoord /= 10;
                    double curX = areaPoints[0].chunkPosX + 0.5;
                    double curY = areaPoints[0].chunkPosY + 0.5;
                    double curZ = areaPoints[0].chunkPosZ + 0.5;
                    double totalDistance = 0;
                    double maxDistance = PneumaticCraftUtils.distBetween(areaPoints[0], areaPoints[1]);
                    while(totalDistance <= maxDistance) {
                        totalDistance += 0.1;
                        curX += lineVec.xCoord;
                        curY += lineVec.yCoord;
                        curZ += lineVec.zCoord;
                        ChunkPosition pos = new ChunkPosition((int)curX, (int)curY, (int)curZ);
                        if(!area.contains(pos)) area.add(pos);
                    }
                }
                break;
            case X_WALL:
                if(areaPoints[1] != null) {
                    Vec3 lineVec = Vec3.createVectorHelper(0, areaPoints[1].chunkPosY - areaPoints[0].chunkPosY, areaPoints[1].chunkPosZ - areaPoints[0].chunkPosZ).normalize();
                    lineVec.yCoord /= 10;
                    lineVec.zCoord /= 10;
                    double curY = areaPoints[0].chunkPosY + 0.5;
                    double curZ = areaPoints[0].chunkPosZ + 0.5;
                    double totalDistance = 0;
                    double maxDistance = Math.sqrt(Math.pow(areaPoints[0].chunkPosY - areaPoints[1].chunkPosY, 2) + Math.pow(areaPoints[0].chunkPosZ - areaPoints[1].chunkPosZ, 2));
                    while(totalDistance <= maxDistance) {
                        totalDistance += 0.1;
                        curY += lineVec.yCoord;
                        curZ += lineVec.zCoord;
                        for(int i = minX; i <= maxX; i++) {
                            ChunkPosition pos = new ChunkPosition(i, (int)curY, (int)curZ);
                            if(!area.contains(pos)) area.add(pos);
                        }
                    }
                }
                break;
            case Y_WALL:
                if(areaPoints[1] != null) {
                    Vec3 lineVec = Vec3.createVectorHelper(areaPoints[1].chunkPosX - areaPoints[0].chunkPosX, 0, areaPoints[1].chunkPosZ - areaPoints[0].chunkPosZ).normalize();
                    lineVec.xCoord /= 10;
                    lineVec.zCoord /= 10;
                    double curX = areaPoints[0].chunkPosX + 0.5;
                    double curZ = areaPoints[0].chunkPosZ + 0.5;
                    double totalDistance = 0;
                    double maxDistance = Math.sqrt(Math.pow(areaPoints[0].chunkPosX - areaPoints[1].chunkPosX, 2) + Math.pow(areaPoints[0].chunkPosZ - areaPoints[1].chunkPosZ, 2));
                    while(totalDistance <= maxDistance) {
                        totalDistance += 0.1;
                        curX += lineVec.xCoord;
                        curZ += lineVec.zCoord;
                        for(int i = minY; i <= maxY; i++) {
                            ChunkPosition pos = new ChunkPosition((int)curX, i, (int)curZ);
                            if(!area.contains(pos)) area.add(pos);
                        }
                    }
                }
                break;
            case Z_WALL:
                if(areaPoints[1] != null) {
                    Vec3 lineVec = Vec3.createVectorHelper(areaPoints[1].chunkPosX - areaPoints[0].chunkPosX, areaPoints[1].chunkPosY - areaPoints[0].chunkPosY, 0).normalize();
                    lineVec.xCoord /= 10;
                    lineVec.yCoord /= 10;
                    double curX = areaPoints[0].chunkPosX + 0.5;
                    double curY = areaPoints[0].chunkPosY + 0.5;
                    double totalDistance = 0;
                    double maxDistance = Math.sqrt(Math.pow(areaPoints[0].chunkPosX - areaPoints[1].chunkPosX, 2) + Math.pow(areaPoints[0].chunkPosY - areaPoints[1].chunkPosY, 2));
                    while(totalDistance <= maxDistance) {
                        totalDistance += 0.1;
                        curX += lineVec.xCoord;
                        curY += lineVec.yCoord;
                        for(int i = minZ; i <= maxZ; i++) {
                            ChunkPosition pos = new ChunkPosition((int)curX, (int)curY, i);
                            if(!area.contains(pos)) area.add(pos);
                        }
                    }
                }
                break;

        }
        return area;
    }

    private AxisAlignedBB getAABB(){
        ChunkPosition[] areaPoints = getAreaPoints();
        if(areaPoints[0] == null) return null;
        int minX;
        int minY;
        int minZ;
        int maxX;
        int maxY;
        int maxZ;
        if(areaPoints[1] != null) {
            minX = Math.min(areaPoints[0].chunkPosX, areaPoints[1].chunkPosX);
            minY = Math.min(areaPoints[0].chunkPosY, areaPoints[1].chunkPosY);
            minZ = Math.min(areaPoints[0].chunkPosZ, areaPoints[1].chunkPosZ);
            maxX = Math.max(areaPoints[0].chunkPosX, areaPoints[1].chunkPosX);
            maxY = Math.max(areaPoints[0].chunkPosY, areaPoints[1].chunkPosY);
            maxZ = Math.max(areaPoints[0].chunkPosZ, areaPoints[1].chunkPosZ);
        } else {
            minX = maxX = areaPoints[0].chunkPosX;
            minY = maxY = areaPoints[0].chunkPosY;
            minZ = maxZ = areaPoints[0].chunkPosZ;
        }
        return AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
    }

    public List<Entity> getEntitiesWithinArea(World world, IEntitySelector filter){
        AxisAlignedBB aabb = getAABB();
        return aabb != null ? world.getEntitiesWithinAABBExcludingEntity(null, aabb, filter) : new ArrayList<Entity>();
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setInteger("x1", x1);
        tag.setInteger("y1", y1);
        tag.setInteger("z1", z1);
        tag.setInteger("x2", x2);
        tag.setInteger("y2", y2);
        tag.setInteger("z2", z2);
        if(type != null) tag.setInteger("type", type.ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        x1 = tag.getInteger("x1");
        y1 = tag.getInteger("y1");
        z1 = tag.getInteger("z1");
        x2 = tag.getInteger("x2");
        y2 = tag.getInteger("y2");
        z2 = tag.getInteger("z2");
        type = EnumAreaType.values()[tag.getInteger("type")];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer){
        return new GuiProgWidgetArea(this, guiProgrammer);
    }

    @Override
    public String getGuiTabText(){
        return "This module is used as parameter for other modules. It can provide an area.";
    }

    @Override
    public int getGuiTabColor(){
        return 0xFF209600;
    }

}
