package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.common.inventory.ContainerMinigunMagazine;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.InventoryPlayer;

public class GuiMinigunMagazine extends GuiPneumaticContainerBase {
    public GuiMinigunMagazine(InventoryPlayer inventoryPlayer) {
        super(new ContainerMinigunMagazine(inventoryPlayer.player), null, Textures.GUI_MINIGUN_MAGAZINE);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int i, int j) {
        super.drawGuiContainerBackgroundLayer(partialTicks, i, j);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
    }

    @Override
    protected boolean shouldAddProblemTab() {
        return false;
    }
}
