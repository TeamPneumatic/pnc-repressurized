package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemLogisticsConfigurator extends ItemPressurizable {

    ItemLogisticsConfigurator() {
        super("logistics_configurator", PneumaticValues.AIR_CANISTER_MAX_AIR, PneumaticValues.AIR_CANISTER_VOLUME);
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote && stack.getMaxDamage() - stack.getItemDamage() >= 100) {
            ISemiBlock semiBlock = SemiBlockManager.getInstance(world).getSemiBlock(world, pos);
            if (semiBlock != null) {
                if (player.isSneaking()) {
                    SemiBlockManager.getInstance(world).breakSemiBlock(world, pos, player);
                    return EnumActionResult.SUCCESS;
                } else {
                    if (semiBlock.onRightClickWithConfigurator(player)) {
                        addAir(stack, -100);
                        return EnumActionResult.SUCCESS;
                    }
                }
            }
        }
        return EnumActionResult.PASS;
    }
}
