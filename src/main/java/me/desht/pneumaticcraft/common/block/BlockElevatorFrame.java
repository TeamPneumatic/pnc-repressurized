package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorFrame;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
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
import java.util.List;

public class BlockElevatorFrame extends BlockPneumaticCraftModeled {
    public static final PropertyBool NE = PropertyBool.create("ne");
    public static final PropertyBool SE = PropertyBool.create("se");
    public static final PropertyBool SW = PropertyBool.create("sw");
    public static final PropertyBool NW = PropertyBool.create("nw");

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
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, NE, SW, SE, NW);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return super.getStateFromMeta(meta);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        boolean[] connected = getConnections(worldIn, pos);
        for (Corner corner : Corner.values()) {
            state = state.withProperty(corner.prop, connected[corner.ordinal()]);
        }
        return state;
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
            ItemStack playerStack = PneumaticCraftRepressurized.proxy.getClientPlayer().getHeldItemMainhand();
            if (playerStack.getItem() == Item.getItemFromBlock(this)) {
                // ensure a full bounding box for ease of placement of frames against frames
                return super.collisionRayTrace(state, world, pos, origin, direction);
            }
        }

        boolean isColliding = false;
        boolean[] connected = getConnections(world, pos);
        for (Corner corner : Corner.values()) {
            if (!connected[corner.ordinal()]) {
                setBlockBounds(corner.aabb);
                if (super.collisionRayTrace(state, world, pos, origin, direction) != null) {
                    isColliding = true;
                }
            }
        }

        setBlockBounds(FULL_BLOCK_AABB);
        return isColliding ? super.collisionRayTrace(state, world, pos, origin, direction) : null;
    }

    private boolean[] getConnections(IBlockAccess world, BlockPos pos) {
        boolean[] res = new boolean[4];

        boolean frameXPos = world.getBlockState(pos.east()).getBlock() == Blockss.ELEVATOR_FRAME;
        boolean frameXNeg = world.getBlockState(pos.west()).getBlock() == Blockss.ELEVATOR_FRAME;
        boolean frameZPos = world.getBlockState(pos.south()).getBlock() == Blockss.ELEVATOR_FRAME;
        boolean frameZNeg = world.getBlockState(pos.north()).getBlock() == Blockss.ELEVATOR_FRAME;

        res[Corner.SE.ordinal()]  = frameXPos || frameZPos;
        res[Corner.NE.ordinal()]  = frameXPos || frameZNeg;
        res[Corner.SW.ordinal()]  = frameXNeg || frameZPos;
        res[Corner.NW.ordinal()]  = frameXNeg || frameZNeg;

        return res;
    }


    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        boolean[] connected = getConnections(worldIn, pos);

        for (Corner corner : Corner.values()) {
            if (!connected[corner.ordinal()]) {
                addCollisionBoxToList(pos, entityBox, collidingBoxes, corner.aabb);
            }
        }

        float blockHeight = getElevatorBlockHeight(worldIn, pos);
        if (blockHeight > 0) {
            AxisAlignedBB aabb = new AxisAlignedBB(0, 0, 0, 1, blockHeight, 1);
            addCollisionBoxToList(pos, entityBox, collidingBoxes, aabb);
        }
    }

    @Override
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        TileEntityElevatorBase te = getElevatorTE(world, pos);
        if (te != null && te.oldExtension != te.extension) {
            entity.setPosition(entity.posX, te.getPos().getY() + 1 + te.extension, entity.posZ);
        }
        entity.fallDistance = 0;
    }

    static TileEntityElevatorBase getElevatorTE(IBlockAccess world, BlockPos pos) {
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

    private enum Corner {
        NE(1, -1, BlockElevatorFrame.NE, new AxisAlignedBB(14f / 16f, 0, 0, 1, 1, 2f/16f)),
        SE(1, 1, BlockElevatorFrame.SE, new AxisAlignedBB(14f / 16f, 0, 14f / 16f, 1, 1, 1)),
        SW(-1, 1, BlockElevatorFrame.SW, new AxisAlignedBB(0, 0, 14f / 16f, 2f / 16f, 1, 1)),
        NW(-1,-1, BlockElevatorFrame.NW, new AxisAlignedBB(0, 0, 0, 2f/16f, 1, 2f/16f));

        final int x;
        final int z;
        final PropertyBool prop;
        final AxisAlignedBB aabb;

        Corner(int x, int z, PropertyBool prop, AxisAlignedBB aabb) {
            this.x = x; this.z = z;
            this.prop = prop;
            this.aabb = aabb;
        }
    }
}
