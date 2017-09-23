package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.block.IPneumaticWrenchable;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.thirdparty.ModInteractionUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Sounds;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

//TODO Buildcraft dep @Optional.Interface(iface = "buildcraft.api.tools.IToolWrench", modid = ModIds.BUILDCRAFT)
public class ItemPneumaticWrench extends ItemPressurizable /*implements IToolWrench*/ {

    public ItemPneumaticWrench() {
        super("pneumatic_wrench", PneumaticValues.PNEUMATIC_WRENCH_MAX_AIR, PneumaticValues.PNEUMATIC_WRENCH_VOLUME);
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            IPneumaticWrenchable wrenchable;
            if (block instanceof IPneumaticWrenchable) {
                wrenchable = (IPneumaticWrenchable) block;
            } else {
                wrenchable = ModInteractionUtils.getInstance().getWrenchable(world.getTileEntity(pos));
            }
            if (wrenchable != null && ((ItemPneumaticWrench) Itemss.PNEUMATIC_WRENCH).getPressure(stack) > 0) {
                if (wrenchable.rotateBlock(world, player, pos, side)) {
                    if (!player.capabilities.isCreativeMode)
                        ((ItemPneumaticWrench) Itemss.PNEUMATIC_WRENCH).addAir(stack, -PneumaticValues.USAGE_PNEUMATIC_WRENCH);
                    NetworkHandler.sendToAllAround(new PacketPlaySound(Sounds.PNEUMATIC_WRENCH, SoundCategory.PLAYERS, pos, 1.0F, 1.0F, false), world);
                    return EnumActionResult.SUCCESS;
                }
            } else {
                //rotating normal blocks doesn't cost energy.
                if (block.rotateBlock(world, pos, side)) {
                    NetworkHandler.sendToAllAround(new PacketPlaySound(Sounds.PNEUMATIC_WRENCH, SoundCategory.PLAYERS, pos, 1.0F, 1.0F, false), world);
                    return EnumActionResult.SUCCESS;
                }
            }
            return EnumActionResult.PASS;
        } else {
            return EnumActionResult.PASS;
        }
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack iStack, EntityPlayer player, EntityLivingBase target, EnumHand hand) {
        if (!player.world.isRemote) {
            if (target.isEntityAlive() && target instanceof IPneumaticWrenchable && ((ItemPneumaticWrench) Itemss.PNEUMATIC_WRENCH).getPressure(iStack) > 0) {
                if (((IPneumaticWrenchable) target).rotateBlock(target.world, player, null, null)) {
                    if (!player.capabilities.isCreativeMode)
                        ((ItemPneumaticWrench) Itemss.PNEUMATIC_WRENCH).addAir(iStack, -PneumaticValues.USAGE_PNEUMATIC_WRENCH);
                    NetworkHandler.sendToAllAround(new PacketPlaySound(Sounds.PNEUMATIC_WRENCH, SoundCategory.PLAYERS, target.posX, target.posY, target.posZ, 1.0F, 1.0F, false), target.world);
                    return true;
                }
            }
        }
        return false;
    }

    /* @Override
     public boolean canWrench(EntityPlayer player, int x, int y, int z){
         return true;
     }

     @Override
     public void wrenchUsed(EntityPlayer player, int x, int y, int z){}*/
}
