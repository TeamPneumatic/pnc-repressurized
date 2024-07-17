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

package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.programmer.ProgWidgetAreaToolScreen;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetArea;
import me.desht.pneumaticcraft.common.item.GPSAreaToolItem;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketChangeGPSToolCoordinate;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GPSAreaToolScreen extends GPSToolScreen {

    private static final int CHANGE_AREA_BUTTON_WIDTH = 4 * 22 + 40 + 5 * 2;
    private static final int P1P2_BUTTON_WIDTH = 30;

    private final BlockPos[] p1p2Pos = new BlockPos[2];
    private final String[] vars = new String[2];
    private final boolean[] playerGlobals = new boolean[2];
    private int index;

    private GPSAreaToolScreen(ItemStack stack, InteractionHand hand, int index) {
        super(stack.getHoverName(), hand,
                GPSAreaToolItem.getGPSLocation(Minecraft.getInstance().player, stack, index).orElse(ClientUtils.getClientPlayer().blockPosition()),
                GPSAreaToolItem.getVariable(Minecraft.getInstance().player, stack, index));

        this.index = index;
        for (int i = 0; i <= 1; i++) {
            p1p2Pos[i] = GPSAreaToolItem.getGPSLocation(Minecraft.getInstance().player, stack, i).orElse(ClientUtils.getClientPlayer().blockPosition());
            vars[i] = GPSAreaToolItem.getVariable(Minecraft.getInstance().player, stack, i);
            playerGlobals[i] = !vars[i].startsWith("%");
            vars[i] = GlobalVariableHelper.getInstance().stripVarPrefix(vars[i]);
        }
    }

    public static void showGUI(ItemStack stack, InteractionHand hand, int index) {
        Minecraft.getInstance().setScreen(new GPSAreaToolScreen(stack, hand, index));
    }

    @Override
    public void init() {
        super.init();

        int xMiddle = width / 2;
        int yMiddle = height / 2;

        int x = xMiddle - CHANGE_AREA_BUTTON_WIDTH / 2;
        int y = yMiddle + 100;
        addRenderableWidget(new WidgetButtonExtended(x, y, CHANGE_AREA_BUTTON_WIDTH, 20, xlate("pneumaticcraft.gui.gps_area_tool.changeAreaType"), b -> {
            ItemStack stack = ClientUtils.getClientPlayer().getItemInHand(hand);
            ProgWidgetArea area = GPSAreaToolItem.getArea(ClientUtils.getClientPlayer(), stack);
            minecraft.setScreen(new ProgWidgetAreaToolScreen(area, hand, () -> minecraft.setScreen(new GPSAreaToolScreen(stack, hand, index))));
        }));

        addRenderableWidget(new WidgetButtonExtended(xMiddle - P1P2_BUTTON_WIDTH / 2, yMiddle - 45, P1P2_BUTTON_WIDTH, 20, getToggleLabel(),
                this::toggleP1P2));
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
            if (changed(i)) {
                String varName = GlobalVariableHelper.getInstance().getPrefixedVar(vars[i], playerGlobals[i]);
                NetworkHandler.sendToServer(new PacketChangeGPSToolCoordinate(p1p2Pos[i], hand, varName, i));
            }
        }
    }

    private boolean changed(int index) {
        ItemStack stack = ClientUtils.getClientPlayer().getItemInHand(hand);
        BlockPos p = GPSAreaToolItem.getGPSLocation(ClientUtils.getClientPlayer(), stack, index).orElse(PneumaticCraftUtils.invalidPos());
        String var = GPSAreaToolItem.getVariable(ClientUtils.getClientPlayer(), stack, index);
        String var2 = GlobalVariableHelper.getInstance().getPrefixedVar(vars[index], playerGlobals[index]);
        return !p.equals(p1p2Pos[index]) || !var.equals(var2);
    }

    @Override
    protected void toggleVarType() {
        playerGlobals[index] = !playerGlobals[index];
        varTypeButton.setMessage(Component.literal(GlobalVariableHelper.getInstance().getVarPrefix(playerGlobals[index])));
    }

    private void toggleP1P2(Button b) {
        ItemStack stack = ClientUtils.getClientPlayer().getItemInHand(hand);
        if (stack.getItem() instanceof GPSAreaToolItem) {
            p1p2Pos[index] = new BlockPos(textFields[0].getIntValue(), textFields[1].getIntValue(), textFields[2].getIntValue());
            vars[index] = variableField.getValue();

            index = 1 - index;

            b.setMessage(getToggleLabel());
            textFields[0].setValue(p1p2Pos[index].getX());
            textFields[1].setValue(p1p2Pos[index].getY());
            textFields[2].setValue(p1p2Pos[index].getZ());
            variableField.setValue(vars[index]);
            varTypeButton.setMessage(Component.literal(GlobalVariableHelper.getInstance().getVarPrefix(playerGlobals[index])));
            if (teleportButton != null) {
                BlockPos pos = getBlockPos();
                teleportButton.setTooltipText(Component.literal(String.format("/tp %d %d %d", pos.getX(), pos.getY(), pos.getZ())).withStyle(ChatFormatting.YELLOW));
            }
        }
    }

    private Component getToggleLabel() {
        ChatFormatting color = index == 0 ? ChatFormatting.RED : ChatFormatting.GREEN;
        return Component.literal("P" + (index + 1)).withStyle(color);
    }
}
