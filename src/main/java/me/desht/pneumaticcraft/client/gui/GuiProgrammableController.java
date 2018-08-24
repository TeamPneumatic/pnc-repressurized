package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.inventory.ContainerProgrammableController;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammableController;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public class GuiProgrammableController extends GuiPneumaticContainerBase<TileEntityProgrammableController> implements
        IGuiDrone {

    public GuiProgrammableController(InventoryPlayer player, TileEntityProgrammableController te) {
        super(new ContainerProgrammableController(player, te), te, Textures.GUI_PROGRAMMABLE_CONTROLLER);
    }

    @Override
    public void initGui() {
        super.initGui();

        List<String> exc = TileEntityProgrammableController.BLACKLISTED_WIDGETS.stream()
                .map(s -> "\u2022 " + I18n.format("programmingPuzzle." + s + ".name"))
                .sorted()
                .collect(Collectors.toList());
        addAnimatedStat("gui.tab.info.programmable_controller.excluded",
                new ItemStack(Itemss.DRONE), 0xFFFF5050, true).setText(exc);
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
