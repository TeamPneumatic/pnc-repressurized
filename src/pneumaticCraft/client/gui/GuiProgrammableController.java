package pneumaticCraft.client.gui;

import net.minecraft.entity.player.InventoryPlayer;
import pneumaticCraft.common.inventory.ContainerProgrammableController;
import pneumaticCraft.common.tileentity.TileEntityProgrammableController;
import pneumaticCraft.lib.Textures;

public class GuiProgrammableController extends GuiPneumaticContainerBase<TileEntityProgrammableController>{

    public GuiProgrammableController(InventoryPlayer player, TileEntityProgrammableController te){
        super(new ContainerProgrammableController(player, te), te, Textures.GUI_PROGRAMMER);
    }

}
