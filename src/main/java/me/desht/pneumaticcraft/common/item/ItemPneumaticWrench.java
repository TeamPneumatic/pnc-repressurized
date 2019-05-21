package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.block.IPneumaticWrenchable;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
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

public class ItemPneumaticWrench extends ItemPressurizable {

    public ItemPneumaticWrench() {
        super("pneumatic_wrench", PneumaticValues.PNEUMATIC_WRENCH_MAX_AIR, PneumaticValues.PNEUMATIC_WRENCH_VOLUME);
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            boolean didWork = true;
            float pressure = getPressure(stack);
            IPneumaticWrenchable wrenchable = IPneumaticWrenchable.forBlock(block);
            if (wrenchable != null && pressure > 0) {
                if (wrenchable.rotateBlock(world, player, pos, side, hand) && !player.capabilities.isCreativeMode) {
                    addAir(stack, -PneumaticValues.USAGE_PNEUMATIC_WRENCH);
                }
            } else {
                // rotating normal blocks doesn't use pressure
                didWork = block.rotateBlock(world, pos, side);
            }
            if (didWork) playWrenchSound(world, pos);
            return didWork ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
        } else {
            // client-side: prevent GUI's opening etc.
            return EnumActionResult.SUCCESS;
        }
    }

    private void playWrenchSound(World world, BlockPos pos) {
        NetworkHandler.sendToAllAround(new PacketPlaySound(Sounds.PNEUMATIC_WRENCH, SoundCategory.PLAYERS, pos, 1.0F, 1.0F, false), world);
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack iStack, EntityPlayer player, EntityLivingBase target, EnumHand hand) {
        if (!player.world.isRemote) {
            if (target.isEntityAlive() && target instanceof IPneumaticWrenchable && getPressure(iStack) > 0) {
                if (((IPneumaticWrenchable) target).rotateBlock(target.world, player, null, null, hand)) {
                    if (!player.capabilities.isCreativeMode) {
                        addAir(iStack, -PneumaticValues.USAGE_PNEUMATIC_WRENCH);
                    }
                    NetworkHandler.sendToAllAround(new PacketPlaySound(Sounds.PNEUMATIC_WRENCH, SoundCategory.PLAYERS, target.posX, target.posY, target.posZ, 1.0F, 1.0F, false), target.world);
                    return true;
                }
            }
        }
        return false;
    }
}
