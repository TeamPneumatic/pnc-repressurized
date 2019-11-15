package me.desht.pneumaticcraft.common.thirdparty.waila;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerDataProvider;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HeatProvider {
    public static class Data implements IServerDataProvider<TileEntity> {
        @Override
        public void appendServerData(CompoundNBT compoundNBT, ServerPlayerEntity serverPlayerEntity, World world, TileEntity te) {
            if (!(te instanceof IHeatExchanger)) return;

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
                        heatTag.putByte("side", (byte) face.ordinal());
                        heatTag.putInt("temp", (int) logic.getTemperature());
                        tagList.add(heatTag);
                    }
                }
                compoundNBT.put("heat", tagList);
            } else if (logic != null) {
                compoundNBT.putInt("temp", (int) logic.getTemperature());
            }
        }
    }

    public static class Component implements IComponentProvider {
        @Override
        public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
            CompoundNBT tag = accessor.getServerData();
            if (tag.contains("heat")) {
                ListNBT tagList = tag.getList("heat", Constants.NBT.TAG_COMPOUND);
                for (int i = 0; i < tagList.size(); i++) {
                    CompoundNBT heatTag = tagList.getCompound(i);
                    Direction face = Direction.byIndex(heatTag.getByte("side"));
                    tooltip.add(HeatUtil.formatHeatString(face, heatTag.getInt("temp")));
                }
            } else {
                tooltip.add(HeatUtil.formatHeatString(tag.getInt("temp")).applyTextStyle(Waila.COLOR));
            }
        }
    }
}
