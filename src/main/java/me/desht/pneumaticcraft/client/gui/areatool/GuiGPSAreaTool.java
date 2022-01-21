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

package me.desht.pneumaticcraft.client.gui.areatool;

import me.desht.pneumaticcraft.client.gui.GuiGPSTool;
import me.desht.pneumaticcraft.common.item.ItemGPSAreaTool;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketChangeGPSToolCoordinate;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetArea;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiGPSAreaTool extends GuiGPSTool {

    private static final int CHANGE_AREA_BUTTON_WIDTH = 4 * 22 + 40 + 5 * 2;
    private static final int P1P2_BUTTON_WIDTH = 30;

    private final BlockPos[] p1p2Pos = new BlockPos[2];
    private final String[] vars = new String[2];
    private int index;

    private GuiGPSAreaTool(ItemStack stack, InteractionHand hand, int index) {
        super(stack.getHoverName(), hand,
                ItemGPSAreaTool.getGPSLocation(Minecraft.getInstance().level, stack, index),
                ItemGPSAreaTool.getVariable(stack, index));

        this.index = index;
        for (int i = 0; i <= 1; i++) {
            p1p2Pos[i] = ItemGPSAreaTool.getGPSLocation(Minecraft.getInstance().level, stack, i);
            vars[i] = ItemGPSAreaTool.getVariable(stack, i);
        }
    }

    public static void showGUI(ItemStack stack, InteractionHand hand, int index) {
        Minecraft.getInstance().setScreen(new GuiGPSAreaTool(stack, hand, index));
    }

    @Override
    public void init() {
        super.init();

        int xMiddle = width / 2;
        int yMiddle = height / 2;

        int x = xMiddle - CHANGE_AREA_BUTTON_WIDTH / 2;
        int y = yMiddle + 100;
        addRenderableWidget(new Button(x, y, CHANGE_AREA_BUTTON_WIDTH, 20, xlate("pneumaticcraft.gui.gps_area_tool.changeAreaType"), b -> {
            ItemStack stack = minecraft.player.getItemInHand(hand);
            ProgWidgetArea area = ItemGPSAreaTool.getArea(stack);
            minecraft.setScreen(new GuiProgWidgetAreaTool(area, hand, () -> minecraft.setScreen(new GuiGPSAreaTool(stack, hand, index))));
        }));

        addRenderableWidget(new Button(xMiddle - P1P2_BUTTON_WIDTH / 2, yMiddle - 45, P1P2_BUTTON_WIDTH, 20, getToggleLabel(),
                this::toggle));
    }

    @Override
    protected int getIndex() {
        return index;
    }

    @Override
    protected void syncToServer() {
        p1p2Pos[index] = new BlockPos(textFields[0].getIntValue(), textFields[1].getIntValue(), textFields[2].getIntValue());
        vars[index] = variableField.getValue();
        for (int i = 0; i <= 1; i++) {
            if (changed(i)) NetworkHandler.sendToServer(new PacketChangeGPSToolCoordinate(p1p2Pos[i], hand, vars[i], i));
        }
    }

    private boolean changed(int index) {
        ItemStack stack = minecraft.player.getItemInHand(hand);
        BlockPos p = ItemGPSAreaTool.getGPSLocation(Minecraft.getInstance().level, stack, index);
        String var = ItemGPSAreaTool.getVariable(stack, index);
        return !p.equals(p1p2Pos[index]) || !var.equals(vars[index]);
    }

    private void toggle(Button b) {
        ItemStack stack = Minecraft.getInstance().player.getItemInHand(hand);
        if (stack.getItem() instanceof ItemGPSAreaTool) {
            p1p2Pos[index] = new BlockPos(textFields[0].getIntValue(), textFields[1].getIntValue(), textFields[2].getIntValue());
            vars[index] = variableField.getValue();

            index = 1 - index;

            b.setMessage(getToggleLabel());
            textFields[0].setValue(p1p2Pos[index].getX());
            textFields[1].setValue(p1p2Pos[index].getY());
            textFields[2].setValue(p1p2Pos[index].getZ());
            variableField.setValue(vars[index]);
        }
    }

    private Component getToggleLabel() {
        ChatFormatting color = index == 0 ? ChatFormatting.RED : ChatFormatting.GREEN;
        return new TextComponent("P" + (index + 1)).withStyle(color);
    }
}
