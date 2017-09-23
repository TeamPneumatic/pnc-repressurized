package me.desht.pneumaticcraft.common.remote;

import me.desht.pneumaticcraft.client.gui.GuiButtonSpecial;
import me.desht.pneumaticcraft.client.gui.GuiRemoteEditor;
import me.desht.pneumaticcraft.client.gui.remote.GuiRemoteButton;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSetGlobalVariable;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class ActionWidgetButton extends ActionWidgetVariable<GuiButtonSpecial> implements IActionWidgetLabeled {

    public BlockPos settingCoordinate = new BlockPos(0, 0, 0);//The coordinate the variable is set to when the button is pressed.

    public ActionWidgetButton() {
        super();
    }

    public ActionWidgetButton(GuiButtonSpecial widget) {
        super(widget);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag, int guiLeft, int guiTop) {
        super.readFromNBT(tag, guiLeft, guiTop);
        widget = new GuiButtonSpecial(-1, tag.getInteger("x") + guiLeft, tag.getInteger("y") + guiTop, tag.getInteger("width"), tag.getInteger("height"), tag.getString("text"));
        settingCoordinate = new BlockPos(tag.getInteger("settingX"), tag.getInteger("settingY"), tag.getInteger("settingZ"));
        widget.setTooltipText(tag.getString("tooltip"));
    }

    @Override
    public NBTTagCompound toNBT(int guiLeft, int guiTop) {
        NBTTagCompound tag = super.toNBT(guiLeft, guiTop);
        tag.setInteger("x", widget.x - guiLeft);
        tag.setInteger("y", widget.y - guiTop);
        tag.setInteger("width", widget.width);
        tag.setInteger("height", widget.height);
        tag.setString("text", widget.displayString);
        tag.setInteger("settingX", settingCoordinate.getX());
        tag.setInteger("settingY", settingCoordinate.getY());
        tag.setInteger("settingZ", settingCoordinate.getZ());
        tag.setString("tooltip", widget.getTooltip());
        return tag;
    }

    @Override
    public String getId() {
        return "button";
    }

    @Override
    public void setText(String text) {
        widget.displayString = text;
    }

    @Override
    public String getText() {
        return widget.displayString;
    }

    @Override
    public void onActionPerformed() {
        NetworkHandler.sendToServer(new PacketSetGlobalVariable(getVariableName(), settingCoordinate));
    }

    @Override
    public void onVariableChange() {
        // widget.checked = GlobalVariableManager.getBoolean(getVariableName());
    }

    @Override
    public GuiScreen getGui(GuiRemoteEditor guiRemote) {
        return new GuiRemoteButton(this, guiRemote);
    }

    @Override
    public void setWidgetPos(int x, int y) {
        widget.x = x;
        widget.y = y;
    }

    public void setWidth(int width) {
        widget.width = width;
    }

    public int getWidth() {
        return widget.width;
    }

    public void setHeight(int height) {
        widget.height = height;
    }

    public int getHeight() {
        return widget.height;
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
