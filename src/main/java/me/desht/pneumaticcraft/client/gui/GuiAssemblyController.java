package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.inventory.ContainerAssemblyController;
import me.desht.pneumaticcraft.common.tileentity.*;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
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
        statusStat = addAnimatedStat("Assembly Controller Status", new ItemStack(Blockss.ASSEMBLY_CONTROLLER), 0xFFFFAA00, false);
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
        List<String> text = new ArrayList<String>();

        List<IAssemblyMachine> machineList = te.getMachines();
        boolean platformFound = false;
        boolean drillFound = false;
        boolean laserFound = false;
        boolean IOUnitExportFound = false;
        boolean IOUnitImportFound = false;
        text.add("\u00a77Machine Status:");
        for (IAssemblyMachine machine : machineList) {
            if (machine instanceof TileEntityAssemblyPlatform) {
                platformFound = true;
                text.add(TextFormatting.GREEN + "-Assembly Platform online");
            } else if (machine instanceof TileEntityAssemblyDrill) {
                drillFound = true;
                text.add(TextFormatting.GREEN + "-Assembly Drill online");
            } else if (machine instanceof TileEntityAssemblyIOUnit) {
                if (((TileEntityAssemblyIOUnit) machine).isImportUnit()) {
                    IOUnitImportFound = true;
                    text.add(TextFormatting.GREEN + "-Assembly IO Unit (import) online");
                } else {
                    IOUnitExportFound = true;
                    text.add(TextFormatting.GREEN + "-Assembly IO Unit (export) online");
                }
            } else if (machine instanceof TileEntityAssemblyLaser) {
                laserFound = true;
                text.add(TextFormatting.GREEN + "-Assembly Laser online");
            }
        }
        if (!platformFound) text.add(TextFormatting.DARK_RED + "-Assembly Platform offline");
        if (!drillFound) text.add(TextFormatting.DARK_RED + "-Assembly Drill offline");
        if (!laserFound) text.add(TextFormatting.DARK_RED + "-Assembly Laser offline");
        if (!IOUnitExportFound) text.add(TextFormatting.DARK_RED + "-Assembly IO Unit (export) offline");
        if (!IOUnitImportFound) text.add(TextFormatting.DARK_RED + "-Assembly IO Unit (import) offline");
        return text;
    }

    @Override
    protected void addProblems(List<String> textList) {
        te.addProblems(textList);
    }
}
