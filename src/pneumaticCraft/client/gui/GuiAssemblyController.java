package pneumaticCraft.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.inventory.ContainerAssemblyController;
import pneumaticCraft.common.tileentity.IAssemblyMachine;
import pneumaticCraft.common.tileentity.TileEntityAssemblyController;
import pneumaticCraft.common.tileentity.TileEntityAssemblyDrill;
import pneumaticCraft.common.tileentity.TileEntityAssemblyIOUnit;
import pneumaticCraft.common.tileentity.TileEntityAssemblyLaser;
import pneumaticCraft.common.tileentity.TileEntityAssemblyPlatform;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiAssemblyController extends GuiPneumaticContainerBase<TileEntityAssemblyController>{

    private GuiAnimatedStat statusStat;

    public GuiAssemblyController(InventoryPlayer player, TileEntityAssemblyController te){

        super(new ContainerAssemblyController(player, te), te, Textures.GUI_ASSEMBLY_CONTROLLER);
    }

    @Override
    public void initGui(){
        super.initGui();
        statusStat = addAnimatedStat("Assembly Controller Status", new ItemStack(Blockss.assemblyController), 0xFFFFAA00, false);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){
        super.drawGuiContainerForegroundLayer(x, y);
        fontRendererObj.drawString("Upgr.", 18, 21, 4210752);
        fontRendererObj.drawString("Prog.", 70, 24, 4210752);

    }

    @Override
    public void updateScreen(){
        super.updateScreen();
        statusStat.setText(getStatusText());
    }

    private List<String> getStatusText(){
        List<String> text = new ArrayList<String>();

        List<IAssemblyMachine> machineList = te.getMachines();
        boolean platformFound = false;
        boolean drillFound = false;
        boolean laserFound = false;
        boolean IOUnitExportFound = false;
        boolean IOUnitImportFound = false;
        text.add("\u00a77Machine Status:");
        for(IAssemblyMachine machine : machineList) {
            if(machine instanceof TileEntityAssemblyPlatform) {
                platformFound = true;
                text.add(EnumChatFormatting.GREEN + "-Assembly Platform online");
            } else if(machine instanceof TileEntityAssemblyDrill) {
                drillFound = true;
                text.add(EnumChatFormatting.GREEN + "-Assembly Drill online");
            } else if(machine instanceof TileEntityAssemblyIOUnit) {
                if(((TileEntityAssemblyIOUnit)machine).getBlockMetadata() == 0) {
                    IOUnitImportFound = true;
                    text.add(EnumChatFormatting.GREEN + "-Assembly IO Unit (import) online");
                } else {
                    IOUnitExportFound = true;
                    text.add(EnumChatFormatting.GREEN + "-Assembly IO Unit (export) online");
                }
            } else if(machine instanceof TileEntityAssemblyLaser) {
                laserFound = true;
                text.add(EnumChatFormatting.GREEN + "-Assembly Laser online");
            }
        }
        if(!platformFound) text.add(EnumChatFormatting.DARK_RED + "-Assembly Platform offline");
        if(!drillFound) text.add(EnumChatFormatting.DARK_RED + "-Assembly Drill offline");
        if(!laserFound) text.add(EnumChatFormatting.DARK_RED + "-Assembly Laser offline");
        if(!IOUnitExportFound) text.add(EnumChatFormatting.DARK_RED + "-Assembly IO Unit (export) offline");
        if(!IOUnitImportFound) text.add(EnumChatFormatting.DARK_RED + "-Assembly IO Unit (import) offline");
        return text;
    }

    @Override
    protected void addProblems(List<String> textList){
        te.addProblems(textList);
    }
}
