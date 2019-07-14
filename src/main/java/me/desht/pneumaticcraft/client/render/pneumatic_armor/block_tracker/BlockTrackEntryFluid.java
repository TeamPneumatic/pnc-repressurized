package me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.FluidTrackEvent;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IBlockTrackEntry;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import java.util.Collections;
import java.util.List;

public class BlockTrackEntryFluid implements IBlockTrackEntry {
    @Override
    public boolean shouldTrackWithThisEntry(IBlockReader world, BlockPos pos, BlockState state, TileEntity te) {
        return te != null
                && !TrackerBlacklistManager.isFluidBlacklisted(te)
                && IBlockTrackEntry.hasCapabilityOnAnyFace(te, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
                && !MinecraftForge.EVENT_BUS.post(new FluidTrackEvent(te));
    }

    @Override
    public List<BlockPos> getServerUpdatePositions(TileEntity te) {
        return Collections.singletonList(te.getPos());
    }

    @Override
    public int spamThreshold() {
        return 10;
    }

    @Override
    public void addInformation(World world, BlockPos pos, TileEntity te, Direction face, List<String> infoList) {
        try {
            te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face).ifPresent(handler -> {
                int i = 1;
                for (IFluidTankProperties tank : handler.getTankProperties()) {
                    FluidStack stack = tank.getContents();
                    if (stack != null) {
                        infoList.add(I18n.format("blockTracker.info.fluids.tankFull", i, stack.amount, tank.getCapacity(), stack.getLocalizedName()));
                    } else {
                        infoList.add(I18n.format("blockTracker.info.fluids.tankEmpty", i, tank.getCapacity()));
                    }
                    i++;
                }
            });
        } catch (Throwable e) {
            TrackerBlacklistManager.addFluidTEToBlacklist(te, e);
        }
    }

    @Override
    public String getEntryName() {
        return "blockTracker.module.fluids";
    }
}
