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

package me.desht.pneumaticcraft.client.gui.pneumatic_armor.options;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.gui.ItemSearcherScreen;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.pneumatic_armor.ClientArmorRegistry;
import me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler.SearchClientHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModMenuTypes;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateSearchItem;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class SearchOptions extends IOptionPage.SimpleOptionPage<SearchClientHandler> {
    private static ItemSearcherScreen searchGui;

    private final Player player = Minecraft.getInstance().player;

    public SearchOptions(IGuiScreen screen, SearchClientHandler upgradeHandler) {
        super(screen, upgradeHandler);
    }

    @Override
    public void populateGui(IGuiScreen gui) {
        gui.addWidget(new WidgetButtonExtended(30, 55, 150, 20,
                xlate("pneumaticcraft.gui.misc.searchItem"), b -> openSearchGui()));

        gui.addWidget(ClientArmorRegistry.getInstance().makeStatMoveButton(30, settingsYposition() + 12, getClientUpgradeHandler()));

        if (searchGui != null && !player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            ItemStack helmetStack = ClientUtils.getWornArmor(EquipmentSlot.HEAD);
            Item newSearchedItem = searchGui.getSearchStack().getItem();
            Item oldSearchedItem = PneumaticArmorItem.getSearchedItem(helmetStack);
            if (newSearchedItem != oldSearchedItem) {
                PneumaticArmorItem.setSearchedItem(helmetStack, newSearchedItem);
                NetworkHandler.sendToServer(new PacketUpdateSearchItem(newSearchedItem));
            }
        }
    }

    private void openSearchGui() {
        ClientUtils.openContainerGui(ModMenuTypes.ITEM_SEARCHER.get(), Component.literal("Search"));
        if (Minecraft.getInstance().screen instanceof ItemSearcherScreen) {
            searchGui = (ItemSearcherScreen) Minecraft.getInstance().screen;
            if (!player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
                Item searchItem = PneumaticArmorItem.getSearchedItem(player.getItemBySlot(EquipmentSlot.HEAD));
                if (searchItem != null) searchGui.setSearchStack(new ItemStack(searchItem));
            }
        }
    }

    @Override
    public boolean displaySettingsHeader() {
        return true;
    }
}
