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
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.drone.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.item.GPSToolItem;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetCoordinate extends ProgWidget implements IVariableWidget {
    public static final MapCodec<ProgWidgetCoordinate> CODEC = RecordCodecBuilder.mapCodec(builder ->
            baseParts(builder).and(builder.group(
                    BlockPos.CODEC.optionalFieldOf("pos", null).forGetter(p -> p.coord),
                    Codec.STRING.optionalFieldOf("var", "").forGetter(ProgWidgetCoordinate::getVariable),
                    Codec.BOOL.optionalFieldOf("using_var", false).forGetter(ProgWidgetCoordinate::isUsingVariable)
            )).apply(builder, ProgWidgetCoordinate::new));

    private BlockPos coord;
    private String variable = "";
    private boolean useVariable;
    private DroneAIManager aiManager;

    public ProgWidgetCoordinate() {
        super(PositionFields.DEFAULT);
    }

    private ProgWidgetCoordinate(PositionFields pos, BlockPos coord, String variable, boolean useVariable) {
        super(pos);

        this.coord = coord;
        this.variable = variable;
        this.useVariable = useVariable;
    }

    public static ProgWidgetCoordinate fromPos(BlockPos pos) {
        ProgWidgetCoordinate w = new ProgWidgetCoordinate();
        w.setCoordinate(pos);
        return w;
    }

    public static ProgWidgetCoordinate fromGPSTool(ItemStack gpsTool) {
        ProgWidgetCoordinate w = new ProgWidgetCoordinate();
        w.loadFromGPSTool(gpsTool);
        return w;
    }

    @Override
    public boolean hasStepInput() {
        return false;
    }

    @Override
    public ProgWidgetType<?> returnType() {
        return ModProgWidgetTypes.COORDINATE.get();
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgetTypes.COORDINATE.get());
    }

    @Override
    public void addWarnings(List<Component> curInfo, List<IProgWidget> widgets) {
        super.addWarnings(curInfo, widgets);
        if (!useVariable && coord == null) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.coordinate.warning.noCoordinate"));
        }
    }

    @Override
    public void addErrors(List<Component> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (useVariable && variable.isEmpty()) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.general.error.emptyVariable"));
        }
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.GREEN;
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.ADVANCED;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_COORDINATE;
    }

//    @Override
//    public void writeToNBT(CompoundTag tag, HolderLookup.Provider provider) {
//        super.writeToNBT(tag, provider);
//        if (coord != null) {
//            tag.put("coord", NbtUtils.writeBlockPos(coord));
//        }
//        if (!variable.isEmpty()) tag.putString("variable", variable);
//        if (useVariable) tag.putBoolean("useVariable", true);
//    }
//
//    @Override
//    public void readFromNBT(CompoundTag tag, HolderLookup.Provider provider) {
//        super.readFromNBT(tag, provider);
//        if (tag.contains("coord")) {
//            coord = NbtUtils.readBlockPos(tag.getCompound("coord"));
//        } else if (tag.contains("posX")) {
//            // legacy import
//            coord = new BlockPos(tag.getInt("posX"), tag.getInt("posY"), tag.getInt("posZ"));
//        } else {
//            coord = null;
//        }
//        variable = tag.getString("variable");
//        useVariable = tag.getBoolean("useVariable");
//    }

    @Override
    public void writeToPacket(RegistryFriendlyByteBuf buf) {
        super.writeToPacket(buf);
        BlockPos.STREAM_CODEC.encode(buf, coord);
        buf.writeUtf(variable);
        buf.writeBoolean(useVariable);
    }

    @Override
    public void readFromPacket(RegistryFriendlyByteBuf buf) {
        super.readFromPacket(buf);
        coord = BlockPos.STREAM_CODEC.decode(buf);
        variable = buf.readUtf(GlobalVariableManager.MAX_VARIABLE_LEN);
        useVariable = buf.readBoolean();
    }

    @Override
    public void setAIManager(DroneAIManager aiManager) {
        this.aiManager = aiManager;
    }

    public Optional<BlockPos> getCoordinate() {
        if (useVariable && aiManager != null) {
            return aiManager.getCoordinate(aiManager.getDrone().getOwnerUUID(), variable);
        } else {
            return getRawCoordinate();
        }
    }

    public Optional<BlockPos> getRawCoordinate() {
        return Optional.ofNullable(coord);
    }

    public void setCoordinate(BlockPos pos) {
        coord = pos;
    }

    public void setVariable(String varName) {
        variable = varName;
    }

    public String getVariable() {
        return variable;
    }

    public boolean isUsingVariable() {
        return useVariable;
    }

    public void setUsingVariable(boolean useVariable) {
        this.useVariable = useVariable;
    }
    
    public void loadFromGPSTool(ItemStack gpsTool){
        String variable = GPSToolItem.getVariable(gpsTool);
        if (variable.isEmpty()) {
            setCoordinate(GPSToolItem.getGPSLocation(gpsTool).orElse(BlockPos.ZERO));
            setUsingVariable(false);
        } else {
            setVariable(variable);
            setUsingVariable(true);
        }
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.COORDINATE.get();
    }

    @Override
    public void getTooltip(List<Component> curTooltip) {
        super.getTooltip(curTooltip);

        if (useVariable) {
            curTooltip.add(Component.literal("XYZ: var '" + variable + "'"));
        } else {
            curTooltip.add(Component.literal(PneumaticCraftUtils.posToString(coord)));
        }
    }

    @Override
    public List<Component> getExtraStringInfo() {
        return useVariable ?
                Collections.singletonList(varAsTextComponent(variable)) :
                Collections.singletonList(Component.literal(PneumaticCraftUtils.posToString(coord)));
    }

    @Override
    public void addVariables(Set<String> variables) {
        variables.add(variable);
    }
}
