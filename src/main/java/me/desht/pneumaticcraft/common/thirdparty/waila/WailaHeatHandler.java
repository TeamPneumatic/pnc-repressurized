package me.desht.pneumaticcraft.common.thirdparty.waila;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WailaHeatHandler implements IWailaDataProvider {
    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return ItemStack.EMPTY;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        addTipToMachine(currenttip, accessor);
        return currenttip;
    }

    private static void addTipToMachine(List<String> currenttip, IWailaDataAccessor accessor) {
        NBTTagCompound tag = accessor.getNBTData();
        if (tag.hasKey("heat")) {
            NBTTagList tagList = tag.getTagList("heat", 10);
            for (int i = 0; i < tagList.tagCount(); i++) {
                NBTTagCompound heatTag = tagList.getCompoundTagAt(i);
                String dir = EnumFacing.getFront(heatTag.getByte("side")).toString().toLowerCase();
                currenttip.add(I18n.format("waila.temperature." + dir, heatTag.getInteger("temp") - 273));
            }
        } else {
            currenttip.add(I18n.format("waila.temperature", tag.getInteger("temp") - 273));
        }
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    @Nonnull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
        if (te instanceof IHeatExchanger) {
            Set<IHeatExchangerLogic> heatExchangers = new HashSet<>();
            IHeatExchangerLogic logic = null;
            boolean isMultisided = true;
            for (EnumFacing face : EnumFacing.values()) {
                logic = ((IHeatExchanger) te).getHeatExchangerLogic(face);
                if (logic != null) {
                    if (heatExchangers.contains(logic)) {
                        isMultisided = false;
                        break;
                    } else {
                        heatExchangers.add(logic);
                    }
                }
            }

            if (isMultisided) {
                NBTTagList tagList = new NBTTagList();
                for (EnumFacing face : EnumFacing.values()) {
                    logic = ((IHeatExchanger) te).getHeatExchangerLogic(face);
                    if (logic != null) {
                        NBTTagCompound heatTag = new NBTTagCompound();
                        heatTag.setByte("side", (byte) face.ordinal());
                        heatTag.setInteger("temp", (int) logic.getTemperature());
                        tagList.appendTag(heatTag);
                    }
                }
                tag.setTag("heat", tagList);
            } else if (logic != null) {
                tag.setInteger("temp", (int) logic.getTemperature());
            }
        }
        return tag;
    }
}
