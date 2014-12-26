package pneumaticCraft.common.progwidgets;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.programmer.GuiProgWidgetString;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ProgWidgetString extends ProgWidget{
    public String string = "";

    @Override
    public void getTooltip(List<String> curTooltip){
        super.getTooltip(curTooltip);
        curTooltip.add("Value: \"" + string + "\"");
    }

    @Override
    public String getExtraStringInfo(){
        return "\"" + string + "\"";
    }

    @Override
    public boolean hasStepInput(){
        return false;
    }

    @Override
    public Class<? extends IProgWidget> returnType(){
        return ProgWidgetString.class;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetString.class};
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_TEXT;
    }

    @Override
    public String getWidgetString(){
        return "text";
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setString("string", string);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        string = tag.getString("string");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer){
        return new GuiProgWidgetString(this, guiProgrammer);
    }

    @Override
    public String getGuiTabText(){
        return "This module is used as parameter for other modules. It can provide a string";
    }

    @Override
    public int getGuiTabColor(){
        return 0xFF0000FF;
    }

    @Override
    public WidgetCategory getCategory(){
        return WidgetCategory.PARAMETER;
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.CHOPPER_PLANT_DAMAGE;
    }

}
