package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.NBTUtil;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class ItemMinigun extends ItemPressurizable {

    private final Minigun minigun = new MinigunItem();

    public ItemMinigun() {
        super("minigun", PneumaticValues.AIR_CANISTER_MAX_AIR, PneumaticValues.AIR_CANISTER_VOLUME);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    /* public EnumAction getItemUseAction(ItemStack p_77661_1_)
     {
         return EnumAction.bow;
     }*/
    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean currentItem) {
        super.onUpdate(stack, world, entity, slot, currentItem);
        EntityPlayer player = (EntityPlayer) entity;
        getMinigun(stack, player);
        if (!currentItem) {
            minigun.setMinigunSoundCounter(-1);
            minigun.setMinigunSpeed(0);
            minigun.setMinigunActivated(false);
            minigun.setMinigunTriggerTimeOut(0);
        }
        minigun.update(player.posX, player.posY, player.posZ);
        if (world.isRemote && currentItem && minigun.getMinigunSpeed() > 0) {
            suppressSwitchAnimation();
        }
    }

    @SideOnly(Side.CLIENT)
    private void suppressSwitchAnimation() {
        Minecraft mc = Minecraft.getMinecraft();
        ItemRenderer renderer = mc.entityRenderer.itemRenderer;
        renderer.updateEquippedItem();
//        renderer.equippedProgress = 1;
//        renderer.prevEquippedProgress = 1;

    }

    public Minigun getMinigun(ItemStack stack, EntityPlayer player) {
        minigun.setItemStack(stack).setAmmo(getAmmo(player)).setPlayer(player).setPressurizable(this, 20).setWorld(player.world);
        return minigun;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand handIn) {
        ItemStack stack = player.getHeldItem(handIn);
        if (!world.isRemote) {
            ItemStack ammo = getAmmo(player);
            if (!ammo.isEmpty()) {
                boolean usedAmmo = getMinigun(stack, player).tryFireMinigun(null);
                if (usedAmmo) {
                    PneumaticCraftUtils.consumeInventoryItem(player.inventory, Itemss.GUN_AMMO);
                }
            }
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }

    private ItemStack getAmmo(EntityPlayer player) {
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack.getItem() == Itemss.GUN_AMMO) return stack;
        }
        return ItemStack.EMPTY;
    }

    private class MinigunItem extends Minigun {

        public MinigunItem() {
            super(false);
        }

        @Override
        public boolean isMinigunActivated() {
            return NBTUtil.getBoolean(stack, "activated");
        }

        @Override
        public void setMinigunActivated(boolean activated) {
            NBTUtil.setBoolean(stack, "activated", activated);
        }

        @Override
        public void setAmmoColorStack(@Nonnull ItemStack ammo) {
            if (!ammo.isEmpty() ) {
                NBTTagCompound tag = new NBTTagCompound();
                ammo.writeToNBT(tag);
                NBTUtil.setCompoundTag(stack, "ammoColorStack", tag);
            } else {
                NBTUtil.removeTag(stack, "ammoColorStack");
            }
        }

        @Override
        public int getAmmoColor() {
            ItemStack ammo = null;
            if (NBTUtil.hasTag(stack, "ammoColorStack")) {
                NBTTagCompound tag = NBTUtil.getCompoundTag(stack, "ammoColorStack");
                ammo = new ItemStack(tag);
            }
            return getAmmoColor(ammo);
        }

        @Override
        public void playSound(SoundEvent soundName, float volume, float pitch) {
            NetworkHandler.sendToAllAround(new PacketPlaySound(soundName, SoundCategory.PLAYERS, player.getPosition(), volume, pitch, false), world);
//            world.playSound(player.posX, player.posY, player.posZ, soundName, SoundCategory.PLAYERS, volume, pitch, true);
//            world.playSoundAtEntity(player, soundName, volume, pitch);
        }

        @Override
        public double getMinigunSpeed() {
            return NBTUtil.getDouble(stack, "speed");
        }

        @Override
        public void setMinigunSpeed(double minigunSpeed) {
            NBTUtil.setDouble(stack, "speed", minigunSpeed);
        }

        @Override
        public int getMinigunTriggerTimeOut() {
            return NBTUtil.getInteger(stack, "triggerTimeout");
        }

        @Override
        public void setMinigunTriggerTimeOut(int minigunTriggerTimeOut) {
            NBTUtil.setInteger(stack, "triggerTimeout", minigunTriggerTimeOut);
        }

        @Override
        public int getMinigunSoundCounter() {
            return NBTUtil.getInteger(stack, "soundCounter");
        }

        @Override
        public void setMinigunSoundCounter(int minigunSoundCounter) {
            NBTUtil.setInteger(stack, "soundCounter", minigunSoundCounter);
        }

        @Override
        public double getMinigunRotation() {
            return NBTUtil.getDouble(stack, "rotation");
        }

        @Override
        public void setMinigunRotation(double minigunRotation) {
            NBTUtil.setDouble(stack, "rotation", minigunRotation);
        }

        @Override
        public double getOldMinigunRotation() {
            return NBTUtil.getDouble(stack, "oldRotation");
        }

        @Override
        public void setOldMinigunRotation(double oldMinigunRotation) {
            NBTUtil.setDouble(stack, "oldRotation", oldMinigunRotation);
        }
    }

}
