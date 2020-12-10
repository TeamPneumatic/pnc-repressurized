package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ProgWidgetForEachItem extends ProgWidget implements IJumpBackWidget, IJump, IVariableSetWidget {
    private String elementVariable = "";
    private int curIndex; //iterator index
    private DroneAIManager aiManager;

    public ProgWidgetForEachItem() {
        super(ModProgWidgets.FOR_EACH_ITEM.get());
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.YELLOW;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_FOR_EACH_ITEM;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.ITEM_FILTER.get(), ModProgWidgets.TEXT.get());
    }

    @Override
    public void addVariables(Set<String> variables) {
        variables.add(elementVariable);
    }

    @Override
    public String getVariable() {
        return elementVariable;
    }

    @Override
    public void setVariable(String variable) {
        elementVariable = variable;
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        tag.putString("variable", elementVariable);
        super.writeToNBT(tag);
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        elementVariable = tag.getString("variable");
        super.readFromNBT(tag);
    }

    @Override
    public void writeToPacket(PacketBuffer buf) {
        super.writeToPacket(buf);
        buf.writeString(elementVariable);
    }

    @Override
    public void readFromPacket(PacketBuffer buf) {
        super.readFromPacket(buf);
        elementVariable = buf.readString(GlobalVariableManager.MAX_VARIABLE_LEN);
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.ADVANCED;
    }

    @Override
    public IProgWidget getOutputWidget(IDroneBase drone, List<IProgWidget> allWidgets) {
        List<String> locations = getPossibleJumpLocations();
        ItemStack filter = getFilterForIndex(curIndex++);
        if (locations.size() > 0 && filter != null && (curIndex == 1 || !aiManager.getStack(elementVariable).isEmpty())) {
            aiManager.setItem(elementVariable, filter);
            return ProgWidgetJump.jumpToLabel(drone, allWidgets, locations.get(0));
        }
        curIndex = 0;
        return super.getOutputWidget(drone, allWidgets);
    }

    private ItemStack getFilterForIndex(int index) {
        ProgWidgetItemFilter widget = (ProgWidgetItemFilter) getConnectedParameters()[0];
        for (int i = 0; i < index; i++) {
            if (widget == null) return null;
            widget = (ProgWidgetItemFilter) widget.getConnectedParameters()[0];
        }
        return widget != null ? widget.getFilter() : null;
    }

    @Override
    public List<String> getPossibleJumpLocations() {
        IProgWidget widget = getConnectedParameters()[getParameters().size() - 1];
        ProgWidgetText textWidget = widget != null ? (ProgWidgetText) widget : null;
        List<String> locations = new ArrayList<>();
        if (textWidget != null) locations.add(textWidget.string);
        return locations;
    }

    @Override
    public List<ITextComponent> getExtraStringInfo() {
        return Collections.singletonList(varAsTextComponent(elementVariable));
    }

    @Override
    public void setAIManager(DroneAIManager aiManager) {
        this.aiManager = aiManager;
    }

    @Override
    public boolean hasStepInput() {
        return true;
    }

    @Override
    public ProgWidgetType<?> returnType() {
        return null;
    }

    @Override
    protected boolean hasBlacklist() {
        return false;
    }
}
