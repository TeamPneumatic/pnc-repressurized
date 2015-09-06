package pneumaticCraft.common.remote;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import pneumaticCraft.client.gui.GuiRemoteEditor;
import pneumaticCraft.client.gui.widget.IGuiWidget;
import pneumaticCraft.lib.Log;

public abstract class ActionWidget<Widget extends IGuiWidget> {
    protected Widget widget;
    private String enableVariable = "";

    public ActionWidget(Widget widget){
        this.widget = widget;
    }

    public ActionWidget(){}

    public void readFromNBT(NBTTagCompound tag, int guiLeft, int guiTop){
        enableVariable = tag.getString("enableVariable");
    }

    public NBTTagCompound toNBT(int guiLeft, int guitTop){
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("id", getId());
        tag.setString("enableVariable", enableVariable);
        return tag;
    }

    public ActionWidget copy(){
        try {
            ActionWidget widget = this.getClass().newInstance();
            widget.readFromNBT(this.toNBT(0, 0), 0, 0);
            return widget;
        } catch(Exception e) {
            Log.error("Error occured when trying to copy an " + getId() + " action widget.");
            e.printStackTrace();
            return null;
        }
    }

    public IGuiWidget getWidget(){
        return widget;
    }

    public abstract void setWidgetPos(int x, int y);

    public abstract String getId();

    public GuiScreen getGui(GuiRemoteEditor guiRemote){
        return null;
    }

    public void setEnableVariable(String varName){
        this.enableVariable = varName;
    }

    public String getEnableVariable(){
        return enableVariable;
    }

    public boolean isEnabled(){
        return enableVariable.equals("") || GlobalVariableManager.getInstance().getBoolean(enableVariable);
    }
}