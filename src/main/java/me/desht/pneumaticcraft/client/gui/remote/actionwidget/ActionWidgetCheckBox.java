package me.desht.pneumaticcraft.client.gui.remote.actionwidget;

import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSetGlobalVariable;
import me.desht.pneumaticcraft.common.util.NBTUtils;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants;

import java.util.List;

public class ActionWidgetCheckBox extends ActionWidgetVariable<WidgetCheckBox> implements IActionWidgetLabeled {
    public ActionWidgetCheckBox() {
    }

    public ActionWidgetCheckBox(WidgetCheckBox widget) {
        super(widget);
    }

    @Override
    public void readFromNBT(CompoundNBT tag, int guiLeft, int guiTop) {
        super.readFromNBT(tag, guiLeft, guiTop);
        widget = new WidgetCheckBox(tag.getInt("x") + guiLeft, tag.getInt("y") + guiTop, 0xFF404040, deserializeTextComponent(tag.getString("text")), b -> onActionPerformed());
//        setTooltip(tag.getString("tooltip"));
        widget.setTooltip(NBTUtils.deserializeTextComponents(tag.getList("tooltip", Constants.NBT.TAG_STRING)));
    }

    @Override
    public CompoundNBT toNBT(int guiLeft, int guiTop) {
        CompoundNBT tag = super.toNBT(guiLeft, guiTop);
        tag.putInt("x", widget.x - guiLeft);
        tag.putInt("y", widget.y - guiTop);
        tag.putString("text", ITextComponent.Serializer.toJson(widget.getMessage()));
        tag.put("tooltip", NBTUtils.serializeTextComponents(widget.getTooltip()));
        return tag;
    }

    @Override
    public String getId() {
        return "checkbox";
    }

    @Override
    public void setText(ITextComponent text) {
        widget.setMessage(text);
    }

    @Override
    public ITextComponent getText() {
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
    public void setTooltip(List<ITextComponent> text) {
        widget.setTooltip(text);
    }

    @Override
    public List<ITextComponent> getTooltip() {
        return widget.getTooltip();
    }
}
