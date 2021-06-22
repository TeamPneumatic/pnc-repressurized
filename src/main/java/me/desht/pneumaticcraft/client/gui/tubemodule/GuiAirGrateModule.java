package me.desht.pneumaticcraft.client.gui.tubemodule;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.block.tubes.ModuleAirGrate;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateAirGrateModule;
import me.desht.pneumaticcraft.common.util.EntityFilter;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;

public class GuiAirGrateModule extends GuiTubeModule<ModuleAirGrate> {
    private int sendTimer = 0;
    private WidgetButtonExtended warningButton;
    private WidgetButtonExtended rangeButton;
    private TextFieldWidget textfield;

    public GuiAirGrateModule(ModuleAirGrate module) {
        super(module);

        ySize = 57;
    }

    @Override
    public void init() {
        super.init();

        addLabel(this.title, this.guiLeft + this.xSize / 2, this.guiTop + 5, WidgetLabel.Alignment.CENTRE);

        WidgetLabel filterLabel = addLabel(PneumaticCraftUtils.xlate("pneumaticcraft.gui.entityFilter"), this.guiLeft + 10, this.guiTop + 21);
        filterLabel.visible = this.module.isUpgraded();

        WidgetLabel helpLabel = addLabel(PneumaticCraftUtils.xlate("pneumaticcraft.gui.holdF1forHelp"), this.guiLeft + this.xSize / 2, this.guiTop + this.ySize + 5, WidgetLabel.Alignment.CENTRE)
                .setColor(0xC0C0C0);
        helpLabel.visible = this.module.isUpgraded();

        WidgetButtonExtended advPCB = new WidgetButtonExtended(this.guiLeft + 10, this.guiTop + 21, 20, 20, StringTextComponent.EMPTY)
                .setRenderStacks(new ItemStack(ModItems.ADVANCED_PCB.get()))
                .setTooltipKey("pneumaticcraft.gui.redstoneModule.addAdvancedPCB").setVisible(false);
        advPCB.visible = !module.isUpgraded();
        addButton(advPCB);

        int tx = 12 + filterLabel.getWidth();
        textfield = new WidgetTextField(font, guiLeft + tx, guiTop + 20, xSize - tx - 10, 10);
        textfield.setText(module.getEntityFilterString());
        textfield.setResponder(s -> sendTimer = 5);
        textfield.setFocused2(true);
        textfield.setVisible(module.isUpgraded());
        setListener(textfield);
        addButton(textfield);

        warningButton = new WidgetButtonExtended(guiLeft + 152, guiTop + 20, 20, 20, StringTextComponent.EMPTY)
                .setVisible(false)
                .setRenderedIcon(Textures.GUI_PROBLEMS_TEXTURE);
        addButton(warningButton);

        rangeButton = new WidgetButtonExtended(this.guiLeft + this.xSize - 20, this.guiTop + this.ySize - 20, 16, 16, getRangeButtonText(), b -> {
            module.setShowRange(!this.module.isShowRange());
            rangeButton.setMessage(getRangeButtonText());
        });
        addButton(rangeButton);

        validateEntityFilter(textfield.getText());
    }

    private ITextComponent getRangeButtonText() {
        return new StringTextComponent((this.module.isShowRange() ? TextFormatting.AQUA : TextFormatting.DARK_GRAY) + "R");
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

        if (ClientUtils.isKeyDown(GLFW.GLFW_KEY_F1) && module.isUpgraded()) {
            GuiUtils.showPopupHelpScreen(matrixStack, this, font,
                    GuiUtils.xlateAndSplit("pneumaticcraft.gui.entityFilter.helpText"));
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!textfield.isFocused()) textfield.setText(module.getEntityFilterString());

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
