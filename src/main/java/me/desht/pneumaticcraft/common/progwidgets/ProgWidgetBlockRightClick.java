package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.DroneAIRightClickBlock;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class ProgWidgetBlockRightClick extends ProgWidgetPlace implements IBlockRightClicker, ISidedWidget {
    private Direction clickSide = Direction.UP;
    private boolean sneaking;

    public ProgWidgetBlockRightClick() {
        super(ModProgWidgets.BLOCK_RIGHT_CLICK);
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_BLOCK_RIGHT_CLICK;
    }

    @Override
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return setupMaxActions(new DroneAIRightClickBlock(drone, (ProgWidgetAreaItemBase) widget), (IMaxActions) widget);
    }

    @Override
    public boolean isSneaking() {
        return sneaking;
    }

    public void setSneaking(boolean sneaking) {
        this.sneaking = sneaking;
    }

    public Direction getClickSide() {
        return clickSide;
    }

    public void setClickSide(Direction clickSide) {
        this.clickSide = clickSide;
    }

    @Override
    public void getTooltip(List<ITextComponent> curTooltip) {
        super.getTooltip(curTooltip);

        curTooltip.add(new TranslationTextComponent("gui.progWidget.blockRightClick.clickSide").appendText(": " + PneumaticCraftUtils.getOrientationName(clickSide)));
        if (sneaking) {
            curTooltip.add(new TranslationTextComponent("gui.progWidget.blockRightClick.sneaking"));
        }
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        tag.putBoolean("sneaking", sneaking);
        tag.putInt("dir", clickSide.ordinal());
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        sneaking = tag.getBoolean("sneaking");
        clickSide = Direction.byIndex(tag.getInt("dir"));
    }

    @Override
    public void writeToPacket(PacketBuffer buf) {
        super.writeToPacket(buf);
        buf.writeBoolean(sneaking);
        buf.writeByte(clickSide.ordinal());
    }

    @Override
    public void readFromPacket(PacketBuffer buf) {
        super.readFromPacket(buf);
        sneaking = buf.readBoolean();
        clickSide = Direction.byIndex(buf.readByte());
    }

    @Override
    public void setSides(boolean[] sides) {
        clickSide = ISidedWidget.getDirForSides(sides);
    }

    @Override
    public boolean[] getSides() {
        return ISidedWidget.getSidesFromDir(clickSide);
    }
}
