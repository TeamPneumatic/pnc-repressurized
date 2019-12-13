package me.desht.pneumaticcraft.common.remote;

import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSetGlobalVariable;
import net.minecraft.nbt.CompoundNBT;

public class ActionWidgetCheckBox extends ActionWidgetVariable<WidgetCheckBox> implements IActionWidgetLabeled {
    public ActionWidgetCheckBox() {
    }

    public ActionWidgetCheckBox(WidgetCheckBox widget) {
        super(widget);
    }

    @Override
    public void readFromNBT(CompoundNBT tag, int guiLeft, int guiTop) {
        super.readFromNBT(tag, guiLeft, guiTop);
        widget = new WidgetCheckBox(tag.getInt("x") + guiLeft, tag.getInt("y") + guiTop, 0xFF404040, tag.getString("text"), b -> onActionPerformed());
        setTooltip(tag.getString("tooltip"));
    }

    @Override
    public CompoundNBT toNBT(int guiLeft, int guiTop) {
        CompoundNBT tag = super.toNBT(guiLeft, guiTop);
        tag.putInt("x", widget.x - guiLeft);
        tag.putInt("y", widget.y - guiTop);
        tag.putString("text", widget.getMessage());
        tag.putString("tooltip", widget.getTooltip());
        return tag;
    }

    @Override
    public String getId() {
        return "checkbox";
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
    public void onActionPerformed() {
        NetworkHandler.sendToServer(new PacketSetGlobalVariable(getVariableName(), widget.checked));
    }

    @Override
    public void onVariableChange() {
        widget.checked = GlobalVariableManager.getInstance().getBoolean(getVariableName());
    }

    @Override
    public void setWidgetPos(int x, int y) {
        widget.x = x;
        widget.y = y;
    }

    @Override
    public void setTooltip(String text) {
        widget.setTooltip(text);
    }

    @Override
    public String getTooltip() {
        return widget.getTooltip();
    }
}
