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

import me.desht.pneumaticcraft.api.drone.IProgWidget.WidgetDifficulty;
import me.desht.pneumaticcraft.client.gui.InventorySearcherScreen;
import me.desht.pneumaticcraft.client.gui.ItemSearcherScreen;
import me.desht.pneumaticcraft.client.gui.ProgrammerScreen;
import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetItemFilter;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import me.desht.pneumaticcraft.common.thirdparty.ModNameCache;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.StringUtils;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetItemFilterScreen extends AbstractProgWidgetScreen<ProgWidgetItemFilter> {
    private ItemSearcherScreen itemSearchGui;
    private InventorySearcherScreen invSearchGui;
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

    public ProgWidgetItemFilterScreen(ProgWidgetItemFilter widget, ProgrammerScreen guiProgrammer) {
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
                .build(this::addRenderableWidget);
        itemRad.visible = varRad.visible = advancedMode;

        // buttons to open item & inv search when in item filter mode
        addRenderableWidget(itemLabel = new WidgetLabel(guiLeft + 8, guiTop + 55, xlate("pneumaticcraft.gui.progWidget.itemFilter.itemLabel").append(":")));
        addRenderableWidget(itemSearchButton = new WidgetButtonExtended(guiLeft + itemLabel.getWidth() + 35, guiTop + 50, 20, 20, Component.empty(),
                b -> openSearcher()).setRenderStacks(new ItemStack(Items.COMPASS)).setTooltipKey("pneumaticcraft.gui.misc.searchItem"));
        addRenderableWidget(invSearchButton = new WidgetButtonExtended(itemSearchButton.getX() + 25, guiTop + 50, 20, 20, Component.empty(),
                b -> openInventorySearcher()).setRenderStacks(new ItemStack(Items.CHEST)).setTooltipKey("pneumaticcraft.gui.misc.searchInventory"));

        // variable dropdown when in variable filter mode
        addRenderableWidget(variableLabel = new WidgetLabel(guiLeft + 8, guiTop + 53, xlate("pneumaticcraft.gui.progWidget.itemFilter.variableLabel").append(":")));
        addRenderableWidget(variableField = new WidgetComboBox(font, guiLeft + 12 + variableLabel.getWidth(), guiTop + 52, 80, font.lineHeight + 3)
                .setElements(guiProgrammer.te.getAllVariables()));
        variableField.setMaxLength(GlobalVariableManager.MAX_VARIABLE_LEN);
        variableField.setValue(progWidget.getVariable());

        // checkboxes shown in both modes
        addRenderableWidget(checkBoxUseDurability = new WidgetCheckBox(guiLeft + 8, guiTop + 96, 0xFF404040,
                xlate("pneumaticcraft.gui.logistics_frame.matchDurability"), b -> progWidget.setCheckDurability(b.checked))
                .setTooltipKey("pneumaticcraft.gui.logistics_frame.matchDurability.tooltip")
                .setChecked(progWidget.isCheckDurability())
        );
        addRenderableWidget(checkBoxUseNBT = new WidgetCheckBox(guiLeft + 8, guiTop + 108, 0xFF404040,
                xlate("pneumaticcraft.gui.logistics_frame.matchComponents"), b -> progWidget.setMatchComponents(b.checked))
                .setTooltipKey("pneumaticcraft.gui.logistics_frame.matchComponents.tooltip")
                .setChecked(progWidget.isMatchComponents())
        );
        addRenderableWidget(checkBoxUseModSimilarity = new WidgetCheckBox(guiLeft + 8, guiTop + 120, 0xFF404040,
                xlate("pneumaticcraft.gui.logistics_frame.matchModId"), b -> progWidget.setMatchMod(b.checked))
                .setTooltipKey("pneumaticcraft.gui.logistics_frame.matchModId.tooltip")
                .setChecked(progWidget.isMatchMod())
        );
        addRenderableWidget(checkBoxMatchBlock = new WidgetCheckBox(guiLeft + 8, guiTop + 132, 0xFF404040,
                xlate("pneumaticcraft.gui.logistics_frame.matchBlockstate"), b -> progWidget.setMatchBlock(b.checked))
                .setTooltipKey("pneumaticcraft.gui.logistics_frame.matchBlockstate.tooltip")
                .setChecked(progWidget.isMatchBlock())
        );

        if (itemSearchGui != null) progWidget.setFilter(itemSearchGui.getSearchStack());
        itemSearchGui = null;
        if (invSearchGui != null) progWidget.setFilter(invSearchGui.getSearchStack());
        invSearchGui = null;
    }

    private void openSearcher() {
        ClientUtils.openContainerGui(ModMenuTypes.ITEM_SEARCHER.get(), Component.literal("Search"));
        if (minecraft.screen instanceof ItemSearcherScreen) {
            itemSearchGui = (ItemSearcherScreen) minecraft.screen;
            itemSearchGui.setSearchStack(progWidget.getFilter());
        }
    }

    private void openInventorySearcher() {
        ClientUtils.openContainerGui(ModMenuTypes.INVENTORY_SEARCHER.get(), Component.literal("Search"));
        if (minecraft.screen instanceof InventorySearcherScreen) {
            invSearchGui = (InventorySearcherScreen) minecraft.screen;
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
        MutableComponent msg = xlate("pneumaticcraft.gui.logistics_frame.matchModId");
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);

        if (itemRad.isChecked()) {
            graphics.blit(getTexture(), guiLeft + itemX, guiTop + 51, 186, 0, 18, 18);
            if (!progWidget.getRawFilter().isEmpty()) {
                graphics.renderItem(progWidget.getRawFilter(), guiLeft + itemX + 1, guiTop + 52);
                if (mouseX >= guiLeft + itemX && mouseX <= guiLeft + itemX + 16 && mouseY >= guiTop + 51 && mouseY <= guiTop + 67) {
                    graphics.renderTooltip(font, progWidget.getRawFilter(), mouseX, mouseY);
                }
            }
        }
    }
}
