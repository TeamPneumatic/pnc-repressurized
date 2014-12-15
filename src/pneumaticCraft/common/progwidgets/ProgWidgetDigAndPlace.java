package pneumaticCraft.common.progwidgets;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.programmer.GuiProgWidgetDigAndPlace;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class ProgWidgetDigAndPlace extends ProgWidgetAreaItemBase implements IBlockOrdered{
    private EnumOrder order;

    @Override
    public EnumOrder getOrder(){
        return order;
    }

    @Override
    public void setOrder(EnumOrder order){
        this.order = order;
    }

    @Override
    public void getTooltip(List<String> curTooltip){
        super.getTooltip(curTooltip);
        curTooltip.add("Order: " + order.getLocalizedName());
    }

    public ProgWidgetDigAndPlace(EnumOrder order){
        this.order = order;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer){
        return new GuiProgWidgetDigAndPlace(this, guiProgrammer);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setInteger("order", order.ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        order = EnumOrder.values()[tag.getInteger("order")];
    }

    @Override
    public String getExtraStringInfo(){
        return order.getLocalizedName();
    }

}
