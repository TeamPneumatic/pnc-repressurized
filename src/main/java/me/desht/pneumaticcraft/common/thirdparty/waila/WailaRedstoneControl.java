package me.desht.pneumaticcraft.common.thirdparty.waila;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import me.desht.pneumaticcraft.common.tileentity.IRedstoneControl;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WailaRedstoneControl implements IWailaDataProvider {
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
        //This is used so that we can split values later easier and have them all in the same layout.
        Map<String, String> values = new HashMap<>();

        if (tag.hasKey("redstoneMode")) {
            int mode = tag.getInteger("redstoneMode");
            TileEntity te = accessor.getTileEntity();
            if (te instanceof TileEntityBase) {
                values.put(((TileEntityBase) te).getRedstoneTabTitle(), ((TileEntityBase) te).getRedstoneButtonText(mode));
            }
        }

        //Get all the values from the map and put them in the list.
        for (Map.Entry<String, String> entry : values.entrySet()) {
            currenttip.add(WailaCallback.COLOR + I18n.format(entry.getKey()) + ": " + TextFormatting.RED + I18n.format(entry.getValue()));
        }
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public CompoundNBT getNBTData(ServerPlayerEntity player, TileEntity te, CompoundNBT tag, World world, BlockPos pos) {
        if (te instanceof IRedstoneControl) {
            tag.setInteger("redstoneMode", ((IRedstoneControl) te).getRedstoneMode());
        }
        return tag;
    }
}
