package pneumaticCraft.common.progwidgets;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.programmer.GuiProgWidgetImportExport;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class ProgWidgetInventoryBase extends ProgWidgetAreaItemBase implements ISidedWidget, ICountWidget{
    private boolean[] accessingSides = new boolean[]{true, true, true, true, true, true};
    private boolean useCount;
    private int count = 1;

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
    public void setSides(boolean[] sides){
        accessingSides = sides;
    }

    @Override
    public boolean[] getSides(){
        return accessingSides;
    }

    @Override
    public boolean useCount(){
        return useCount;
    }

    @Override
    public void setUseCount(boolean useCount){
        this.useCount = useCount;
    }

    @Override
    public int getCount(){
        return count;
    }

    @Override
    public void setCount(int count){
        this.count = count;
    }

    @Override
    public void getTooltip(List<String> curTooltip){
        super.getTooltip(curTooltip);
        if(isUsingSides()) curTooltip.add("Accessing sides:");
        curTooltip.add(getExtraStringInfo());
        if(useCount) curTooltip.add("Using count (" + count + ")");
    }

    protected boolean isUsingSides(){
        return true;
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
        tag.setBoolean("useCount", useCount);
        tag.setInteger("count", count);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        for(int i = 0; i < 6; i++) {
            accessingSides[i] = tag.getBoolean(ForgeDirection.getOrientation(i).name());
        }
        useCount = tag.getBoolean("useCount");
        count = tag.getInteger("count");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer){
        return new GuiProgWidgetImportExport(this, guiProgrammer);
    }
}
