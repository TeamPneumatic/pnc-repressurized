package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.programmer.GuiProgWidgetImportExport;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public abstract class ProgWidgetInventoryBase extends ProgWidgetAreaItemBase implements ISidedWidget, ICountWidget {
    private boolean[] accessingSides = new boolean[]{true, true, true, true, true, true};
    private boolean useCount;
    private int count = 1;

    @Override
    public void addErrors(List<String> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);

        boolean sideActive = false;
        for (boolean bool : accessingSides) {
            sideActive |= bool;
        }
        if (!sideActive) curInfo.add("gui.progWidget.general.error.noSideActive");
    }

    @Override
    public void setSides(boolean[] sides) {
        accessingSides = sides;
    }

    @Override
    public boolean[] getSides() {
        return accessingSides;
    }

    @Override
    public boolean useCount() {
        return useCount;
    }

    @Override
    public void setUseCount(boolean useCount) {
        this.useCount = useCount;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public void getTooltip(List<String> curTooltip) {
        super.getTooltip(curTooltip);
        if (isUsingSides()) curTooltip.add("Accessing sides:");
        curTooltip.add(getExtraStringInfo());
        if (useCount) curTooltip.add("Using count (" + count + ")");
    }

    protected boolean isUsingSides() {
        return true;
    }

    @Override
    public String getExtraStringInfo() {
        boolean allSides = true;
        boolean noSides = true;
        for (boolean bool : accessingSides) {
            if (bool) {
                noSides = false;
            } else {
                allSides = false;
            }
        }
        if (allSides) {
            return "All sides";
        } else if (noSides) {
            return "No Sides";
        } else {
            StringBuilder tip = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                if (accessingSides[i]) {
                    switch (EnumFacing.getFront(i)) {
                        case UP:
                            tip.append("top, ");
                            break;
                        case DOWN:
                            tip.append("bottom, ");
                            break;
                        case NORTH:
                            tip.append("north, ");
                            break;
                        case SOUTH:
                            tip.append("south, ");
                            break;
                        case EAST:
                            tip.append("east, ");
                            break;
                        case WEST:
                            tip.append("west, ");
                            break;
                    }
                }
            }
            return tip.substring(0, tip.length() - 2);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        for (int i = 0; i < 6; i++) {
            tag.setBoolean(EnumFacing.getFront(i).name(), accessingSides[i]);
        }
        tag.setBoolean("useCount", useCount);
        tag.setInteger("count", count);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        for (int i = 0; i < 6; i++) {
            accessingSides[i] = tag.getBoolean(EnumFacing.getFront(i).name());
        }
        useCount = tag.getBoolean("useCount");
        count = tag.getInteger("count");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer) {
        return new GuiProgWidgetImportExport(this, guiProgrammer);
    }
}
