package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.common.progwidgets.area.*;
import me.desht.pneumaticcraft.common.progwidgets.area.AreaType.AreaTypeWidget;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.Entity;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * The Area widget itself
 */
public class ProgWidgetArea extends ProgWidget implements IAreaProvider, IVariableWidget {
    public int x1, y1, z1, x2, y2, z2;
    private String coord1Variable = "", coord2Variable = "";
    private DroneAIManager aiManager;
    private IVariableProvider variableProvider;
    public AreaType type = new AreaTypeBox();

    private static final Map<String, Supplier<? extends AreaType>> areaTypes = new LinkedHashMap<>(); // We want to preserve order in the GUI
    private static final Map<Class<? extends AreaType>, String> typeToIDs = new HashMap<>();

    static {
        register(AreaTypeBox.ID, AreaTypeBox.class, AreaTypeBox::new);
        register(AreaTypeSphere.ID, AreaTypeSphere.class, AreaTypeSphere::new);
        register(AreaTypeLine.ID, AreaTypeLine.class, AreaTypeLine::new);
        register(AreaTypeWall.ID, AreaTypeWall.class, AreaTypeWall::new);
        register(AreaTypeCylinder.ID, AreaTypeCylinder.class, AreaTypeCylinder::new);
        register(AreaTypePyramid.ID, AreaTypePyramid.class, AreaTypePyramid::new);
        register(AreaTypeGrid.ID, AreaTypeGrid.class, AreaTypeGrid::new);
        register(AreaTypeRandom.ID, AreaTypeRandom.class, AreaTypeRandom::new);
    }

    public ProgWidgetArea() {
        super(ModProgWidgets.AREA);
    }

    private static <T extends AreaType> void register(String id, Class<T> clazz, Supplier<T> creator) {
        if (areaTypes.containsKey(id)) {
            throw new IllegalStateException("Area type " + clazz + " could not be registered, duplicate id: " + id);
        }

        areaTypes.put(id, creator);
        typeToIDs.put(clazz, id);
    }

    public static List<AreaType> getAllAreaTypes() {
        return areaTypes.values().stream().map(Supplier::get).collect(Collectors.toList());
    }

    public static ProgWidgetArea fromPosition(BlockPos p1) {
        return fromPositions(p1, p1);
    }

    public static ProgWidgetArea fromPosition(BlockPos p1, int expand) {
        return fromPosition(p1, expand, expand, expand);
    }

    public static ProgWidgetArea fromPosition(BlockPos p1, int expandX, int expandY, int expandZ) {
        int x = expandX / 2;
        int y = expandY / 2;
        int z = expandZ / 2;
        return fromPositions(p1.add(-x, -y, -z), p1.add(x, y, z));
    }

    public static ProgWidgetArea fromPositions(BlockPos p1, BlockPos p2) {
        ProgWidgetArea area = new ProgWidgetArea();
        area.setP1(p1);
        area.setP2(p2);
        return area;
    }

    @Override
    public String getExtraStringInfo() {
        StringBuilder res = new StringBuilder();
        if (!coord1Variable.isEmpty()) {
            res.append("\"").append(coord1Variable).append("\"");
        } else if (x1 != 0 && y1 != 0 && z1 != 0) {
            res.append(String.format("%d, %d, %d", x1, y1, z1));
        }
        if (res.length() > 0) res.append("${br}");
        if (!coord2Variable.isEmpty()) {
            res.append("\"").append(coord2Variable).append("\"");
        } else if (x2 != 0 && y2 != 0 && z2 != 0) {
            res.append(String.format("%d, %d, %d", x2, y2, z2));
        }
        return res.toString();
    }

    @Override
    public void getTooltip(List<ITextComponent> curTooltip) {
        super.getTooltip(curTooltip);

        String c1;
        if (coord1Variable.equals("")) {
            c1 = x1 != 0 || y1 != 0 || z1 != 0 ? String.format("P1: [ %d, %d, %d ]", x1, y1, z1) : null;
        } else {
            c1 = "P1: var \"" + coord1Variable + "\"";
        }
        String c2;
        if (coord2Variable.equals("")) {
            c2 = x2 != 0 || y2 != 0 || z2 != 0 ? String.format("P2: [ %d, %d, %d ]", x2, y2, z2) : null;
        } else {
            c2 = "P2: var \"" + coord2Variable + "\"";
        }

        if (c1 != null) curTooltip.add(new StringTextComponent(c1));
        if (c2 != null) curTooltip.add(new StringTextComponent(c2));

//        if (c1 == null) {
//            c1 = c2;
//            c2 = null;
//        }
//
//        if (c1 != null) {
//            if (c2 != null) {
//                curTooltip.add(new StringTextComponent("Contains the points:"));
//                curTooltip.add(new StringTextComponent(c1.replace("%s", "1")));
//                curTooltip.add(new StringTextComponent(c2.replace("%s", "2")));
//            } else {
//                curTooltip.add(new StringTextComponent("Contains the point:"));
//                curTooltip.add(new StringTextComponent(c1.replace("%s", "1")));
//            }
//        }

        addAreaTypeTooltip(curTooltip);
    }

