package pneumaticCraft.client.gui;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.ClientTickHandler;
import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.inventory.ContainerSecurityStationHacking;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketSecurityStationFailedHack;
import pneumaticCraft.common.network.PacketUseItem;
import pneumaticCraft.common.tileentity.TileEntitySecurityStation;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSecurityStationHacking extends GuiSecurityStationBase{
    private GuiAnimatedStat statusStat;

    private NetworkConnectionBackground playerBackgroundBridges;
    private NetworkConnectionBackground aiBackgroundBridges;
    private NetworkConnectionPlayerHandler hackerBridges;
    private NetworkConnectionAIHandler aiBridges;

    private int stopWorms = 0;
    private int nukeViruses = 0;

    private final ItemStack stopWorm = new ItemStack(Itemss.stopWorm);
    private final ItemStack nukeVirus = new ItemStack(Itemss.nukeVirus);

    public GuiSecurityStationHacking(InventoryPlayer player, TileEntitySecurityStation te){

        super(new ContainerSecurityStationHacking(player, te), te, Textures.GUI_HACKING);
        ySize = 238;
    }

    @Override
    public void initGui(){
        super.initGui();

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        statusStat = addAnimatedStat("Security Status", new ItemStack(Blockss.securityStation), 0xFFFFAA00, false);
        addAnimatedStat("gui.tab.info", Textures.GUI_INFO_LOCATION, 0xFF8888FF, true).setText("gui.tab.info.tile.securityStation.hacking");
        addAnimatedStat("gui.tab.upgrades", Textures.GUI_UPGRADES_LOCATION, 0xFF0000FF, true).setText("gui.tab.upgrades.tile.securityStation.hacking");
        addAnimatedStat(Itemss.nukeVirus.getUnlocalizedName() + ".name", new ItemStack(Itemss.nukeVirus), 0xFF18c9e8, false).setText("gui.tab.info.tile.securityStation.nukeVirus");
        addAnimatedStat(Itemss.stopWorm.getUnlocalizedName() + ".name", new ItemStack(Itemss.stopWorm), 0xFFc13232, false).setText("gui.tab.info.tile.securityStation.stopWorm");

        if(playerBackgroundBridges == null) {
            playerBackgroundBridges = new NetworkConnectionBackground(this, te, xStart + 21, yStart + 26, 31, 0xAA4444FF);
            aiBackgroundBridges = new NetworkConnectionBackground(this, te, xStart + 23, yStart + 27, 31, 0xAA4444FF);
            hackerBridges = new NetworkConnectionPlayerHandler(this, te, xStart + 21, yStart + 26, 31, 0xFF00FF00);
            aiBridges = new NetworkConnectionAIHandler(this, te, xStart + 23, yStart + 27, 31, 0xFFFF0000);
        } else {
            playerBackgroundBridges = new NetworkConnectionBackground(playerBackgroundBridges, xStart + 21, yStart + 26);
            aiBackgroundBridges = new NetworkConnectionBackground(aiBackgroundBridges, xStart + 23, yStart + 27);
            hackerBridges = new NetworkConnectionPlayerHandler(hackerBridges, xStart + 21, yStart + 26);
            aiBridges = new NetworkConnectionAIHandler(aiBridges, xStart + 23, yStart + 27);
        }
    }

    @Override
    protected boolean shouldAddInfoTab(){
        return false;
    }

    @Override
    protected boolean shouldAddUpgradeTab(){
        return false;
    }

    @Override
    protected boolean shouldAddRedstoneTab(){
        return false;
    }

    @Override
    protected Point getInvNameOffset(){
        return null;
    }

    @Override
    protected Point getInvTextOffset(){
        return null;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){
        super.drawGuiContainerForegroundLayer(x, y);
        fontRendererObj.drawString((aiBridges.isTracing() ? EnumChatFormatting.RED : EnumChatFormatting.GREEN) + "Tracing: " + PneumaticCraftUtils.convertTicksToMinutesAndSeconds(aiBridges.getRemainingTraceTime(), true), 15, 7, 4210752);
        renderConsumables(x, y);
    }

    private void renderConsumables(int x, int y){
        stopWorms = 0;
        nukeViruses = 0;
        EntityPlayer player = FMLClientHandler.instance().getClient().thePlayer;
        for(ItemStack stack : player.inventory.mainInventory) {
            if(stack != null) {
                if(stack.getItem() == Itemss.stopWorm) stopWorms += stack.stackSize;
                if(stack.getItem() == Itemss.nukeVirus) nukeViruses += stack.stackSize;
            }
        }
        GuiUtils.drawItemStack(nukeVirus, 155, 30);
        GuiUtils.drawItemStack(stopWorm, 155, 55);
        fontRendererObj.drawString(PneumaticCraftUtils.convertAmountToString(nukeViruses), 155, 45, 0xFFFFFFFF);
        fontRendererObj.drawString(PneumaticCraftUtils.convertAmountToString(stopWorms), 155, 70, 0xFFFFFFFF);

    }

    @Override
    public void updateScreen(){
        super.updateScreen();
        statusStat.setText(getStatusText());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y){
        super.drawGuiContainerBackgroundLayer(opacity, x, y);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        playerBackgroundBridges.render();
        aiBackgroundBridges.render();
        hackerBridges.render();
        aiBridges.render();

        if(x >= guiLeft + 155 && x <= guiLeft + 171 && y >= guiTop + 30 && y <= guiTop + 50) {
            List<String> text = new ArrayList<String>();
            text.add("Nuke Virus");
            if(hasNukeViruses()) {
                text.add(EnumChatFormatting.GRAY + "Middle-click a hackable node to use.");
            } else {
                text.add(EnumChatFormatting.RED + "You don't have any Nuke Viruses.");
            }
            drawHoveringString(text, x, y, fontRendererObj);
        }
        if(x >= guiLeft + 155 && x <= guiLeft + 171 && y >= guiTop + 55 && y <= guiTop + 75) {
            List<String> text = new ArrayList<String>();
            text.add("STOP! Worm");
            if(stopWorms > 0) {
                if(aiBridges.isTracing()) {
                    text.add(EnumChatFormatting.GRAY + "Left-click to use.");
                } else {
                    text.add(EnumChatFormatting.GRAY + "STOP! Worms can only be used when being traced.");
                }
            } else {
                text.add(EnumChatFormatting.RED + "You don't have any STOP! Worms.");
            }
            drawHoveringString(text, x, y, fontRendererObj);
        }
    }

    @Override
    protected void addProblems(List<String> text){
        super.addProblems(text);
        if(aiBridges.isTracing()) {
            text.add(EnumChatFormatting.GRAY + "Intrusion detected!");
            text.add(EnumChatFormatting.BLACK + "Time till trace: " + PneumaticCraftUtils.convertTicksToMinutesAndSeconds(aiBridges.getRemainingTraceTime(), false));
        }
    }

    private List<String> getStatusText(){
        List<String> text = new ArrayList<String>();
        text.add(EnumChatFormatting.GRAY + "Security Level");
        text.add(EnumChatFormatting.BLACK + "Level " + te.getSecurityLevel());
        text.add(EnumChatFormatting.GRAY + "Security Range");
        text.add(EnumChatFormatting.BLACK.toString() + te.getSecurityRange() + "m (square)");
        return text;
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3){
        if(par3 != 2) super.mouseClicked(par1, par2, par3);
        hackerBridges.mouseClicked(par1, par2, par3, getSlotAtPosition(par1, par2));
        if(aiBridges.isTracing() && par1 >= guiLeft + 155 && par1 <= guiLeft + 171 && par2 >= guiTop + 55 && par2 <= guiTop + 75) {
            EntityPlayer player = FMLClientHandler.instance().getClient().thePlayer;
            NetworkHandler.sendToServer(new PacketUseItem(Itemss.stopWorm, 1));
            player.inventory.consumeInventoryItem(Itemss.stopWorm);
            aiBridges.applyStopWorm();
        }
    }

    /* @Override
     protected void drawItemStackTooltip(ItemStack par1ItemStack, int par2, int par3){
         List list = par1ItemStack.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips);

         for(int k = 0; k < list.size(); ++k) {
             if(k == 0) {
                 list.set(k, "\u00a7" + Integer.toHexString(par1ItemStack.getRarity().rarityColor) + (String)list.get(k));
             } else {
                 list.set(k, EnumChatFormatting.GRAY + (String)list.get(k));
             }
         }

         handleItemTooltip(par1ItemStack, par2, par3, list);
         func_102021_a(list, par2, par3);
     }*/
    //TODO fix for without NEI
    public List<String> handleItemTooltip(ItemStack stack, int mousex, int mousey, List<String> currenttip){
        if(stack != null) {
            Slot slot = getSlotAtPosition(mousex, mousey);
            if(slot != null) {
                if(hackerBridges.slotHacked[slot.slotNumber]) {
                    if(!hackerBridges.slotFortified[slot.slotNumber]) {
                        currenttip.add(EnumChatFormatting.RED + "DETECTION: " + te.getDetectionChance() + "%");
                        currenttip.add(EnumChatFormatting.YELLOW + "Right-click to fortify");
                    }
                } else if(hackerBridges.canHackSlot(slot.slotNumber)) {
                    currenttip.add(EnumChatFormatting.RED + "DETECTION: " + te.getDetectionChance() + "%");
                    currenttip.add(EnumChatFormatting.GREEN + "Left-click to hack");

                }
            }
        }
        return currenttip;
    }

    public boolean hasNukeViruses(){
        return nukeViruses > 0;
    }

    public void onSlotHack(int slot){
        if(Math.random() < te.getDetectionChance() / 100D) {
            aiBridges.setTracing(true);
        }
    }

    public void onSlotFortification(int slot){
        aiBridges.slotFortified[slot] = true;
        if(Math.random() < te.getDetectionChance() / 100D) {
            aiBridges.setTracing(true);
        }
    }

    @Override
    public void onGuiClosed(){
        if(aiBridges.isTracing() && !hackerBridges.hackedSuccessfully) NetworkHandler.sendToServer(new PacketSecurityStationFailedHack(te.xCoord, te.yCoord, te.zCoord));
        removeUpdatesOnConnectionHandlers();
        super.onGuiClosed();
    }

    public void removeUpdatesOnConnectionHandlers(){
        ClientTickHandler.instance().removeUpdatedObject(hackerBridges);
        ClientTickHandler.instance().removeUpdatedObject(aiBridges);
    }

}
