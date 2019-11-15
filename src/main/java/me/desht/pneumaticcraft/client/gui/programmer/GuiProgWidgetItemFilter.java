package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiInventorySearcher;
import me.desht.pneumaticcraft.client.gui.GuiItemSearcher;
import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.GuiButtonSpecial;
import me.desht.pneumaticcraft.client.gui.widget.GuiCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget.WidgetDifficulty;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetItemFilter;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;

public class GuiProgWidgetItemFilter extends GuiProgWidgetOptionBase<ProgWidgetItemFilter> {
    private GuiItemSearcher searchGui;
    private GuiInventorySearcher invSearchGui;
    private GuiCheckBox checkBoxUseDamage;
    private GuiCheckBox checkBoxUseNBT;
    private GuiCheckBox checkBoxUseOreDict;
    private GuiCheckBox checkBoxUseModSimilarity;
    private GuiCheckBox checkBoxMatchBlock;
    private WidgetComboBox variableField;

    public GuiProgWidgetItemFilter(ProgWidgetItemFilter widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        addButton(new GuiButtonSpecial(guiLeft + 4, guiTop + 20, 70, 20, "Search item...", b -> openSearcher()));
        addButton(new GuiButtonSpecial(guiLeft + 78, guiTop + 20, 100, 20, "Search inventory...", b -> openInventorySearcher()));

        checkBoxUseDamage = new GuiCheckBox(guiLeft + 4, guiTop + 72, 0xFF404040,
                "Use damage values", b -> progWidget.useItemDamage = b.checked);
        checkBoxUseDamage.setTooltip(Arrays.asList("Check to handle differently damaged", "tools as different."));
        checkBoxUseDamage.checked = progWidget.useItemDamage;
        addButton(checkBoxUseDamage);

        checkBoxUseNBT = new GuiCheckBox(guiLeft + 4, guiTop + 108, 0xFF404040, "Use NBT", b -> {
            progWidget.useNBT = b.checked;
            checkBoxMatchBlock.enabled = !b.checked;
        });
        checkBoxUseNBT.setTooltip(Arrays.asList("Check to handle items like Enchanted Books", "or Firework as different."));
        checkBoxUseNBT.checked = progWidget.useNBT;
        addButton(checkBoxUseNBT);

        checkBoxUseOreDict = new GuiCheckBox(guiLeft + 4, guiTop + 120, 0xFF404040, "Use Item Tags", b -> {
            progWidget.useItemTags = b.checked;
            checkBoxUseDamage.enabled = !b.checked;
            checkBoxUseNBT.enabled = !b.checked;
            checkBoxUseModSimilarity.enabled = !b.checked;
            checkBoxMatchBlock.enabled = !b.checked;
        });
        checkBoxUseOreDict.setTooltip(Arrays.asList("Check to handle items with", "common Item Tags as the same."));
        checkBoxUseOreDict.checked = progWidget.useItemTags;
        addButton(checkBoxUseOreDict);

        checkBoxUseModSimilarity = new GuiCheckBox(guiLeft + 4, guiTop + 132, 0xFF404040, "Use Mod similarity", b -> {
            progWidget.useModSimilarity = b.checked;
            checkBoxUseDamage.enabled = !b.checked;
            checkBoxUseNBT.enabled = !b.checked;
            checkBoxUseOreDict.enabled = !b.checked;
            checkBoxMatchBlock.enabled = !b.checked;
        });
        checkBoxUseModSimilarity.setTooltip(Arrays.asList("Check to handle items from the", "same mod as the same."));
        checkBoxUseModSimilarity.checked = progWidget.useModSimilarity;
        addButton(checkBoxUseModSimilarity);

        checkBoxMatchBlock = new GuiCheckBox(guiLeft + 4, guiTop + 144, 0xFF404040, "Match by Block", b -> {
            progWidget.matchBlock = b.checked;
            checkBoxUseModSimilarity.enabled = !b.checked;
            checkBoxUseNBT.enabled = !b.checked;
            checkBoxUseOreDict.enabled = !b.checked;
        });
        checkBoxMatchBlock.setTooltip(Arrays.asList("Check to match by block instead of", "dropped item. Useful for blocks", "which don't drop an item.", TextFormatting.GRAY.toString() + TextFormatting.ITALIC + "Only used by the 'Dig' programming piece."));
        checkBoxMatchBlock.checked = progWidget.matchBlock;
        addButton(checkBoxMatchBlock);

        variableField = new WidgetComboBox(font, guiLeft + 90, guiTop + 56, 80, font.FONT_HEIGHT + 1);
        variableField.setElements(guiProgrammer.te.getAllVariables());
        variableField.setText(progWidget.getVariable());

        if (PNCConfig.Client.programmerDifficulty == WidgetDifficulty.ADVANCED) {
            addButton(variableField);
        }

        checkBoxUseDamage.enabled = !checkBoxUseOreDict.checked && !checkBoxUseModSimilarity.checked;
        checkBoxUseNBT.enabled = !checkBoxUseOreDict.checked && !checkBoxUseModSimilarity.checked && !checkBoxMatchBlock.checked;
        checkBoxUseOreDict.enabled = !checkBoxUseModSimilarity.checked && !checkBoxMatchBlock.checked;
        checkBoxUseModSimilarity.enabled = !checkBoxUseOreDict.checked && !checkBoxMatchBlock.checked;
        checkBoxMatchBlock.enabled = !checkBoxUseNBT.checked && !checkBoxUseModSimilarity.checked && !checkBoxUseOreDict.checked;

        if (searchGui != null) progWidget.setFilter(searchGui.getSearchStack());
        if (invSearchGui != null) progWidget.setFilter(invSearchGui.getSearchStack());
    }

    private void openSearcher() {
        ClientUtils.openContainerGui(ModContainerTypes.SEARCHER, new StringTextComponent("Search"));
        if (minecraft.currentScreen instanceof GuiItemSearcher) {
            searchGui = (GuiItemSearcher) minecraft.currentScreen;
            searchGui.setSearchStack(progWidget.getFilter());
        }
    }

    private void openInventorySearcher() {
        ClientUtils.openContainerGui(ModContainerTypes.INVENTORY_SEARCHER, new StringTextComponent("Search"));
        if (minecraft.currentScreen instanceof GuiInventorySearcher) {
            invSearchGui = (GuiInventorySearcher) minecraft.currentScreen;
            invSearchGui.setSearchStack(progWidget.getFilter());
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        
        progWidget.setVariable(variableField.getText());
    }
    
    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        
        if (PNCConfig.Client.programmerDifficulty == WidgetDifficulty.ADVANCED) {
            font.drawString("Variable:", guiLeft + 90, guiTop + 45, 0xFF404040);
        }
        font.drawString("Filter:", guiLeft + 10, guiTop + 53, 0xFF404040);

        String oldVarName = progWidget.getVariable();
        progWidget.setVariable("");
        if (!progWidget.getFilter().isEmpty())
            ProgWidgetItemFilter.drawItemStack(progWidget.getFilter(), guiLeft + 50, guiTop + 48, "");
        progWidget.setVariable(oldVarName);
    }
}
