package me.desht.pneumaticcraft.client.gui.tubemodule;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.common.block.tubes.ModuleRedstone;
import me.desht.pneumaticcraft.common.block.tubes.ModuleRedstone.Operation;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSyncRedstoneModuleToServer;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.dyeColorDesc;

public class GuiRedstoneModule extends GuiTubeModule {
    private WidgetComboBox comboBox;
    private WidgetLabel constLabel;
    private WidgetTextFieldNumber textField;
    private WidgetLabel otherColorLabel;
    private GuiButtonSpecial ourColorButton;
    private GuiButtonSpecial otherColorButton;
    private int ourColor;
    private int otherColor;
    private GuiCheckBox invertCheckBox;

    public GuiRedstoneModule(BlockPos modulePos) {
        super(modulePos);
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.GUI_WIDGET_OPTIONS;
    }

    @Override
    public void init() {
        super.init();

        ModuleRedstone mr = (ModuleRedstone) module;
        ourColor = mr.getColorChannel();
        otherColor = mr.getOtherColor();

        addButton(new WidgetLabel(guiLeft + xSize / 2, guiTop + 5, "Redstone Module").setAlignment(WidgetLabel.Alignment.CENTRE));

        WidgetLabel ourColorLabel;
        addButton(ourColorLabel = new WidgetLabel(guiLeft + 10, guiTop + 20, "Our Color"));

        WidgetLabel opLabel;
        addButton(opLabel = new WidgetLabel(guiLeft + 10, guiTop + 40, "Operation"));

        otherColorLabel = new WidgetLabel(guiLeft + 10, guiTop + 60, "Other Color");
        addButton(otherColorLabel);

        constLabel = new WidgetLabel(guiLeft + 15, guiTop + 60, "Constant");
        addButton(constLabel);

        int w = 0;
        for (WidgetLabel label : ImmutableList.of(ourColorLabel, otherColorLabel, opLabel, constLabel)) {
            w = Math.max(label.getBounds().width, w);
        }
        int xBase = guiLeft + w + 15;

        ourColorButton = new GuiButtonSpecial(xBase, guiTop + 15, 20, 20, "") {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == 0) {
                    if (--ourColor < 0) ourColor = 15;
                    return true;
                } else if (button == 1) {
                    if (++ourColor > 15) ourColor = 0;
                    return true;
                } else {
                    return super.mouseClicked(mouseX, mouseX, button);
                }
            }
        };
        addButton(ourColorButton);

        List<String> ops = new ArrayList<>();
        for (Operation op : Operation.values()) {
            ops.add(I18n.format(op.getTranslationKey()));
        }
        comboBox = new WidgetComboBox(font, xBase, guiTop + 39, xSize - xBase + guiLeft - 10, 12)
                .setFixedOptions().setShouldSort(false).setElements(ops);
        comboBox.selectElement(mr.getOperation().ordinal());
        addButton(comboBox);

        otherColorButton = new GuiButtonSpecial(xBase, guiTop + 55, 20, 20, "") {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == 0) {
                    if (--otherColor < 0) otherColor = 15;
                    return true;
                } else if (button == 1) {
                    if (++otherColor > 15) otherColor = 0;
                    return true;
                } else {
                    return super.mouseClicked(mouseX, mouseX, button);
                }
            }
        };
        addButton(otherColorButton);

        textField = new WidgetTextFieldNumber(font, xBase, guiTop + 58, 30, 12);
        textField.minValue = 0;
        textField.setDecimals(0);
        textField.setValue(mr.getConstantVal());
        addButton(textField);

        invertCheckBox = new GuiCheckBox(guiLeft + 10, guiTop + 80, 0xFF404040, "Invert Output?") {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (comboBox.isFocused()) return true;  // it hangs over the button
                return super.mouseClicked(mouseX, mouseY, button);
            }
        };
        invertCheckBox.checked = mr.isInvert();
        invertCheckBox.setTooltip(I18n.format("gui.redstoneModule.invert.tooltip"));
        addButton(invertCheckBox);

        updateWidgetVisibility();
    }

    @Override
    public void tick() {
        super.tick();

        updateWidgetVisibility();
    }

    private void updateWidgetVisibility() {
        Operation op = getSelectedOp();
        constLabel.visible = op.useConst();
        textField.setVisible(op.useConst());
        otherColorLabel.visible = op.useOtherColor();
        otherColorButton.visible = op.useOtherColor();
        otherColorButton.setVisible(op.useOtherColor());
        // TODO create a color cycler widget
//        ourColorButton.setRenderStacks(new ItemStack(Blocks.CONCRETE, 1, DyeColor.byDyeDamage(ourColor).getMetadata()));
//        otherColorButton.setRenderStacks(new ItemStack(Blocks.CONCRETE, 1, DyeColor.byDyeDamage(otherColor).getMetadata()));
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        Operation op = getSelectedOp();
        String key = op.getTranslationKey() + ".tooltip";
        String s;
        if (op.useConst()) {
            s = I18n.format(key, dyeColorDesc(ourColor), textField.getValue());
        } else if (op.useOtherColor()) {
            s = I18n.format(key, dyeColorDesc(ourColor), dyeColorDesc(otherColor));
        } else {
            s = I18n.format(key, dyeColorDesc(ourColor));
        }
        List<String> l = PneumaticCraftUtils.convertStringIntoList(s, 30);
        int yBase = guiTop + ySize - l.size() * font.FONT_HEIGHT - 10;
        for (int i = 0; i < l.size(); i++) {
            font.drawString(l.get(i), guiLeft + 10, yBase + i * font.FONT_HEIGHT, 0xFF404040);
        }
    }

    private Operation getSelectedOp() {
        return Operation.values()[comboBox.getSelectedElementIndex()];
    }

    @Override
    public void onClose() {
        super.onClose();

        ((ModuleRedstone) module).setColorChannel(ourColor);
        ((ModuleRedstone) module).setInvert(invertCheckBox.checked);
        ((ModuleRedstone) module).setOperation(getSelectedOp(), otherColor, textField.getValue());
        NetworkHandler.sendToServer(new PacketSyncRedstoneModuleToServer((ModuleRedstone) module));
    }
}
