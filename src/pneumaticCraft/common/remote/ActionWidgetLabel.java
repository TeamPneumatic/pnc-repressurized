package pneumaticCraft.common.remote;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import pneumaticCraft.client.gui.GuiRemoteEditor;
import pneumaticCraft.client.gui.remote.GuiRemoteOptionBase;

public class ActionWidgetLabel extends ActionWidget<WidgetLabelVariable> implements IActionWidgetLabeled{

    public ActionWidgetLabel(WidgetLabelVariable widget){
        super(widget);
    }

    public ActionWidgetLabel(){}

    @Override
    public NBTTagCompound toNBT(int guiLeft, int guiTop){
        NBTTagCompound tag = super.toNBT(guiLeft, guiTop);
        tag.setString("text", widget.text);
        tag.setInteger("x", widget.getBounds().x - guiLeft);
        tag.setInteger("y", widget.getBounds().y - guiTop);
        tag.setString("tooltip", widget.getTooltip());
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag, int guiLeft, int guiTop){
        super.readFromNBT(tag, guiLeft, guiTop);
        widget = new WidgetLabelVariable(tag.getInteger("x") + guiLeft, tag.getInteger("y") + guiTop, tag.getString("text"));
        widget.setTooltipText(tag.getString("tooltip"));
    }

    @Override
    public String getId(){
        return "label";
    }

    @Override
    public void setText(String text){
        widget.text = text;
    }

    @Override
    public String getText(){
        return widget.text;
    }

    @Override
    public GuiScreen getGui(GuiRemoteEditor guiRemote){
        return new GuiRemoteOptionBase(this, guiRemote);
    }

    @Override
    public void setWidgetPos(int x, int y){
        widget.x = x;
        widget.y = y;
    }

    @Override
    public void setTooltip(String text){
        widget.setTooltipText(text);
    }

    @Override
    public String getTooltip(){
        return widget.getTooltip();
    }
}
