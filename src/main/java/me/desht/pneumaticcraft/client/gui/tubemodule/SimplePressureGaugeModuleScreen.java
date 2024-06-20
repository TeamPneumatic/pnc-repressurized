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

import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdatePressureModule;
import me.desht.pneumaticcraft.common.tubemodules.AbstractRedstoneReceivingModule;
import me.desht.pneumaticcraft.common.tubemodules.AbstractTubeModule;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class SimplePressureGaugeModuleScreen extends AbstractTubeModuleScreen<AbstractTubeModule> {
    private WidgetTextFieldNumber thresholdField;
    private WidgetButtonExtended moreOrLessButton;

    SimplePressureGaugeModuleScreen(AbstractTubeModule module) {
        super(module);

        ySize = 57;
    }

    @Override
    public void init() {
        super.init();

        addLabel(title, width / 2 - font.width(title) / 2, guiTop + 5);

        WidgetCheckBox advancedMode = new WidgetCheckBox(guiLeft + 6, guiTop + 20, 0xFF404040, xlate("pneumaticcraft.gui.tubeModule.advancedConfig"), b -> {
            module.advancedConfig = true;
            NetworkHandler.sendToServer(PacketUpdatePressureModule.forModule(module));
        }).setTooltipKey("pneumaticcraft.gui.tubeModule.advancedConfig.tooltip").setChecked(false);
        addRenderableWidget(advancedMode);

        thresholdField = new WidgetTextFieldNumber(font, guiLeft + 105, guiTop + 35, 30, font.lineHeight + 2)
                .setDecimals(1)
                .setAdjustments(0.1, 1.0);
        addRenderableWidget(thresholdField);
        thresholdField.setWidth(40);
        setInitialFocus(thresholdField);

        if (module instanceof AbstractRedstoneReceivingModule) {
            thresholdField.setValue(((AbstractRedstoneReceivingModule) module).getThreshold());
            Component s = xlate("pneumaticcraft.gui.tubeModule.simpleConfig.threshold");
            addLabel(s, guiLeft + 80 - font.width(s), guiTop + 36);
        } else {
            thresholdField.setValue(module.lowerBound);
            Component s = xlate("pneumaticcraft.gui.tubeModule.simpleConfig.turn");
            addLabel(s,guiLeft + 80 - font.width(s), guiTop + 36);
            moreOrLessButton = new WidgetButtonExtended(guiLeft + 85, guiTop + 33, 16, 16, module.lowerBound < module.higherBound ? ">" : "<", b -> flipThreshold());
            moreOrLessButton.setTooltipText(xlate(module.lowerBound < module.higherBound ?
                    "pneumaticcraft.gui.tubeModule.simpleConfig.higherThan" :
                    "pneumaticcraft.gui.tubeModule.simpleConfig.lowerThan")
            );
            addRenderableWidget(moreOrLessButton);
        }
        addLabel(xlate("pneumaticcraft.gui.general.bar"), thresholdField.getX() + thresholdField.getWidth() + 3, thresholdField.getY() + 1);
    }

    private void flipThreshold() {
        float temp = module.higherBound;
        module.higherBound = module.lowerBound;
        module.lowerBound = temp;

        updateThreshold();
        moreOrLessButton.setMessage(Component.literal(module.lowerBound < module.higherBound ? ">" : "<"));
        moreOrLessButton.setTooltipText(xlate(module.lowerBound < module.higherBound ?
                "pneumaticcraft.gui.tubeModule.simpleConfig.higherThan" :
                "pneumaticcraft.gui.tubeModule.simpleConfig.lowerThan")
        );
        NetworkHandler.sendToServer(PacketUpdatePressureModule.forModule(module));
    }

    @Override
    public void tick() {
        super.tick();
        if (module.advancedConfig) {
            module.lowerBound = (float) thresholdField.getDoubleValue();
            minecraft.setScreen(new PressureGaugeModuleScreen(module));
        }
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.GUI_MODULE_SIMPLE;
    }
    
    private void updateThreshold(){
        boolean moreThanMode = module.lowerBound > module.higherBound;
        module.lowerBound = (float) thresholdField.getDoubleValue();
        if (moreThanMode) {
            module.higherBound = module.lowerBound - 0.1F;
        } else {
            module.higherBound = module.lowerBound + 0.1F;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER && thresholdField.isFocused()) {
            removed();
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public void removed() {
        updateThreshold();
        NetworkHandler.sendToServer(PacketUpdatePressureModule.forModule(module));
        super.removed();
    }
}
