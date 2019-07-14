package me.desht.pneumaticcraft.common.thirdparty.waila;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
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
        CompoundNBT tag = accessor.getNBTData();
        if (tag.hasKey("heat")) {
            ListNBT tagList = tag.getTagList("heat", 10);
            for (int i = 0; i < tagList.tagCount(); i++) {
                CompoundNBT heatTag = tagList.getCompoundTagAt(i);
                Direction face = Direction.byIndex(heatTag.getByte("side"));
                currenttip.add(HeatUtil.formatHeatString(face, heatTag.getInteger("temp")));
            }
        } else {
            currenttip.add(WailaCallback.COLOR + HeatUtil.formatHeatString(tag.getInteger("temp")));
        }
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    @Nonnull
    @Override
    public CompoundNBT getNBTData(ServerPlayerEntity player, TileEntity te, CompoundNBT tag, World world, BlockPos pos) {
        if (te instanceof IHeatExchanger) {
            Set<IHeatExchangerLogic> heatExchangers = new HashSet<>();
            IHeatExchangerLogic logic = null;
            boolean isMultisided = true;
            for (Direction face : Direction.VALUES) {
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
                ListNBT tagList = new ListNBT();
                for (Direction face : Direction.VALUES) {
                    logic = ((IHeatExchanger) te).getHeatExchangerLogic(face);
                    if (logic != null) {
                        CompoundNBT heatTag = new CompoundNBT();
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
