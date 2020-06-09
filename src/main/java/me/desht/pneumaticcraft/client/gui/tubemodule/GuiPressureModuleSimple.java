package me.desht.pneumaticcraft.client.gui.tubemodule;

import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.block.tubes.TubeModuleRedstoneReceiving;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdatePressureModule;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

public class GuiPressureModuleSimple extends GuiTubeModule<TubeModule> {
    private WidgetTextFieldNumber thresholdField;
    private WidgetButtonExtended moreOrLessButton;

    GuiPressureModuleSimple(BlockPos pos) {
        super(pos);

        ySize = 57;
    }

    GuiPressureModuleSimple(TubeModule module) {
        super(module);

        ySize = 57;
    }

    @Override
    public void init() {
        super.init();

        String titleText = title.getFormattedText();
        addLabel(titleText, width / 2 - font.getStringWidth(titleText) / 2, guiTop + 5);

        WidgetCheckBox advancedMode = new WidgetCheckBox(guiLeft + 6, guiTop + 20, 0xFF404040, "pneumaticcraft.gui.tubeModule.advancedConfig", b -> {
            module.advancedConfig = true;
            NetworkHandler.sendToServer(new PacketUpdatePressureModule(module));
        }).setTooltip(I18n.format("pneumaticcraft.gui.tubeModule.advancedConfig.tooltip"));
        advancedMode.checked = false;
        addButton(advancedMode);

        thresholdField = new WidgetTextFieldNumber(font, guiLeft + 105, guiTop + 35, 30, font.FONT_HEIGHT + 2).setDecimals(1);
        addButton(thresholdField);
        setFocused(thresholdField);
        thresholdField.setFocused2(true);

        if (module instanceof TubeModuleRedstoneReceiving) {
            thresholdField.setValue(((TubeModuleRedstoneReceiving) module).getThreshold());
            String s = I18n.format("pneumaticcraft.gui.tubeModule.simpleConfig.threshold");
            addLabel(s, guiLeft + 80 - font.getStringWidth(s), guiTop + 36);
        } else {
            thresholdField.setValue(module.lowerBound);
            String s = I18n.format("pneumaticcraft.gui.tubeModule.simpleConfig.turn");
            addLabel(s,guiLeft + 80 - font.getStringWidth(s), guiTop + 36);
            moreOrLessButton = new WidgetButtonExtended(guiLeft + 85, guiTop + 33, 16, 16, module.lowerBound < module.higherBound ? ">" : "<", b -> flipThreshold());
            moreOrLessButton.setTooltipText(I18n.format(module.lowerBound < module.higherBound ? "pneumaticcraft.gui.tubeModule.simpleConfig.higherThan" : "pneumaticcraft.gui.tubeModule.simpleConfig.lowerThan"));
            addButton(moreOrLessButton);
        }
        addLabel(I18n.format("pneumaticcraft.gui.general.bar"), guiLeft + 137, guiTop + 37);
    }

    private void flipThreshold() {
        float temp = module.higherBound;
        module.higherBound = module.lowerBound;
        module.lowerBound = temp;

        updateThreshold();
        moreOrLessButton.setMessage(module.lowerBound < module.higherBound ? ">" : "<");
        moreOrLessButton.setTooltipText(I18n.format(module.lowerBound < module.higherBound ? "pneumaticcraft.gui.tubeModule.simpleConfig.higherThan" : "pneumaticcraft.gui.tubeModule.simpleConfig.lowerThan"));
        NetworkHandler.sendToServer(new PacketUpdatePressureModule(module));
    }

    @Override
    public void tick() {
        super.tick();
        if (module.advancedConfig) {
            module.lowerBound = (float) thresholdField.getDoubleValue();
            minecraft.displayGuiScreen(new GuiPressureModule(module));
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
            onClose();
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public void onClose() {
        updateThreshold();
        NetworkHandler.sendToServer(new PacketUpdatePressureModule(module));
        super.onClose();
    }
}
