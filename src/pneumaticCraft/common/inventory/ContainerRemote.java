package pneumaticCraft.common.inventory;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketSetGlobalVariable;
import pneumaticCraft.common.remote.GlobalVariableManager;
import pneumaticCraft.common.remote.RemoteLayout;

public class ContainerRemote extends Container{
    private final List<String> syncedVars;
    private final ChunkPosition[] lastValues;

    public ContainerRemote(ItemStack remote){
        syncedVars = RemoteLayout.getRelevantVariableNames(remote);
        lastValues = new ChunkPosition[syncedVars.size()];
    }

    @Override
    public void detectAndSendChanges(){
        super.detectAndSendChanges();
        for(int i = 0; i < lastValues.length; i++) {
            ChunkPosition newValue = GlobalVariableManager.getPos(syncedVars.get(i));
            if(!newValue.equals(lastValues[i])) {
                lastValues[i] = newValue;
                for(Object o : crafters) {
                    if(o instanceof EntityPlayerMP) NetworkHandler.sendTo(new PacketSetGlobalVariable(syncedVars.get(i), newValue), (EntityPlayerMP)o);
                }
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player){
        return player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() == Itemss.remote;
    }
}
