package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemLingeringPotion;
import net.minecraft.item.ItemSplashPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ItemGunAmmoStandard extends ItemGunAmmo {

    private static final String NBT_POTION = "potion";

    public ItemGunAmmoStandard() {
        super("gun_ammo");
    }

    @Override
    protected int getCartridgeSize() {
        return ConfigHandler.minigun.standardAmmoCartridgeSize;
    }

    @Nonnull
    private static ItemStack getPotion(ItemStack ammo) {
        if (ammo.getTagCompound() != null && ammo.getTagCompound().hasKey(NBT_POTION)) {
            return new ItemStack(ammo.getTagCompound().getCompoundTag(NBT_POTION));
        } else {
            return ItemStack.EMPTY;
        }
    }

    public static void setPotion(ItemStack ammo, ItemStack potion) {
        NBTTagCompound tag = new NBTTagCompound();
        potion.writeToNBT(tag);
        NBTUtil.setCompoundTag(ammo, "potion", tag);
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {
        if (this.isInCreativeTab(tab)) {
            super.getSubItems(tab, list);
            NonNullList<ItemStack> potions = NonNullList.create();
            Items.POTIONITEM.getSubItems(tab, potions);
            for (ItemStack potion : potions) {
                ItemStack ammo = new ItemStack(this);
                setPotion(ammo, potion);
                list.add(ammo);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey(NBT_POTION);
    }

    @Override
    public int getAmmoCost(ItemStack ammoStack) {
        ItemStack potion = getPotion(ammoStack);
        return potion.isEmpty() ? 1 : getPotionAmmoCost(potion.getItem());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getAmmoColor(ItemStack ammo) {
        ItemStack potion = getPotion(ammo);
        return potion.isEmpty() ? 0x00FFFF00 : Minecraft.getMinecraft().getItemColors().colorMultiplier(potion, 0);
    }

    @Override
    public float getAirUsageMultiplier(Minigun minigun, ItemStack ammoStack) {
        if (minigun.getUpgrades(IItemRegistry.EnumUpgrade.DISPENSER) > 0 && !getPotion(ammoStack).isEmpty()) {
            return minigun.getUpgrades(IItemRegistry.EnumUpgrade.DISPENSER) + 1f;
        } else {
            return 1f;
        }
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> infoList, ITooltipFlag extraInfo) {
        super.addInformation(stack, world, infoList, extraInfo);
        ItemStack potion = getPotion(stack);
        if (!potion.isEmpty()) {
            List<String> potionInfo = new ArrayList<>();
            potion.getItem().addInformation(potion, world, potionInfo, extraInfo);
            String extra = "";
            if (potion.getItem() instanceof ItemSplashPotion) {
                extra = " " + I18n.format("gui.tooltip.gunAmmo.splash");
            } else if (potion.getItem() instanceof ItemLingeringPotion) {
                extra = " " + I18n.format("gui.tooltip.gunAmmo.lingering");
            }
            infoList.add(I18n.format("gui.tooltip.gunAmmo") + " " + potionInfo.get(0) + extra);
        } else {
            infoList.add(I18n.format("gui.tooltip.gunAmmo.combineWithPotion"));
        }
    }

    @Override
    public int onTargetHit(Minigun minigun, ItemStack ammo, Entity target) {
        ItemStack potion = getPotion(ammo);
        if (!potion.isEmpty() && target instanceof EntityLivingBase) {
            EntityPlayer shooter = minigun.getPlayer();
            if (minigun.dispenserWeightedPercentage(ConfigHandler.minigun.potionProcChance, 0.25f)) {
                if (potion.getItem() == Items.POTIONITEM) {
                    List<PotionEffect> effects = PotionUtils.getEffectsFromStack(potion);
                    for (PotionEffect effect : effects) {
                        ((EntityLivingBase) target).addPotionEffect(new PotionEffect(effect));
                    }
                    NetworkHandler.sendToAllAround(new PacketPlaySound(SoundEvents.ENTITY_SPLASH_POTION_BREAK, SoundCategory.PLAYERS,
                            target.posX, target.posY, target.posZ, 1.0f, 1.0f, true), target.world);
                } else if (potion.getItem() == Items.SPLASH_POTION || potion.getItem() == Items.LINGERING_POTION) {
                    EntityPotion entityPotion = new EntityPotion(shooter.world, shooter, potion);
                    entityPotion.setPosition(target.posX, target.posY, target.posZ);
                    shooter.world.spawnEntity(entityPotion);
                }
            }
            return getPotionAmmoCost(potion.getItem());
        } else {
            return super.onTargetHit(minigun, ammo, target);
        }
    }

    @Override
    public int onBlockHit(Minigun minigun, ItemStack ammo, BlockPos pos, EnumFacing face, Vec3d hitVec) {
        ItemStack potion = getPotion(ammo);
        if (potion.getItem() == Items.SPLASH_POTION || potion.getItem() == Items.LINGERING_POTION) {
            EntityPlayer shooter = minigun.getPlayer();
            int chance = ConfigHandler.minigun.potionProcChance + minigun.getUpgrades(IItemRegistry.EnumUpgrade.DISPENSER) * 2;
            if (shooter.world.rand.nextInt(100) < chance) {
                EntityPotion entityPotion = new EntityPotion(shooter.world, shooter, potion);
                BlockPos pos2 = pos.offset(face);
                entityPotion.setPosition(pos2.getX() + 0.5, pos2.getY() + 0.5, pos2.getZ() + 0.5);
                shooter.world.spawnEntity(entityPotion);
            }
            return getPotionAmmoCost(potion.getItem());
        } else {
            return super.onBlockHit(minigun, ammo, pos, face, hitVec);
        }
    }

    private static int getPotionAmmoCost(Item item) {
        if (item == Items.LINGERING_POTION) {
            return 6;
        } else if (item == Items.SPLASH_POTION) {
            return 3;
        } else if (item == Items.POTIONITEM) {
            return 1;
        } else {
            throw new IllegalArgumentException("Item " + item + " is not a potion!");
        }
    }
}
