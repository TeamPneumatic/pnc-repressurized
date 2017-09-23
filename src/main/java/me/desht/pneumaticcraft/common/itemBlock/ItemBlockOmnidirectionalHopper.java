package me.desht.pneumaticcraft.common.itemBlock;

import me.desht.pneumaticcraft.common.tileentity.TileEntityOmnidirectionalHopper;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemBlockOmnidirectionalHopper extends ItemBlockPneumaticCraft {

    public ItemBlockOmnidirectionalHopper(Block block) {
        super(block);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        EnumActionResult result = super.onItemUse(playerIn, worldIn, pos, hand, side, hitX, hitY, hitZ);
        if (result == EnumActionResult.SUCCESS) {
            TileEntity te = worldIn.getTileEntity(pos.offset(side));
            if (te instanceof TileEntityOmnidirectionalHopper) {
                ((TileEntityOmnidirectionalHopper) te).setRotation(side.getOpposite());
            }
        }
        return result;
    }

}
