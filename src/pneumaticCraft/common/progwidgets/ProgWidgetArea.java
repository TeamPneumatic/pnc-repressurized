package pneumaticCraft.common.progwidgets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
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
import pneumaticCraft.common.ai.DroneAIManager;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ProgWidgetArea extends ProgWidget implements IAreaProvider, IVariableWidget{
    public int x1, y1, z1, x2, y2, z2;
    private String coord1Variable = "", coord2Variable = "";
    private DroneAIManager aiManager;
    public EnumAreaType type = EnumAreaType.FILL;
    public int typeInfo;//For the grid type, it's the grid interval, for Random it's the amount of selected blocks.

    public enum EnumAreaType{
        FILL("Filled"), FRAME("Frame"), WALL("Walls"), SPHERE("Sphere"), LINE("Line"), X_WALL("X-Wall"), Y_WALL(
                "Y-Wall"), Z_WALL("Z-Wall"), X_CYLINDER("X-Cylinder"), Y_CYLINDER("Y-Cylinder"), Z_CYLINDER(
                "Z-Cylinder"), X_PYRAMID("X-Pyramid"), Y_PYRAMID("Y-Pyramid"), Z_PYRAMID("Z-Pyramid"), GRID("Grid",
                true), RANDOM("Random", true);

        private final String name;
        public final boolean utilizesTypeInfo;

        private EnumAreaType(String name){
            this(name, false);
        }

        private EnumAreaType(String name, boolean utilizesTypeInfo){
            this.name = name;
            this.utilizesTypeInfo = utilizesTypeInfo;
        }

        @Override
        public String toString(){
            return name;
        }
    }

    @Override
    public void getTooltip(List<String> curTooltip){
        super.getTooltip(curTooltip);

        String c1;
        if(coord1Variable.equals("")) {
            c1 = x1 != 0 || y1 != 0 || z1 != 0 ? "X%s: " + x1 + ", Y%s: " + y1 + ", Z%s: " + z1 : null;
        } else {
            c1 = "XYZ%s: \"" + coord1Variable + "\"";
        }
        String c2;
        if(coord2Variable.equals("")) {
            c2 = x2 != 0 || y2 != 0 || z2 != 0 ? "X%s: " + x2 + ", Y%s: " + y2 + ", Z%s: " + z2 : null;
        } else {
            c2 = "XYZ%s: \"" + coord2Variable + "\"";
        }
        if(c1 == null) {
            c1 = c2;
            c2 = null;
        }

        if(c1 != null) {
            if(c2 != null) {
                curTooltip.add("Contains the points:");
                curTooltip.add(c1.replace("%s", "1"));
                curTooltip.add(c2.replace("%s", "2"));
            } else {
                curTooltip.add("Contains the point:");
                curTooltip.add(c1.replace("%s", "1"));
            }
        }

        curTooltip.add("Area type: " + type);
    }

    @Override
    public void addErrors(List<String> curInfo, List<IProgWidget> widgets){
        super.addErrors(curInfo, widgets);
        if(coord1Variable.equals("") && coord2Variable.equals("") && x1 == 0 && y1 == 0 && z1 == 0 && x2 == 0 && y2 == 0 && z2 == 0) {
            curInfo.add("gui.progWidget.area.error.noArea");
        }
    }

    private ChunkPosition[] getAreaPoints(){
        ChunkPosition c1;
        if(coord1Variable.equals("")) {
            c1 = x1 != 0 || y1 != 0 || z1 != 0 ? new ChunkPosition(x1, y1, z1) : null;
        } else {
            c1 = aiManager != null ? aiManager.getCoordinate(coord1Variable) : null;
        }
        ChunkPosition c2;
        if(coord2Variable.equals("")) {
            c2 = x2 != 0 || y2 != 0 || z2 != 0 ? new ChunkPosition(x2, y2, z2) : null;
        } else {
            c2 = aiManager != null ? aiManager.getCoordinate(coord2Variable) : null;
        }
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
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_AREA;
    }

    @Override
    public void getArea(Set<ChunkPosition> area){
        ChunkPosition[] areaPoints = getAreaPoints();
        if(areaPoints[0] == null && areaPoints[1] == null) return;

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
        int size = (maxX - minX) * (maxY - minY) * (maxZ - minZ);
        if(size > 100000) { //Prevent memory problems when getting to ridiculous areas.
            if(aiManager != null) aiManager.getDrone().overload();
            return;
        }

        switch(type){
            case FILL:
                for(int x = minX; x <= maxX; x++) {
                    for(int y = Math.min(255, maxY); y >= minY && y >= 0; y--) {
                        for(int z = minZ; z <= maxZ; z++) {
                            area.add(new ChunkPosition(x, y, z));
                        }
                    }
                }
                break;
            case FRAME:
                for(int x = minX; x <= maxX; x++) {
                    for(int y = Math.max(0, minY); y <= maxY && y < 256; y++) {
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
                    for(int y = Math.max(0, minY); y <= maxY && y < 256; y++) {
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
                    for(int y = Math.max(0, minY); y <= maxY && y < 256; y++) {
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
                        if(curY >= 0 && curY < 256) {
                            ChunkPosition pos = new ChunkPosition((int)curX, (int)curY, (int)curZ);
                            if(!area.contains(pos)) area.add(pos);
                        }
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
                            if(curY >= 0 && curY < 256) {
                                ChunkPosition pos = new ChunkPosition(i, (int)curY, (int)curZ);
                                if(!area.contains(pos)) area.add(pos);
                            }
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
                        for(int i = Math.max(0, minY); i <= Math.min(maxY, 255); i++) {
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
                            if(curY >= 0 && curY < 256) {
                                ChunkPosition pos = new ChunkPosition((int)curX, (int)curY, i);
                                if(!area.contains(pos)) area.add(pos);
                            }
                        }
                    }
                }
                break;
            case X_CYLINDER:
                if(areaPoints[1] != null) {
                    double rad = areaPoints[1] != null ? PneumaticCraftUtils.distBetween(areaPoints[0].chunkPosY, areaPoints[0].chunkPosZ, areaPoints[1].chunkPosY, areaPoints[1].chunkPosZ) : 0;
                    minY = (int)(areaPoints[0].chunkPosY - rad - 1);
                    minZ = (int)(areaPoints[0].chunkPosZ - rad - 1);
                    maxY = (int)(areaPoints[0].chunkPosY + rad + 1);
                    maxZ = (int)(areaPoints[0].chunkPosZ + rad + 1);
                    for(int y = Math.max(0, minY); y <= maxY && y < 256; y++) {
                        for(int z = minZ; z <= maxZ; z++) {
                            if(PneumaticCraftUtils.distBetween(areaPoints[0].chunkPosY, areaPoints[0].chunkPosZ, y, z) <= rad) {
                                for(int x = minX; x <= maxX; x++) {
                                    area.add(new ChunkPosition(x, y, z));
                                }
                            }
                        }
                    }
                }
                break;
            case Y_CYLINDER:
                if(areaPoints[1] != null) {
                    double rad = areaPoints[1] != null ? PneumaticCraftUtils.distBetween(areaPoints[0].chunkPosX, areaPoints[0].chunkPosZ, areaPoints[1].chunkPosX, areaPoints[1].chunkPosZ) : 0;
                    minX = (int)(areaPoints[0].chunkPosX - rad - 1);
                    minZ = (int)(areaPoints[0].chunkPosZ - rad - 1);
                    maxX = (int)(areaPoints[0].chunkPosX + rad + 1);
                    maxZ = (int)(areaPoints[0].chunkPosZ + rad + 1);
                    for(int x = minX; x <= maxX; x++) {
                        for(int z = minZ; z <= maxZ; z++) {
                            if(PneumaticCraftUtils.distBetween(areaPoints[0].chunkPosX, areaPoints[0].chunkPosZ, x, z) <= rad) {
                                for(int y = Math.max(0, minY); y <= maxY && y < 256; y++) {
                                    area.add(new ChunkPosition(x, y, z));
                                }
                            }
                        }
                    }
                }
                break;
            case Z_CYLINDER:
                if(areaPoints[1] != null) {
                    double rad = areaPoints[1] != null ? PneumaticCraftUtils.distBetween(areaPoints[0].chunkPosX, areaPoints[0].chunkPosY, areaPoints[1].chunkPosX, areaPoints[1].chunkPosY) : 0;
                    minX = (int)(areaPoints[0].chunkPosX - rad - 1);
                    minY = (int)(areaPoints[0].chunkPosY - rad - 1);
                    maxX = (int)(areaPoints[0].chunkPosX + rad + 1);
                    maxY = (int)(areaPoints[0].chunkPosY + rad + 1);
                    for(int x = minX; x <= maxX; x++) {
                        for(int y = Math.max(0, minY); y <= maxY && y < 256; y++) {
                            if(PneumaticCraftUtils.distBetween(areaPoints[0].chunkPosX, areaPoints[0].chunkPosY, x, y) <= rad) {
                                for(int z = minZ; z <= maxZ; z++) {
                                    area.add(new ChunkPosition(x, y, z));
                                }
                            }
                        }
                    }
                }
                break;
            case X_PYRAMID:
                if(areaPoints[1] != null && areaPoints[1].chunkPosX != areaPoints[0].chunkPosX) {
                    Vec3 lineVec = Vec3.createVectorHelper(areaPoints[1].chunkPosX - areaPoints[0].chunkPosX, areaPoints[1].chunkPosY - areaPoints[0].chunkPosY, areaPoints[1].chunkPosZ - areaPoints[0].chunkPosZ).normalize();
                    lineVec.yCoord /= lineVec.xCoord;
                    lineVec.zCoord /= lineVec.xCoord;
                    double curY = areaPoints[0].chunkPosY - lineVec.yCoord;
                    int x = areaPoints[0].chunkPosX + (areaPoints[1].chunkPosX > areaPoints[0].chunkPosX ? -1 : 1);
                    double curZ = areaPoints[0].chunkPosZ - lineVec.zCoord;
                    while(x != areaPoints[1].chunkPosX) {

                        x += areaPoints[1].chunkPosX > areaPoints[0].chunkPosX ? 1 : -1;
                        curY += lineVec.yCoord;
                        curZ += lineVec.zCoord;

                        int dY = Math.abs((int)(curY - areaPoints[0].chunkPosY));
                        int dZ = Math.abs((int)(curZ - areaPoints[0].chunkPosZ));
                        for(int y = areaPoints[0].chunkPosY - dY; y <= areaPoints[0].chunkPosY + dY; y++) {
                            for(int z = areaPoints[0].chunkPosZ - dZ; z <= areaPoints[0].chunkPosZ + dZ; z++) {
                                if(y > 0 && y < 256) area.add(new ChunkPosition(x, y, z));
                            }
                        }
                    }
                }
                break;
            case Y_PYRAMID:
                if(areaPoints[1] != null && areaPoints[1].chunkPosY != areaPoints[0].chunkPosY) {
                    Vec3 lineVec = Vec3.createVectorHelper(areaPoints[1].chunkPosX - areaPoints[0].chunkPosX, areaPoints[1].chunkPosY - areaPoints[0].chunkPosY, areaPoints[1].chunkPosZ - areaPoints[0].chunkPosZ).normalize();
                    lineVec.xCoord /= lineVec.yCoord;
                    lineVec.zCoord /= lineVec.yCoord;
                    double curX = areaPoints[0].chunkPosX - lineVec.xCoord;
                    int y = areaPoints[0].chunkPosY + (areaPoints[1].chunkPosY > areaPoints[0].chunkPosY ? -1 : 1);
                    double curZ = areaPoints[0].chunkPosZ - lineVec.zCoord;
                    while(y != areaPoints[1].chunkPosY) {

                        y += areaPoints[1].chunkPosY > areaPoints[0].chunkPosY ? 1 : -1;
                        curX += lineVec.xCoord;
                        curZ += lineVec.zCoord;

                        int dX = Math.abs((int)(curX - areaPoints[0].chunkPosX));
                        int dZ = Math.abs((int)(curZ - areaPoints[0].chunkPosZ));
                        for(int x = areaPoints[0].chunkPosX - dX; x <= areaPoints[0].chunkPosX + dX; x++) {
                            for(int z = areaPoints[0].chunkPosZ - dZ; z <= areaPoints[0].chunkPosZ + dZ; z++) {
                                if(y > 0 && y < 256) area.add(new ChunkPosition(x, y, z));
                            }
                        }
                    }
                }
                break;
            case Z_PYRAMID:
                if(areaPoints[1] != null && areaPoints[1].chunkPosZ != areaPoints[0].chunkPosZ) {
                    Vec3 lineVec = Vec3.createVectorHelper(areaPoints[1].chunkPosX - areaPoints[0].chunkPosX, areaPoints[1].chunkPosY - areaPoints[0].chunkPosY, areaPoints[1].chunkPosZ - areaPoints[0].chunkPosZ).normalize();
                    lineVec.xCoord /= lineVec.zCoord;
                    lineVec.yCoord /= lineVec.zCoord;
                    double curX = areaPoints[0].chunkPosX - lineVec.xCoord;
                    int z = areaPoints[0].chunkPosZ + (areaPoints[1].chunkPosZ > areaPoints[0].chunkPosZ ? -1 : 1);
                    double curY = areaPoints[0].chunkPosY - lineVec.yCoord;
                    while(z != areaPoints[1].chunkPosZ) {

                        z += areaPoints[1].chunkPosZ > areaPoints[0].chunkPosZ ? 1 : -1;
                        curX += lineVec.xCoord;
                        curY += lineVec.yCoord;

                        int dX = Math.abs((int)(curX - areaPoints[0].chunkPosX));
                        int dY = Math.abs((int)(curY - areaPoints[0].chunkPosY));
                        for(int x = areaPoints[0].chunkPosX - dX; x <= areaPoints[0].chunkPosX + dX; x++) {
                            for(int y = areaPoints[0].chunkPosY - dY; y <= areaPoints[0].chunkPosY + dY; y++) {
                                if(y > 0 && y < 256) area.add(new ChunkPosition(x, y, z));
                            }
                        }
                    }
                }
                break;
            case GRID:
                if(areaPoints[1] == null || areaPoints[0].equals(areaPoints[1]) || typeInfo <= 0) {
                    area.add(areaPoints[0]);
                } else {
                    int interval = typeInfo;
                    for(int x = areaPoints[0].chunkPosX; areaPoints[0].chunkPosX < areaPoints[1].chunkPosX ? x <= areaPoints[1].chunkPosX : x >= areaPoints[1].chunkPosX; x += (areaPoints[0].chunkPosX < areaPoints[1].chunkPosX ? 1 : -1) * interval) {
                        for(int y = areaPoints[0].chunkPosY; areaPoints[0].chunkPosY < areaPoints[1].chunkPosY ? y <= areaPoints[1].chunkPosY : y >= areaPoints[1].chunkPosY; y += (areaPoints[0].chunkPosY < areaPoints[1].chunkPosY ? 1 : -1) * interval) {
                            for(int z = areaPoints[0].chunkPosZ; areaPoints[0].chunkPosZ < areaPoints[1].chunkPosZ ? z <= areaPoints[1].chunkPosZ : z >= areaPoints[1].chunkPosZ; z += (areaPoints[0].chunkPosZ < areaPoints[1].chunkPosZ ? 1 : -1) * interval) {
                                if(y > 0 && y < 256) area.add(new ChunkPosition(x, y, z));
                            }
                        }
                    }
                }
                break;
            case RANDOM:
                type = EnumAreaType.FILL;

                Set<ChunkPosition> filledArea = new HashSet<ChunkPosition>();
                getArea(filledArea);
                type = EnumAreaType.RANDOM;
                if(typeInfo >= filledArea.size()) {
                    area.addAll(filledArea);
                    return;
                }
                Random rand = new Random();
                Set<Integer> randomIndexes = new HashSet<Integer>();
                while(randomIndexes.size() < typeInfo) {
                    randomIndexes.add(rand.nextInt(filledArea.size()));
                }
                int curIndex = 0;
                for(ChunkPosition pos : filledArea) {
                    if(randomIndexes.contains(curIndex)) area.add(pos);
                    curIndex++;
                }
                break;
        }
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
        tag.setInteger("typeInfo", typeInfo);
        tag.setString("coord1Variable", coord1Variable);
        tag.setString("coord2Variable", coord2Variable);
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
        typeInfo = tag.getInteger("typeInfo");
        coord1Variable = tag.getString("coord1Variable");
        coord2Variable = tag.getString("coord2Variable");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer){
        return new GuiProgWidgetArea(this, guiProgrammer);
    }

    @Override
    public WidgetDifficulty getDifficulty(){
        return WidgetDifficulty.EASY;
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.CREEPER_PLANT_DAMAGE;
    }

    public String getCoord1Variable(){
        return coord1Variable;
    }

    public void setCoord1Variable(String coord1Variable){
        this.coord1Variable = coord1Variable;
    }

    public String getCoord2Variable(){
        return coord2Variable;
    }

    public void setCoord2Variable(String coord2Variable){
        this.coord2Variable = coord2Variable;
    }

    @Override
    public void setAIManager(DroneAIManager aiManager){
        this.aiManager = aiManager;
    }

    @Override
    public void addVariables(Set<String> variables){
        variables.add(coord1Variable);
        variables.add(coord2Variable);
    }

}
