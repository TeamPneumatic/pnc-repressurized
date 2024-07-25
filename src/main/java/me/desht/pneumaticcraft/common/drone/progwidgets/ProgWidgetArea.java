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
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.api.drone.area.AreaType;
import me.desht.pneumaticcraft.api.drone.area.AreaTypeSerializer;
import me.desht.pneumaticcraft.api.drone.area.AreaTypeWidget;
import me.desht.pneumaticcraft.api.misc.IVariableProvider;
import me.desht.pneumaticcraft.api.registry.PNCRegistries;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.drone.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.drone.progwidgets.area.AreaTooBigException;
import me.desht.pneumaticcraft.common.drone.progwidgets.area.AreaTypeBox;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * The Area widget itself
 */
public class ProgWidgetArea extends ProgWidget implements IAreaProvider, IVariableWidget {
    public static final MapCodec<ProgWidgetArea> CODEC = RecordCodecBuilder.mapCodec(builder ->
            baseParts(builder).and(builder.group(
                            BlockPos.CODEC.optionalFieldOf("pos1").forGetter(p -> p.getPos(0)),
                            BlockPos.CODEC.optionalFieldOf("pos2").forGetter(p -> p.getPos(1)),
                            AreaType.CODEC.fieldOf("area_type").forGetter(p -> p.areaType),
                            Codec.STRING.optionalFieldOf("var1", "").forGetter(p -> p.getVarName(0)),
                            Codec.STRING.optionalFieldOf("var2", "").forGetter(p -> p.getVarName(1))
                    )
            ).apply(builder, ProgWidgetArea::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ProgWidgetArea> STREAM_CODEC = StreamCodec.composite(
            PositionFields.STREAM_CODEC, ProgWidget::getPosition,
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC), p -> p.getPos(0),
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC), p -> p.getPos(1),
            AreaType.STREAM_CODEC, p -> p.areaType,
            ByteBufCodecs.STRING_UTF8, p -> p.getVarName(0),
            ByteBufCodecs.STRING_UTF8, p -> p.getVarName(1),
            ProgWidgetArea::new
    );

    private DroneAIManager aiManager;
    private final BlockPos[] pos = new BlockPos[] { null, null };
    private final String[] varNames = new String[] { "", "" };
    private AreaType areaType = new AreaTypeBox();
    private IVariableProvider variableProvider;
    private UUID playerID;  // for player-global variable context

    public ProgWidgetArea() {
        this(PositionFields.DEFAULT, Optional.empty(), Optional.empty(), new AreaTypeBox(), "", "");
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private ProgWidgetArea(PositionFields pos, Optional<BlockPos> pos1, Optional<BlockPos> pos2, AreaType areaType, String varName1, String varName2) {
        super(pos);

        setPos(0, pos1.orElse(null));
        setPos(1, pos2.orElse(null));
        this.areaType = areaType;
        setVarName(0, varName1);
        setVarName(1, varName2);
    }

    @Override
    public IProgWidget copyWidget() {
        return new ProgWidgetArea(getPosition(), getPos(0), getPos(1), areaType.copy(), getVarName(0), getVarName(1));
    }

    public static List<? extends AreaType> getAllAreaTypes() {
        return PNCRegistries.AREA_TYPE_SERIALIZER_REGISTRY.stream()
                .map(AreaTypeSerializer::createDefaultInstance).toList();
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
        return new ProgWidgetArea(PositionFields.DEFAULT, Optional.ofNullable(p1), Optional.ofNullable(p2), new AreaTypeBox(), "", "");
    }

    public AreaType getAreaType() {
        return areaType;
    }

    public void setAreaType(AreaType areaType) {
        this.areaType = areaType;
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
                MutableComponent c = xlate(areaType.getTranslationKey());
                List<AreaTypeWidget> widgets = new ArrayList<>();
                areaType.addUIWidgets(widgets);
                widgets.forEach(w -> c.append("/").append(w.getDisplayName()));
                res.add(c);
            }
        }
        return res;
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.AREA.get();
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
        curTooltip.add(xlate("pneumaticcraft.gui.progWidget.area.type").append(xlate(areaType.getTranslationKey()).withStyle(ChatFormatting.YELLOW)));

        List<AreaTypeWidget> widgets = new ArrayList<>();
        areaType.addUIWidgets(widgets);
        for (AreaTypeWidget widget : widgets) {
            curTooltip.add(xlate(widget.getTranslationKey()).append(" ").append(widget.getDisplayName().copy().withStyle(ChatFormatting.YELLOW)));
        }
    }

    @Override
    public void addErrors(List<Component> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (varNames[0].isEmpty() && varNames[1].isEmpty() && pos[0] == null && pos[1] == null) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.area.error.noArea"));
        }
        if (!(areaType instanceof AreaTypeBox)) {
            IProgWidget p = this;
            while ((p = p.getParent()) != null) {
                ProgWidgetType<?> type = p.getType();
                if (type == ModProgWidgetTypes.ENTITY_ATTACK.get() || type == ModProgWidgetTypes.ENTITY_IMPORT.get()
                        || type == ModProgWidgetTypes.ENTITY_RIGHT_CLICK.get() || type == ModProgWidgetTypes.CONDITION_ENTITY.get()
                        || type == ModProgWidgetTypes.PICKUP_ITEM.get()) {
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
        return ModProgWidgetTypes.AREA.get();
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgetTypes.AREA.get());
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_AREA;
    }

    @Override
    public Set<BlockPos> getArea(Set<BlockPos> area) {
        getArea(area, areaType);
        return area;
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
                IDrone drone = aiManager.getDrone();
                Log.warning("Drone @ {} (DIM {}) was killed due to excessively large area ({} > {}). See 'maxProgrammingArea' in config.",
                        drone.getDronePos().toString(), drone.getDroneLevel().dimension().location(), size, maxSize);
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
        return pos[index] == null ? Optional.empty() : Optional.of(pos[index].immutable());
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

    public void updateFrom(ProgWidgetArea otherArea) {
        setPos(0, otherArea.getPos(0).orElse(null));
        setPos(1, otherArea.getPos(1).orElse(null));
        areaType = otherArea.areaType.copy();
        setVarName(0, otherArea.getVarName(0));
        setVarName(1, otherArea.getVarName(1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgWidgetArea that = (ProgWidgetArea) o;
        return Objects.equals(getPosition(), that.getPosition()) && Objects.deepEquals(pos, that.pos) && Objects.deepEquals(varNames, that.varNames) && Objects.equals(areaType, that.areaType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPosition(), Arrays.hashCode(pos), Arrays.hashCode(varNames), areaType);
    }
}
