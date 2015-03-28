package pneumaticCraft.common.remote;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.client.gui.GuiButtonSpecial;
import pneumaticCraft.client.gui.GuiRemoteEditor;
import pneumaticCraft.client.gui.remote.GuiRemoteButton;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketSetGlobalVariable;

public class ActionWidgetButton extends ActionWidgetVariable<GuiButtonSpecial> implements IActionWidgetLabeled{

    public ChunkPosition settingCoordinate = new ChunkPosition(0, 0, 0);//The coordinate the variable is set to when the button is pressed.

    public ActionWidgetButton(){
        super();
    }

    public ActionWidgetButton(GuiButtonSpecial widget){
        super(widget);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag, int guiLeft, int guiTop){
        super.readFromNBT(tag, guiLeft, guiTop);
        widget = new GuiButtonSpecial(-1, tag.getInteger("x") + guiLeft, tag.getInteger("y") + guiTop, tag.getInteger("width"), tag.getInteger("height"), tag.getString("text"));
        settingCoordinate = new ChunkPosition(tag.getInteger("settingX"), tag.getInteger("settingY"), tag.getInteger("settingZ"));
        widget.setTooltipText(tag.getString("tooltip"));
    }

    @Override
    public NBTTagCompound toNBT(int guiLeft, int guiTop){
        NBTTagCompound tag = super.toNBT(guiLeft, guiTop);
        tag.setInteger("x", widget.xPosition - guiLeft);
        tag.setInteger("y", widget.yPosition - guiTop);
        tag.setInteger("width", widget.width);
        tag.setInteger("height", widget.height);
        tag.setString("text", widget.displayString);
        tag.setInteger("settingX", settingCoordinate.chunkPosX);
        tag.setInteger("settingY", settingCoordinate.chunkPosY);
        tag.setInteger("settingZ", settingCoordinate.chunkPosZ);
        tag.setString("tooltip", widget.getTooltip());
        return tag;
    }

    @Override
    public String getId(){
        return "button";
    }

    @Override
    public void setText(String text){
        widget.displayString = text;
    }

    @Override
    public String getText(){
        return widget.displayString;
    }

    @Override
    public void onActionPerformed(){
        NetworkHandler.sendToServer(new PacketSetGlobalVariable(getVariableName(), settingCoordinate));
    }

    @Override
    public void onVariableChange(){
        // widget.checked = GlobalVariableManager.getBoolean(getVariableName());
    }

    @Override
    public GuiScreen getGui(GuiRemoteEditor guiRemote){
        return new GuiRemoteButton(this, guiRemote);
    }

    @Override
    public void setWidgetPos(int x, int y){
        widget.xPosition = x;
        widget.yPosition = y;
    }

    public void setWidth(int width){
        widget.width = width;
    }

    public int getWidth(){
        return widget.width;
    }

    public void setHeight(int height){
        widget.height = height;
    }

    public int getHeight(){
        return widget.height;
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
