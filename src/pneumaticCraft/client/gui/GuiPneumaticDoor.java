package pneumaticCraft.client.gui;

import net.minecraft.entity.player.InventoryPlayer;
import pneumaticCraft.common.inventory.ContainerPneumaticDoor;
import pneumaticCraft.common.tileentity.TileEntityPneumaticDoorBase;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiPneumaticDoor extends GuiPneumaticContainerBase<TileEntityPneumaticDoorBase>{

    public GuiPneumaticDoor(InventoryPlayer player, TileEntityPneumaticDoorBase te){

        super(new ContainerPneumaticDoor(player, te), te, Textures.GUI_PNEUMATIC_DOOR);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){
        super.drawGuiContainerForegroundLayer(x, y);
        fontRendererObj.drawString("Upgr.", 28, 19, 4210752);
        fontRendererObj.drawString("Camo", 73, 26, 4210752);
    }

    @Override
    public String getRedstoneButtonText(int mode){
        switch(mode){
            case 0:
                return "gui.tab.redstoneBehaviour.pneumaticDoor.button.playerNearby";
            case 1:
                return "gui.tab.redstoneBehaviour.pneumaticDoor.button.playerNearbyAndLooking";
            case 2:
                return "gui.tab.redstoneBehaviour.pneumaticDoor.button.woodenDoor";
        }
        return "<ERROR>";
    }

    @Override
    public String getRedstoneString(){
        return "gui.tab.redstoneBehaviour.pneumaticDoor.openWhen";
    }
}
