package me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IBlockTrackEntry;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;

import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class BlockTrackEntryEnergy implements IBlockTrackEntry {
    private static final ResourceLocation ID = RL("block_tracker.module.energy");

    @Override
    public boolean shouldTrackWithThisEntry(IBlockReader world, BlockPos pos, BlockState state, TileEntity te) {
        return te != null
                && !TrackerBlacklistManager.isEnergyBlacklisted(te)
                && IBlockTrackEntry.hasCapabilityOnAnyFace(te, CapabilityEnergy.ENERGY);
    }

    @Override
    public List<BlockPos> getServerUpdatePositions(TileEntity te) {
        return Collections.singletonList(te.getBlockPos());
    }

    @Override
    public int spamThreshold() {
        return 8;
    }

    @Override
    public void addInformation(World world, BlockPos pos, TileEntity te, Direction face, List<ITextComponent> infoList) {
        try {
            infoList.add(xlate("pneumaticcraft.blockTracker.info.rf"));
            // FIXME: getting capabilities client-side is not a reliable way to do this
            // Need a more formal framework for sync'ing server-side data to the client
            te.getCapability(CapabilityEnergy.ENERGY, face)
                    .ifPresent(storage -> infoList.add(new StringTextComponent(storage.getEnergyStored() + " / " + storage.getMaxEnergyStored() + " RF")));
        } catch (Throwable e) {
            TrackerBlacklistManager.addEnergyTEToBlacklist(te, e);
        }
    }

    @Override
    public ResourceLocation getEntryID() {
        return ID;
    }
}
