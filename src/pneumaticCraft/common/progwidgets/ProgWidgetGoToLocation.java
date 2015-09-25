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
import pneumaticCraft.common.ai.IDroneBase;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ProgWidgetGoToLocation extends ProgWidget implements IGotoWidget, IAreaProvider{

    public boolean doneWhenDeparting;

    @Override
    public void addErrors(List<String> curInfo, List<IProgWidget> widgets){
        super.addErrors(curInfo, widgets);
        if(getConnectedParameters()[0] == null) {
            curInfo.add("gui.progWidget.area.error.noArea");
        }
    }

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
    public EntityAIBase getWidgetAI(IDroneBase drone, IProgWidget widget){
        return new DroneEntityAIGoToLocation(drone, (ProgWidget)widget);
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
    public void getArea(Set<ChunkPosition> area){
        ProgWidgetAreaItemBase.getArea(area, (ProgWidgetArea)getConnectedParameters()[0], (ProgWidgetArea)getConnectedParameters()[getParameters().length]);
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
    public WidgetDifficulty getDifficulty(){
        return WidgetDifficulty.EASY;
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.CHOPPER_PLANT_DAMAGE;
    }
}
