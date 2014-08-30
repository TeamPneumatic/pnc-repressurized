package pneumaticCraft.client.gui;

import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import pneumaticCraft.common.item.ItemNetworkComponents;
import pneumaticCraft.common.tileentity.TileEntitySecurityStation;
import pneumaticCraft.lib.TileEntityConstants;
import cpw.mods.fml.client.FMLClientHandler;

public class NetworkConnectionAIHandler extends NetworkConnectionHandler{
    private boolean tracing;
    private int ticksTillTrace;
    private boolean simulating;
    private int stopWormTime = 0;

    public NetworkConnectionAIHandler(GuiSecurityStationBase gui, TileEntitySecurityStation station, int baseX,
            int baseY, int nodeSpacing, int color){
        super(gui, station, baseX, baseY, nodeSpacing, color, TileEntityConstants.NETWORK_AI_BRIDGE_SPEED);
        for(int i = 0; i < 35; i++) {
            if(station.getStackInSlot(i) != null && station.getStackInSlot(i).getItemDamage() == ItemNetworkComponents.DIAGNOSTIC_SUBROUTINE) {
                slotHacked[i] = true;
            }
        }
    }

    public NetworkConnectionAIHandler(NetworkConnectionAIHandler copy){
        super(copy);
    }

    public NetworkConnectionAIHandler(NetworkConnectionAIHandler copy, int baseX, int baseY){
        super(copy, baseX, baseY);
    }

    public void setTracing(boolean tracing){
        this.tracing = tracing;
    }

    public boolean isTracing(){
        return tracing;
    }

    public void setSimulating(){
        simulating = true;
    }

    public int getRemainingTraceTime(){
        return ticksTillTrace;
    }

    public void applyStopWorm(){
        stopWormTime += 100;
    }

    @Override
    public void update(){
        if(stopWormTime <= 0) super.update();
        if(tracing) {
            for(int i = 0; i < 35; i++)
                tryToHackSlot(i);
            if(ticksTillTrace % 20 == 0 && !simulating) {
                updateTimer();
            } else if(stopWormTime <= 0) {
                ticksTillTrace--;
            }
            if(stopWormTime > 0) stopWormTime--;
        }
    }

    private void updateTimer(){
        NetworkConnectionAIHandler dummy = new NetworkConnectionAIHandler(this);
        dummy.setSimulating();
        dummy.setTracing(true);
        ticksTillTrace = 0;
        int ioPortSlot = -1;
        for(int i = 0; i < 35; i++) {
            if(station.getStackInSlot(i) != null && station.getStackInSlot(i).getItemDamage() == ItemNetworkComponents.NETWORK_IO_PORT) {
                ioPortSlot = i;
                break;
            }
        }
        while(!dummy.slotHacked[ioPortSlot]) {
            dummy.update();
            ticksTillTrace++;
        }
    }

    @Override
    public void onSlotHack(int slot, boolean nuked){
        if(!simulating && station.getStackInSlot(slot) != null && station.getStackInSlot(slot).getItemDamage() == ItemNetworkComponents.NETWORK_IO_PORT) {
            FMLClientHandler.instance().getClient().thePlayer.closeScreen();
            FMLClientHandler.instance().getClient().thePlayer.addChatComponentMessage(new ChatComponentTranslation(EnumChatFormatting.RED + "Hacking unsuccessful! The Diagnostic Subroutine traced to your location!"));
            if(gui instanceof GuiSecurityStationHacking) ((GuiSecurityStationHacking)gui).removeUpdatesOnConnectionHandlers();
        }
    }

}
