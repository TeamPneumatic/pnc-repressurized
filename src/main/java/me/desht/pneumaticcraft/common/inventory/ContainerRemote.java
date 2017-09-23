package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSetGlobalVariable;
import me.desht.pneumaticcraft.common.remote.GlobalVariableManager;
import me.desht.pneumaticcraft.common.remote.TextVariableParser;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContainerRemote extends ContainerPneumaticBase {
    private final List<String> syncedVars;
    private final BlockPos[] lastValues;
    public String[] variables = new String[0];

    public ContainerRemote(ItemStack remote) {
        super(null);
        syncedVars = new ArrayList<>(getRelevantVariableNames(remote));
        lastValues = new BlockPos[syncedVars.size()];
    }

    private static Set<String> getRelevantVariableNames(@Nonnull ItemStack remote) {
        Set<String> variables = new HashSet<>();
        NBTTagCompound tag = remote.getTagCompound();
        if (tag != null) {
            NBTTagList tagList = tag.getTagList("actionWidgets", 10);
            for (int i = 0; i < tagList.tagCount(); i++) {
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
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (int i = 0; i < lastValues.length; i++) {
            BlockPos newValue = GlobalVariableManager.getInstance().getPos(syncedVars.get(i));
            if (!newValue.equals(lastValues[i])) {
                lastValues[i] = newValue;
                for (Object o : listeners) {
                    if (o instanceof EntityPlayerMP)
                        NetworkHandler.sendTo(new PacketSetGlobalVariable(syncedVars.get(i), newValue), (EntityPlayerMP) o);
                }
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return player.getHeldItemMainhand().getItem() == Itemss.REMOTE;
    }
}
