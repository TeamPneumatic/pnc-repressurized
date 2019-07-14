package me.desht.pneumaticcraft.api.client.pneumatic_helmet;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired when a helmet Block Tracker is about to track a block. Can be canceled to prevent tracking.
 * Posted on MinecraftForge.EVENT_BUS
 *
 * @author MineMaarten
 */
@Cancelable
public class BlockTrackEvent extends Event {

    public final World world;
    public final BlockPos pos;
    public final TileEntity te;

    public BlockTrackEvent(World world, BlockPos pos, TileEntity te) {
        this.world = world;
        this.pos = pos;
        this.te = te;
    }

}
