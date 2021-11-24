/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.gui.programmer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.client.gui.GuiInventorySearcher;
import me.desht.pneumaticcraft.client.gui.GuiItemSearcher;
import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget.WidgetDifficulty;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetItemFilter;
import me.desht.pneumaticcraft.common.thirdparty.ModNameCache;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.lang3.StringUtils;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiProgWidgetItemFilter extends GuiProgWidgetOptionBase<ProgWidgetItemFilter> {
    private GuiItemSearcher itemSearchGui;
    private GuiInventorySearcher invSearchGui;
    private WidgetCheckBox checkBoxUseDurability;
    private WidgetCheckBox checkBoxUseNBT;
    private WidgetCheckBox checkBoxUseModSimilarity;
    private WidgetCheckBox checkBoxMatchBlock;
    private WidgetComboBox variableField;
    private WidgetRadioButton itemRad, varRad;
    private WidgetButtonExtended itemSearchButton, invSearchButton;
    private WidgetLabel itemLabel;
    private WidgetLabel variableLabel;
    public int itemX = -1;

    public GuiProgWidgetItemFilter(ProgWidgetItemFilter widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        boolean advancedMode = ConfigHelper.client().general.programmerDifficulty.get() == WidgetDifficulty.ADVANCED;

        // radio buttons to select between filtering by item or variable
        boolean useItemFilter = progWidget.getVariable().isEmpty() || !advancedMode;
        WidgetRadioButton.Builder.create()
                .addRadioButton(itemRad = new WidgetRadioButton(guiLeft + 8, guiTop + 22, 0x404040,
                                xlate("pneumaticcraft.gui.progWidget.itemFilter.itemLabel")),
                        useItemFilter)
                .addRadioButton(varRad = new WidgetRadioButton(guiLeft + 8, guiTop + 34, 0x404040,
                                xlate("pneumaticcraft.gui.progWidget.itemFilter.variableLabel")),
                        !useItemFilter)
                .build(this::addButton);
        itemRad.visible = varRad.visible = advancedMode;

        // buttons to open item & inv search when in item filter mode
        addButton(itemLabel = new WidgetLabel(guiLeft + 8, guiTop + 55, xlate("pneumaticcraft.gui.progWidget.itemFilter.itemLabel").append(":")));
        addButton(itemSearchButton = new WidgetButtonExtended(guiLeft + itemLabel.getWidth() + 35, guiTop + 50, 20, 20, StringTextComponent.EMPTY,
                b -> openSearcher()).setRenderStacks(new ItemStack(Items.COMPASS)).setTooltipKey("pneumaticcraft.gui.misc.searchItem"));
        addButton(invSearchButton = new WidgetButtonExtended(itemSearchButton.x + 25, guiTop + 50, 20, 20, StringTextComponent.EMPTY,
                b -> openInventorySearcher()).setRenderStacks(new ItemStack(Items.CHEST)).setTooltipKey("pneumaticcraft.gui.misc.searchInventory"));

        // variable dropdown when in variable filter mode
        addButton(variableLabel = new WidgetLabel(guiLeft + 8, guiTop + 53, xlate("pneumaticcraft.gui.progWidget.itemFilter.variableLabel").append(":")));
        addButton(variableField = new WidgetComboBox(font, guiLeft + 12 + variableLabel.getWidth(), guiTop + 52, 80, font.lineHeight + 1)
                .setElements(guiProgrammer.te.getAllVariables()));
        variableField.setMaxLength(GlobalVariableManager.MAX_VARIABLE_LEN);
        variableField.setValue(progWidget.getVariable());

        // checkboxes shown in both modes
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

        if (itemSearchGui != null) progWidget.setFilter(itemSearchGui.getSearchStack());
        itemSearchGui = null;
        if (invSearchGui != null) progWidget.setFilter(invSearchGui.getSearchStack());
        invSearchGui = null;
    }

    private void openSearcher() {
        ClientUtils.openContainerGui(ModContainers.ITEM_SEARCHER.get(), new StringTextComponent("Search"));
        if (minecraft.screen instanceof GuiItemSearcher) {
            itemSearchGui = (GuiItemSearcher) minecraft.screen;
            itemSearchGui.setSearchStack(progWidget.getFilter());
        }
    }

    private void openInventorySearcher() {
        ClientUtils.openContainerGui(ModContainers.INVENTORY_SEARCHER.get(), new StringTextComponent("Search"));
        if (minecraft.screen instanceof GuiInventorySearcher) {
            invSearchGui = (GuiInventorySearcher) minecraft.screen;
            invSearchGui.setSearchStack(progWidget.getFilter());
        }
    }

    public void setFilterStack(ItemStack stack) {
        progWidget.setFilter(stack);
    }

    @Override
    public void tick() {
        super.tick();

        itemSearchButton.visible = itemRad.isChecked();
        invSearchButton.visible = itemRad.isChecked();
        variableLabel.visible = varRad.isChecked() && ConfigHelper.client().general.programmerDifficulty.get() == WidgetDifficulty.ADVANCED;
        itemLabel.visible = !variableLabel.visible;
        variableField.visible = varRad.isChecked() && ConfigHelper.client().general.programmerDifficulty.get() == WidgetDifficulty.ADVANCED;

        if (itemRad.isChecked()) {
            itemX = itemLabel.getWidth() + 9;
        } else {
            itemX = -1;
        }
        ItemStack filter = progWidget.getRawFilter();
        checkBoxUseDurability.active = varRad.isChecked() || filter.getMaxDamage() > 0 && !checkBoxUseModSimilarity.checked;
        checkBoxUseNBT.active = varRad.isChecked() || !filter.isEmpty() && !checkBoxUseModSimilarity.checked && !checkBoxMatchBlock.checked;
        checkBoxUseModSimilarity.active = varRad.isChecked() || !filter.isEmpty() && !checkBoxMatchBlock.checked;
        TranslationTextComponent msg = xlate("pneumaticcraft.gui.logistics_frame.matchModId");
        String modName = StringUtils.abbreviate(ModNameCache.getModName(filter.getItem()), 22);
        checkBoxUseModSimilarity.setMessage(filter.isEmpty() ? msg : msg.append(" (" + modName + ")"));
        checkBoxMatchBlock.active = varRad.isChecked() || filter.getItem() instanceof BlockItem && !checkBoxUseNBT.checked && !checkBoxUseModSimilarity.checked;
    }

    @Override
    public void removed() {
        progWidget.setVariable(variableField.getValue());

        super.removed();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        if (itemRad.isChecked()) {
            minecraft.getTextureManager().bind(getTexture());
            RenderSystem.enableTexture();
            RenderSystem.color4f(1, 1, 1, 1);
            blit(matrixStack, guiLeft + itemX, guiTop + 51, 186, 0, 18, 18);
            if (!progWidget.getRawFilter().isEmpty()) {
                GuiUtils.renderItemStack(matrixStack, progWidget.getRawFilter(), guiLeft + itemX + 1, guiTop + 52);
                if (mouseX >= guiLeft + itemX && mouseX <= guiLeft + itemX + 16 && mouseY >= guiTop + 51 && mouseY <= guiTop + 67) {
                    renderTooltip(matrixStack, progWidget.getRawFilter(), mouseX, mouseY);
                }
            }
        }
    }
}
