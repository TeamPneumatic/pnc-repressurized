package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.programmer.GuiProgWidgetEmitRedstone;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.item.ItemPlastic;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;

public class ProgWidgetEmitRedstone extends ProgWidget implements IRedstoneEmissionWidget, ISidedWidget {
    private boolean[] accessingSides = new boolean[]{true, true, true, true, true, true};

    @Override
    public int getEmittingRedstone() {
        if (getConnectedParameters()[0] != null) {
            return NumberUtils.toInt(((ProgWidgetString) getConnectedParameters()[0]).string);
        } else {
            return 0;
        }
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
    public void addErrors(List<String> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);

        boolean sideActive = false;
        for (boolean bool : accessingSides) {
            sideActive |= bool;
        }
        if (!sideActive) curInfo.add("gui.progWidget.general.error.noSideActive");
    }

    @Override
    public void getTooltip(List<String> curTooltip) {
        super.getTooltip(curTooltip);
        curTooltip.add("Affecting sides:");
        curTooltip.add(getExtraStringInfo());
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
            StringBuilder tipBuilder = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                if (accessingSides[i]) {
                    switch (EnumFacing.getFront(i)) {
                        case UP:
                            tipBuilder.append("top, ");
                            break;
                        case DOWN:
                            tipBuilder.append("bottom, ");
                            break;
                        case NORTH:
                            tipBuilder.append("north, ");
                            break;
                        case SOUTH:
                            tipBuilder.append("south, ");
                            break;
                        case EAST:
                            tipBuilder.append("east, ");
                            break;
                        case WEST:
                            tipBuilder.append("west, ");
                            break;
                    }
                }
            }
            String tip = tipBuilder.toString();
            return tip.substring(0, tip.length() - 2);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        for (int i = 0; i < 6; i++) {
            tag.setBoolean(EnumFacing.getFront(i).name(), accessingSides[i]);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        for (int i = 0; i < 6; i++) {
            accessingSides[i] = tag.getBoolean(EnumFacing.getFront(i).name());
        }
    }

    @Override
    public boolean hasStepInput() {
        return true;
    }

    @Override
    public Class<? extends IProgWidget> returnType() {
        return null;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters() {
        return new Class[]{ProgWidgetString.class};
    }

    @Override
    protected boolean hasBlacklist() {
        return false;
    }

    @Override
    public String getWidgetString() {
        return "emitRedstone";
    }

    @Override
    public int getCraftingColorIndex() {
        return ItemPlastic.RED;
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.EASY;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_EMIT_REDSTONE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer) {
        return new GuiProgWidgetEmitRedstone(this, guiProgrammer);
    }

    @Override
    public EntityAIBase getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return new DroneAIEmitRedstone(drone, widget);
    }

    private static class DroneAIEmitRedstone extends EntityAIBase {

        private final IProgWidget widget;
        private final IDroneBase drone;

        public DroneAIEmitRedstone(IDroneBase drone, IProgWidget widget) {
            this.widget = widget;
            this.drone = drone;
        }

        @Override
        public boolean shouldExecute() {
            boolean[] sides = ((ISidedWidget) widget).getSides();
            for (int i = 0; i < 6; i++) {
                if (sides[i]) {
                    drone.setEmittingRedstone(EnumFacing.getFront(i), ((IRedstoneEmissionWidget) widget).getEmittingRedstone());
                }
            }
            return false;
        }

    }

}
