package me.desht.pneumaticcraft.common.remote;

import me.desht.pneumaticcraft.client.gui.GuiRemoteEditor;
import me.desht.pneumaticcraft.client.gui.remote.GuiRemoteButton;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSetGlobalVariable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

public class ActionWidgetButton extends ActionWidgetVariable<WidgetButtonExtended> implements IActionWidgetLabeled {
    public BlockPos settingCoordinate = BlockPos.ZERO; // The coordinate the variable is set to when the button is pressed.

    public ActionWidgetButton() {
    }

    public ActionWidgetButton(WidgetButtonExtended widget) {
        super(widget);
    }

    @Override
    public void readFromNBT(CompoundNBT tag, int guiLeft, int guiTop) {
        super.readFromNBT(tag, guiLeft, guiTop);
        widget = new WidgetButtonExtended(tag.getInt("x") + guiLeft, tag.getInt("y") + guiTop, tag.getInt("width"), tag.getInt("height"), tag.getString("text"), b -> onActionPerformed());
        settingCoordinate = new BlockPos(tag.getInt("settingX"), tag.getInt("settingY"), tag.getInt("settingZ"));
        widget.setTooltipText(tag.getString("tooltip"));
    }

    @Override
    public CompoundNBT toNBT(int guiLeft, int guiTop) {
        CompoundNBT tag = super.toNBT(guiLeft, guiTop);
        tag.putInt("x", widget.x - guiLeft);
        tag.putInt("y", widget.y - guiTop);
        tag.putInt("width", widget.getWidth());
        tag.putInt("height", widget.getHeight());
        tag.putString("text", widget.getMessage());
        tag.putInt("settingX", settingCoordinate.getX());
        tag.putInt("settingY", settingCoordinate.getY());
        tag.putInt("settingZ", settingCoordinate.getZ());
        tag.putString("tooltip", widget.getTooltip());
        return tag;
    }

    @Override
    public String getId() {
        return "button";
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
        NetworkHandler.sendToServer(new PacketSetGlobalVariable(getVariableName(), settingCoordinate));
    }

    @Override
    public void onVariableChange() {
    }

    @Override
    public Screen getGui(GuiRemoteEditor guiRemote) {
        return new GuiRemoteButton(this, guiRemote);
    }

    @Override
    public void setWidgetPos(int x, int y) {
        widget.x = x;
        widget.y = y;
    }

    public void setWidth(int width) {
        widget.setWidth(width);
    }

    public int getWidth() {
        return widget.getWidth();
    }

    public void setHeight(int height) {
        widget.setHeight(height);
    }

    public int getHeight() {
        return widget.getHeight();
    }

    @Override
    public void setTooltip(String text) {
        widget.setTooltipText(text);
    }

    @Override
    public String getTooltip() {
        return widget.getTooltip();
    }
}
