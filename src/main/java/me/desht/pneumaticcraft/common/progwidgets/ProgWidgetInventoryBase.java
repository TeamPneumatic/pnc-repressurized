package me.desht.pneumaticcraft.common.progwidgets;

import joptsimple.internal.Strings;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * Base class for widgets which have side filtering and count limits.
 */
public abstract class ProgWidgetInventoryBase extends ProgWidgetAreaItemBase implements ISidedWidget, ICountWidget {
    private boolean[] accessingSides = new boolean[]{false, true, false, false, false, false};
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
        if (!sideActive) curInfo.add(xlate("pneumaticcraft.gui.progWidget.general.error.noSideActive"));
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
        if (isUsingSides()) curTooltip.add(xlate("pneumaticcraft.gui.progWidget.inventory.accessingSides"));
        curTooltip.add(new StringTextComponent(Symbols.TRIANGLE_RIGHT + " ").append(getExtraStringInfo().get(0)));
        if (useCount) curTooltip.add(xlate("pneumaticcraft.gui.progWidget.inventory.usingCount", count));
    }

    protected boolean isUsingSides() {
        return true;
    }

    @Override
    public List<ITextComponent> getExtraStringInfo() {
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
            return Collections.singletonList(ALL_TEXT);
        } else if (noSides) {
            return Collections.singletonList(NONE_TEXT);
        } else {
            List<String> l = Arrays.stream(DirectionUtil.VALUES)
                    .filter(side -> accessingSides[side.get3DDataValue()])
                    .map(ClientUtils::translateDirection)
                    .collect(Collectors.toList());
            return Collections.singletonList(new StringTextComponent(Strings.join(l, ", ")));
        }
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        for (int i = 0; i < 6; i++) {
            if (accessingSides[i]) tag.putBoolean(Direction.from3DDataValue(i).name(), true);
        }
        if (useCount) tag.putBoolean("useCount", true);
        tag.putInt("count", count);
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        for (int i = 0; i < 6; i++) {
            accessingSides[i] = tag.getBoolean(Direction.from3DDataValue(i).name());
        }
        useCount = tag.getBoolean("useCount");
        count = tag.getInt("count");
    }

    @Override
    public void writeToPacket(PacketBuffer buf) {
        super.writeToPacket(buf);
        for (int i = 0; i < 6; i++) {
            buf.writeBoolean(accessingSides[i]);
        }
        buf.writeBoolean(useCount);
        buf.writeVarInt(count);
    }

    @Override
    public void readFromPacket(PacketBuffer buf) {
        super.readFromPacket(buf);
        for (int i = 0; i < 6; i++) {
            accessingSides[i] = buf.readBoolean();
        }
        useCount = buf.readBoolean();
        count = buf.readVarInt();
    }
}
