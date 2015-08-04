package pneumaticCraft.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import pneumaticCraft.common.semiblock.ISemiBlock;
import pneumaticCraft.common.semiblock.SemiBlockManager;

public class ItemLogisticsConfigurator extends ItemPressurizable{

    public ItemLogisticsConfigurator(String textureLocation, int maxAir, int volume){
        super(textureLocation, maxAir, volume);
    }

    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitVecX, float hitVecY, float hitVecZ){
        if(!world.isRemote && getMaxDamage() - stack.getItemDamage() >= 100) {
            ISemiBlock semiBlock = SemiBlockManager.getInstance(world).getSemiBlock(world, x, y, z);
            if(semiBlock != null) {
                if(player.isSneaking()) {
                    SemiBlockManager.getInstance(world).breakSemiBlock(world, x, y, z, player);
                    return true;
                } else {
                    if(semiBlock.onRightClickWithConfigurator(player)) {
                        addAir(stack, -100);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
