package me.desht.pneumaticcraft.client.gui.tubemodule;

import me.desht.pneumaticcraft.client.gui.widget.GuiButtonSpecial;
import me.desht.pneumaticcraft.client.gui.widget.GuiCheckBox;
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

public class GuiPressureModuleSimple extends GuiTubeModule {
    private WidgetTextFieldNumber thresholdField;
    private GuiButtonSpecial moreOrLessButton;

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

        GuiCheckBox advancedMode = new GuiCheckBox(guiLeft + 6, guiTop + 15, 0xFF404040, "gui.tubeModule.advancedConfig", b -> {
            module.advancedConfig = true;
            NetworkHandler.sendToServer(new PacketUpdatePressureModule(module));
        }).setTooltip(I18n.format("gui.tubeModule.advancedConfig.tooltip"));
        advancedMode.checked = false;
        addButton(advancedMode);

        thresholdField = new WidgetTextFieldNumber(font, guiLeft + 110, guiTop + 33, 30, font.FONT_HEIGHT).setDecimals(1);
        addButton(thresholdField);
        setFocused(thresholdField);
        thresholdField.setFocused2(true);

        if (module instanceof TubeModuleRedstoneReceiving) {
            thresholdField.setValue(((TubeModuleRedstoneReceiving) module).getThreshold());
            addLabel(I18n.format("gui.tubeModule.simpleConfig.threshold"), guiLeft + 6, guiTop + 33);
        } else {
            thresholdField.setValue(module.lowerBound);
            addLabel(I18n.format("gui.tubeModule.simpleConfig.turn"), guiLeft + 6, guiTop + 33);
            moreOrLessButton = new GuiButtonSpecial(guiLeft + 85, guiTop + 28, 20, 20, module.lowerBound < module.higherBound ? ">" : "<", b -> flipThreshold());
            moreOrLessButton.setTooltipText(I18n.format(module.lowerBound < module.higherBound ? "gui.tubeModule.simpleConfig.higherThan" : "gui.tubeModule.simpleConfig.lowerThan"));
            addButton(moreOrLessButton);
        }
        addLabel(I18n.format("gui.general.bar"), guiLeft + 145, guiTop + 34);
    }

    private void flipThreshold() {
        float temp = module.higherBound;
        module.higherBound = module.lowerBound;
        module.lowerBound = temp;

        updateThreshold();
        moreOrLessButton.setMessage(module.lowerBound < module.higherBound ? ">" : "<");
        moreOrLessButton.setTooltipText(I18n.format(module.lowerBound < module.higherBound ? "gui.tubeModule.simpleConfig.higherThan" : "gui.tubeModule.simpleConfig.lowerThan"));
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
