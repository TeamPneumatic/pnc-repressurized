package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class NetworkConnectionAIHandler extends NetworkConnectionHandler {
    private boolean tracing;
    private int ticksTillTrace;
    private boolean simulating;
    private int stopWormTime = 0;

    NetworkConnectionAIHandler(GuiSecurityStationBase gui, TileEntitySecurityStation station, int baseX,
                               int baseY, int nodeSpacing, int color) {
        super(gui, station, baseX, baseY, nodeSpacing, color, TileEntityConstants.NETWORK_AI_BRIDGE_SPEED);

        for (int i = 0; i < station.getPrimaryInventory().getSlots(); i++) {
            ItemStack stack = station.getPrimaryInventory().getStackInSlot(i);
            if (stack.getItem() == ModItems.DIAGNOSTIC_SUBROUTINE) {
                slotHacked[i] = true;
            }
        }
    }

    private NetworkConnectionAIHandler(NetworkConnectionAIHandler copy) {
        super(copy);
    }

    NetworkConnectionAIHandler(NetworkConnectionAIHandler copy, int baseX, int baseY) {
        super(copy, baseX, baseY);
    }

    void setTracing(boolean tracing) {
        this.tracing = tracing;
    }

    boolean isTracing() {
        return tracing;
    }

    private void setSimulating() {
        simulating = true;
    }

    int getRemainingTraceTime() {
        return ticksTillTrace;
    }

    void applyStopWorm() {
        stopWormTime += 100;
    }

    @Override
    public void update() {
        if (stopWormTime <= 0) super.update();
        if (tracing) {
            for (int i = 0; i < 35; i++)
                tryToHackSlot(i);
            if (ticksTillTrace % 20 == 0 && !simulating) {
                updateTimer();
            } else if (stopWormTime <= 0) {
                ticksTillTrace--;
            }
            if (stopWormTime > 0) stopWormTime--;
        }
    }

    private void updateTimer() {
        NetworkConnectionAIHandler dummy = new NetworkConnectionAIHandler(this);
        dummy.setSimulating();
        dummy.setTracing(true);
        ticksTillTrace = 0;
        int ioPortSlot = -1;
        for (int i = 0; i < station.getPrimaryInventory().getSlots(); i++) {
            if (station.getPrimaryInventory().getStackInSlot(i).getItem() == ModItems.NETWORK_IO_PORT) {
                ioPortSlot = i;
                break;
            }
        }
        while (!dummy.slotHacked[ioPortSlot]) {
            dummy.update();
            ticksTillTrace++;
        }
    }

    @Override
    public void onSlotHack(int slot, boolean nuked) {
        ItemStack stack = station.getPrimaryInventory().getStackInSlot(slot);
        if (!simulating && stack.getItem() == ModItems.NETWORK_IO_PORT) {
            Minecraft.getInstance().player.closeScreen();
            Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(TextFormatting.RED + "Hacking unsuccessful! The Diagnostic Subroutine traced to your location!"), false);
            if (gui instanceof GuiSecurityStationHacking)
                ((GuiSecurityStationHacking) gui).removeUpdatesOnConnectionHandlers();
        }
    }

}
