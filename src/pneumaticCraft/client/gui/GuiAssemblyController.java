package pneumaticCraft.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.inventory.ContainerAssemblyController;
import pneumaticCraft.common.tileentity.IAssemblyMachine;
import pneumaticCraft.common.tileentity.TileEntityAssemblyController;
import pneumaticCraft.common.tileentity.TileEntityAssemblyDrill;
import pneumaticCraft.common.tileentity.TileEntityAssemblyIOUnit;
import pneumaticCraft.common.tileentity.TileEntityAssemblyLaser;
import pneumaticCraft.common.tileentity.TileEntityAssemblyPlatform;
import pneumaticCraft.lib.GuiConstants;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiAssemblyController extends GuiPneumaticContainerBase{

    private static final ResourceLocation guiTexture = new ResourceLocation(Textures.GUI_ASSEMBLY_CONTROLLER);
    private final TileEntityAssemblyController teAssemblyController;
    private GuiAnimatedStat pressureStat;
    private GuiAnimatedStat problemStat;
    private GuiAnimatedStat statusStat;
    private GuiAnimatedStat infoStat;
    private GuiAnimatedStat upgradeStat;

    public GuiAssemblyController(InventoryPlayer player, TileEntityAssemblyController teAssemblyController){

        super(new ContainerAssemblyController(player, teAssemblyController));
        ySize = 176;
        this.teAssemblyController = teAssemblyController;
    }

    @Override
    public void initGui(){
        super.initGui();
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        pressureStat = new GuiAnimatedStat(this, "Pressure", new ItemStack(Blockss.pressureTube), xStart + xSize, yStart + 5, 0xFF00AA00, null, false);
        problemStat = new GuiAnimatedStat(this, "Problems", Textures.GUI_PROBLEMS_TEXTURE, xStart + xSize, 3, 0xFFFF0000, pressureStat, false);
        statusStat = new GuiAnimatedStat(this, "Assembly Controller Status", new ItemStack(Blockss.assemblyController), xStart + xSize, 3, 0xFFFFAA00, problemStat, false);
        infoStat = new GuiAnimatedStat(this, "Information", Textures.GUI_INFO_LOCATION, xStart, 5 + yStart, 0xFF8888FF, null, true);
        upgradeStat = new GuiAnimatedStat(this, "Upgrades", Textures.GUI_UPGRADES_LOCATION, xStart, 3, 0xFF0000FF, infoStat, true);
        animatedStatList.add(pressureStat);
        animatedStatList.add(problemStat);
        animatedStatList.add(statusStat);
        animatedStatList.add(infoStat);
        animatedStatList.add(upgradeStat);
        infoStat.setText(GuiConstants.INFO_ASSEMBLY_CONTROLLER);
        upgradeStat.setText(GuiConstants.UPGRADES_ASSEMBLY_CONTROLLER);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){

        String containerName = teAssemblyController.hasCustomInventoryName() ? teAssemblyController.getInventoryName() : StatCollector.translateToLocal(teAssemblyController.getInventoryName());

        fontRendererObj.drawString(containerName, xSize / 2 - fontRendererObj.getStringWidth(containerName) / 2, 6, 4210752);
        fontRendererObj.drawString("Upgr.", 18, 21, 4210752);
        fontRendererObj.drawString("Prog.", 70, 24, 4210752);

        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 106 + 2, 4210752);

    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y){
        super.drawGuiContainerBackgroundLayer(opacity, x, y);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        mc.getTextureManager().bindTexture(guiTexture);
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        drawTexturedModalRect(xStart, yStart, 0, 0, xSize, ySize);

        GuiUtils.drawPressureGauge(fontRendererObj, -1, PneumaticValues.MAX_PRESSURE_ASSEMBLY_CONTROLLER, PneumaticValues.DANGER_PRESSURE_ASSEMBLY_CONTROLLER, PneumaticValues.MIN_PRESSURE_ASSEMBLY_CONTROLLER, teAssemblyController.getPressure(ForgeDirection.UNKNOWN), xStart + xSize * 3 / 4, yStart + ySize * 1 / 4 + 4, zLevel);

        pressureStat.setText(getPressureStats());
        problemStat.setText(getProblems());
        statusStat.setText(getStatusText());
    }

    private List<String> getPressureStats(){
        List<String> pressureStatText = new ArrayList<String>();
        pressureStatText.add("\u00a77Current Pressure:");
        pressureStatText.add("\u00a70" + (double)Math.round(teAssemblyController.getPressure(ForgeDirection.UNKNOWN) * 10) / 10 + " bar.");
        pressureStatText.add("\u00a77Current Air:");
        pressureStatText.add("\u00a70" + (double)Math.round(teAssemblyController.currentAir + teAssemblyController.volume) + " mL.");

        pressureStatText.add("\u00a77Volume:");
        pressureStatText.add("\u00a70" + (double)Math.round(PneumaticValues.VOLUME_ASSEMBLY_CONTROLLER) + " mL.");
        float pressureLeft = teAssemblyController.volume - PneumaticValues.VOLUME_ASSEMBLY_CONTROLLER;
        if(pressureLeft > 0) {
            pressureStatText.add("\u00a70" + (double)Math.round(pressureLeft) + " mL. (Volume Upgrades)");
            pressureStatText.add("\u00a70--------+");
            pressureStatText.add("\u00a70" + (double)Math.round(teAssemblyController.volume) + " mL.");
        }
        return pressureStatText;
    }

    private List<String> getStatusText(){
        List<String> text = new ArrayList<String>();

        List<IAssemblyMachine> machineList = teAssemblyController.getMachines();
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

    private List<String> getProblems(){
        List<String> textList = new ArrayList<String>();
        teAssemblyController.addProblems(textList);
        if(textList.size() == 0) {
            textList.add(EnumChatFormatting.BLACK + "No problems.");
        }

        return textList;
    }
}
