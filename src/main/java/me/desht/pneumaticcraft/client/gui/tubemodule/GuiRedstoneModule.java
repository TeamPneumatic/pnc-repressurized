package me.desht.pneumaticcraft.client.gui.tubemodule;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.common.block.tubes.ModuleRedstone;
import me.desht.pneumaticcraft.common.block.tubes.ModuleRedstone.Operation;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSyncRedstoneModuleToServer;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.dyeColorDesc;

public class GuiRedstoneModule extends GuiTubeModule {
    private WidgetComboBox comboBox;
    private WidgetLabel constLabel;
    private WidgetTextFieldNumber textField;
    private WidgetLabel otherColorLabel;
    private WidgetButtonExtended ourColorButton;
    private WidgetButtonExtended otherColorButton;
    private int ourColor;
    private int otherColor;
    private WidgetCheckBox invertCheckBox;

    public GuiRedstoneModule(BlockPos modulePos) {
        super(modulePos);

        ySize = 202;
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
            w = Math.max(label.getWidth(), w);
        }
        int xBase = guiLeft + w + 15;

        // TODO add a proper colour selector widget
        ourColorButton = new WidgetButtonExtended(xBase, guiTop + 15, 20, 20, "*", b -> {
            if (Screen.hasShiftDown()) {
                if (--ourColor < 0) ourColor = 15;
            } else {
                if (++ourColor > 15) ourColor = 0;
            }
        }).setRenderStacks(getColourItem(ourColor));
        addButton(ourColorButton);

        List<String> ops = new ArrayList<>();
        for (Operation op : Operation.values()) {
            ops.add(I18n.format(op.getTranslationKey()));
        }
        comboBox = new WidgetComboBox(font, xBase, guiTop + 39, xSize - xBase + guiLeft - 10, 12)
                .setFixedOptions().setShouldSort(false).setElements(ops);
        comboBox.selectElement(mr.getOperation().ordinal());
        addButton(comboBox);

        otherColorButton = new WidgetButtonExtended(xBase, guiTop + 55, 20, 20, "*", b -> {
            if (Screen.hasShiftDown()) {
                if (--otherColor < 0) otherColor = 15;
            } else {
                if (++otherColor > 15) otherColor = 0;
            }
        }).setRenderStacks(getColourItem(otherColor));
        addButton(otherColorButton);

        textField = new WidgetTextFieldNumber(font, xBase, guiTop + 58, 30, 12);
        textField.minValue = 0;
        textField.setDecimals(0);
        textField.setValue(mr.getConstantVal());
        addButton(textField);

        invertCheckBox = new WidgetCheckBox(guiLeft + 10, guiTop + 80, 0xFF404040, "Invert Output?") {
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

    private ItemStack getColourItem(int colour) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", DyeColor.byId(colour).getName() + "_concrete"));
        return new ItemStack(item);
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
        ourColorButton.setRenderStacks(getColourItem(ourColor));
        otherColorButton.setRenderStacks(getColourItem(otherColor));
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        minecraft.getTextureManager().bindTexture(getTexture());

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
        List<String> l = PneumaticCraftUtils.splitString(s, 30);
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
