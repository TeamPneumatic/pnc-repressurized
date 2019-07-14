package me.desht.pneumaticcraft.common.remote;

import me.desht.pneumaticcraft.client.gui.GuiRemoteEditor;
import me.desht.pneumaticcraft.client.gui.remote.GuiRemoteVariable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.nbt.CompoundNBT;

public abstract class ActionWidgetVariable<W extends Widget> extends ActionWidget<W> {
    private String variableName = "";

    ActionWidgetVariable(W widget) {
        super(widget);
    }

    ActionWidgetVariable() {
    }

    @Override
    public void readFromNBT(CompoundNBT tag, int guiLeft, int guiTop) {
        super.readFromNBT(tag, guiLeft, guiTop);
        variableName = tag.getString("variableName");
    }

    @Override
    public CompoundNBT toNBT(int guiLeft, int guiTop) {
        CompoundNBT tag = super.toNBT(guiLeft, guiTop);
        tag.putString("variableName", variableName);
        return tag;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public Screen getGui(GuiRemoteEditor guiRemote) {
        return new GuiRemoteVariable(this, guiRemote);
    }

    public abstract void onActionPerformed();

    public void onKeyTyped() {
    }

    public abstract void onVariableChange();
}
