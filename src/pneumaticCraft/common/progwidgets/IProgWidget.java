package pneumaticCraft.common.progwidgets;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
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

    public Class<? extends IProgWidget> returnType();//true for widgets that can give info to the widget left of it (like areas or filters)

    public Class<? extends IProgWidget>[] getParameters(); //the entity attack widget for instance returns the filter and area class.

    public void setParameter(int index, IProgWidget parm);

    public IProgWidget[] getConnectedParameters();//this includes whitelist and blacklist. whitelist will go in the first half of elements, blacklist in the second half.

    public void setParent(IProgWidget widget);

    public IProgWidget getParent();

    /**
     * Unique identifier
     * @return
     */
    public String getWidgetString();

    /**
     * Just a method to have backwards compatibility.
     * @return
     */
    @Deprecated
    public String getLegacyString();

    public String getGuiTabText();

    public int getGuiTabColor();

    /**
     * At least do a tag.setString("id", getWidgetString());
     * @param tag
     */
    public void writeToNBT(NBTTagCompound tag);

    public void readFromNBT(NBTTagCompound tag);

    public IProgWidget copy();

    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer);
}
