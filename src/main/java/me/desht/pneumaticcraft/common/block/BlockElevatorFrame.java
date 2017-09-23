package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorFrame;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockElevatorFrame extends BlockPneumaticCraftModeled {

    public BlockElevatorFrame() {
        super(Material.IRON, "elevator_frame");
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        TileEntityElevatorBase elevatorBase = getElevatorTE(world, pos);
        if (elevatorBase != null) {
            elevatorBase.updateMaxElevatorHeight();
        }
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityElevatorFrame.class;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return super.getBoundingBox(state, source, pos);
    }

    @Override
    public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d origin, Vec3d direction) {
        if (world.isRemote) {
            ItemStack playerStack = PneumaticCraftRepressurized.proxy.getPlayer().getHeldItemMainhand();
            if (playerStack.getItem() == Item.getItemFromBlock(this)) {
                return super.collisionRayTrace(state, world, pos, origin, direction);
            }
        }
        boolean frameXPos = world.getBlockState(pos.add(1, 0, 0)).getBlock() == Blockss.ELEVATOR_FRAME;
        boolean frameXNeg = world.getBlockState(pos.add(-1, 0, 0)).getBlock() == Blockss.ELEVATOR_FRAME;
        boolean frameZPos = world.getBlockState(pos.add(0, 0, 1)).getBlock() == Blockss.ELEVATOR_FRAME;
        boolean frameZNeg = world.getBlockState(pos.add(0, 0, -1)).getBlock() == Blockss.ELEVATOR_FRAME;

        boolean isColliding = false;

        if (!frameXNeg && !frameZNeg) {
            setBlockBounds(new AxisAlignedBB(0, 0, 0, 2 / 16F, 1, 2 / 16F));
            if (super.collisionRayTrace(state, world, pos, origin, direction) != null) isColliding = true;
        }
        if (!frameXNeg && !frameZPos) {
            setBlockBounds(new AxisAlignedBB(0, 0, 14 / 16F, 2 / 16F, 1, 1));
            if (super.collisionRayTrace(state, world, pos, origin, direction) != null) isColliding = true;
        }
        if (!frameXPos && !frameZPos) {
            setBlockBounds(new AxisAlignedBB(14 / 16F, 0, 14 / 16F, 1, 1, 1));
            if (super.collisionRayTrace(state, world, pos, origin, direction) != null) isColliding = true;
        }
        if (!frameXPos && !frameZNeg) {
            setBlockBounds(new AxisAlignedBB(14 / 16F, 0, 0, 1, 1, 2 / 16F));
            if (super.collisionRayTrace(state, world, pos, origin, direction) != null) isColliding = true;
        }

        setBlockBounds(FULL_BLOCK_AABB);
        return isColliding ? super.collisionRayTrace(state, world, pos, origin, direction) : null;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        float blockHeight = getElevatorBlockHeight(worldIn, pos);
        if (blockHeight > 0F) {
            // this.setBlockBounds(0, 0, 0, 1, blockHeight, 1);
            // return super.getCollisionBoundingBoxFromPool(par1World, par2,
            // par3, par4);
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            return new AxisAlignedBB(x, y, z, x + 1, y + blockHeight, z + 1);
        } else {
            return null;
        }
    }

    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
        //  float blockHeight = getElevatorBlockHeight(world, x, y, z);
        //   if(blockHeight > 0) {
        // if(entity.posY < y + blockHeight) {
        //     entity.setPosition(entity.posX, y + blockHeight + 2, entity.posZ);
        TileEntityElevatorBase te = getElevatorTE(world, pos);
        if (te != null && te.oldExtension != te.extension) {
            entity.setPosition(entity.posX, te.getPos().getY() + te.extension + entity.getYOffset() + entity.height + 1, entity.posZ);
        }
        entity.fallDistance = 0;
        //}
        //   }
    }

    public static TileEntityElevatorBase getElevatorTE(IBlockAccess world, BlockPos pos) {
        while (true) {
            pos = pos.offset(EnumFacing.DOWN);
            if (world.getBlockState(pos).getBlock() == Blockss.ELEVATOR_BASE) break;
            if (world.getBlockState(pos).getBlock() != Blockss.ELEVATOR_FRAME || pos.getY() <= 0) return null;
        }
        return (TileEntityElevatorBase) world.getTileEntity(pos);
    }

    private float getElevatorBlockHeight(IBlockAccess world, BlockPos pos) {
        TileEntityElevatorBase te = getElevatorTE(world, pos);
        if (te == null) return 0F;
        float blockHeight = te.extension - (pos.getY() - te.getPos().getY()) + 1;
        // System.out.println("blockHeight (" + x + ", " + y + ", " + z + "): " + blockHeight);
        // + blockHeight);
        if (blockHeight < 0F) return 0F;
        if (blockHeight > 1F) return 1F;
        return blockHeight;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntityElevatorBase elevatorBase = getElevatorTE(world, pos);
        if (elevatorBase != null) {
            elevatorBase.updateMaxElevatorHeight();
        }
        super.breakBlock(world, pos, state);
    }
}
