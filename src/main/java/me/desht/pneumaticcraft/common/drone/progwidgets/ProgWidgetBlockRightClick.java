/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.drone.progwidgets;

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.drone.ai.DroneAIRightClickBlock;
import me.desht.pneumaticcraft.common.registry.ModProgWidgets;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.List;

public class ProgWidgetBlockRightClick extends ProgWidgetPlace implements IBlockRightClicker, ISidedWidget {
    private Direction clickSide = Direction.UP;
    private boolean sneaking;
    private RightClickType clickType = RightClickType.CLICK_ITEM;

    public ProgWidgetBlockRightClick() {
        super(ModProgWidgets.BLOCK_RIGHT_CLICK.get());
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
    public boolean supportsMaxActions() {
        return false;
    }

    @Override
    public boolean isSneaking() {
        return sneaking;
    }

    public void setSneaking(boolean sneaking) {
        this.sneaking = sneaking;
    }

    @Override
    public RightClickType getClickType() {
        return clickType;
    }

    public void setClickType(RightClickType clickType) {
        this.clickType = clickType;
    }

    public Direction getClickSide() {
        return clickSide;
    }

    public void setClickSide(Direction clickSide) {
        this.clickSide = clickSide;
    }

    @Override
    public void getTooltip(List<Component> curTooltip) {
        super.getTooltip(curTooltip);

        curTooltip.add(Component.translatable("pneumaticcraft.gui.progWidget.blockRightClick.clickSide")
                .append(": " + ClientUtils.translateDirection(clickSide)));
        if (sneaking) {
            curTooltip.add(Component.translatable("pneumaticcraft.gui.progWidget.blockRightClick.sneaking"));
        }
        curTooltip.add(Component.translatable("pneumaticcraft.gui.progWidget.blockRightClick.operation")
                .append(": ")
                .append(Component.translatable(clickType.getTranslationKey())));
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        if (sneaking) tag.putBoolean("sneaking", true);
        tag.putInt("dir", clickSide.get3DDataValue());
        tag.putString("clickType", clickType.toString());
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        sneaking = tag.getBoolean("sneaking");
        clickSide = Direction.from3DDataValue(tag.getInt("dir"));
        clickType = tag.contains("clickType") ? RightClickType.valueOf(tag.getString("clickType")) : RightClickType.CLICK_ITEM;
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf) {
        super.writeToPacket(buf);
        buf.writeBoolean(sneaking);
        buf.writeEnum(clickSide);
        buf.writeEnum(clickType);
    }

    @Override
    public void readFromPacket(FriendlyByteBuf buf) {
        super.readFromPacket(buf);
        sneaking = buf.readBoolean();
        clickSide = buf.readEnum(Direction.class);
        clickType = buf.readEnum(RightClickType.class);
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