    public void addAreaTypeTooltip(List<ITextComponent> curTooltip) {
        curTooltip.add(xlate("pneumaticcraft.gui.progWidget.area.type").append(xlate(type.getTranslationKey())));

        List<AreaTypeWidget> widgets = new ArrayList<>();
        type.addUIWidgets(widgets);
        for (AreaTypeWidget widget : widgets) {
            curTooltip.add(xlate(widget.title).appendString(" ").appendString(widget.getCurValue()));
        }
    }

    @Override
    public void addErrors(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (coord1Variable.equals("") && coord2Variable.equals("") && x1 == 0 && y1 == 0 && z1 == 0 && x2 == 0 && y2 == 0 && z2 == 0) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.area.error.noArea"));
        }
    }

    public void setP1(BlockPos p) {
        x1 = p.getX();
        y1 = p.getY();
        z1 = p.getZ();
    }

    public void setP2(BlockPos p) {
        x2 = p.getX();
        y2 = p.getY();
        z2 = p.getZ();
    }

    private BlockPos[] getAreaPoints() {
        BlockPos c1;
        if (coord1Variable.equals("")) {
            c1 =    x1 != 0 || y1 != 0 || z1 != 0 ? new BlockPos(x1, y1, z1) : null;
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
    public ProgWidgetType<?> returnType() {
        return ModProgWidgets.AREA;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.AREA);
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
        final int maxSize = PNCConfig.Common.General.maxProgrammingArea;
        if (size > maxSize) { // Prevent memory problems when getting to ridiculous areas.
            if (aiManager != null) {
                // We still need to do run-time checks:
                // 1) Drones programmed before the compile-time validation was added
                // 2) Programs using variables where we don't necessarily have the values at compile-time
                IDroneBase drone = aiManager.getDrone();
                Log.warning(String.format("Drone @ %s (DIM %s) was killed due to excessively large area (%d > %d). See 'I:maxProgrammingArea' in config.",
                        drone.getDronePos().toString(), drone.world().getDimensionKey().getLocation().toString(), size, maxSize));
                drone.overload("areaTooLarge", maxSize);
                return;
            }
            // We're in the Programmer (no AI manager).  Continue to update the area,
            // but don't let it grow without bounds.
        }

        Consumer<BlockPos> addFunc = p -> {
            if (p.getY() >= 0 && p.getY() < 256 && area.add(p) && area.size() > maxSize) {
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

    List<Entity> getEntitiesWithinArea(World world, Predicate<? super Entity> predicate) {
        AxisAlignedBB aabb = getAABB();
        return aabb != null ? world.getEntitiesInAABBexcluding(null, aabb, predicate) : new ArrayList<>();
    }

    @Override
    public void writeToPacket(PacketBuffer buf) {
        super.writeToPacket(buf);
        buf.writeInt(x1);
        buf.writeInt(y1);
        buf.writeInt(z1);
        buf.writeInt(x2);
        buf.writeInt(y2);
        buf.writeInt(z2);
        buf.writeString(typeToIDs.get(type.getClass()));
        type.writeToPacket(buf);
        buf.writeString(coord1Variable);
        buf.writeString(coord2Variable);
    }

    @Override
    public void readFromPacket(PacketBuffer buf) {
        super.readFromPacket(buf);
        x1 = buf.readInt();
        y1 = buf.readInt();
        z1 = buf.readInt();
        x2 = buf.readInt();
        y2 = buf.readInt();
        z2 = buf.readInt();
        type = createType(buf.readString(32));
        type.readFromPacket(buf);
        coord1Variable = buf.readString(256);
        coord2Variable = buf.readString(256);
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        tag.putInt("x1", x1);
        tag.putInt("y1", y1);
        tag.putInt("z1", z1);
        tag.putInt("x2", x2);
        tag.putInt("y2", y2);
        tag.putInt("z2", z2);

        String typeId = typeToIDs.get(type.getClass());
        if (typeId == null) {
            Log.error("No type id for area type " + type + "! Substituting Box.");
            typeId = AreaTypeBox.ID;
        } else {
            type.writeToNBT(tag);
        }
        tag.putString("type", typeId);

        tag.putString("coord1Variable", coord1Variable);
        tag.putString("coord2Variable", coord2Variable);
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        x1 = tag.getInt("x1");
        y1 = tag.getInt("y1");
        z1 = tag.getInt("z1");
        x2 = tag.getInt("x2");
        y2 = tag.getInt("y2");
        z2 = tag.getInt("z2");

        type = createType(tag.getString("type"));
        type.readFromNBT(tag);

        coord1Variable = tag.getString("coord1Variable");
        coord2Variable = tag.getString("coord2Variable");
    }

    public static AreaType createType(String id) {
        Supplier<? extends AreaType> creator = areaTypes.get(id);
        if (creator != null) {
            return creator.get();
        } else {
            Log.error("No Area type found for id '" + id + "'! Substituting Box!");
            return new AreaTypeBox();
        }
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.EASY;
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.GREEN;
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

    @Override
    public void addVariables(Set<String> variables) {
        variables.add(coord1Variable);
        variables.add(coord2Variable);
    }

}
