package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.common.inventory.ContainerPneumaticDoor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDoorBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiPneumaticDoor extends GuiPneumaticContainerBase<TileEntityPneumaticDoorBase> {

    public GuiPneumaticDoor(InventoryPlayer player, TileEntityPneumaticDoorBase te) {

        super(new ContainerPneumaticDoor(player, te), te, Textures.GUI_PNEUMATIC_DOOR);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
        fontRenderer.drawString("Upgr.", 28, 19, 4210752);
    }

}
