package pneumaticCraft.common.progwidgets;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.nbt.NBTTagCompound;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.common.entity.living.EntityDrone;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IProgWidget{
    public int getX();

    public int getY();

    public void setX(int x);

    public void setY(int y);

    public int getWidth();

    public int getHeight();

    public void render();

    public void getTooltip(List<String> curTooltip);

    public void renderExtraInfo();

    public void addCompileErrors(List<String> curErrors);

    public boolean hasStepInput();

    public boolean hasStepOutput();

    /**
     * 
     * @param drone
     * @param widget Will be 'this' most of the times, but not when controlled by ComputerCraft.
     * @return
     */
    public EntityAIBase getWidgetTargetAI(EntityDrone drone, IProgWidget widget);

    public EntityAIBase getWidgetAI(EntityDrone drone, IProgWidget widget);

    public void setOutputWidget(IProgWidget widget);

    public IProgWidget getOutputWidget();

    /**
     * This one will be called when running in an actual program.
     * @param drone
     * @param allWidgets
     * @return
     */
    public IProgWidget getOutputWidget(EntityDrone drone, List<IProgWidget> allWidgets);

    public Class<? extends IProgWidget> returnType();//true for widgets that can give info to the widget left of it (like areas or filters)

    public Class<? extends IProgWidget>[] getParameters(); //the entity attack widget for instance returns the filter and area class.

    public void setParameter(int index, IProgWidget parm);

    public boolean canSetParameter(int index);

    public IProgWidget[] getConnectedParameters();//this includes whitelist and blacklist. whitelist will go in the first half of elements, blacklist in the second half.

    public void setParent(IProgWidget widget);

    public IProgWidget getParent();

    /**
     * Unique identifier
     * @return
     */
    public String getWidgetString();

    public String getGuiTabText();

    public int getGuiTabColor();

    public int getCraftingColorIndex();

    /**
     * At least do a tag.setString("id", getWidgetString());
     * @param tag
     */
    public void writeToNBT(NBTTagCompound tag);

    public void readFromNBT(NBTTagCompound tag);

    public IProgWidget copy();

    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer);

    public WidgetCategory getCategory();

    public static enum WidgetCategory{
        PARAMETER("parameters"), FLOW_CONTROL("flowControl"), ACTION("actions"), CONDITION("conditions");

        private final String name;

        private WidgetCategory(String name){
            this.name = name;
        }

        public String getLocalizedName(){
            return I18n.format("gui.progWidget.category." + name);
        }
    }
}
