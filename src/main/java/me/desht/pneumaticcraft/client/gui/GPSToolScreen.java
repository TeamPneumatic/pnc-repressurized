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

import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.item.GPSToolItem;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketChangeGPSToolCoordinate;
import me.desht.pneumaticcraft.common.network.PacketTeleportCommand;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.regex.Pattern;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GPSToolScreen extends AbstractPneumaticCraftScreen {
    private static final int TEXTFIELD_WIDTH = 60;
    private static final Pattern VAR_PATTERN = Pattern.compile("^\\w*$", Pattern.CASE_INSENSITIVE);

    protected final WidgetTextFieldNumber[] textFields = new WidgetTextFieldNumber[3];
    protected WidgetTextField variableField;
    protected WidgetButtonExtended varTypeButton;
    protected WidgetButtonExtended teleportButton;
    protected final InteractionHand hand;
    private final BlockPos oldGPSLoc;
    private String oldVarName;
    protected boolean playerGlobal;

    protected GPSToolScreen(Component title, InteractionHand hand, BlockPos gpsLoc, String oldVarName) {
        super(title);
        xSize = 183;
        ySize = 202;
        this.hand = hand;
        this.oldGPSLoc = gpsLoc;
        this.oldVarName = oldVarName;
    }

    public static void showGUI(ItemStack stack, InteractionHand handIn, BlockPos pos) {
        Minecraft.getInstance().setScreen(
                new GPSToolScreen(stack.getHoverName(), handIn, pos != null ? pos : BlockPos.ZERO, GPSToolItem.getVariable(stack))
        );
    }

    @Override
    public void init() {
        super.init();
        int[] oldText = new int[3];
        if (textFields[0] == null) {
            oldText[0] = oldGPSLoc.getX();
            oldText[1] = oldGPSLoc.getY();
            oldText[2] = oldGPSLoc.getZ();
        } else {
            for (int i = 0; i < 3; i++)
                oldText[i] = textFields[i].getIntValue();
        }
        int xMiddle = width / 2;
        int yMiddle = height / 2;
        for (int i = 0; i < 3; i++) {
            int min = i == 1 ? ClientUtils.getClientLevel().getMinBuildHeight(): Integer.MIN_VALUE;
            int max = i == 1 ? ClientUtils.getClientLevel().getMaxBuildHeight() : Integer.MAX_VALUE;
            textFields[i] = new WidgetTextFieldNumber(font, xMiddle - TEXTFIELD_WIDTH / 2, yMiddle - 15 + i * 22, TEXTFIELD_WIDTH, font.lineHeight + 2)
                    .setValue(oldText[i])
                    .setRange(min, max)
                    .setAdjustments(1, 10);
            addRenderableWidget(textFields[i]);
        }

        for (int i = 0; i < 3; i++) {
            final int idx = i;
            addRenderableWidget(new WidgetButtonExtended(xMiddle - 52 - TEXTFIELD_WIDTH / 2, yMiddle - 20 + i * 22, 25, 20,
                    Component.literal("-10"), b -> updateTextField(idx, -10)));
            addRenderableWidget(new WidgetButtonExtended(xMiddle - 25 - TEXTFIELD_WIDTH / 2, yMiddle - 20 + i * 22, 22, 20,
                    Component.literal("-1"), b -> updateTextField(idx, -1)));
            addRenderableWidget(new WidgetButtonExtended(xMiddle + 3 + TEXTFIELD_WIDTH / 2, yMiddle - 20 + i * 22, 22, 20,
                    Component.literal("+1"), b -> updateTextField(idx, 1)));
            addRenderableWidget(new WidgetButtonExtended(xMiddle + 27 + TEXTFIELD_WIDTH / 2, yMiddle - 20 + i * 22, 25, 20,
                    Component.literal("+10"), b -> updateTextField(idx, 10)));
        }

        if (variableField != null) oldVarName = variableField.getValue();
        variableField = new WidgetTextField(font, xMiddle - 50, yMiddle + 60, 100, font.lineHeight + 1);
        playerGlobal = !oldVarName.startsWith("%");
        oldVarName = GlobalVariableHelper.stripVarPrefix(oldVarName);
        variableField.setFilter(s -> VAR_PATTERN.matcher(s).matches());
        variableField.setValue(oldVarName);
        addRenderableWidget(variableField);

        varTypeButton = new WidgetButtonExtended(variableField.getX() - 13, yMiddle + 58, 12, 14, playerGlobal ? "#" : "%",
                b -> toggleVarType())
                .setTooltipKey("pneumaticcraft.gui.remote.varType.tooltip");
        addRenderableWidget(varTypeButton);

        if (ClientUtils.getClientPlayer().hasPermissions(2)) {
            BlockPos pos = getBlockPos();
            teleportButton = new WidgetButtonExtended(xMiddle - 50, variableField.getY() + variableField.getHeight() + 5, 100, 20, xlate("pneumaticcraft.gui.gps_tool.teleport"), p -> {
                NetworkHandler.sendToServer(new PacketTeleportCommand(getBlockPos()));
                if (!ClientUtils.hasShiftDown()) onClose();
            }).setTooltipText(Component.literal(String.format("/tp %d %d %d", pos.getX(), pos.getY(), pos.getZ())).withStyle(ChatFormatting.YELLOW));
            addRenderableWidget(teleportButton);
        }

        addLabel(xlate("pneumaticcraft.gui.progWidget.coordinate.variable").append(":"), variableField.getX(), variableField.getY() - font.lineHeight - 2).setColor(0xFFFFFF);
    }

    protected BlockPos getBlockPos() {
        return new BlockPos(textFields[0].getIntValue(), textFields[1].getIntValue(), textFields[2].getIntValue());
    }

    protected void toggleVarType() {
        playerGlobal = !playerGlobal;
        varTypeButton.setMessage(Component.literal(GlobalVariableHelper.getVarPrefix(playerGlobal)));
    }

    private void updateTextField(int idx, int amount) {
        textFields[idx].setValue(textFields[idx].getIntValue() + amount);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics, mouseX, mouseY, partialTicks);
        super.render(graphics, mouseX, mouseY, partialTicks);

        int xMiddle = width / 2;
        int yMiddle = height / 2;
        int stringX = xMiddle - 60 - TEXTFIELD_WIDTH / 2;
        graphics.drawCenteredString(font, getTitle(), xMiddle, yMiddle - 58, 0xFFFFFFFF);
        graphics.drawString(font, "X:", stringX, yMiddle - 10 - font.lineHeight / 2, 0xFFFFFFFF, false);
        graphics.drawString(font, "Y:", stringX, yMiddle + 4 + font.lineHeight / 2, 0xFFFFFFFF, false);
        graphics.drawString(font, "Z:", stringX, yMiddle + 34 - font.lineHeight / 2, 0xFFFFFFFF, false);
    }

    @Override
    public void removed() {
        syncToServer();
        super.removed();
    }

    protected void syncToServer() {
        String varName = GlobalVariableHelper.getPrefixedVar(GlobalVariableHelper.stripVarPrefix(variableField.getValue()), playerGlobal);
        NetworkHandler.sendToServer(new PacketChangeGPSToolCoordinate(getBlockPos(), hand, varName, getIndex()));
    }

    protected int getIndex() {
        return 0;
    }

    @Override
    protected ResourceLocation getTexture() {
        return null;
    }
}
