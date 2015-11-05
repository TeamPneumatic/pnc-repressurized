package pneumaticCraft.common.progwidgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.programmer.GuiProgWidgetForEach;
import pneumaticCraft.common.ai.DroneAIManager;
import pneumaticCraft.common.ai.IDroneBase;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ProgWidgetForEachItem extends ProgWidget implements IJumpBackWidget, IJump, IVariableSetWidget{
    private String elementVariable = "";
    private int curIndex; //iterator index
    private DroneAIManager aiManager;

    @Override
    public String getWidgetString(){
        return "forEachItem";
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.HELIUM_PLANT_DAMAGE;
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_FOR_EACH_ITEM;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetItemFilter.class, ProgWidgetString.class};
    }

    @Override
    public void addVariables(Set<String> variables){
        variables.add(elementVariable);
    }

    @Override
    public String getVariable(){
        return elementVariable;
    }

    @Override
    public void setVariable(String variable){
        elementVariable = variable;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        tag.setString("variable", elementVariable);
        super.writeToNBT(tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        elementVariable = tag.getString("variable");
        super.readFromNBT(tag);
    }

    @Override
    public WidgetDifficulty getDifficulty(){
        return WidgetDifficulty.ADVANCED;
    }

    @Override
    public IProgWidget getOutputWidget(IDroneBase drone, List<IProgWidget> allWidgets){
        List<String> locations = getPossibleJumpLocations();
        ItemStack filter = getFilterForIndex(curIndex++);
        if(locations.size() > 0 && filter != null && (curIndex == 1 || aiManager.getStack(elementVariable) != null)) {
            aiManager.setItem(elementVariable, filter);
            return ProgWidgetJump.jumpToLabel(drone, allWidgets, locations.get(0));
        }
        curIndex = 0;
        return super.getOutputWidget(drone, allWidgets);
    }

    private ItemStack getFilterForIndex(int index){
        ProgWidgetItemFilter widget = (ProgWidgetItemFilter)getConnectedParameters()[0];
        for(int i = 0; i < index; i++) {
            if(widget == null) return null;
            widget = (ProgWidgetItemFilter)widget.getConnectedParameters()[0];
        }
        return widget != null ? widget.getFilter() : null;
    }

    @Override
    public List<String> getPossibleJumpLocations(){
        IProgWidget widget = getConnectedParameters()[getParameters().length - 1];
        ProgWidgetString textWidget = widget != null ? (ProgWidgetString)widget : null;
        List<String> locations = new ArrayList<String>();
        if(textWidget != null) locations.add(textWidget.string);
        return locations;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer){
        return new GuiProgWidgetForEach(this, guiProgrammer);
    }

    @Override
    public String getExtraStringInfo(){
        return "\"" + elementVariable + "\"";
    }

    @Override
    public void setAIManager(DroneAIManager aiManager){
        this.aiManager = aiManager;
    }

    @Override
    public boolean hasStepInput(){
        return true;
    }

    @Override
    public Class<? extends IProgWidget> returnType(){
        return null;
    }

    @Override
    protected boolean hasBlacklist(){
        return false;
    }
}
