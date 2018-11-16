package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.inventory.ContainerAssemblyController;
import me.desht.pneumaticcraft.common.recipes.programs.AssemblyProgram.EnumMachine;
import me.desht.pneumaticcraft.common.tileentity.IAssemblyMachine;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyController;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiAssemblyController extends GuiPneumaticContainerBase<TileEntityAssemblyController> {

    private GuiAnimatedStat statusStat;

    public GuiAssemblyController(InventoryPlayer player, TileEntityAssemblyController te) {

        super(new ContainerAssemblyController(player, te), te, Textures.GUI_ASSEMBLY_CONTROLLER);
    }

    @Override
    public void initGui() {
        super.initGui();
        statusStat = addAnimatedStat("gui.tab.status", new ItemStack(Blockss.ASSEMBLY_CONTROLLER), 0xFFFFAA00, false);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
        fontRenderer.drawString("Upgr.", 18, 21, 4210752);
        fontRenderer.drawString("Prog.", 70, 24, 4210752);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        statusStat.setText(getStatusText());
    }

    private List<String> getStatusText() {
        List<String> text = new ArrayList<>();

        EnumSet<EnumMachine> foundMachines = EnumSet.of(EnumMachine.CONTROLLER);
        for (IAssemblyMachine machine : te.findMachines(EnumMachine.values().length)) {
            foundMachines.add(machine.getAssemblyType());
        }
        for (EnumMachine m : EnumMachine.values()) {
            if (m == EnumMachine.CONTROLLER) continue; // we *are* the controller!
            String s = foundMachines.contains(m) ? TextFormatting.DARK_GREEN + "\u2714 " : TextFormatting.RED + "\u2717 ";
            text.add(s + TextFormatting.BLACK + " " + I18n.format(m.getTranslationKey()));
        }
        return text;
    }

    @Override
    protected void addProblems(List<String> textList) {
        super.addProblems(textList);

        te.addProblems(textList);
    }
}
