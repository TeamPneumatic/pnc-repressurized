package pneumaticCraft.common.progwidgets;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.programmer.GuiProgWidgetPlace;
import pneumaticCraft.common.ai.DroneAIPlace;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Log;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ProgWidgetPlace extends ProgWidgetDigAndPlace implements ISidedWidget{
    public ForgeDirection placeDir = ForgeDirection.DOWN;

    public ProgWidgetPlace(){
        super(ProgWidgetDigAndPlace.EnumOrder.LOW_TO_HIGH);
    }

    @Override
    public void setSides(boolean[] sides){
        placeDir = getDirForSides(sides);
    }

    @Override
    public boolean[] getSides(){
        return getSidesFromDir(placeDir);
    }

    public static ForgeDirection getDirForSides(boolean[] sides){
        for(int i = 0; i < sides.length; i++) {
            if(sides[i]) {
                return ForgeDirection.getOrientation(i);
            }
        }
        Log.error("[ProgWidgetPlace] Sides boolean array empty!");
        return ForgeDirection.DOWN;
    }

    public static boolean[] getSidesFromDir(ForgeDirection dir){
        boolean[] dirs = new boolean[6];
        dirs[dir.ordinal()] = true;
        return dirs;
    }

    @Override
    public void getTooltip(List<String> curTooltip){
        super.getTooltip(curTooltip);
        curTooltip.add("Placing direction: " + PneumaticCraftUtils.getOrientationName(placeDir));
    }

    @Override
    public String getWidgetString(){
        return "place";
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_PLACE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer){
        return new GuiProgWidgetPlace(this, guiProgrammer);
    }

    @Override
    public EntityAIBase getWidgetAI(EntityDrone drone, IProgWidget widget){
        return new DroneAIPlace(drone, drone.getSpeed(), (ProgWidgetAreaItemBase)widget);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setInteger("dir", placeDir.ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        placeDir = ForgeDirection.getOrientation(tag.getInteger("dir"));
    }

    @Override
    public String getGuiTabText(){
        return "This program is used to allow the Drone to place blocks in the area selected. The Drone will take them from it's inventory. You can specify which blocks are allowed to be placed by using item filters. You can also somewhat influence in which order the blocks are placed down by right clicking the program. \n \nAir usage: " + PneumaticValues.DRONE_USAGE_PLACE + "mL/block.";
    }

    @Override
    public int getGuiTabColor(){
        return 0xFFbfd503;
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.HELIUM_PLANT_DAMAGE;
    }

}
