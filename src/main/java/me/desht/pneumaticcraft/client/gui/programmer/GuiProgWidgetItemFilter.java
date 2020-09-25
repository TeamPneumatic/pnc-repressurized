package me.desht.pneumaticcraft.client.gui.programmer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.client.gui.GuiInventorySearcher;
import me.desht.pneumaticcraft.client.gui.GuiItemSearcher;
import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget.WidgetDifficulty;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetItemFilter;
import me.desht.pneumaticcraft.common.thirdparty.ModNameCache;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.lang3.StringUtils;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiProgWidgetItemFilter extends GuiProgWidgetOptionBase<ProgWidgetItemFilter> {
    private GuiItemSearcher searchGui;
    private GuiInventorySearcher invSearchGui;
    private WidgetCheckBox checkBoxUseDurability;
    private WidgetCheckBox checkBoxUseNBT;
    private WidgetCheckBox checkBoxUseModSimilarity;
    private WidgetCheckBox checkBoxMatchBlock;
    private WidgetComboBox variableField;

    public GuiProgWidgetItemFilter(ProgWidgetItemFilter widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        addButton(new WidgetButtonExtended(guiLeft + 4, guiTop + 24, 70, 20, "Search item...", b -> openSearcher()));
        addButton(new WidgetButtonExtended(guiLeft + 78, guiTop + 24, 100, 20, "Search inventory...", b -> openInventorySearcher()));

        addButton(checkBoxUseDurability = new WidgetCheckBox(guiLeft + 8, guiTop + 96, 0xFF404040,
                xlate("pneumaticcraft.gui.logistics_frame.matchDurability"), b -> progWidget.useItemDurability = b.checked)
                .setTooltipKey("pneumaticcraft.gui.logistics_frame.matchDurability.tooltip")
                .setChecked(progWidget.useItemDurability)
        );

        addButton(checkBoxUseNBT = new WidgetCheckBox(guiLeft + 8, guiTop + 108, 0xFF404040,
                xlate("pneumaticcraft.gui.logistics_frame.matchNBT"), b -> progWidget.useNBT = b.checked)
                .setTooltipKey("pneumaticcraft.gui.logistics_frame.matchNBT.tooltip")
                .setChecked(progWidget.useNBT)
        );

        addButton(checkBoxUseModSimilarity = new WidgetCheckBox(guiLeft + 8, guiTop + 120, 0xFF404040,
                xlate("pneumaticcraft.gui.logistics_frame.matchModId"), b -> progWidget.useModSimilarity = b.checked)
                .setTooltipKey("pneumaticcraft.gui.logistics_frame.matchModId.tooltip")
                .setChecked(progWidget.useModSimilarity)
        );

        addButton(checkBoxMatchBlock = new WidgetCheckBox(guiLeft + 8, guiTop + 132, 0xFF404040,
                xlate("pneumaticcraft.gui.logistics_frame.matchBlockstate"), b -> progWidget.matchBlock = b.checked)
                .setTooltipKey("pneumaticcraft.gui.logistics_frame.matchBlockstate.tooltip")
                .setChecked(progWidget.matchBlock)
        );

        variableField = new WidgetComboBox(font, guiLeft + 90, guiTop + 60, 80, font.FONT_HEIGHT + 1)
                .setElements(guiProgrammer.te.getAllVariables());
        variableField.setMaxStringLength(GlobalVariableManager.MAX_VARIABLE_LEN);
        variableField.setText(progWidget.getVariable());

        if (PNCConfig.Client.programmerDifficulty == WidgetDifficulty.ADVANCED) {
            addButton(variableField);
        }

        if (searchGui != null) progWidget.setFilter(searchGui.getSearchStack());
        if (invSearchGui != null) progWidget.setFilter(invSearchGui.getSearchStack());
        searchGui = null;
        invSearchGui = null;
    }

    private void openSearcher() {
        ClientUtils.openContainerGui(ModContainers.ITEM_SEARCHER.get(), new StringTextComponent("Search"));
        if (minecraft.currentScreen instanceof GuiItemSearcher) {
            searchGui = (GuiItemSearcher) minecraft.currentScreen;
            searchGui.setSearchStack(progWidget.getFilter());
        }
    }

    private void openInventorySearcher() {
        ClientUtils.openContainerGui(ModContainers.INVENTORY_SEARCHER.get(), new StringTextComponent("Search"));
        if (minecraft.currentScreen instanceof GuiInventorySearcher) {
            invSearchGui = (GuiInventorySearcher) minecraft.currentScreen;
            invSearchGui.setSearchStack(progWidget.getFilter());
        }
    }

    public void setFilterStack(ItemStack stack) {
        progWidget.setFilter(stack);
    }

    @Override
    public void tick() {
        super.tick();

        ItemStack filter = progWidget.getRawFilter();
        checkBoxUseDurability.active = filter.getMaxDamage() > 0 && !checkBoxUseModSimilarity.checked;
        checkBoxUseNBT.active = !filter.isEmpty() && !checkBoxUseModSimilarity.checked && !checkBoxMatchBlock.checked;
        checkBoxUseModSimilarity.active = !filter.isEmpty() && !checkBoxMatchBlock.checked;
        TranslationTextComponent msg = xlate("pneumaticcraft.gui.logistics_frame.matchModId");
        String modName = StringUtils.abbreviate(ModNameCache.getModName(filter.getItem()), 22);
        checkBoxUseModSimilarity.setMessage(filter.isEmpty() ? msg : msg.appendString(" (" + modName + ")"));
        checkBoxMatchBlock.active = filter.getItem() instanceof BlockItem && !checkBoxUseNBT.checked && !checkBoxUseModSimilarity.checked;
    }

    @Override
    public void onClose() {
        super.onClose();

        progWidget.setVariable(variableField.getText());
    }
    
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        minecraft.getTextureManager().bindTexture(getTexture());
        RenderSystem.enableTexture();
        RenderSystem.color4f(1, 1, 1, 1);
        blit(matrixStack, guiLeft + 49, guiTop + 51, 186, 0, 18, 18);
        if (PNCConfig.Client.programmerDifficulty == WidgetDifficulty.ADVANCED) {
            font.drawString(matrixStack, I18n.format("pneumaticcraft.gui.progWidget.itemFilter.variableLabel"), guiLeft + 90, guiTop + 49, 0xFF404040);
        }
        String f = I18n.format("pneumaticcraft.gui.progWidget.itemFilter.filterLabel");
        font.drawString(matrixStack, f, guiLeft + 48 - font.getStringWidth(f), guiTop + 56, 0xFF404040);
        if (!progWidget.getRawFilter().isEmpty()) {
            GuiUtils.renderItemStack(matrixStack, progWidget.getRawFilter(), guiLeft + 50, guiTop + 52);
            if (mouseX >= guiLeft + 49 && mouseX <= guiLeft + 66 && mouseY >= guiTop + 51 && mouseY <= guiTop + 68) {
                renderTooltip(matrixStack, progWidget.getRawFilter(), mouseX, mouseY);
            }
        }
    }
}
