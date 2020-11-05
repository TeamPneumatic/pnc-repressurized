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

public class SearchOptions extends IOptionPage.SimpleToggleableOptions<SearchClientHandler> {
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
                b -> Minecraft.getInstance().displayGuiScreen(new GuiMoveStat(getClientUpgradeHandler(), ArmorHUDLayout.LayoutTypes.ITEM_SEARCH))));

        if (searchGui != null && !player.getItemStackFromSlot(EquipmentSlotType.HEAD).isEmpty()) {
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
        if (Minecraft.getInstance().currentScreen instanceof GuiItemSearcher) {
            searchGui = (GuiItemSearcher) Minecraft.getInstance().currentScreen;
            if (!player.getItemStackFromSlot(EquipmentSlotType.HEAD).isEmpty()) {
                Item searchItem = ItemPneumaticArmor.getSearchedItem(player.getItemStackFromSlot(EquipmentSlotType.HEAD));
                if (searchItem != null) searchGui.setSearchStack(new ItemStack(searchItem));
            }
        }
    }

    public boolean displaySettingsHeader() {
        return true;
    }
}
