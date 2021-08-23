package me.desht.pneumaticcraft.common.thirdparty.waila;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerDataProvider;
import me.desht.pneumaticcraft.common.block.BlockPressureTube;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.List;

public class TubeModuleProvider {
    public static class Data implements IServerDataProvider<TileEntity> {
        @Override
        public void appendServerData(CompoundNBT compoundNBT, ServerPlayerEntity player, World world, TileEntity te) {
            if (te instanceof TileEntityPressureTube) {
                TubeModule module = BlockPressureTube.getFocusedModule(world, te.getBlockPos(), player);
                if (module != null) {
                    compoundNBT.put("module", module.writeToNBT(new CompoundNBT()));
                    compoundNBT.putByte("side", (byte) module.getDirection().get3DDataValue());
                }
            }
        }
    }

    public static class Component implements IComponentProvider {
        @Override
        public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
            if (accessor.getTileEntity() instanceof TileEntityPressureTube) {
                TileEntityPressureTube tube = (TileEntityPressureTube) accessor.getTileEntity();
                CompoundNBT tubeTag = accessor.getServerData();
                if (tubeTag.contains("side", Constants.NBT.TAG_BYTE)) {
                    int side = tubeTag.getByte("side");
                    TubeModule module = tube.getModule(Direction.from3DDataValue(side));
                    if (module != null) {
                        module.readFromNBT(tubeTag.getCompound("module"));
                        module.addInfo(tooltip);
                    }
                }
            }
        }
    }
}
