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

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.item.ItemGPSTool;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketChangeGPSToolCoordinate;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiGPSTool extends GuiPneumaticScreenBase {
    private static final int TEXTFIELD_WIDTH = 60;

    protected final WidgetTextFieldNumber[] textFields = new WidgetTextFieldNumber[3];
    protected WidgetTextField variableField;
    protected final InteractionHand hand;
    private final BlockPos oldGPSLoc;
    private String oldVarName;

    protected GuiGPSTool(Component title, InteractionHand hand, BlockPos gpsLoc, String oldVarName) {
        super(title);

        this.hand = hand;
        this.oldGPSLoc = gpsLoc;
        this.oldVarName = oldVarName;
    }

    public static void showGUI(ItemStack stack, InteractionHand handIn, BlockPos pos) {
        Minecraft.getInstance().setScreen(
                new GuiGPSTool(stack.getHoverName(), handIn, pos != null ? pos : BlockPos.ZERO, ItemGPSTool.getVariable(stack))
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
            int min = i == 1 ? PneumaticCraftUtils.getMinHeight(ClientUtils.getClientLevel()) : Integer.MIN_VALUE;
            int max = i == 1 ? ClientUtils.getClientLevel().getMaxBuildHeight() : Integer.MAX_VALUE;
            textFields[i] = new WidgetTextFieldNumber(font, xMiddle - TEXTFIELD_WIDTH / 2, yMiddle - 15 + i * 22, TEXTFIELD_WIDTH, font.lineHeight)
                    .setValue(oldText[i])
                    .setRange(min, max)
                    .setAdjustments(1, 10);
            addRenderableWidget(textFields[i]);
        }

        for (int i = 0; i < 3; i++) {
            final int idx = i;
            addRenderableWidget(new Button(xMiddle - 49 - TEXTFIELD_WIDTH / 2, yMiddle - 20 + i * 22, 22, 20,
                    new TextComponent("-10"), b -> updateTextField(idx, -10)));
            addRenderableWidget(new Button(xMiddle - 25 - TEXTFIELD_WIDTH / 2, yMiddle - 20 + i * 22, 22, 20,
                    new TextComponent("-1"), b -> updateTextField(idx, -1)));
            addRenderableWidget(new Button(xMiddle + 3 + TEXTFIELD_WIDTH / 2, yMiddle - 20 + i * 22, 22, 20,
                    new TextComponent("+1"), b -> updateTextField(idx, 1)));
            addRenderableWidget(new Button(xMiddle + 27 + TEXTFIELD_WIDTH / 2, yMiddle - 20 + i * 22, 22, 20,
                    new TextComponent("+10"), b -> updateTextField(idx, 10)));
        }

        if (variableField != null) oldVarName = variableField.getValue();
        variableField = new WidgetTextField(font, xMiddle - 50, yMiddle + 60, 100, font.lineHeight);
        variableField.setValue(oldVarName);
        addRenderableWidget(variableField);

        Component var = xlate("pneumaticcraft.gui.progWidget.coordinate.variable").append(" #");
        addRenderableWidget(new WidgetLabel(variableField.x - 1 - font.width(var), yMiddle + 61, var, 0xc0c0c0));
    }

    private void updateTextField(int idx, int amount) {
        textFields[idx].setValue(textFields[idx].getIntValue() + amount);
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        int xMiddle = width / 2;
        int yMiddle = height / 2;
        int stringX = xMiddle - 60 - TEXTFIELD_WIDTH / 2;
        drawCenteredString(matrixStack, font, getTitle(), xMiddle, yMiddle - 58, 0xFFFFFFFF);
        drawString(matrixStack, font, "X:", stringX, yMiddle - 10 - font.lineHeight / 2, 0xFFFFFFFF);
        drawString(matrixStack, font, "Y:", stringX, yMiddle + 4 + font.lineHeight / 2, 0xFFFFFFFF);
        drawString(matrixStack, font, "Z:", stringX, yMiddle + 34 - font.lineHeight / 2, 0xFFFFFFFF);
    }

    @Override
    public void removed() {
        syncToServer();
        super.removed();
    }

    protected void syncToServer() {
        BlockPos newPos = new BlockPos(textFields[0].getIntValue(), textFields[1].getIntValue(), textFields[2].getIntValue());
        NetworkHandler.sendToServer(new PacketChangeGPSToolCoordinate(
                newPos.equals(oldGPSLoc) ? new BlockPos(-1, -1, -1) : newPos,
                hand, variableField.getValue(), getIndex())
        );

    }

    protected int getIndex() {
        return 0;
    }

    @Override
    protected ResourceLocation getTexture() {
        return null;
    }
}
