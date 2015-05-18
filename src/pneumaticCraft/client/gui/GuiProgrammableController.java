package pneumaticCraft.client.gui;

import java.util.List;

import net.minecraft.entity.player.InventoryPlayer;
import pneumaticCraft.common.ai.IDroneBase;
import pneumaticCraft.common.inventory.ContainerProgrammableController;
import pneumaticCraft.common.tileentity.TileEntityProgrammableController;
import pneumaticCraft.lib.Textures;

public class GuiProgrammableController extends GuiPneumaticContainerBase<TileEntityProgrammableController> implements
        IGuiDrone{

    public GuiProgrammableController(InventoryPlayer player, TileEntityProgrammableController te){
        super(new ContainerProgrammableController(player, te), te, Textures.GUI_PROGRAMMABLE_CONTROLLER);
    }

    @Override
    public IDroneBase getDrone(){
        return te;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){
        super.drawGuiContainerForegroundLayer(x, y);
        fontRendererObj.drawString("Upgr.", 28, 19, 4210752);
    }

    @Override
    protected void addProblems(List<String> curInfo){
        super.addProblems(curInfo);
        if(te.getStackInSlot(0) == null) curInfo.add("gui.tab.problems.programmableController.noProgram");
    }
}
