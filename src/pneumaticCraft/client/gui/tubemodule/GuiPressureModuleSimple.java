package pneumaticCraft.client.gui.tubemodule;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.client.gui.GuiButtonSpecial;
import pneumaticCraft.client.gui.widget.GuiCheckBox;
import pneumaticCraft.client.gui.widget.IGuiWidget;
import pneumaticCraft.client.gui.widget.WidgetTextFieldNumber;
import pneumaticCraft.common.block.tubes.TubeModule;
import pneumaticCraft.common.block.tubes.TubeModuleRedstoneReceiving;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketUpdatePressureModule;
import pneumaticCraft.lib.Textures;

public class GuiPressureModuleSimple extends GuiTubeModule{
    private GuiCheckBox advancedMode;
    private WidgetTextFieldNumber thresholdField;
    private GuiButtonSpecial moreOrLessButton;

    public GuiPressureModuleSimple(EntityPlayer player, int x, int y, int z){
        super(player, x, y, z);
        ySize = 57;
    }

    public GuiPressureModuleSimple(TubeModule module){
        super(module);
        ySize = 57;
    }

    @Override
    public void initGui(){
        super.initGui();

        String title = I18n.format("item." + module.getType() + ".name");
        addLabel(title, width / 2 - fontRendererObj.getStringWidth(title) / 2, guiTop + 5);

        advancedMode = new GuiCheckBox(0, guiLeft + 6, guiTop + 15, 0xFF000000, "gui.tubeModule.advancedConfig").setTooltip(I18n.format("gui.tubeModule.advancedConfig.tooltip"));
        advancedMode.checked = false;
        addWidget(advancedMode);

        thresholdField = new WidgetTextFieldNumber(fontRendererObj, guiLeft + 110, guiTop + 33, 30, fontRendererObj.FONT_HEIGHT).setDecimals(1);
        addWidget(thresholdField);

        if(module instanceof TubeModuleRedstoneReceiving) {
            thresholdField.setValue(((TubeModuleRedstoneReceiving)module).getThreshold());
            addLabel(I18n.format("gui.tubeModule.simpleConfig.threshold"), guiLeft + 6, guiTop + 33);
        } else {
            thresholdField.setValue(module.lowerBound);
            addLabel(I18n.format("gui.tubeModule.simpleConfig.turn"), guiLeft + 6, guiTop + 33);
            moreOrLessButton = new GuiButtonSpecial(1, guiLeft + 85, guiTop + 28, 20, 20, module.lowerBound < module.higherBound ? ">" : "<");
            moreOrLessButton.setTooltipText(I18n.format(module.lowerBound < module.higherBound ? "gui.tubeModule.simpleConfig.higherThan" : "gui.tubeModule.simpleConfig.lowerThan"));
            addWidget(moreOrLessButton);
        }
        addLabel(I18n.format("gui.general.bar"), guiLeft + 145, guiTop + 34);
    }

    @Override
    public void updateScreen(){
        super.updateScreen();
        if(module.advancedConfig) {
            module.lowerBound = (float)thresholdField.getDoubleValue();
            mc.displayGuiScreen(new GuiPressureModule(module));
        }
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.GUI_MODULE_SIMPLE;
    }

    @Override
    public void actionPerformed(IGuiWidget widget){
        super.actionPerformed(widget);
        switch(widget.getID()){
            case 0:
                module.advancedConfig = true;//((GuiCheckBox)widget).checked;
                NetworkHandler.sendToServer(new PacketUpdatePressureModule(module, 2, module.advancedConfig ? 1 : 0));
                // initGui();
                break;
            case 1:
                if(module.lowerBound < module.higherBound) {
                    module.higherBound = module.lowerBound - 0.1F;
                } else {
                    module.higherBound = module.lowerBound + 0.1F;
                }
                moreOrLessButton.displayString = module.lowerBound < module.higherBound ? ">" : "<";
                moreOrLessButton.setTooltipText(I18n.format(module.lowerBound < module.higherBound ? "gui.tubeModule.simpleConfig.higherThan" : "gui.tubeModule.simpleConfig.lowerThan"));
                NetworkHandler.sendToServer(new PacketUpdatePressureModule(module, 1, module.higherBound));
                break;
        }
    }

    @Override
    public void onGuiClosed(){
        NetworkHandler.sendToServer(new PacketUpdatePressureModule(module, 0, (float)thresholdField.getDoubleValue()));
        super.onGuiClosed();
    }
}
