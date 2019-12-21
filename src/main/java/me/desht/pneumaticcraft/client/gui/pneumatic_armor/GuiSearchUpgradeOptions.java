package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.gui.GuiItemSearcher;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.SearchUpgradeHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.aux.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.core.ModContainerTypes;
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

public class GuiSearchUpgradeOptions extends IOptionPage.SimpleToggleableOptions<SearchUpgradeHandler> {
    private static GuiItemSearcher searchGui;

    private final PlayerEntity player = Minecraft.getInstance().player;

    public GuiSearchUpgradeOptions(IGuiScreen screen, SearchUpgradeHandler upgradeHandler) {
        super(screen, upgradeHandler);
    }

    @Override
    public String getPageName() {
        return "Item Searcher";
    }

    @Override
    public void populateGui(IGuiScreen gui) {
        gui.addWidget(new WidgetButtonExtended(30, 40, 150, 20,
                "Search for item...", b -> openSearchGui()));

        gui.addWidget(new Button(30, 128, 150, 20, "Move Stat Screen...",
                b -> Minecraft.getInstance().displayGuiScreen(new GuiMoveStat(getUpgradeHandler(), ArmorHUDLayout.LayoutTypes.ITEM_SEARCH))));

        if (searchGui != null && !player.getItemStackFromSlot(EquipmentSlotType.HEAD).isEmpty()) {
            ItemStack searchStack = searchGui.getSearchStack();
            Item searchedItem = ItemPneumaticArmor.getSearchedItem(ClientUtils.getWornArmor(EquipmentSlotType.HEAD));
            ItemStack helmetStack = new ItemStack(searchedItem);
            if (searchStack.isEmpty() && !helmetStack.isEmpty() || !searchStack.isEmpty() && helmetStack.isEmpty()
                    || !searchStack.isEmpty() && !helmetStack.isEmpty() && !searchStack.isItemEqual(helmetStack)) {
                NetworkHandler.sendToServer(new PacketUpdateSearchItem(searchedItem));
            }
        }
    }

    private void openSearchGui() {
        ClientUtils.openContainerGui(ModContainerTypes.SEARCHER, new StringTextComponent("Search"));
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
