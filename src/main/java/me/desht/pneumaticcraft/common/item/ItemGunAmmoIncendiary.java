package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.minigun.Minigun;
import net.minecraft.entity.Entity;
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
    public int onTargetHit(Minigun minigun, ItemStack ammo, Entity target) {
        target.setFire(8);
        super.onTargetHit(minigun, ammo, target);
        return 0;
    }

    @Override
    public int onBlockHit(Minigun minigun, ItemStack ammo, BlockPos pos, EnumFacing face) {
        if (minigun.getWorld().rand.nextInt(5) == 0) {
            BlockSnapshot snapshot = BlockSnapshot.getBlockSnapshot(minigun.getWorld(), pos.offset(face));
            BlockEvent.PlaceEvent event = ForgeEventFactory.onPlayerBlockPlace(minigun.getPlayer(), snapshot, face, EnumHand.MAIN_HAND);
            if (!event.isCanceled()) {
                minigun.getWorld().setBlockState(pos.offset(face), Blocks.FIRE.getDefaultState());
            }
        }
        super.onBlockHit(minigun, ammo, pos, face);
        return 0;
    }
}
