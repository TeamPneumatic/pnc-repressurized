package me.desht.pneumaticcraft.common.thirdparty.waila;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WailaPneumaticHandler implements IWailaDataProvider {
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
        TileEntity te = accessor.getTileEntity();
        if(te instanceof IInfoForwarder){
            BlockPos infoPos = new BlockPos(tag.getInteger("infoX"), tag.getInteger("infoY"), tag.getInteger("infoZ"));
            te = accessor.getWorld().getTileEntity(infoPos);
        }
        
        if (te instanceof IPneumaticMachine) {
            addTipToMachine(currenttip, (IPneumaticMachine) te, tag.getFloat("pressure"));
        }
    }

//    public static void addTipToMachine(List<String> currenttip, IPneumaticMachine machine) {
//        addTipToMachine(currenttip, machine, machine.getAirHandler(null).getPressure());
//    }

    private static void addTipToMachine(List<String> currenttip, IPneumaticMachine machine, float pressure) {
        Map<String, String> values = new HashMap<>();

        values.put("Pressure", PneumaticCraftUtils.roundNumberTo(pressure, 1) + " bar");

        IAirHandler base = machine.getAirHandler(null);
        values.put("Max Pressure", PneumaticCraftUtils.roundNumberTo(base.getDangerPressure(), 1) + " bar");

        for (Map.Entry<String, String> entry : values.entrySet()) {
            currenttip.add(WailaCallback.COLOR + entry.getKey() + ": " + TextFormatting.WHITE + entry.getValue());
        }
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    @Nonnull
    @Override
    public CompoundNBT getNBTData(ServerPlayerEntity player, TileEntity te, CompoundNBT tag, World world, BlockPos pos) {
        TileEntity teInfo;
        if(te instanceof IInfoForwarder){
            teInfo = ((IInfoForwarder)te).getInfoTileEntity();
            if(teInfo != null){
                tag.setInteger("infoX", teInfo.getPos().getX());
                tag.setInteger("infoY", teInfo.getPos().getY());
                tag.setInteger("infoZ", teInfo.getPos().getZ());
            }
        }else{
            teInfo = te;
        }
        
        if (teInfo instanceof IPneumaticMachine) {
            tag.setFloat("pressure", ((IPneumaticMachine) teInfo).getAirHandler(null).getPressure());
        }
        
        return tag;
    }
}
