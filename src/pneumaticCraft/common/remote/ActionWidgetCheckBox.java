package pneumaticCraft.common.remote;

import net.minecraft.nbt.NBTTagCompound;
import pneumaticCraft.client.gui.widget.GuiCheckBox;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketSetGlobalVariable;

public class ActionWidgetCheckBox extends ActionWidgetVariable<GuiCheckBox> implements IActionWidgetLabeled{

    public ActionWidgetCheckBox(){
        super();
    }

    public ActionWidgetCheckBox(GuiCheckBox widget){
        super(widget);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag, int guiLeft, int guiTop){
        super.readFromNBT(tag, guiLeft, guiTop);
        widget = new GuiCheckBox(-1, tag.getInteger("x") + guiLeft, tag.getInteger("y") + guiTop, 0xFF000000, tag.getString("text"));
        setTooltip(tag.getString("tooltip"));
    }

    @Override
    public NBTTagCompound toNBT(int guiLeft, int guiTop){
        NBTTagCompound tag = super.toNBT(guiLeft, guiTop);
        tag.setInteger("x", widget.x - guiLeft);
        tag.setInteger("y", widget.y - guiTop);
        tag.setString("text", widget.text);
        tag.setString("tooltip", widget.getTooltip());
        return tag;
    }

    @Override
    public String getId(){
        return "checkbox";
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
    public void onActionPerformed(){
        NetworkHandler.sendToServer(new PacketSetGlobalVariable(getVariableName(), widget.checked));
    }

    @Override
    public void onVariableChange(){
        widget.checked = GlobalVariableManager.getInstance().getBoolean(getVariableName());
    }

    @Override
    public void setWidgetPos(int x, int y){
        widget.x = x;
        widget.y = y;
    }

    @Override
    public void setTooltip(String text){
        widget.setTooltip(text);
    }

    @Override
    public String getTooltip(){
        return widget.getTooltip();
    }
}
