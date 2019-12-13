package me.desht.pneumaticcraft.common.remote;

import me.desht.pneumaticcraft.client.gui.GuiRemoteEditor;
import me.desht.pneumaticcraft.client.gui.remote.GuiRemoteOptionBase;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.CompoundNBT;

public class ActionWidgetLabel extends ActionWidget<WidgetLabelVariable> implements IActionWidgetLabeled {

    public ActionWidgetLabel(WidgetLabelVariable widget) {
        super(widget);
    }

    public ActionWidgetLabel() {
    }

    @Override
    public CompoundNBT toNBT(int guiLeft, int guiTop) {
        CompoundNBT tag = super.toNBT(guiLeft, guiTop);
        tag.putString("text", widget.getMessage());
        tag.putInt("x", widget.x - guiLeft);
        tag.putInt("y", widget.y - guiTop);
//        tag.putString("tooltip", widget.getTooltip());
        return tag;
    }

    @Override
    public void readFromNBT(CompoundNBT tag, int guiLeft, int guiTop) {
        super.readFromNBT(tag, guiLeft, guiTop);
        widget = new WidgetLabelVariable(tag.getInt("x") + guiLeft, tag.getInt("y") + guiTop, tag.getString("text"));
//        widget.setTooltipText(tag.getString("tooltip"));
    }

    @Override
    public String getId() {
        return "label";
    }

    @Override
    public void setText(String text) {
        widget.setMessage(text);
    }

    @Override
    public String getText() {
        return widget.getMessage();
    }

    @Override
    public Screen getGui(GuiRemoteEditor guiRemote) {
        return new GuiRemoteOptionBase(this, guiRemote);
    }

    @Override
    public void setWidgetPos(int x, int y) {
        widget.x = x;
        widget.y = y;
    }

    @Override
    public void setTooltip(String text) {
//        widget.setTooltipText(text);
    }

    @Override
    public String getTooltip() {
        return "";//widget.getTooltip();
    }
}
