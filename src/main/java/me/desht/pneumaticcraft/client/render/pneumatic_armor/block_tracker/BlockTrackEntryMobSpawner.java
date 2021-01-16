package me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IBlockTrackEntry;
import me.desht.pneumaticcraft.common.entity.semiblock.EntitySpawnerAgitator;
import me.desht.pneumaticcraft.common.hacking.block.HackableMobSpawner;
import me.desht.pneumaticcraft.common.semiblock.SemiblockTracker;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.Reflections;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.spawner.AbstractSpawner;

import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class BlockTrackEntryMobSpawner implements IBlockTrackEntry {

    @Override
    public boolean shouldTrackWithThisEntry(IBlockReader world, BlockPos pos, BlockState state, TileEntity te) {
        return state.getBlock() == Blocks.SPAWNER;
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
    public void addInformation(World world, BlockPos pos, TileEntity te, Direction face, List<ITextComponent> infoList) {
        // FIXME translations
        if (te instanceof MobSpawnerTileEntity) {
            AbstractSpawner spawner = ((MobSpawnerTileEntity) te).getSpawnerBaseLogic();
            Entity e = spawner.getCachedEntity();
            if (e == null) {
                // seems to happen with enderman spawners, possibly related to EndermanEntity#readAdditional() doing a bad world cast
                // certainly spams a lot a vanilla-related errors
                infoList.add(new StringTextComponent("<ERROR> Missing entity?"));
                return;
            }
            infoList.add(xlate("pneumaticcraft.blockTracker.info.spawner.type", e.getName().getString()));
            if (Reflections.isActivated(spawner) || hasAgitator(world, pos)) {
                infoList.add(xlate("pneumaticcraft.blockTracker.info.spawner.time",
                        PneumaticCraftUtils.convertTicksToMinutesAndSeconds(spawner.spawnDelay, false)));
            } else if (HackableMobSpawner.isHacked(world, pos)) {
                infoList.add(xlate("pneumaticcraft.blockTracker.info.spawner.hacked"));
            } else {
                infoList.add(xlate("pneumaticcraft.blockTracker.info.spawner.standby"));
            }
        }
    }

    private boolean hasAgitator(World world, BlockPos pos) {
        return SemiblockTracker.getInstance().getSemiblock(world, pos) instanceof EntitySpawnerAgitator;
    }

    @Override
    public ResourceLocation getEntryID() {
        return RL("block_tracker.module.spawner");
    }
}
