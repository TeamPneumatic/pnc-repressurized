package me.desht.pneumaticcraft.client.gui.tubemodule;

import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.block.tubes.TubeModuleRedstoneReceiving;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdatePressureModule;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiPressureModuleSimple extends GuiTubeModule<TubeModule> {
    private WidgetTextFieldNumber thresholdField;
    private WidgetButtonExtended moreOrLessButton;

    GuiPressureModuleSimple(TubeModule module) {
        super(module);

        ySize = 57;
    }

    @Override
    public void init() {
        super.init();

        addLabel(title, width / 2 - font.width(title) / 2, guiTop + 5);

        WidgetCheckBox advancedMode = new WidgetCheckBox(guiLeft + 6, guiTop + 20, 0xFF404040, xlate("pneumaticcraft.gui.tubeModule.advancedConfig"), b -> {
            module.advancedConfig = true;
            NetworkHandler.sendToServer(new PacketUpdatePressureModule(module));
        }).setTooltipKey("pneumaticcraft.gui.tubeModule.advancedConfig.tooltip").setChecked(false);
        addButton(advancedMode);

        thresholdField = new WidgetTextFieldNumber(font, guiLeft + 105, guiTop + 35, 30, font.lineHeight + 2)
                .setDecimals(1)
                .setAdjustments(0.1, 1.0);
        addButton(thresholdField);
        setFocused(thresholdField);
        thresholdField.setWidth(40);
        thresholdField.setFocus(true);

        if (module instanceof TubeModuleRedstoneReceiving) {
            thresholdField.setValue(((TubeModuleRedstoneReceiving) module).getThreshold());
            ITextComponent s = xlate("pneumaticcraft.gui.tubeModule.simpleConfig.threshold");
            addLabel(s, guiLeft + 80 - font.width(s), guiTop + 36);
        } else {
            thresholdField.setValue(module.lowerBound);
            ITextComponent s = xlate("pneumaticcraft.gui.tubeModule.simpleConfig.turn");
            addLabel(s,guiLeft + 80 - font.width(s), guiTop + 36);
            moreOrLessButton = new WidgetButtonExtended(guiLeft + 85, guiTop + 33, 16, 16, module.lowerBound < module.higherBound ? ">" : "<", b -> flipThreshold());
            moreOrLessButton.setTooltipText(xlate(module.lowerBound < module.higherBound ?
                    "pneumaticcraft.gui.tubeModule.simpleConfig.higherThan" :
                    "pneumaticcraft.gui.tubeModule.simpleConfig.lowerThan")
            );
            addButton(moreOrLessButton);
        }
        addLabel(xlate("pneumaticcraft.gui.general.bar"), thresholdField.x + thresholdField.getWidth() + 3, thresholdField.y + 1);
    }

    private void flipThreshold() {
        float temp = module.higherBound;
        module.higherBound = module.lowerBound;
        module.lowerBound = temp;

        updateThreshold();
        moreOrLessButton.setMessage(new StringTextComponent(module.lowerBound < module.higherBound ? ">" : "<"));
        moreOrLessButton.setTooltipText(xlate(module.lowerBound < module.higherBound ?
                "pneumaticcraft.gui.tubeModule.simpleConfig.higherThan" :
                "pneumaticcraft.gui.tubeModule.simpleConfig.lowerThan")
        );
        NetworkHandler.sendToServer(new PacketUpdatePressureModule(module));
    }

    @Override
    public void tick() {
        super.tick();
        if (module.advancedConfig) {
            module.lowerBound = (float) thresholdField.getDoubleValue();
            minecraft.setScreen(new GuiPressureModule(module));
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
        NetworkHandler.sendToServer(new PacketUpdatePressureModule(module));
        super.removed();
    }
}
