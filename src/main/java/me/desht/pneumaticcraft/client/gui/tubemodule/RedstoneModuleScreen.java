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

package me.desht.pneumaticcraft.client.gui.tubemodule;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSyncRedstoneModuleToServer;
import me.desht.pneumaticcraft.common.registry.ModSounds;
import me.desht.pneumaticcraft.common.tubemodules.RedstoneModule;
import me.desht.pneumaticcraft.common.tubemodules.RedstoneModule.EnumRedstoneDirection;
import me.desht.pneumaticcraft.common.tubemodules.RedstoneModule.Operation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.DyeColor;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.dyeColorDesc;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class RedstoneModuleScreen extends AbstractTubeModuleScreen<RedstoneModule> {
    private WidgetComboBox comboBox;
    private WidgetLabel constLabel;
    private WidgetTextFieldNumber constTextField;
    private WidgetLabel otherColorLabel;
    private WidgetColorSelector otherColorButton;
    private int ourColor;
    private int otherColor;
    private WidgetCheckBox invertCheckBox;
    private WidgetCheckBox comparatorInputCheckBox;
    private boolean upgraded;
    private boolean output;
    private final List<FormattedCharSequence> lowerText = new ArrayList<>();

    public RedstoneModuleScreen(RedstoneModule module) {
        super(module);

        ySize = module.getRedstoneDirection() == EnumRedstoneDirection.OUTPUT ? 202 : 57;
    }

    @Override
    public void init() {
        super.init();

        upgraded = module.isUpgraded();
        output = module.getRedstoneDirection() == EnumRedstoneDirection.OUTPUT;
        ourColor = module.getColorChannel();
        otherColor = module.getOtherColor();

        addRenderableWidget(new WidgetButtonExtended(guiLeft + xSize - 22, guiTop + 2, 18, 12,
                getDirText(module), b -> toggleRedstoneDirection())
                .setTooltipText(ImmutableList.of(
                        xlate(module.getRedstoneDirection().getTranslationKey()),
                        xlate("pneumaticcraft.gui.redstoneModule.clickToToggle").withStyle(ChatFormatting.GRAY)
                ))
        );

        addRenderableWidget(new WidgetLabel(guiLeft + xSize / 2, guiTop + 5, getTitle()).setAlignment(WidgetLabel.Alignment.CENTRE));

        WidgetLabel ourColorLabel;
        addRenderableWidget(ourColorLabel = new WidgetLabel(guiLeft + 10, guiTop + 25, xlate("pneumaticcraft.gui.tubeModule.channel")));

        WidgetLabel opLabel;
        addRenderableWidget(opLabel = new WidgetLabel(guiLeft + 10, guiTop + 45, xlate("pneumaticcraft.gui.redstoneModule.operation")));
        opLabel.visible = output;

        otherColorLabel = new WidgetLabel(guiLeft + 10, guiTop + 65, xlate("pneumaticcraft.gui.tubeModule.otherChannel"));
        otherColorLabel.visible = output;
        addRenderableWidget(otherColorLabel);

        constLabel = new WidgetLabel(guiLeft + 15, guiTop + 65, xlate("pneumaticcraft.gui.redstoneModule.constant"));
        addRenderableWidget(constLabel);
        constLabel.visible = output;

        int w = 0;
        for (WidgetLabel label : ImmutableList.of(ourColorLabel, otherColorLabel, opLabel, constLabel)) {
            w = Math.max(label.getWidth(), w);
        }
        int xBase = guiLeft + w + 15;

        addRenderableWidget(new WidgetColorSelector(xBase, guiTop + 20, b -> ourColor = b.getColor().getId())
                .withInitialColor(DyeColor.byId(ourColor)));

        if (!output) {
            comparatorInputCheckBox = new WidgetCheckBox(guiLeft + 10, guiTop + 40, 0xFF404040, xlate("pneumaticcraft.gui.redstoneModule.comparatorInput"));
            comparatorInputCheckBox.setChecked(module.isComparatorInput());
            comparatorInputCheckBox.setTooltipKey("pneumaticcraft.gui.redstoneModule.comparatorInput.tooltip");
            comparatorInputCheckBox.visible = !output && upgraded;
            addRenderableWidget(comparatorInputCheckBox);
        } else {
            comboBox = new WidgetComboBox(font, xBase, guiTop + 43, xSize - xBase + guiLeft - 10, 12)
                    .initFromEnum(module.getOperation());
            comboBox.active = upgraded;
            addRenderableWidget(comboBox);

            otherColorButton = new WidgetColorSelector(xBase, guiTop + 60, b -> otherColor = b.getColor().getId())
                    .withInitialColor(DyeColor.byId(otherColor));
            otherColorButton.active = upgraded;
            addRenderableWidget(otherColorButton);

            constTextField = new WidgetTextFieldNumber(font, xBase, guiTop + 63, 30, 12);
            constTextField.minValue = 0;
            constTextField.setDecimals(0);
            constTextField.setValue(module.getConstantVal());
            constTextField.active = upgraded;
            addRenderableWidget(constTextField);

            invertCheckBox = new WidgetCheckBox(guiLeft + 10, guiTop + 85, 0xFF404040, xlate("pneumaticcraft.gui.redstoneModule.invert")) {
                @Override
                public boolean mouseClicked(double mouseX, double mouseY, int button) {
                    if (comboBox.isFocused()) return true;  // it hangs over the button
                    return super.mouseClicked(mouseX, mouseY, button);
                }
            };
            invertCheckBox.setChecked(module.isInverted());
            invertCheckBox.setTooltipKey("pneumaticcraft.gui.redstoneModule.invert.tooltip");
            addRenderableWidget(invertCheckBox);

            updateWidgetVisibility();
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (output) {
            if (upgraded) {
                updateWidgetVisibility();
            }
            updateLowerText();
        }
    }

    private void updateWidgetVisibility() {
        Operation op = getSelectedOp();
        constLabel.visible = op.useConstant();
        constTextField.setVisible(op.useConstant());
        constTextField.setRange(op.getConstMin(), op.getConstMax());
        otherColorLabel.visible = op.useOtherColor();
        otherColorButton.visible = op.useOtherColor();
        otherColorButton.setVisible(op.useOtherColor());
    }

    private void updateLowerText() {
        lowerText.clear();

        Operation op = getSelectedOp();
        String key = op.getTranslationKey() + ".tooltip";
        List<Component> l = new ArrayList<>();
        if (op.useConstant()) {
            l.add(xlate(key, dyeColorDesc(ourColor), constTextField.getValue()));
        } else if (op.useOtherColor()) {
            l.add(xlate(key, dyeColorDesc(ourColor), dyeColorDesc(otherColor)));
        } else {
            l.add(xlate(key, dyeColorDesc(ourColor)));
        }
        if (!upgraded) {
            l.add(xlate("pneumaticcraft.gui.redstoneModule.addAdvancedPCB").withStyle(ChatFormatting.DARK_BLUE));
        }
        lowerText.addAll(GuiUtils.wrapTextComponentList(l, xSize - 20, font));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);

        int yBase = guiTop + ySize - lowerText.size() * font.lineHeight - 10;
        for (int i = 0; i < lowerText.size(); i++) {
            graphics.drawString(font, lowerText.get(i), guiLeft + 10, yBase + i * font.lineHeight, 0xFF404040, false);
        }
    }

    private Operation getSelectedOp() {
        return Operation.values()[comboBox.getSelectedElementIndex()];
    }

    @Override
    public void removed() {
        super.removed();

        module.setColorChannel(ourColor);
        if (output) {
            module.setInverted(invertCheckBox.checked);
            module.setOperation(getSelectedOp(), otherColor, constTextField.getIntValue());
        } else {
            module.setComparatorInput(comparatorInputCheckBox.checked);
        }
        NetworkHandler.sendToServer(PacketSyncRedstoneModuleToServer.forModule(module));
    }

    private void toggleRedstoneDirection() {
        module.setRedstoneDirection(module.getRedstoneDirection().toggle());

        // close and re-open... will call onClose() to sync the settings
        removed();
        minecraft.setScreen(new RedstoneModuleScreen(module));
        minecraft.player.playSound(ModSounds.INTERFACE_DOOR.get(), 0.7f, 2f);
    }

    private String getDirText(RedstoneModule module) {
        return module.getRedstoneDirection() == EnumRedstoneDirection.INPUT ?
                ChatFormatting.DARK_RED + Symbols.TRIANGLE_LEFT :
                ChatFormatting.RED + Symbols.TRIANGLE_RIGHT;
    }
}
