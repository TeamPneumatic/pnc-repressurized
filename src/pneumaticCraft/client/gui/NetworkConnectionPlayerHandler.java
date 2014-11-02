package pneumaticCraft.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.render.RenderProgressingLine;
import pneumaticCraft.common.item.ItemNetworkComponents;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketSecurityStationAddHacker;
import pneumaticCraft.common.network.PacketUseItem;
import pneumaticCraft.common.tileentity.TileEntitySecurityStation;
import pneumaticCraft.lib.TileEntityConstants;
import cpw.mods.fml.client.FMLClientHandler;

public class NetworkConnectionPlayerHandler extends NetworkConnectionHandler{
    private final List<GuiStatBalloon> balloons = new ArrayList<GuiStatBalloon>();
    public boolean hackedSuccessfully;

    public NetworkConnectionPlayerHandler(GuiSecurityStationBase gui, TileEntitySecurityStation station, int baseX,
            int baseY, int nodeSpacing, int color){
        super(gui, station, baseX, baseY, nodeSpacing, color, TileEntityConstants.NETWORK_NORMAL_BRIDGE_SPEED);
        for(int i = 0; i < 35; i++) {
            if(station.getStackInSlot(i) != null && station.getStackInSlot(i).getItemDamage() == ItemNetworkComponents.NETWORK_IO_PORT) {
                slotHacked[i] = true;
            }
        }
    }

    public NetworkConnectionPlayerHandler(NetworkConnectionPlayerHandler copy, int baseX, int baseY){
        super(copy, baseX, baseY);
    }

    @Override
    public void render(){
        super.render();
        GL11.glEnable(GL11.GL_BLEND);
        // GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1, 1, 1, 0.5F);
        for(GuiStatBalloon balloon : balloons) {
            balloon.render();
        }
        // GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    @Override
    public void update(){
        super.update();
        for(GuiStatBalloon balloon : balloons) {
            String numberText = balloon.text.replace("%", "");
            if(numberText.equals("")) {
                balloon.text = "0%";
            } else if(!numberText.contains("+")) {
                int percentage = Integer.parseInt(numberText) + 1;
                if(percentage <= 100) {
                    balloon.text = percentage + "%";
                } else {
                    balloon.text = "+1";
                    onSlotFortification(balloon.slotNumber);
                }
            }
        }
    }

    public void mouseClicked(int x, int y, int mouseButton, Slot slot){
        if(slot != null) {
            if(mouseButton == 0) tryToHackSlot(slot.slotNumber);
            if(mouseButton == 1 && slotHacked[slot.slotNumber]) {
                boolean alreadyFortifying = false;
                for(GuiStatBalloon balloon : balloons) {
                    if(balloon.slotNumber == slot.slotNumber) {
                        alreadyFortifying = true;
                        break;
                    }
                }
                if(!alreadyFortifying) {
                    balloons.add(new GuiStatBalloon(slot.xDisplayPosition + gui.getGuiLeft() + 8, slot.yDisplayPosition + gui.getGuiTop() - 5, slot.slotNumber));
                }
            }
            if(mouseButton == 2 && !slotHacked[slot.slotNumber] && ((GuiSecurityStationHacking)gui).hasNukeViruses()) {
                int linesBefore = lineList.size();
                if(tryToHackSlot(slot.slotNumber)) {
                    EntityPlayer player = FMLClientHandler.instance().getClient().thePlayer;
                    NetworkHandler.sendToServer(new PacketUseItem(Itemss.nukeVirus, 1));
                    player.inventory.consumeInventoryItem(Itemss.nukeVirus);

                    for(int i = linesBefore; i < lineList.size(); i++) {
                        RenderProgressingLine line = lineList.get(i);
                        line.setProgress(1);
                        slotHacked[slot.slotNumber] = true;
                        onSlotHack(slot.slotNumber, true);
                    }
                }
            }
        }
    }

    private void onSlotFortification(int slot){
        if(gui instanceof GuiSecurityStationHacking) {
            ((GuiSecurityStationHacking)gui).onSlotFortification(slot);
        }
    }

    @Override
    protected void onSlotHack(int slot, boolean nuked){
        if(!nuked && gui instanceof GuiSecurityStationHacking) {
            ((GuiSecurityStationHacking)gui).onSlotHack(slot);
        }
        if(station.getStackInSlot(slot) != null && (station.getStackInSlot(slot).getItemDamage() == ItemNetworkComponents.NETWORK_REGISTRY || station.getStackInSlot(slot).getItemDamage() == ItemNetworkComponents.DIAGNOSTIC_SUBROUTINE)) {
            hackedSuccessfully = true;
            EntityPlayer player = FMLClientHandler.instance().getClient().thePlayer;
            NetworkHandler.sendToServer(new PacketSecurityStationAddHacker(station, player.getCommandSenderName()));
            FMLClientHandler.instance().getClient().thePlayer.closeScreen();
            player.addChatComponentMessage(new ChatComponentTranslation(EnumChatFormatting.GREEN + "Hacking successful! This Security Station now doesn't protect the area any longer!"));
            if(gui instanceof GuiSecurityStationHacking) ((GuiSecurityStationHacking)gui).removeUpdatesOnConnectionHandlers();
        }
    }

}
