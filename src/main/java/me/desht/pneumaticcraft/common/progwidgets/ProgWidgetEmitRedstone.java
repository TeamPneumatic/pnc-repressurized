package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import joptsimple.internal.Strings;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetEmitRedstone extends ProgWidget implements IRedstoneEmissionWidget, ISidedWidget {
    private boolean[] accessingSides = new boolean[]{true, true, true, true, true, true};

    public ProgWidgetEmitRedstone() {
        super(ModProgWidgets.EMIT_REDSTONE.get());
    }

    @Override
    public int getEmittingRedstone() {
        if (getConnectedParameters()[0] != null) {
            return NumberUtils.toInt(((ProgWidgetText) getConnectedParameters()[0]).string);
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
    public void addErrors(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);

        boolean sideActive = false;
        for (boolean bool : accessingSides) {
            sideActive |= bool;
        }
        if (!sideActive) curInfo.add(xlate("pneumaticcraft.gui.progWidget.general.error.noSideActive"));
    }

    @Override
    public void getTooltip(List<ITextComponent> curTooltip) {
        super.getTooltip(curTooltip);
        curTooltip.add(xlate("pneumaticcraft.gui.progWidget.general.affectingSides"));
        curTooltip.addAll(getExtraStringInfo());
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
                    .filter(side -> accessingSides[side.getIndex()])
                    .map(ClientUtils::translateDirection)
                    .collect(Collectors.toList());
            return Collections.singletonList(new StringTextComponent(Strings.join(l, ", ")));
        }
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        for (int i = 0; i < 6; i++) {
            if (accessingSides[i]) tag.putBoolean(Direction.byIndex(i).name(), true);
        }
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        for (int i = 0; i < 6; i++) {
            accessingSides[i] = tag.getBoolean(Direction.byIndex(i).name());
        }
    }

    @Override
    public void writeToPacket(PacketBuffer buf) {
        super.writeToPacket(buf);
        for (int i = 0; i < 6; i++) {
            buf.writeBoolean(accessingSides[i]);
        }
    }

    @Override
    public void readFromPacket(PacketBuffer buf) {
        super.readFromPacket(buf);
        for (int i = 0; i < 6; i++) {
            accessingSides[i] = buf.readBoolean();
        }
    }

    @Override
    public boolean hasStepInput() {
        return true;
    }

    @Override
    public ProgWidgetType<?> returnType() {
        return null;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.TEXT.get());
    }

    @Override
    protected boolean hasBlacklist() {
        return false;
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.RED;
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
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return new DroneAIEmitRedstone(drone, widget);
    }

    private static class DroneAIEmitRedstone extends Goal {

        private final IProgWidget widget;
        private final IDroneBase drone;

        DroneAIEmitRedstone(IDroneBase drone, IProgWidget widget) {
            this.widget = widget;
            this.drone = drone;
        }

        @Override
        public boolean shouldExecute() {
            boolean[] sides = ((ISidedWidget) widget).getSides();
            for (int i = 0; i < 6; i++) {
                if (sides[i]) {
                    drone.setEmittingRedstone(Direction.byIndex(i), ((IRedstoneEmissionWidget) widget).getEmittingRedstone());
                }
            }
            return false;
        }

    }

}
