package me.desht.pneumaticcraft.client.gui.tubemodule;

import me.desht.pneumaticcraft.client.gui.widget.GuiButtonSpecial;
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

public class GuiAirGrateModule extends GuiTubeModule {
    private int sendTimer = 0;
    private GuiButtonSpecial warningButton;

    public GuiAirGrateModule(BlockPos pos) {
        super(pos);

        ySize = 61;
    }

    private TextFieldWidget textfield;

    @Override
    public void init() {
        super.init();
        addLabel(I18n.format("gui.entityFilter"), guiLeft + 10, guiTop + 14);

        textfield = new TextFieldWidget(font, guiLeft + 10, guiTop + 25, 140, 10,
                ((ModuleAirGrate) module).getEntityFilterString());
        textfield.func_212954_a(s -> sendTimer = 5);

        warningButton = new GuiButtonSpecial(guiLeft + 152, guiTop + 20, 20, 20, "");
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

        if (!textfield.isFocused()) textfield.setText(((ModuleAirGrate) module).getEntityFilterString());

        if (ClientUtils.isKeyDown(GLFW.GLFW_KEY_F1)) {
            GuiUtils.showPopupHelpScreen(this, font,
                    PneumaticCraftUtils.convertStringIntoList(I18n.format("gui.entityFilter.helpText"), 60));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (sendTimer > 0 && --sendTimer == 0) {
            NetworkHandler.sendToServer(new PacketUpdateAirGrateModule(module, textfield.getText()));
        }
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.GUI_TEXT_WIDGET;
    }

}
