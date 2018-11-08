package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.config.ConfigHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class ItemGunAmmoExplosive extends ItemGunAmmo {
    private static final float EXPLOSION_STRENGTH = 1.5f;

    public ItemGunAmmoExplosive() {
        super("gun_ammo_explosive");
    }

    @Override
    protected int getCartridgeSize() {
        return 125;
    }

    @Override
    public int getAmmoColor(ItemStack ammo) {
        return 0x00FF0000;
    }

    @Override
    public void onTargetHit(ItemStack ammo, EntityPlayer shooter, Entity target) {
        super.onTargetHit(ammo, shooter, target);
        shooter.world.createExplosion(null, target.posX, target.posY, target.posZ, EXPLOSION_STRENGTH, ConfigHandler.general.minigunExplosiveAmmoTerrainDamage);
    }

    @Override
    public void onBlockHit(ItemStack ammo, EntityPlayer player, BlockPos pos, EnumFacing face) {
        super.onBlockHit(ammo, player, pos, face);
        BlockPos pos2 = pos.offset(face);
        player.world.createExplosion(null, pos2.getX() + 0.5, pos2.getY() + 0.5, pos2.getZ() + 0.5, EXPLOSION_STRENGTH, ConfigHandler.general.minigunExplosiveAmmoTerrainDamage);
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> infoList, ITooltipFlag extraInfo) {
        super.addInformation(stack, world, infoList, extraInfo);
        if (ConfigHandler.general.minigunExplosiveAmmoTerrainDamage) {
            infoList.add(I18n.format("gui.tooltip.item.gun_ammo_explosive.terrainWarning"));
        }
    }
}
