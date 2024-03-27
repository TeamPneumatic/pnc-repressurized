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

package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetList;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.block.entity.TagWorkbenchBlockEntity;
import me.desht.pneumaticcraft.common.inventory.TagWorkbenchMenu;
import me.desht.pneumaticcraft.common.item.TagFilterItem;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketGuiButton;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class TagWorkbenchScreen extends AbstractPneumaticCraftContainerScreen<TagWorkbenchMenu, TagWorkbenchBlockEntity> {
    private static final int AVAILABLE_X = 9;
    private static final int SELECTED_X = 123;
    private static final int LIST_HEIGHT = 126;
    private static final int LIST_WIDTH = 98;
    private static final int LIST_Y = 41;

    private Item lastItem = null;
    private ItemStack lastPaperStack = ItemStack.EMPTY;

    private WidgetButtonExtended addButton;
    private WidgetButtonExtended removeButton;
    private WidgetList<ResourceLocation> availableList;
    private WidgetList<ResourceLocation> selectedList;
    private WidgetButtonExtended writeButton;

    public TagWorkbenchScreen(TagWorkbenchMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);

        imageWidth = 234;
        imageHeight = 256;
    }

    @Override
    public void init() {
        super.init();

        addRenderableWidget(writeButton = new WidgetButtonExtended(leftPos + 162, topPos + 16, 20, 20, Component.empty(), b -> writeTags())
                .setRenderStacks(new ItemStack(Items.WRITABLE_BOOK))
                .setTooltipText(xlate("pneumaticcraft.gui.tooltip.tag_workbench.write_button")));
        addRenderableWidget(addButton = new WidgetButtonExtended(leftPos + 108, topPos + 90, 13, 13, Symbols.TRIANGLE_RIGHT,
                b -> addAvailable()));
        addRenderableWidget(removeButton = new WidgetButtonExtended(leftPos + 108, topPos + 106, 13, 13, Symbols.TRIANGLE_LEFT,
                b -> removeSelected()));

        addRenderableWidget(availableList = new WidgetList<>(leftPos + AVAILABLE_X, topPos + LIST_Y, LIST_WIDTH, LIST_HEIGHT, this::onSelected));
        addRenderableWidget(selectedList = new WidgetList<>(leftPos + SELECTED_X, topPos + LIST_Y, LIST_WIDTH, LIST_HEIGHT, this::onSelected));
    }

    private void writeTags() {
        List<String> l = selectedList.getLines().stream().map(ResourceLocation::toString).collect(Collectors.toList());
        NetworkHandler.sendToServer(new PacketGuiButton("write:" + String.join("," , l)));
        selectedList.clear();
    }

    private void onSelected(WidgetList<ResourceLocation> w) {
        if (w == availableList) {
            selectedList.unselectAll();
            if (w.isDoubleClicked()) addAvailable();
        } else if (w == selectedList) {
            availableList.unselectAll();
            if (w.isDoubleClicked()) removeSelected();
        }
    }

    private void addAvailable() {
        ResourceLocation rl = availableList.getSelectedLine();
        if (rl != null && !selectedList.contains(rl)) {
            selectedList.add(rl);
        }
    }

    private void removeSelected() {
        ResourceLocation rl = selectedList.getSelectedLine();
        if (rl != null) {
            selectedList.removeSelected();
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();

        ItemStack stack = menu.getSlot(0).getItem();
        if (stack.getItem() != lastItem) {
            availableList.clear();
            stack.getItem().builtInRegistryHolder().tags().forEach(tagKey -> availableList.add(tagKey.location()));
            availableList.unselectAll();
            lastItem = stack.getItem();
        }
        ItemStack stack1 = menu.getSlot(1).getItem();
        if (!ItemStack.matches(stack1, lastPaperStack)) {
            if (stack1.getItem() == ModItems.TAG_FILTER.get()) {
                Set<TagKey<Item>> s = TagFilterItem.getConfiguredTagList(stack1);
                s.addAll(selectedList.getLines().stream().map(rl -> TagKey.create(Registries.ITEM, rl)).toList());
                selectedList.clear();
                s.forEach(rl -> selectedList.add(rl.location()));
            }
            selectedList.unselectAll();
            lastPaperStack = stack1.copy();
        }

        addButton.active = availableList.getSelectedLine() != null;
        removeButton.active = selectedList.getSelectedLine() != null;
        writeButton.active = selectedList.size() > 0
                && (!menu.getSlot(TagWorkbenchBlockEntity.PAPER_SLOT).getItem().isEmpty()
                || !menu.getSlot(TagWorkbenchBlockEntity.OUTPUT_SLOT).getItem().isEmpty());
    }

    @Override
    protected PointXY getInvTextOffset() {
        return null;
    }

    @Override
    protected boolean shouldAddProblemTab() {
        return false;
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_TAG_WORKBENCH;
    }
}
