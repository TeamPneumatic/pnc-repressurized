package pneumaticCraft.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.EnumChatFormatting;
import pneumaticCraft.common.TickHandlerPneumaticCraft;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.inventory.Container4UpgradeSlots;
import pneumaticCraft.common.tileentity.TileEntityElectrostaticCompressor;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiElectrostaticCompressor extends GuiPneumaticContainerBase<TileEntityElectrostaticCompressor>{
    private int connectedCompressors = 1;
    private int ticksExisted;

    public GuiElectrostaticCompressor(InventoryPlayer player, TileEntityElectrostaticCompressor te){

        super(new Container4UpgradeSlots(player, te), te, Textures.GUI_4UPGRADE_SLOTS);

    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){
        super.drawGuiContainerForegroundLayer(x, y);
        fontRendererObj.drawString("Upgr.", 53, 19, 4210752);
    }

    @Override
    public String getRedstoneButtonText(int mode){
        return mode == 0 ? "gui.tab.redstoneBehaviour.button.never" : "gui.tab.redstoneBehaviour.electrostaticCompressor.button.struckByLightning";
    }

    @Override
    protected void addPressureStatInfo(List<String> pressureStatText){
        super.addPressureStatInfo(pressureStatText);

        pressureStatText.add(EnumChatFormatting.GRAY + "Energy production:");
        pressureStatText.add(EnumChatFormatting.BLACK + PneumaticCraftUtils.roundNumberTo(PneumaticValues.PRODUCTION_ELECTROSTATIC_COMPRESSOR / connectedCompressors, 1) + " mL/lightning strike");
        pressureStatText.add(EnumChatFormatting.GRAY + "Maximum air redirection:");
        pressureStatText.add(EnumChatFormatting.BLACK + PneumaticCraftUtils.roundNumberTo(PneumaticValues.MAX_REDIRECTION_PER_IRON_BAR * te.ironBarsBeneath, 1) + " mL/lightning strike");
    }

    @Override
    protected void addProblems(List<String> textList){
        super.addProblems(textList);
        if(PneumaticValues.MAX_REDIRECTION_PER_IRON_BAR * te.ironBarsBeneath < PneumaticValues.PRODUCTION_ELECTROSTATIC_COMPRESSOR / connectedCompressors) {
            textList.add(EnumChatFormatting.GRAY + "When lightning strikes with a full air tank not all the energy can be redirected!");
            textList.add(EnumChatFormatting.BLACK + "Connect up more Iron Bars to the underside of the Electrostatic Compressor.");
        }
    }

    @Override
    public void updateScreen(){
        super.updateScreen();
        if(ticksExisted % 20 == 0) {
            List<int[]> coordList = new ArrayList<int[]>();
            coordList.add(new int[]{te.xCoord, te.yCoord, te.zCoord});
            TickHandlerPneumaticCraft.getElectrostaticGrid(coordList, te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord);
            connectedCompressors = 0;
            for(int[] coord : coordList) {
                if(te.getWorldObj().getBlock(coord[0], coord[1], coord[2]) == Blockss.electrostaticCompressor) {
                    connectedCompressors++;
                }
            }
        }
        ticksExisted++;

    }
}
