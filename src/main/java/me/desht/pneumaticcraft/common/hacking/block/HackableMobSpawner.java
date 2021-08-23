package me.desht.pneumaticcraft.common.hacking.block;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.spawner.AbstractSpawner;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class HackableMobSpawner implements IHackableBlock {
    @Override
    public ResourceLocation getHackableId() {
        return RL("mob_spawner");
    }

    @Override
    public boolean canHack(IBlockReader world, BlockPos pos, PlayerEntity player) {
        return !isHacked(world, pos);
    }

    public static boolean isHacked(IBlockReader world, BlockPos pos) {
        TileEntity te = world.getBlockEntity(pos);
        return te instanceof MobSpawnerTileEntity && ((MobSpawnerTileEntity) te).getSpawner().requiredPlayerRange == 0;
    }

    @Override
    public void addInfo(IBlockReader world, BlockPos pos, List<ITextComponent> curInfo, PlayerEntity player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.result.neutralize"));
    }

    @Override
    public void addPostHackInfo(IBlockReader world, BlockPos pos, List<ITextComponent> curInfo, PlayerEntity player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.finished.neutralized"));
    }

    @Override
    public int getHackTime(IBlockReader world, BlockPos pos, PlayerEntity player) {
        return 200;
    }

    @Override
    public void onHackComplete(World world, BlockPos pos, PlayerEntity player) {
        if (!world.isClientSide) {
            CompoundNBT tag = new CompoundNBT();
            TileEntity te = world.getBlockEntity(pos);
            if (te != null) {
                te.save(tag);
                tag.putShort("RequiredPlayerRange", (short) 0);
                te.load(te.getBlockState(), tag);
                BlockState state = world.getBlockState(pos);
                world.sendBlockUpdated(pos, state, state, 3);
            }
        }

    }

    @Override
    public boolean afterHackTick(IBlockReader world, BlockPos pos) {
        AbstractSpawner spawner = ((MobSpawnerTileEntity) world.getBlockEntity(pos)).getSpawner();
        spawner.oSpin = spawner.spin;
        spawner.spawnDelay = 10;
        return false;
    }
}
