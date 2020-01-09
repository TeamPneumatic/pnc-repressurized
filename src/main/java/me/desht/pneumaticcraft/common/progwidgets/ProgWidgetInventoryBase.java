package me.desht.pneumaticcraft.common.progwidgets;

import joptsimple.internal.Strings;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * Base class for widgets which have side filtering and count limits.
 */
public abstract class ProgWidgetInventoryBase extends ProgWidgetAreaItemBase implements ISidedWidget, ICountWidget {
    private boolean[] accessingSides = new boolean[]{true, true, true, true, true, true};
    private boolean useCount;
    private int count = 1;

    public ProgWidgetInventoryBase(ProgWidgetType<?> type) {
        super(type);
    }

    @Override
    public void addErrors(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);

        boolean sideActive = false;
        for (boolean bool : accessingSides) {
            sideActive |= bool;
        }
        if (!sideActive) curInfo.add(xlate("gui.progWidget.general.error.noSideActive"));
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
    public void getTooltip(List<ITextComponent> curTooltip) {
        super.getTooltip(curTooltip);
        if (isUsingSides()) curTooltip.add(xlate("gui.progWidget.inventory.accessingSides"));
        curTooltip.add(new StringTextComponent(getExtraStringInfo()));
        if (useCount) curTooltip.add(xlate("gui.progWidget.inventory.usingCount", count));
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
            return "ALL";
        } else if (noSides) {
            return "NONE";
        } else {
            List<String> l = Arrays.stream(Direction.VALUES)
                    .filter(side -> accessingSides[side.getIndex()])
                    .map(Direction::getName)
                    .collect(Collectors.toList());
            return Strings.join(l, ", ");
        }
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        for (int i = 0; i < 6; i++) {
            tag.putBoolean(Direction.byIndex(i).name(), accessingSides[i]);
        }
        tag.putBoolean("useCount", useCount);
        tag.putInt("count", count);
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        for (int i = 0; i < 6; i++) {
            accessingSides[i] = tag.getBoolean(Direction.byIndex(i).name());
        }
        useCount = tag.getBoolean("useCount");
        count = tag.getInt("count");
    }
}
