package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.inventory.ContainerProgrammableController;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammableController;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.InventoryPlayer;

import java.util.List;

public class GuiProgrammableController extends GuiPneumaticContainerBase<TileEntityProgrammableController> implements
        IGuiDrone {

    public GuiProgrammableController(InventoryPlayer player, TileEntityProgrammableController te) {
        super(new ContainerProgrammableController(player, te), te, Textures.GUI_PROGRAMMABLE_CONTROLLER);
    }

    @Override
    public IDroneBase getDrone() {
        return te;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
        fontRenderer.drawString("Upgr.", 28, 19, 4210752);
    }

    @Override
    protected void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);
        if (te.getPrimaryInventory().getStackInSlot(0).isEmpty()) curInfo.add("gui.tab.problems.programmableController.noProgram");
    }
}
