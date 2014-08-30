package pneumaticCraft.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import pneumaticCraft.common.entity.projectile.EntityVortex;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Sounds;

public class ItemVortexCannon extends ItemPressurizable{

    public ItemVortexCannon(String textureLocation){
        super(textureLocation, PneumaticValues.VORTEX_CANNON_MAX_AIR, PneumaticValues.VORTEX_CANNON_VOLUME);
    }

    /**
     * Called whenever this item is equipped and the right mouse button is
     * pressed. Args: itemStack, world, entityPlayer
     */
    @Override
    public ItemStack onItemRightClick(ItemStack iStack, World world, EntityPlayer player){
        if(iStack.getItemDamage() < getMaxDamage()) {
            double factor = 0.2D * getPressure(iStack);
            world.playSoundAtEntity(player, Sounds.CANNON_SOUND, 1.0F, 0.7F + (float)factor * 0.2F /* 1.0F */);
            EntityVortex vortex = new EntityVortex(world, player);
            vortex.motionX *= factor;
            vortex.motionY *= factor;
            vortex.motionZ *= factor;
            if(!world.isRemote) world.spawnEntityInWorld(vortex);

            iStack.setItemDamage(iStack.getItemDamage() + PneumaticValues.USAGE_VORTEX_CANNON);
            if(iStack.getItemDamage() > getMaxDamage()) {
                iStack.setItemDamage(getMaxDamage());
            }
        }

        return iStack;
    }
}
