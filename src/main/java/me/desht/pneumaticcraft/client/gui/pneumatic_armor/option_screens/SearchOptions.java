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

package me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.gui.GuiItemSearcher;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiMoveStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.SearchClientHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateSearchItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class SearchOptions extends IOptionPage.SimpleOptionPage<SearchClientHandler> {
    private static GuiItemSearcher searchGui;

    private final PlayerEntity player = Minecraft.getInstance().player;

    public SearchOptions(IGuiScreen screen, SearchClientHandler upgradeHandler) {
        super(screen, upgradeHandler);
    }

    @Override
    public void populateGui(IGuiScreen gui) {
        gui.addWidget(new WidgetButtonExtended(30, 40, 150, 20,
                xlate("pneumaticcraft.gui.misc.searchItem"), b -> openSearchGui()));

        gui.addWidget(new Button(30, 128, 150, 20, xlate("pneumaticcraft.armor.gui.misc.moveStatScreen"),
                b -> Minecraft.getInstance().setScreen(new GuiMoveStat(getClientUpgradeHandler(), ArmorHUDLayout.LayoutType.ITEM_SEARCH))));

        if (searchGui != null && !player.getItemBySlot(EquipmentSlotType.HEAD).isEmpty()) {
            ItemStack helmetStack = ClientUtils.getWornArmor(EquipmentSlotType.HEAD);
            Item newSearchedItem = searchGui.getSearchStack().getItem();
            Item oldSearchedItem = ItemPneumaticArmor.getSearchedItem(helmetStack);
            if (newSearchedItem != oldSearchedItem) {
                ItemPneumaticArmor.setSearchedItem(helmetStack, newSearchedItem);
                NetworkHandler.sendToServer(new PacketUpdateSearchItem(newSearchedItem));
            }
        }
    }

    private void openSearchGui() {
        ClientUtils.openContainerGui(ModContainers.ITEM_SEARCHER.get(), new StringTextComponent("Search"));
        if (Minecraft.getInstance().screen instanceof GuiItemSearcher) {
            searchGui = (GuiItemSearcher) Minecraft.getInstance().screen;
            if (!player.getItemBySlot(EquipmentSlotType.HEAD).isEmpty()) {
                Item searchItem = ItemPneumaticArmor.getSearchedItem(player.getItemBySlot(EquipmentSlotType.HEAD));
                if (searchItem != null) searchGui.setSearchStack(new ItemStack(searchItem));
            }
        }
    }

    @Override
    public boolean displaySettingsHeader() {
        return true;
    }
}
