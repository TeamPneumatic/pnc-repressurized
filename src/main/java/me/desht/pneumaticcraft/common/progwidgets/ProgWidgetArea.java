package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.programmer.GuiProgWidgetArea;
import me.desht.pneumaticcraft.common.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.item.ItemPlastic;
import me.desht.pneumaticcraft.common.progwidgets.area.*;
import me.desht.pneumaticcraft.common.progwidgets.area.AreaType.AreaTypeWidget;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ProgWidgetArea extends ProgWidget implements IAreaProvider, IVariableWidget {
    public int x1, y1, z1, x2, y2, z2;
    private String coord1Variable = "", coord2Variable = "";
    private DroneAIManager aiManager;
    private IVariableProvider variableProvider;
    public AreaType type = new AreaTypeBox();

    private static final Map<String, Supplier<? extends AreaType>> areaTypes = new LinkedHashMap<>(); //We want to preserve order in the GUI
    private static final Map<Class<? extends AreaType>, String> typeToIDs = new HashMap<>();
    
    /**
     * A way to map from the old to the new format
     * Remove in 1.13.
     */
    @Deprecated
    private static final Map<EnumAreaType, String> oldFormatToAreaTypes = new HashMap<>();
    
    static{
        register(AreaTypeBox.ID, AreaTypeBox.class, AreaTypeBox::new, EnumAreaType.FILL, EnumAreaType.WALL, EnumAreaType.FRAME);
        register(AreaTypeSphere.ID, AreaTypeSphere.class, AreaTypeSphere::new, EnumAreaType.SPHERE);
        register(AreaTypeLine.ID, AreaTypeLine.class, AreaTypeLine::new, EnumAreaType.LINE);
        register(AreaTypeWall.ID, AreaTypeWall.class, AreaTypeWall::new, EnumAreaType.X_WALL, EnumAreaType.Y_WALL, EnumAreaType.Z_WALL);
        register(AreaTypeCylinder.ID, AreaTypeCylinder.class, AreaTypeCylinder::new, EnumAreaType.X_CYLINDER, EnumAreaType.Y_CYLINDER, EnumAreaType.Z_CYLINDER);
        register(AreaTypePyramid.ID, AreaTypePyramid.class, AreaTypePyramid::new, EnumAreaType.X_PYRAMID, EnumAreaType.Y_PYRAMID, EnumAreaType.Z_PYRAMID);
        register(AreaTypeGrid.ID, AreaTypeGrid.class, AreaTypeGrid::new, EnumAreaType.GRID);
        register(AreaTypeRandom.ID, AreaTypeRandom.class, AreaTypeRandom::new, EnumAreaType.RANDOM);
        if(oldFormatToAreaTypes.size() != EnumAreaType.values().length) throw new IllegalStateException("Not all old formats are handled!");
    }
    
    private static <T extends AreaType> void register(String id, Class<T> clazz, Supplier<T> creator, EnumAreaType... oldTypes){
        if(areaTypes.containsKey(id)){
            throw new IllegalStateException("Area type " + clazz + " could not be registered, duplicate id: " + id);
        }
        
        areaTypes.put(id, creator);
        typeToIDs.put(clazz, id);
        
        for(EnumAreaType oldType : oldTypes){
            oldFormatToAreaTypes.put(oldType, id);
        }
    }
   
    public static List<AreaType> getAllAreaTypes(){
        return areaTypes.values().stream().map(Supplier::get).collect(Collectors.toList());
    }
    
    @Deprecated
    public enum EnumAreaType {
        FILL("Filled"), FRAME("Frame"), WALL("Walls"), SPHERE("Sphere"), LINE("Line"), X_WALL("X-Wall"), Y_WALL(
                "Y-Wall"), Z_WALL("Z-Wall"), X_CYLINDER("X-Cylinder"), Y_CYLINDER("Y-Cylinder"), Z_CYLINDER(
                "Z-Cylinder"), X_PYRAMID("X-Pyramid"), Y_PYRAMID("Y-Pyramid"), Z_PYRAMID("Z-Pyramid"), GRID("Grid",
                true), RANDOM("Random", true);

        private final String name;
        public final boolean utilizesTypeInfo;

        EnumAreaType(String name) {
            this(name, false);
        }

        EnumAreaType(String name, boolean utilizesTypeInfo) {
            this.name = name;
            this.utilizesTypeInfo = utilizesTypeInfo;
        }

        @Override
        public String toString() {
            return name;
        }
    }
    
    public static ProgWidgetArea fromPosition(BlockPos p1){
        return fromPositions(p1, p1);
    }
    
    public static ProgWidgetArea fromPosAndExpansions(BlockPos p1, int expX, int expY, int expZ){
        int x = expX / 2;
        int y = expY / 2;
        int z = expZ / 2;
        return fromPositions(p1.add(-x, -y, -z), p1.add(x, y, z));
    }
    
    public static ProgWidgetArea fromPositions(BlockPos p1, BlockPos p2){
        ProgWidgetArea area = new ProgWidgetArea();
        area.setP1(p1);
        area.setP2(p2);
        return area;
    }

    @Override
    public void getTooltip(List<String> curTooltip) {
        super.getTooltip(curTooltip);

        String c1;
        if (coord1Variable.equals("")) {
            c1 = x1 != 0 || y1 != 0 || z1 != 0 ? "X%s: " + x1 + ", Y%s: " + y1 + ", Z%s: " + z1 : null;
        } else {
            c1 = "XYZ%s: \"" + coord1Variable + "\"";
        }
        String c2;
        if (coord2Variable.equals("")) {
            c2 = x2 != 0 || y2 != 0 || z2 != 0 ? "X%s: " + x2 + ", Y%s: " + y2 + ", Z%s: " + z2 : null;
        } else {
            c2 = "XYZ%s: \"" + coord2Variable + "\"";
        }
        if (c1 == null) {
            c1 = c2;
            c2 = null;
        }

        if (c1 != null) {
            if (c2 != null) {
                curTooltip.add("Contains the points:");
                curTooltip.add(c1.replace("%s", "1"));
                curTooltip.add(c2.replace("%s", "2"));
            } else {
                curTooltip.add("Contains the point:");
                curTooltip.add(c1.replace("%s", "1"));
            }
        }

        addAreaTypeTooltip(curTooltip);
    }
    
    public void addAreaTypeTooltip(List<String> curTooltip){
        curTooltip.add("Area type: " + type.getName());
        
        List<AreaTypeWidget> widgets = new ArrayList<>();
        type.addUIWidgets(widgets);
        for(AreaTypeWidget widget : widgets){
            curTooltip.add(String.format("%s %s", I18n.format(widget.title), widget.getCurValue()));
        }
    }

    @Override
    public void addErrors(List<String> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (coord1Variable.equals("") && coord2Variable.equals("") && x1 == 0 && y1 == 0 && z1 == 0 && x2 == 0 && y2 == 0 && z2 == 0) {
            curInfo.add("gui.progWidget.area.error.noArea");
        }
    }
    
    public void setP1(BlockPos p){
        x1 = p.getX();
        y1 = p.getY();
        z1 = p.getZ();
    }
    
    public void setP2(BlockPos p){
        x2 = p.getX();
        y2 = p.getY();
        z2 = p.getZ();
    }
    
    public void setAreaPoint(BlockPos p, int index){
        if(index == 0){
            setP1(p);
        }else{
            setP2(p);
        }
    }
    
    public BlockPos getRawAreaPoint(int index){
        return index == 0 ? new BlockPos(x1, y1, z1) : new BlockPos(x2, y2, z2);
    }

    private BlockPos[] getAreaPoints() {
        BlockPos c1;
        if (coord1Variable.equals("")) {
            c1 = x1 != 0 || y1 != 0 || z1 != 0 ? new BlockPos(x1, y1, z1) : null;
        } else {
            c1 = variableProvider != null ? variableProvider.getCoordinate(coord1Variable) : null;
        }
        BlockPos c2;
        if (coord2Variable.equals("")) {
            c2 = x2 != 0 || y2 != 0 || z2 != 0 ? new BlockPos(x2, y2, z2) : null;
        } else {
            c2 = variableProvider != null ? variableProvider.getCoordinate(coord2Variable) : null;
        }
        if (c1 == null && c2 == null) {
            return new BlockPos[]{null, null};
        } else if (c1 == null) {
            return new BlockPos[]{c2, null};
        } else if (c2 == null) {
            return new BlockPos[]{c1, null};
        } else {
            return new BlockPos[]{c1, c2};
        }
    }

    @Override
    public boolean hasStepInput() {
        return false;
    }

    @Override
    public Class<? extends IProgWidget> returnType() {
        return ProgWidgetArea.class;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters() {
        return new Class[]{ProgWidgetArea.class};
    }

    @Override
    public String getWidgetString() {
        return "area";
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_AREA;
    }
    
    @Override
    public void getArea(Set<BlockPos> area) {
        getArea(area, type);
    }

    public void getArea(Set<BlockPos> area, AreaType areaType) {
        BlockPos[] areaPoints = getAreaPoints();
        if (areaPoints[0] == null && areaPoints[1] == null) return;

        int minX;
        int minY;
        int minZ;
        int maxX;
        int maxY;
        int maxZ;
        if (areaPoints[1] != null) {
            minX = Math.min(areaPoints[0].getX(), areaPoints[1].getX());
            minY = Math.min(areaPoints[0].getY(), areaPoints[1].getY());
            minZ = Math.min(areaPoints[0].getZ(), areaPoints[1].getZ());
            maxX = Math.max(areaPoints[0].getX(), areaPoints[1].getX());
            maxY = Math.max(areaPoints[0].getY(), areaPoints[1].getY());
            maxZ = Math.max(areaPoints[0].getZ(), areaPoints[1].getZ());
        } else {
            minX = maxX = areaPoints[0].getX();
            minY = maxY = areaPoints[0].getY();
            minZ = maxZ = areaPoints[0].getZ();
        }


        // Size validation is now done at compile-time - see ProgWidgetAreaItemBase#addErrors
        // https://github.com/TeamPneumatic/pnc-repressurized/issues/95
        // https://github.com/TeamPneumatic/pnc-repressurized/issues/104
        int size = (maxX - minX) * (maxY - minY) * (maxZ - minZ);
        if (size > ConfigHandler.general.maxProgrammingArea) { // Prevent memory problems when getting to ridiculous areas.
            if (aiManager != null) {
                // We still need to do run-time checks:
                // 1) Drones programmed before the compile-time validation was added
                // 2) Programs using variables where we don't necessarily have the values at compile-time
                IDroneBase drone = aiManager.getDrone();
                Log.warning(String.format("Drone @ %s (DIM %d) was killed due to excessively large area (%d > %d). See 'I:maxProgrammingArea' in config.",
                        drone.getDronePos().toString(), drone.world().provider.getDimension(), size, ConfigHandler.general.maxProgrammingArea));
                drone.overload("areaTooLarge", ConfigHandler.general.maxProgrammingArea);
                return;
            }
            // We're in the Programmer (no AI manager).  Continue to update the area,
            // but don't let it grow without bounds.
        }

        Consumer<BlockPos> addFunc = p -> {
            if (p.getY() >= 0 && p.getY() < 256 && area.add(p) && area.size() > ConfigHandler.general.maxProgrammingArea){
                throw new AreaTooBigException();
            }
        };
        BlockPos p1 = areaPoints[0];
        BlockPos p2 = areaPoints[1] != null ? areaPoints[1] : p1;

        try {
            areaType.addArea(addFunc, p1, p2, minX, minY, minZ, maxX, maxY, maxZ);
        } catch (AreaTooBigException ignored) {
        }
    } 

    private AxisAlignedBB getAABB() {
        BlockPos[] areaPoints = getAreaPoints();
        if (areaPoints[0] == null) return null;
        int minX;
        int minY;
        int minZ;
        int maxX;
        int maxY;
        int maxZ;
        if (areaPoints[1] != null) {
            minX = Math.min(areaPoints[0].getX(), areaPoints[1].getX());
            minY = Math.min(areaPoints[0].getY(), areaPoints[1].getY());
            minZ = Math.min(areaPoints[0].getZ(), areaPoints[1].getZ());
            maxX = Math.max(areaPoints[0].getX(), areaPoints[1].getX());
            maxY = Math.max(areaPoints[0].getY(), areaPoints[1].getY());
            maxZ = Math.max(areaPoints[0].getZ(), areaPoints[1].getZ());
        } else {
            minX = maxX = areaPoints[0].getX();
            minY = maxY = areaPoints[0].getY();
            minZ = maxZ = areaPoints[0].getZ();
        }
        return new AxisAlignedBB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
    }

    public List<Entity> getEntitiesWithinArea(World world, Predicate<? super Entity> predicate) {
        AxisAlignedBB aabb = getAABB();
        return aabb != null ? world.getEntitiesInAABBexcluding(null, aabb, predicate::test) : new ArrayList<>();
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("x1", x1);
        tag.setInteger("y1", y1);
        tag.setInteger("z1", z1);
        tag.setInteger("x2", x2);
        tag.setInteger("y2", y2);
        tag.setInteger("z2", z2);
        
        String typeId = typeToIDs.get(type.getClass());
        if(typeId == null){
            Log.error("No type id for area type " + type + "! Substituting Box.");
            typeId = AreaTypeBox.ID;
        }else{
            type.writeToNBT(tag);
        }
        tag.setString("type", typeId);
        
        tag.setString("coord1Variable", coord1Variable);
        tag.setString("coord2Variable", coord2Variable);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        x1 = tag.getInteger("x1");
        y1 = tag.getInteger("y1");
        z1 = tag.getInteger("z1");
        x2 = tag.getInteger("x2");
        y2 = tag.getInteger("y2");
        z2 = tag.getInteger("z2");
        
        if(tag.getTag("type") instanceof NBTPrimitive){ 
            //Old format
            EnumAreaType oldType = EnumAreaType.values()[tag.getInteger("type")];
            int typeInfo = tag.getInteger("typeInfo");
            type = convertFromLegacyFormat(oldType, typeInfo);
        }else{
            //New format
            type = createType(tag.getString("type"));
            type.readFromNBT(tag);
        }
        
        coord1Variable = tag.getString("coord1Variable");
        coord2Variable = tag.getString("coord2Variable");
    }
    
    private static AreaType createType(String id){
        Supplier<? extends AreaType> creator = areaTypes.get(id);
        if(creator != null){
            return creator.get();
        }else{
            Log.error("No Area type found for id '" + id + "'! Substituting Box!");
            return new AreaTypeBox();
        }
    }
    
    /**
     * Remove in 1.13
     * @param oldType
     * @param typeInfo
     * @return
     */
    @Deprecated
    public static AreaType convertFromLegacyFormat(EnumAreaType oldType, int typeInfo){
        String newTypeId = oldFormatToAreaTypes.get(oldType);
        if(newTypeId == null){
            Log.error("No area converter found for EnumAreaType " + oldType + "! Substituting Box.");
            return new AreaTypeBox();
        }else{
            AreaType type = createType(newTypeId);
            type.convertFromLegacy(oldType, typeInfo);
            return type;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer) {
        return new GuiProgWidgetArea(this, guiProgrammer);
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.EASY;
    }

    @Override
    public int getCraftingColorIndex() {
        return ItemPlastic.GREEN;
    }

    public String getCoord1Variable() {
        return coord1Variable;
    }

    public void setCoord1Variable(String coord1Variable) {
        this.coord1Variable = coord1Variable;
    }

    public String getCoord2Variable() {
        return coord2Variable;
    }

    public void setCoord2Variable(String coord2Variable) {
        this.coord2Variable = coord2Variable;
    }

    @Override
    public void setAIManager(DroneAIManager aiManager) {
        this.aiManager = aiManager;
        this.variableProvider = aiManager;
    }
    
    public void setVariableProvider(IVariableProvider variableProvider){
        this.variableProvider = variableProvider;
    }

    @Override
    public void addVariables(Set<String> variables) {
        variables.add(coord1Variable);
        variables.add(coord2Variable);
    }

}
