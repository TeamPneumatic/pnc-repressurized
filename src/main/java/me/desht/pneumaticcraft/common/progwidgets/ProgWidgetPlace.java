package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.DroneAIPlace;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;

public class ProgWidgetPlace extends ProgWidgetDigAndPlace implements ISidedWidget {
    public Direction placeDir = Direction.DOWN;

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

    public static Direction getDirForSides(boolean[] sides) {
        for (int i = 0; i < sides.length; i++) {
            if (sides[i]) {
                return Direction.byIndex(i);
            }
        }
        Log.error("[ProgWidgetPlace] Sides boolean array empty!");
        return Direction.DOWN;
    }

    public static boolean[] getSidesFromDir(Direction dir) {
        boolean[] dirs = new boolean[6];
        dirs[dir.ordinal()] = true;
        return dirs;
    }

    @Override
    public void getTooltip(List<ITextComponent> curTooltip) {
        super.getTooltip(curTooltip);
        curTooltip.add(new StringTextComponent("Placing direction: " + PneumaticCraftUtils.getOrientationName(placeDir)));
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
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return setupMaxActions(new DroneAIPlace(drone, (ProgWidgetPlace) widget), (IMaxActions) widget);
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        tag.putInt("dir", placeDir.ordinal());
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        placeDir = Direction.byIndex(tag.getInt("dir"));
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.YELLOW;
    }
}
