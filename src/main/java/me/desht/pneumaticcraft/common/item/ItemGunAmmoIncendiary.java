package me.desht.pneumaticcraft.common.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;

public class ItemGunAmmoIncendiary extends ItemGunAmmo {
    public ItemGunAmmoIncendiary() {
        super("gun_ammo_incendiary");
    }

    @Override
    protected int getCartridgeSize() {
        return 500;
    }

    @Override
    public int getAmmoColor(ItemStack ammo) {
        return 0x00FF8000;
    }

    @Override
    public void onTargetHit(ItemStack ammo, EntityPlayer shooter, Entity target) {
        target.setFire(8);
        super.onTargetHit(ammo, shooter, target);
    }

    @Override
    public void onBlockHit(ItemStack ammo, EntityPlayer player, BlockPos pos, EnumFacing face) {
        if (player.world.rand.nextInt(5) == 0) {
            BlockSnapshot snapshot = BlockSnapshot.getBlockSnapshot(player.world, pos.offset(face));
            BlockEvent.PlaceEvent event = ForgeEventFactory.onPlayerBlockPlace(player, snapshot, face, EnumHand.MAIN_HAND);
            if (!event.isCanceled()) {
                player.world.setBlockState(pos.offset(face), Blocks.FIRE.getDefaultState());
            }
        }
        super.onBlockHit(ammo, player, pos, face);
    }
}
