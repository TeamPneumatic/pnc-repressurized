package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.client.util.ProgressingLine;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSecurityStationAddHacker;
import me.desht.pneumaticcraft.common.network.PacketUseItem;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class NetworkConnectionPlayerHandler extends NetworkConnectionHandler {
    private final List<GuiStatBalloon> balloons = new ArrayList<>();
    boolean hackedSuccessfully;

    NetworkConnectionPlayerHandler(GuiSecurityStationBase gui, TileEntitySecurityStation station, int baseX,
                                   int baseY, int nodeSpacing, int color) {
        super(gui, station, baseX, baseY, nodeSpacing, color, TileEntityConstants.NETWORK_NORMAL_BRIDGE_SPEED);
        for (int i = 0; i < station.getPrimaryInventory().getSlots(); i++) {
            if (station.getPrimaryInventory().getStackInSlot(i).getItem() == ModItems.NETWORK_IO_PORT.get()) {
                slotHacked[i] = true;
            }
        }
    }

    NetworkConnectionPlayerHandler(NetworkConnectionPlayerHandler copy, int baseX, int baseY) {
        super(copy, baseX, baseY);
    }

    @Override
    public void render(MatrixStack matrixStack) {
        super.render(matrixStack);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.color4f(1, 1, 1, 0.5F);
        for (GuiStatBalloon balloon : balloons) {
            balloon.render(matrixStack);
        }
        RenderSystem.disableBlend();
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
                    PlayerEntity player = Minecraft.getInstance().player;
                    NetworkHandler.sendToServer(new PacketUseItem(new ItemStack(ModItems.NUKE_VIRUS.get())));
                    PneumaticCraftUtils.consumeInventoryItem(player.inventory, ModItems.NUKE_VIRUS.get());
                    for (int i = linesBefore; i < lineList.size(); i++) {
                        ProgressingLine line = lineList.get(i);
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
        if (stack.getItem() == ModItems.NETWORK_REGISTRY.get() || stack.getItem() == ModItems.DIAGNOSTIC_SUBROUTINE.get()) {
            hackedSuccessfully = true;
            PlayerEntity player = Minecraft.getInstance().player;
            // TODO should be sending entity or player UUID here
            NetworkHandler.sendToServer(new PacketSecurityStationAddHacker(station, player.getName().getString()));
            player.closeScreen();
            player.sendStatusMessage(new StringTextComponent(TextFormatting.GREEN + "Hacking successful! This Security Station has been disabled!"), false);
            if (gui instanceof GuiSecurityStationHacking) {
                ((GuiSecurityStationHacking) gui).removeUpdatesOnConnectionHandlers();
            }
        }
    }

}
