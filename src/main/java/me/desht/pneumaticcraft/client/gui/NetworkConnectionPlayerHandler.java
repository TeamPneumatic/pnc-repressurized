package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.render.RenderProgressingLine;
import me.desht.pneumaticcraft.common.item.ItemNetworkComponents;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSecurityStationAddHacker;
import me.desht.pneumaticcraft.common.network.PacketUseItem;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class NetworkConnectionPlayerHandler extends NetworkConnectionHandler {
    private final List<GuiStatBalloon> balloons = new ArrayList<GuiStatBalloon>();
    public boolean hackedSuccessfully;

    public NetworkConnectionPlayerHandler(GuiSecurityStationBase gui, TileEntitySecurityStation station, int baseX,
                                          int baseY, int nodeSpacing, int color) {
        super(gui, station, baseX, baseY, nodeSpacing, color, TileEntityConstants.NETWORK_NORMAL_BRIDGE_SPEED);
        for (int i = 0; i < station.getPrimaryInventory().getSlots(); i++) {
            if (station.getPrimaryInventory().getStackInSlot(i).getItemDamage() == ItemNetworkComponents.NETWORK_IO_PORT) {
                slotHacked[i] = true;
            }
        }
    }

    public NetworkConnectionPlayerHandler(NetworkConnectionPlayerHandler copy, int baseX, int baseY) {
        super(copy, baseX, baseY);
    }

    @Override
    public void render() {
        super.render();
        GL11.glEnable(GL11.GL_BLEND);
        // GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1, 1, 1, 0.5F);
        for (GuiStatBalloon balloon : balloons) {
            balloon.render();
        }
        // GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    @Override
    public void update() {
        super.update();
        for (GuiStatBalloon balloon : balloons) {
            String numberText = balloon.text.replace("%", "");
            if (numberText.equals("")) {
                balloon.text = "0%";
            } else if (!numberText.contains("+")) {
                int percentage = Integer.parseInt(numberText) + 1;
                if (percentage <= 100) {
                    balloon.text = percentage + "%";
                } else {
                    balloon.text = "+1";
                    onSlotFortification(balloon.slotNumber);
                }
            }
        }
    }

    public void mouseClicked(int x, int y, int mouseButton, Slot slot) {
        if (slot != null) {
            if (mouseButton == 0) tryToHackSlot(slot.slotNumber);
            if (mouseButton == 1 && slotHacked[slot.slotNumber]) {
                boolean alreadyFortifying = false;
                for (GuiStatBalloon balloon : balloons) {
                    if (balloon.slotNumber == slot.slotNumber) {
                        alreadyFortifying = true;
                        break;
                    }
                }
                if (!alreadyFortifying) {
                    balloons.add(new GuiStatBalloon(slot.xPos + gui.getGuiLeft() + 8, slot.yPos + gui.getGuiTop() - 5, slot.slotNumber));
                }
            }
            if (mouseButton == 2 && !slotHacked[slot.slotNumber] && ((GuiSecurityStationHacking) gui).hasNukeViruses()) {
                int linesBefore = lineList.size();
                if (tryToHackSlot(slot.slotNumber)) {
                    EntityPlayer player = FMLClientHandler.instance().getClient().player;
                    NetworkHandler.sendToServer(new PacketUseItem(Itemss.NUKE_VIRUS, 1));
                    PneumaticCraftUtils.consumeInventoryItem(player.inventory, Itemss.NUKE_VIRUS);
                    for (int i = linesBefore; i < lineList.size(); i++) {
                        RenderProgressingLine line = lineList.get(i);
                        line.setProgress(1);
                        slotHacked[slot.slotNumber] = true;
                        onSlotHack(slot.slotNumber, true);
                    }
                }
            }
        }
    }

    private void onSlotFortification(int slot) {
        if (gui instanceof GuiSecurityStationHacking) {
            ((GuiSecurityStationHacking) gui).onSlotFortification(slot);
        }
    }

    @Override
    protected void onSlotHack(int slot, boolean nuked) {
        if (!nuked && gui instanceof GuiSecurityStationHacking) {
            ((GuiSecurityStationHacking) gui).onSlotHack(slot);
        }
        ItemStack stack = station.getPrimaryInventory().getStackInSlot(slot);
        if (stack.getItemDamage() == ItemNetworkComponents.NETWORK_REGISTRY || stack.getItemDamage() == ItemNetworkComponents.DIAGNOSTIC_SUBROUTINE) {
            hackedSuccessfully = true;
            EntityPlayer player = FMLClientHandler.instance().getClient().player;
            NetworkHandler.sendToServer(new PacketSecurityStationAddHacker(station, player.getName()));
            FMLClientHandler.instance().getClient().player.closeScreen();
            player.sendStatusMessage(new TextComponentString(TextFormatting.GREEN + "Hacking successful! This Security Station now doesn't protect the area any longer!"), false);
            if (gui instanceof GuiSecurityStationHacking)
                ((GuiSecurityStationHacking) gui).removeUpdatesOnConnectionHandlers();
        }
    }

}
