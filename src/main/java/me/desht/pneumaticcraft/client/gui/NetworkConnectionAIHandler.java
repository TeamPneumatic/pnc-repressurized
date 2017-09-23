package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.common.item.ItemNetworkComponents;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;

public class NetworkConnectionAIHandler extends NetworkConnectionHandler {
    private boolean tracing;
    private int ticksTillTrace;
    private boolean simulating;
    private int stopWormTime = 0;

    public NetworkConnectionAIHandler(GuiSecurityStationBase gui, TileEntitySecurityStation station, int baseX,
                                      int baseY, int nodeSpacing, int color) {
        super(gui, station, baseX, baseY, nodeSpacing, color, TileEntityConstants.NETWORK_AI_BRIDGE_SPEED);
        for (int i = 0; i < station.getPrimaryInventory().getSlots(); i++) {
            ItemStack stack = station.getPrimaryInventory().getStackInSlot(i);
            if (stack.getItemDamage() == ItemNetworkComponents.DIAGNOSTIC_SUBROUTINE) {
                slotHacked[i] = true;
            }
        }
    }

    public NetworkConnectionAIHandler(NetworkConnectionAIHandler copy) {
        super(copy);
    }

    public NetworkConnectionAIHandler(NetworkConnectionAIHandler copy, int baseX, int baseY) {
        super(copy, baseX, baseY);
    }

    public void setTracing(boolean tracing) {
        this.tracing = tracing;
    }

    public boolean isTracing() {
        return tracing;
    }

    public void setSimulating() {
        simulating = true;
    }

    public int getRemainingTraceTime() {
        return ticksTillTrace;
    }

    public void applyStopWorm() {
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
            if (station.getPrimaryInventory().getStackInSlot(i).getItemDamage() == ItemNetworkComponents.NETWORK_IO_PORT) {
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
        if (!simulating && !stack.isEmpty() && stack.getItemDamage() == ItemNetworkComponents.NETWORK_IO_PORT) {
            FMLClientHandler.instance().getClient().player.closeScreen();
            FMLClientHandler.instance().getClient().player.sendStatusMessage(new TextComponentString(TextFormatting.RED + "Hacking unsuccessful! The Diagnostic Subroutine traced to your location!"), false);
            if (gui instanceof GuiSecurityStationHacking)
                ((GuiSecurityStationHacking) gui).removeUpdatesOnConnectionHandlers();
        }
    }

}
