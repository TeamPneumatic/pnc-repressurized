package me.desht.pneumaticcraft.client.gui.tubemodule;

import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.block.tubes.ModuleAirGrate;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateAirGrateModule;
import me.desht.pneumaticcraft.common.util.EntityFilter;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;

public class GuiAirGrateModule extends GuiTubeModule<ModuleAirGrate> {
    private int sendTimer = 0;
    private WidgetButtonExtended warningButton;

    public GuiAirGrateModule(BlockPos pos) {
        super(pos);

        ySize = 57;
    }

    private TextFieldWidget textfield;

    @Override
    public void init() {
        super.init();

        addLabel(title.getFormattedText(), guiLeft + xSize / 2, guiTop + 5, WidgetLabel.Alignment.CENTRE);
        WidgetLabel label = addLabel(I18n.format("pneumaticcraft.gui.entityFilter"), guiLeft + 10, guiTop + 30);
        addLabel(I18n.format("pneumaticcraft.gui.holdF1forHelp"), guiLeft + xSize / 2, guiTop + ySize + 5, WidgetLabel.Alignment.CENTRE)
                .setColor(0xFFC0C0C0);

        int tx = 12 + label.getWidth();
        textfield = new WidgetTextField(font, guiLeft + tx, guiTop + 29, xSize - tx - 10, 10);
        textfield.setText(module.getEntityFilterString());
        textfield.setResponder(s -> sendTimer = 5);
        textfield.setFocused2(true);
        setFocused(textfield);
        addButton(textfield);

        warningButton = new WidgetButtonExtended(guiLeft + 152, guiTop + 20, 20, 20, "");
        warningButton.setVisible(false);
        warningButton.setRenderedIcon(Textures.GUI_PROBLEMS_TEXTURE);
        addButton(warningButton);

        validateEntityFilter(textfield.getText());
    }

    private void validateEntityFilter(String filter) {
        try {
            warningButton.visible = false;
            warningButton.setTooltipText("");
            new EntityFilter(filter);  // syntax check
        } catch (IllegalArgumentException e) {
            warningButton.visible = true;
            warningButton.setTooltipText(TextFormatting.GOLD + e.getMessage());
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (!textfield.isFocused()) textfield.setText(module.getEntityFilterString());

        if (ClientUtils.isKeyDown(GLFW.GLFW_KEY_F1)) {
            GuiUtils.showPopupHelpScreen(this, font,
                    PneumaticCraftUtils.splitString(I18n.format("pneumaticcraft.gui.entityFilter.helpText"), 60));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (sendTimer > 0 && --sendTimer == 0) {
            module.setEntityFilter(textfield.getText());
            NetworkHandler.sendToServer(new PacketUpdateAirGrateModule(module, textfield.getText()));
        }
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.GUI_MODULE_SIMPLE;
    }

}
