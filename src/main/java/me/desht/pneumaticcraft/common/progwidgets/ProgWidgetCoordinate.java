package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.common.item.ItemGPSTool;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetCoordinate extends ProgWidget implements IVariableWidget {
    private BlockPos coord;
    private String variable = "";
    private boolean useVariable;
    private DroneAIManager aiManager;

    public ProgWidgetCoordinate() {
        super(ModProgWidgets.COORDINATE.get());
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
        return ModProgWidgets.COORDINATE.get();
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.COORDINATE.get());
    }

    @Override
    public void addWarnings(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        super.addWarnings(curInfo, widgets);
        if (!useVariable && coord == null) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.coordinate.warning.noCoordinate"));
        }
    }

    @Override
    public void addErrors(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
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

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        if (coord != null) {
            tag.put("coord", NBTUtil.writeBlockPos(coord));
        }
        if (!variable.isEmpty()) tag.putString("variable", variable);
        if (useVariable) tag.putBoolean("useVariable", true);
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        if (tag.contains("posX")) {
            // TODO remove in 1.17
            coord = new BlockPos(tag.getInt("posX"), tag.getInt("posY"), tag.getInt("posZ"));
        } else if (tag.contains("coord")) {
            coord = NBTUtil.readBlockPos(tag.getCompound("coord"));
        } else {
            coord = null;
        }
        variable = tag.getString("variable");
        useVariable = tag.getBoolean("useVariable");
    }

    @Override
    public void writeToPacket(PacketBuffer buf) {
        super.writeToPacket(buf);
        buf.writeBoolean(coord != null);
        if (coord != null) buf.writeBlockPos(coord);
        buf.writeString(variable);
        buf.writeBoolean(useVariable);
    }

    @Override
    public void readFromPacket(PacketBuffer buf) {
        super.readFromPacket(buf);
        coord = buf.readBoolean() ? buf.readBlockPos() : null;
        variable = buf.readString(GlobalVariableManager.MAX_VARIABLE_LEN);
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
    
    public void loadFromGPSTool(ItemStack gpsTool) {
        String variable = ItemGPSTool.getVariable(gpsTool);
        if (variable.isEmpty()) {
            setCoordinate(ItemGPSTool.getGPSLocation(gpsTool).orElse(BlockPos.ZERO));
            setUsingVariable(false);
        } else {
            setVariable(variable);
            setUsingVariable(true);
        }
    }

    @Override
    public void getTooltip(List<ITextComponent> curTooltip) {
        super.getTooltip(curTooltip);

        if (useVariable) {
            curTooltip.add(new StringTextComponent("XYZ: var '" + variable + "'"));
        } else {
            curTooltip.add(new StringTextComponent(PneumaticCraftUtils.posToString(coord)));
        }
    }

    @Override
    public List<ITextComponent> getExtraStringInfo() {
        return useVariable ?
                Collections.singletonList(varAsTextComponent(variable)) :
                Collections.singletonList(new StringTextComponent(PneumaticCraftUtils.posToString(coord)));
    }

    @Override
    public void addVariables(Set<String> variables) {
        variables.add(variable);
    }
}
