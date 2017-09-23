package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface IProgWidget {
    int getX();

    int getY();

    void setX(int x);

    void setY(int y);

    int getWidth();

    int getHeight();

    void render();

    ResourceLocation getTexture();

    Pair<Double, Double> getMaxUV();

    int getTextureSize();

    void getTooltip(List<String> curTooltip);

    void addWarnings(List<String> curInfo, List<IProgWidget> widgets);

    void addErrors(List<String> curInfo, List<IProgWidget> widgets);

    void renderExtraInfo();

    boolean hasStepInput();

    boolean hasStepOutput();

    /**
     * @param drone
     * @param widget Will be 'this' most of the times, but not when controlled by ComputerCraft.
     * @return
     */
    EntityAIBase getWidgetTargetAI(IDroneBase drone, IProgWidget widget);

    EntityAIBase getWidgetAI(IDroneBase drone, IProgWidget widget);

    void setOutputWidget(IProgWidget widget);

    IProgWidget getOutputWidget();

    /**
     * This one will be called when running in an actual program.
     *
     * @param drone
     * @param allWidgets
     * @return
     */
    IProgWidget getOutputWidget(IDroneBase drone, List<IProgWidget> allWidgets);

    Class<? extends IProgWidget> returnType();//true for widgets that can give info to the widget left of it (like areas or filters)

    Class[] getParameters(); //the entity attack widget for instance returns the filter and area class.

    void setParameter(int index, IProgWidget parm);

    boolean canSetParameter(int index);

    IProgWidget[] getConnectedParameters();//this includes whitelist and blacklist. whitelist will go in the first half of elements, blacklist in the second half.

    void setParent(IProgWidget widget);

    IProgWidget getParent();

    /**
     * Unique identifier
     *
     * @return
     */
    String getWidgetString();

    int getCraftingColorIndex();

    /**
     * At least do a tag.setString("id", getWidgetString());
     *
     * @param tag
     */
    void writeToNBT(NBTTagCompound tag);

    void readFromNBT(NBTTagCompound tag);

    IProgWidget copy();

    boolean canBeRunByComputers(IDroneBase drone, IProgWidget widget);

    @SideOnly(Side.CLIENT)
    GuiScreen getOptionWindow(GuiProgrammer guiProgrammer);

    WidgetDifficulty getDifficulty();

    enum WidgetDifficulty {
        EASY("easy"), MEDIUM("medium"), ADVANCED("advanced");

        private final String name;

        WidgetDifficulty(String name) {
            this.name = name;
        }

        public String getLocalizedName() {
            return I18n.format("gui.progWidget.difficulty." + name);
        }
    }
}
