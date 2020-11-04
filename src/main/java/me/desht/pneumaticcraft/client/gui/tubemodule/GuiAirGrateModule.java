package me.desht.pneumaticcraft.client.gui.tubemodule;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.block.tubes.ModuleAirGrate;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateAirGrateModule;
import me.desht.pneumaticcraft.common.util.EntityFilter;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

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

        addLabel(title, guiLeft + xSize / 2, guiTop + 5, WidgetLabel.Alignment.CENTRE);
        WidgetLabel label = addLabel(xlate("pneumaticcraft.gui.entityFilter"), guiLeft + 10, guiTop + 30);
        addLabel(xlate("pneumaticcraft.gui.holdF1forHelp"), guiLeft + xSize / 2, guiTop + ySize + 5, WidgetLabel.Alignment.CENTRE)
                .setColor(0xFFC0C0C0);

        int tx = 12 + label.getWidth();
        textfield = new WidgetTextField(font, guiLeft + tx, guiTop + 29, xSize - tx - 10, 10);
        textfield.setText(module.getEntityFilterString());
        textfield.setResponder(s -> sendTimer = 5);
        textfield.setFocused2(true);
        setListener(textfield);
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
            warningButton.setTooltipText(StringTextComponent.EMPTY);
            new EntityFilter(filter);  // syntax check
        } catch (IllegalArgumentException e) {
            warningButton.visible = true;
            warningButton.setTooltipText(new StringTextComponent(e.getMessage()).mergeStyle(TextFormatting.GOLD));
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        if (!textfield.isFocused()) textfield.setText(module.getEntityFilterString());

        if (ClientUtils.isKeyDown(GLFW.GLFW_KEY_F1)) {
            GuiUtils.showPopupHelpScreen(matrixStack, this, font,
                    GuiUtils.xlateAndSplit("pneumaticcraft.gui.entityFilter.helpText"));
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
        return Textures.GUI_MODULE_SIMPLE;
    }

}
