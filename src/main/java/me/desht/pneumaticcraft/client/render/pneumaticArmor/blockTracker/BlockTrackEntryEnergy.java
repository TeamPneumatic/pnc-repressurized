package me.desht.pneumaticcraft.client.render.pneumaticArmor.blockTracker;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IBlockTrackEntry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;

import java.util.List;

public class BlockTrackEntryEnergy implements IBlockTrackEntry {
    @Override
    public boolean shouldTrackWithThisEntry(IBlockAccess world, BlockPos pos, IBlockState state, TileEntity te) {
        if (te == null) return false;
        if (te.hasCapability(CapabilityEnergy.ENERGY, null)) {
            return true;
        }
        for (EnumFacing face: EnumFacing.values()) {
            if (te.hasCapability(CapabilityEnergy.ENERGY, face)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldBeUpdatedFromServer(TileEntity te) {
        return false;
    }

    @Override
    public int spamThreshold() {
        return 8;
    }

    @Override
    public void addInformation(World world, BlockPos pos, TileEntity te, List<String> infoList) {
        infoList.add("blockTracker.info.rf");
    }

    @Override
    public String getEntryName() {
        return "blockTracker.module.rf";
    }
}
