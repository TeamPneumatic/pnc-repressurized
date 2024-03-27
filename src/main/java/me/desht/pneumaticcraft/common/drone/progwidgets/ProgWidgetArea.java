/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.drone.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.drone.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.drone.progwidgets.area.*;
import me.desht.pneumaticcraft.common.registry.ModProgWidgets;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

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
    private DroneAIManager aiManager;
    private final BlockPos[] pos = new BlockPos[] { null, null };
    private final String[] varNames = new String[] { "", "" };
    public AreaType type = new AreaTypeBox();
    private IVariableProvider variableProvider;
    private UUID playerID;  // for player-global variable context

    // map string area types to internal numeric ID's (for more efficient sync)
    private static final Map<String, Integer> areaTypeToID = new HashMap<>();
    // collection of area type factories, indexed by internal ID
    private static final List<Supplier<? extends AreaType>> areaTypeFactories = new ArrayList<>();

    static {
        register(AreaTypeBox.ID, AreaTypeBox::new);
        register(AreaTypeSphere.ID, AreaTypeSphere::new);
        register(AreaTypeLine.ID, AreaTypeLine::new);
        register(AreaTypeWall.ID, AreaTypeWall::new);
        register(AreaTypeCylinder.ID, AreaTypeCylinder::new);
        register(AreaTypePyramid.ID, AreaTypePyramid::new);
        register(AreaTypeGrid.ID, AreaTypeGrid::new);
        register(AreaTypeRandom.ID, AreaTypeRandom::new);
        register(AreaTypeTorus.ID, AreaTypeTorus::new);
    }

    public ProgWidgetArea() {
        super(ModProgWidgets.AREA.get());
    }

    private static <T extends AreaType> void register(String id, Supplier<T> factory) {
        if (areaTypeToID.containsKey(id)) {
            throw new IllegalStateException("Area type " + id + " could not be registered, duplicate id");
        }
        areaTypeFactories.add(factory);
        areaTypeToID.put(id, areaTypeFactories.size() - 1);
    }

    public static List<AreaType> getAllAreaTypes() {
        return areaTypeFactories.stream().map(Supplier::get).collect(Collectors.toList());
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
        return fromPositions(p1.offset(-x, -y, -z), p1.offset(x, y, z));
    }

    public static ProgWidgetArea fromPositions(BlockPos p1, BlockPos p2) {
        ProgWidgetArea area = new ProgWidgetArea();
        area.setPos(0, p1);
        area.setPos(1, p2);
        return area;
    }

    @Override
    public List<Component> getExtraStringInfo() {
        List<Component> res = new ArrayList<>();

        if (varNames[0].isEmpty() && varNames[1].isEmpty() && PneumaticCraftUtils.isValidPos(pos[0]) && pos[0].equals(pos[1])) {
            res.add(Component.literal(PneumaticCraftUtils.posToString(pos[0])));
        } else {
            if (!varNames[0].isEmpty()) {
                res.add(Component.literal("\"" + varNames[0] + "\""));
            } else if (PneumaticCraftUtils.isValidPos(pos[0])) {
                res.add(Component.literal(PneumaticCraftUtils.posToString(pos[0])));
            }
            if (!varNames[1].isEmpty() && !varNames[1].equals(varNames[0])) {
                res.add(Component.literal("\"" + varNames[1] + "\""));
            } else if (PneumaticCraftUtils.isValidPos(pos[1]) && !pos[1].equals(pos[0])) {
                res.add(Component.literal(PneumaticCraftUtils.posToString(pos[1])));
            }
            if (res.size() == 2) {
                MutableComponent c = xlate(type.getTranslationKey());
                List<AreaType.AreaTypeWidget> widgets = new ArrayList<>();
                type.addUIWidgets(widgets);
                widgets.forEach(w -> c.append("/").append(w.getDisplayName()));
                res.add(c);
            }
        }
        return res;
    }

    @Override
    public void getTooltip(List<Component> curTooltip) {
        super.getTooltip(curTooltip);

        if (!varNames[0].isEmpty() && varNames[0].equals(varNames[1])) {
            curTooltip.add(Component.literal(String.format("Var \"%s\"", varNames[0])).withStyle(ChatFormatting.YELLOW));
        } else if (PneumaticCraftUtils.isValidPos(pos[0]) && pos[0].equals(pos[1])) {
            curTooltip.add(Component.literal("P1: ").append(Component.literal(PneumaticCraftUtils.posToString(pos[0])).withStyle(ChatFormatting.YELLOW)));
        } else {
            int n = curTooltip.size();
            for (int i = 0; i < 2; i++) {
                String text = varNames[i].isEmpty() ?
                        pos[i] == null ? null : PneumaticCraftUtils.posToString(pos[i]) :
                        String.format("Var \"%s\"", varNames[i]);
                if (text != null) {
                    curTooltip.add(Component.literal("P" + (i + 1) + ": ").append(Component.literal(text).withStyle(ChatFormatting.YELLOW)));
                }
            }
            if (curTooltip.size() - n == 2) {
                addAreaTypeTooltip(curTooltip);
            }
        }
    }

    public void addAreaTypeTooltip(List<Component> curTooltip) {
        curTooltip.add(xlate("pneumaticcraft.gui.progWidget.area.type").append(xlate(type.getTranslationKey()).withStyle(ChatFormatting.YELLOW)));

        List<AreaType.AreaTypeWidget> widgets = new ArrayList<>();
        type.addUIWidgets(widgets);
        for (AreaType.AreaTypeWidget widget : widgets) {
            curTooltip.add(xlate(widget.title).append(" ").append(widget.getDisplayName().copy().withStyle(ChatFormatting.YELLOW)));
        }
    }

    @Override
    public void addErrors(List<Component> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (varNames[0].isEmpty() && varNames[1].isEmpty() && pos[0] == null && pos[1] == null) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.area.error.noArea"));
        }
        if (!(type instanceof AreaTypeBox)) {
            IProgWidget p = this;
            while ((p = p.getParent()) != null) {
                ProgWidgetType<?> type = p.getType();
                if (type == ModProgWidgets.ENTITY_ATTACK.get() || type == ModProgWidgets.ENTITY_IMPORT.get()
                        || type == ModProgWidgets.ENTITY_RIGHT_CLICK.get() || type == ModProgWidgets.CONDITION_ENTITY.get()
                        || type == ModProgWidgets.PICKUP_ITEM.get()) {
                    curInfo.add(xlate("pneumaticcraft.gui.progWidget.area.error.onlyAreaTypeBox", xlate(p.getTranslationKey())));
                    break;
                }
            }
        }
    }

    private BlockPos[] getAreaPoints() {
        BlockPos[] points = new BlockPos[2];
        for (int i = 0; i < 2; i++) {
            if (varNames[i].isEmpty()) points[i] = pos[i];
            else points[i] = variableProvider != null ?
                    variableProvider.getCoordinate(playerID, varNames[i]).orElse(null) :
                    null;
        }
        if (points[0] == null && points[1] == null) {
            return new BlockPos[]{null, null};
        } else if (points[0] == null) {
            return new BlockPos[]{points[1], null};
        } else if (points[1] == null) {
            return new BlockPos[]{points[0], null};
        } else {
            return points;
        }
    }

    @Override
    public boolean hasStepInput() {
        return false;
    }

    @Override
    public ProgWidgetType<?> returnType() {
        return ModProgWidgets.AREA.get();
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.AREA.get());
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
        if (areaPoints[0] == null) return;

        int minX, minY, minZ;
        int maxX, maxY, maxZ;
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


        BlockPos p1 = areaPoints[0];
        BlockPos p2 = areaPoints[1] != null ? areaPoints[1] : p1;

        int size = ((maxX - minX) + 1) * ((maxY - minY) + 1) * ((maxZ - minZ) + 1);
        final int maxSize = ConfigHelper.common().general.maxProgrammingArea.get();
        if (size > maxSize) { // Prevent memory problems when getting to ridiculous areas.
            if (aiManager != null) {
                // We still need to do run-time checks:
                // 1) Drones programmed before the compile-time validation was added
                // 2) Programs using variables where we don't necessarily have the values at compile-time
                IDroneBase drone = aiManager.getDrone();
                Log.warning(String.format("Drone @ %s (DIM %s) was killed due to excessively large area (%d > %d). See 'maxProgrammingArea' in config.",
                        drone.getDronePos().toString(), drone.world().dimension().location(), size, maxSize));
                drone.overload("areaTooLarge", maxSize);
                return;
            }
            // We're in the Programmer (no AI manager).  Continue to update the area,
            // but don't let it grow without bounds.
        }

        Consumer<BlockPos> addFunc = p -> {
            if (/*p.getY() >= 0 && p.getY() < 256 &&*/ area.add(p) && area.size() > maxSize) {
                throw new AreaTooBigException();
            }
        };

        try {
            areaType.addArea(addFunc, p1, p2, minX, minY, minZ, maxX, maxY, maxZ);
        } catch (AreaTooBigException ignored) {
        }
    }

    private AABB getAABB() {
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
        return new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
    }

    List<Entity> getEntitiesWithinArea(Level world, Predicate<? super Entity> predicate) {
        AABB aabb = getAABB();
        return aabb != null ? world.getEntities((Entity) null, aabb, predicate) : new ArrayList<>();
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf) {
        super.writeToPacket(buf);
        BlockPos pos1 = getPos(0).orElse(BlockPos.ZERO);
        BlockPos pos2 = getPos(1).orElse(BlockPos.ZERO);
        buf.writeBlockPos(pos1);
        // looks weird but this ensures the vast majority of offsets can be encoded into one byte
        // (keep numbers positive for best varint results)
        BlockPos offset = pos1.subtract(pos2).offset(127, 127, 127);
        buf.writeBlockPos(offset);
        buf.writeVarInt(areaTypeToID.get(type.getName()));
        type.writeToPacket(buf);
        buf.writeUtf(varNames[0]);
        buf.writeUtf(varNames[1]);
    }

    @Override
    public void readFromPacket(FriendlyByteBuf buf) {
        super.readFromPacket(buf);
        pos[0] = buf.readBlockPos();
        BlockPos offset = buf.readBlockPos().offset(-127, -127, -127);
        pos[1] = pos[0].subtract(offset);
        type = createType(buf.readVarInt());
        type.readFromPacket(buf);
        varNames[0] = buf.readUtf(GlobalVariableManager.MAX_VARIABLE_LEN);
        varNames[1] = buf.readUtf(GlobalVariableManager.MAX_VARIABLE_LEN);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        getPos(0).ifPresent(pos -> tag.put("pos1", NbtUtils.writeBlockPos(pos)));
        getPos(1).ifPresent(pos -> tag.put("pos2", NbtUtils.writeBlockPos(pos)));
        tag.putString("type", type.getName());
        type.writeToNBT(tag);
        if (!varNames[0].isEmpty()) {
            tag.putString("var1", varNames[0]);
        } else {
            tag.remove("var1");
        }
        if (!varNames[1].isEmpty()) {
            tag.putString("var2", varNames[1]);
        } else {
            tag.remove("var2");
        }
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);

        if (tag.contains("x1")) {
            // legacy import
            pos[0] = new BlockPos(tag.getInt("x1"), tag.getInt("y1"), tag.getInt("z1"));
            pos[1] = new BlockPos(tag.getInt("x2"), tag.getInt("y2"), tag.getInt("z2"));
            varNames[0] = tag.getString("coord1Variable");
            varNames[1] = tag.getString("coord2Variable");
        } else {
            pos[0] = NbtUtils.readBlockPos(tag.getCompound("pos1"));
            pos[1] = NbtUtils.readBlockPos(tag.getCompound("pos2"));
            varNames[0] = tag.getString("var1");
            varNames[1] = tag.getString("var2");
        }
        type = createType(tag.getString("type"));
        type.readFromNBT(tag);
    }

    public static AreaType createType(String id) {
        if (!areaTypeToID.containsKey(id)) {
            Log.error("No Area type found for id '" + id + "'! Substituting Box!");
            return new AreaTypeBox();
        }
        return createType(areaTypeToID.get(id));
    }

    public static AreaType createType(int id) {
        return areaTypeFactories.get(id).get();
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.EASY;
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.GREEN;
    }

    public void setPos(int index, BlockPos newPos) {
        pos[index] = newPos;
    }

    public Optional<BlockPos> getPos(int index) {
        return Optional.ofNullable(pos[index]);
    }

    public String getVarName(int index) {
        return varNames[index];
    }

    public void setVarName(int index, String varName) {
        varNames[index] = varName;
    }

    @Override
    public void setAIManager(DroneAIManager aiManager) {
        this.aiManager = aiManager;
        this.variableProvider = aiManager;
    }

    @Override
    public void addVariables(Set<String> variables) {
        variables.add(varNames[0]);
        variables.add(varNames[1]);
    }

    public void setVariableProvider(IVariableProvider provider, UUID playerID) {
        this.variableProvider = provider;
        this.playerID = playerID;
    }
}
