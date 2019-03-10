package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.semiblock.IDirectionalSemiblock;
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

import java.util.List;

public class ItemLogisticsConfigurator extends ItemPressurizable {

    ItemLogisticsConfigurator() {
        super("logistics_configurator", PneumaticValues.AIR_CANISTER_MAX_AIR, PneumaticValues.AIR_CANISTER_VOLUME);
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote && stack.getMaxDamage() - stack.getItemDamage() >= 100) {
            List<ISemiBlock> semiBlocks = SemiBlockManager.getInstance(world).getSemiBlocksAsList(world, pos);
            
            if(semiBlocks.isEmpty()){
                pos = pos.offset(side);
                semiBlocks = SemiBlockManager.getInstance(world).getSemiBlocksAsList(world, pos);
            }

            if (!semiBlocks.isEmpty()) {
                if (player.isSneaking()) {
                    for (ISemiBlock s : semiBlocks) {
                        if (!(s instanceof IDirectionalSemiblock) || ((IDirectionalSemiblock) s).getFacing() == side) {
                            SemiBlockManager.getInstance(world).breakSemiBlock(s, player);
                        }
                    }
                    return EnumActionResult.SUCCESS;
                } else {
                    //TODO raytrace?
                    if (semiBlocks.stream().anyMatch(s -> s.onRightClickWithConfigurator(player, side))) {
                        addAir(stack, -PneumaticValues.USAGE_LOGISTICS_CONFIGURATOR);
                        return EnumActionResult.SUCCESS;
                    }
                }
            }
        } else if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }
}
