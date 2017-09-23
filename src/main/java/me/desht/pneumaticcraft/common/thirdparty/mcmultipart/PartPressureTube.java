package me.desht.pneumaticcraft.common.thirdparty.mcmultipart;

import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.slot.EnumCenterSlot;
import mcmultipart.api.slot.IPartSlot;
import me.desht.pneumaticcraft.common.block.BlockPressureTube;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class PartPressureTube implements IMultipart {
    private BlockPressureTube block;

    public PartPressureTube(BlockPressureTube block) {
        this.block = block;
    }

    @Override
    public IPartSlot getSlotForPlacement(World world, BlockPos pos, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ, EntityLivingBase placer) {
        return EnumCenterSlot.CENTER;
    }

    @Override
    public IPartSlot getSlotFromWorld(IBlockAccess world, BlockPos pos, IBlockState state) {
        return EnumCenterSlot.CENTER;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public List<AxisAlignedBB> getOcclusionBoxes(IPartInfo part) {
        List<AxisAlignedBB> boxes = new ArrayList<>();

        boxes.add(BlockPressureTube.BASE_BOUNDS);
//        boxes.addAll()
        return boxes;
    }

    @Override
    public void onPartChanged(IPartInfo part, IPartInfo otherPart) {
        // ...
    }
}
