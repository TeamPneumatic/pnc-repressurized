package pneumaticCraft.common.inventory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketSetGlobalVariable;
import pneumaticCraft.common.remote.GlobalVariableManager;
import pneumaticCraft.common.remote.TextVariableParser;

public class ContainerRemote extends ContainerPneumaticBase{
    private final List<String> syncedVars;
    private final ChunkPosition[] lastValues;
    public String[] variables = new String[0];

    public ContainerRemote(ItemStack remote){
        super(null);
        syncedVars = new ArrayList<String>(getRelevantVariableNames(remote));
        lastValues = new ChunkPosition[syncedVars.size()];
    }

    private static Set<String> getRelevantVariableNames(ItemStack remote){
        Set<String> variables = new HashSet<String>();
        NBTTagCompound tag = remote.getTagCompound();
        if(tag != null) {
            NBTTagList tagList = tag.getTagList("actionWidgets", 10);
            for(int i = 0; i < tagList.tagCount(); i++) {
                NBTTagCompound widgetTag = tagList.getCompoundTagAt(i);
                variables.add(widgetTag.getString("variableName"));
                variables.add(widgetTag.getString("enableVariable"));
                TextVariableParser parser = new TextVariableParser(widgetTag.getString("text"));
                parser.parse();
                variables.addAll(parser.getRelevantVariables());
            }
        }
        return variables;
    }

    @Override
    public void detectAndSendChanges(){
        super.detectAndSendChanges();
        for(int i = 0; i < lastValues.length; i++) {
            ChunkPosition newValue = GlobalVariableManager.getInstance().getPos(syncedVars.get(i));
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
