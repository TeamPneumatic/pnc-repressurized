package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.common.inventory.ContainerReinforcedChest;
import me.desht.pneumaticcraft.common.tileentity.TileEntityReinforcedChest;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiReinforcedChest extends GuiPneumaticContainerBase<ContainerReinforcedChest, TileEntityReinforcedChest> {
    public GuiReinforcedChest(ContainerReinforcedChest container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);

        ySize = 186;
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_REINFORCED_CHEST;
    }

    @Override
    protected boolean shouldAddProblemTab() {
        return false;
    }
}
