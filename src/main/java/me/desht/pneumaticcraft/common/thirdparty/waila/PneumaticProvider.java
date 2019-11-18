package me.desht.pneumaticcraft.common.thirdparty.waila;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerDataProvider;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IPneumaticMachine;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PneumaticProvider {
    public static class Data implements IServerDataProvider<TileEntity> {
        @Override
        public void appendServerData(CompoundNBT compoundNBT, ServerPlayerEntity serverPlayerEntity, World world, TileEntity te) {
            TileEntity teInfo;
            if (te instanceof IInfoForwarder) {
                teInfo = ((IInfoForwarder) te).getInfoTileEntity();
                if (teInfo != null) {
                    compoundNBT.putInt("infoX", teInfo.getPos().getX());
                    compoundNBT.putInt("infoY", teInfo.getPos().getY());
                    compoundNBT.putInt("infoZ", teInfo.getPos().getZ());
                }
            } else {
                teInfo = te;
            }

            if (teInfo instanceof IPneumaticMachine) {
                compoundNBT.putFloat("pressure", ((IPneumaticMachine) teInfo).getAirHandler(null).getPressure());
            }
        }
    }

    public static class Component implements IComponentProvider {
        @Override
        public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
            CompoundNBT tag = accessor.getServerData();
            TileEntity te = accessor.getTileEntity();
            if (te instanceof IInfoForwarder){
                BlockPos infoPos = new BlockPos(tag.getInt("infoX"), tag.getInt("infoY"), tag.getInt("infoZ"));
                te = accessor.getWorld().getTileEntity(infoPos);
            }
            if (te instanceof IPneumaticMachine) {
                addTipToMachine(tooltip, (IPneumaticMachine) te, tag.getFloat("pressure"));
            }
        }

        private static void addTipToMachine(List<ITextComponent> tooltip, IPneumaticMachine machine, float pressure) {
            Map<String, String> values = new HashMap<>();

            values.put("gui.tooltip.pressure", PneumaticCraftUtils.roundNumberTo(pressure, 1));

            IAirHandler base = machine.getAirHandler(null);
            values.put("gui.tooltip.maxPressure", PneumaticCraftUtils.roundNumberTo(base.getDangerPressure(), 1));

            for (Map.Entry<String, String> entry : values.entrySet()) {
                tooltip.add(new TranslationTextComponent(entry.getKey(), entry.getValue()));
            }
        }
    }
}
