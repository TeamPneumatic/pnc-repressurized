package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.programmer.GuiProgWidgetPlace;
import me.desht.pneumaticcraft.common.ai.DroneAIPlace;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.item.ItemPlastic;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ProgWidgetPlace extends ProgWidgetDigAndPlace implements ISidedWidget {
    public EnumFacing placeDir = EnumFacing.DOWN;

    public ProgWidgetPlace() {
        super(ProgWidgetDigAndPlace.EnumOrder.LOW_TO_HIGH);
    }

    @Override
    public void setSides(boolean[] sides) {
        placeDir = getDirForSides(sides);
    }

    @Override
    public boolean[] getSides() {
        return getSidesFromDir(placeDir);
    }

    public static EnumFacing getDirForSides(boolean[] sides) {
        for (int i = 0; i < sides.length; i++) {
            if (sides[i]) {
                return EnumFacing.getFront(i);
            }
        }
        Log.error("[ProgWidgetPlace] Sides boolean array empty!");
        return EnumFacing.DOWN;
    }

    public static boolean[] getSidesFromDir(EnumFacing dir) {
        boolean[] dirs = new boolean[6];
        dirs[dir.ordinal()] = true;
        return dirs;
    }

    @Override
    public void getTooltip(List<String> curTooltip) {
        super.getTooltip(curTooltip);
        curTooltip.add("Placing direction: " + PneumaticCraftUtils.getOrientationName(placeDir));
    }

    @Override
    public String getWidgetString() {
        return "place";
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_PLACE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer) {
        return new GuiProgWidgetPlace(this, guiProgrammer);
    }

    @Override
    public EntityAIBase getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return setupMaxActions(new DroneAIPlace(drone, (ProgWidgetAreaItemBase) widget), (IMaxActions) widget);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("dir", placeDir.ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        placeDir = EnumFacing.getFront(tag.getInteger("dir"));
    }

    @Override
    public int getCraftingColorIndex() {
        return ItemPlastic.YELLOW;
    }

}
