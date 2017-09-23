package me.desht.pneumaticcraft.common.remote;

import me.desht.pneumaticcraft.client.gui.GuiRemoteEditor;
import me.desht.pneumaticcraft.client.gui.widget.IGuiWidget;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public abstract class ActionWidget<Widget extends IGuiWidget> {
    protected Widget widget;
    private String enableVariable = "";
    private BlockPos enablingValue = new BlockPos(0, 0, 0);

    public ActionWidget(Widget widget) {
        this.widget = widget;
    }

    public ActionWidget() {
    }

    public void readFromNBT(NBTTagCompound tag, int guiLeft, int guiTop) {
        enableVariable = tag.getString("enableVariable");
        enablingValue = tag.hasKey("enablingX") ? new BlockPos(tag.getInteger("enablingX"), tag.getInteger("enablingY"), tag.getInteger("enablingZ")) : new BlockPos(1, 0, 0);
    }

    public NBTTagCompound toNBT(int guiLeft, int guitTop) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("id", getId());
        tag.setString("enableVariable", enableVariable);
        tag.setInteger("enablingX", enablingValue.getX());
        tag.setInteger("enablingY", enablingValue.getY());
        tag.setInteger("enablingZ", enablingValue.getZ());
        return tag;
    }

    public ActionWidget copy() {
        try {
            ActionWidget widget = this.getClass().newInstance();
            widget.readFromNBT(this.toNBT(0, 0), 0, 0);
            return widget;
        } catch (Exception e) {
            Log.error("Error occured when trying to copy an " + getId() + " action widget.");
            e.printStackTrace();
            return null;
        }
    }

    public Widget getWidget() {
        return widget;
    }

    public abstract void setWidgetPos(int x, int y);

    public abstract String getId();

    public GuiScreen getGui(GuiRemoteEditor guiRemote) {
        return null;
    }

    public void setEnableVariable(String varName) {
        this.enableVariable = varName;
    }

    public String getEnableVariable() {
        return enableVariable;
    }

    public boolean isEnabled() {
        return enableVariable.equals("") || GlobalVariableManager.getInstance().getPos(enableVariable).equals(enablingValue);
    }

    public void setEnablingValue(int x, int y, int z) {
        enablingValue = new BlockPos(x, y, z);
    }

    public BlockPos getEnablingValue() {
        return enablingValue;
    }
}