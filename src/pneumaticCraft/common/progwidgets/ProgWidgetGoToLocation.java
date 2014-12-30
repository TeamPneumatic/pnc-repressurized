package pneumaticCraft.common.progwidgets;

import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.programmer.GuiProgWidgetGoto;
import pneumaticCraft.common.ai.DroneEntityAIGoToLocation;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ProgWidgetGoToLocation extends ProgWidget implements IGotoWidget, IAreaProvider{

    public boolean doneWhenDeparting;

    @Override
    public boolean doneWhenDeparting(){
        return doneWhenDeparting;
    }

    @Override
    public void setDoneWhenDeparting(boolean bool){
        doneWhenDeparting = bool;
    }

    @Override
    public void getTooltip(List<String> curTooltip){
        super.getTooltip(curTooltip);
        curTooltip.add("Done when " + (doneWhenDeparting ? "departing" : "arrived"));
    }

    @Override
    public String getWidgetString(){
        return "goto";
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_GOTO;
    }

    @Override
    public EntityAIBase getWidgetAI(EntityDrone drone, IProgWidget widget){
        return new DroneEntityAIGoToLocation(drone, 0.1, (ProgWidget)widget);
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
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetArea.class};
    }

    @Override
    public Set<ChunkPosition> getArea(){
        return ProgWidgetAreaItemBase.getArea((ProgWidgetArea)getConnectedParameters()[0], null);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setBoolean("doneWhenDeparting", doneWhenDeparting);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        doneWhenDeparting = tag.getBoolean("doneWhenDeparting");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer){
        return new GuiProgWidgetGoto(this, guiProgrammer);
    }

    @Override
    public String getGuiTabText(){
        return "This will make the Drone travel to the location specified. When a big area is selected, the Drone will go to the closest reachable position within this area.";
    }

    @Override
    public int getGuiTabColor(){
        return 0xFF0092ef;
    }

    @Override
    public WidgetCategory getCategory(){
        return WidgetCategory.ACTION;
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.CHOPPER_PLANT_DAMAGE;
    }
}
