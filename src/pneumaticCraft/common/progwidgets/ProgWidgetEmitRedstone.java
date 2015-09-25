package pneumaticCraft.common.progwidgets;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.math.NumberUtils;

import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.programmer.GuiProgWidgetEmitRedstone;
import pneumaticCraft.common.ai.IDroneBase;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ProgWidgetEmitRedstone extends ProgWidget implements IRedstoneEmissionWidget, ISidedWidget{
    private boolean[] accessingSides = new boolean[]{true, true, true, true, true, true};

    @Override
    public int getEmittingRedstone(){
        if(getConnectedParameters()[0] != null) {
            return NumberUtils.toInt(((ProgWidgetString)getConnectedParameters()[0]).string);
        } else {
            return 0;
        }
    }

    @Override
    public void setSides(boolean[] sides){
        accessingSides = sides;
    }

    @Override
    public boolean[] getSides(){
        return accessingSides;
    }

    @Override
    public void addErrors(List<String> curInfo, List<IProgWidget> widgets){
        super.addErrors(curInfo, widgets);

        boolean sideActive = false;
        for(boolean bool : accessingSides) {
            sideActive |= bool;
        }
        if(!sideActive) curInfo.add("gui.progWidget.general.error.noSideActive");
    }

    @Override
    public void getTooltip(List<String> curTooltip){
        super.getTooltip(curTooltip);
        curTooltip.add("Affecting sides:");
        curTooltip.add(getExtraStringInfo());
    }

    @Override
    public String getExtraStringInfo(){
        boolean allSides = true;
        boolean noSides = true;
        for(boolean bool : accessingSides) {
            if(bool) {
                noSides = false;
            } else {
                allSides = false;
            }
        }
        if(allSides) {
            return "All sides";
        } else if(noSides) {
            return "No Sides";
        } else {
            String tip = "";
            for(int i = 0; i < 6; i++) {
                if(accessingSides[i]) {
                    switch(ForgeDirection.getOrientation(i)){
                        case UP:
                            tip += "top, ";
                            break;
                        case DOWN:
                            tip += "bottom, ";
                            break;
                        case NORTH:
                            tip += "north, ";
                            break;
                        case SOUTH:
                            tip += "south, ";
                            break;
                        case EAST:
                            tip += "east, ";
                            break;
                        case WEST:
                            tip += "west, ";
                            break;
                    }
                }
            }
            return tip.substring(0, tip.length() - 2);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        for(int i = 0; i < 6; i++) {
            tag.setBoolean(ForgeDirection.getOrientation(i).name(), accessingSides[i]);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        for(int i = 0; i < 6; i++) {
            accessingSides[i] = tag.getBoolean(ForgeDirection.getOrientation(i).name());
        }
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
        return new Class[]{ProgWidgetString.class};
    }

    @Override
    protected boolean hasBlacklist(){
        return false;
    }

    @Override
    public String getWidgetString(){
        return "emitRedstone";
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.FIRE_FLOWER_DAMAGE;
    }

    @Override
    public WidgetDifficulty getDifficulty(){
        return WidgetDifficulty.EASY;
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_EMIT_REDSTONE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer){
        return new GuiProgWidgetEmitRedstone(this, guiProgrammer);
    }

    @Override
    public EntityAIBase getWidgetAI(IDroneBase drone, IProgWidget widget){
        return new DroneAIEmitRedstone(drone, widget);
    }

    private static class DroneAIEmitRedstone extends EntityAIBase{

        private final IProgWidget widget;
        private final IDroneBase drone;

        public DroneAIEmitRedstone(IDroneBase drone, IProgWidget widget){
            this.widget = widget;
            this.drone = drone;
        }

        @Override
        public boolean shouldExecute(){
            boolean[] sides = ((ISidedWidget)widget).getSides();
            for(int i = 0; i < 6; i++) {
                if(sides[i]) {
                    drone.setEmittingRedstone(ForgeDirection.getOrientation(i), ((IRedstoneEmissionWidget)widget).getEmittingRedstone());
                }
            }
            return false;
        }

    }

}
