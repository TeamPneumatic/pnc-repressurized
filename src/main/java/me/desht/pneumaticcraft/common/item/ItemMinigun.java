package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.GuiHandler;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.tileentity.FilteredItemStackHandler;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemMinigun extends ItemPressurizable {

    public static final int MAGAZINE_SIZE = 4;
    private static final String NBT_MAGAZINE = "Magazine";
    private final Minigun minigun = new MinigunItem();

    public ItemMinigun() {
        super("minigun", PneumaticValues.AIR_CANISTER_MAX_AIR, PneumaticValues.AIR_CANISTER_VOLUME);
    }

    public static MagazineHandler getMagazine(ItemStack stack) {
        if (stack.getItem() instanceof ItemMinigun) {
            return new MagazineHandler(stack);
        }
        return null;
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> infoList, ITooltipFlag par4) {
        MagazineHandler handler = getMagazine(stack);
        if (handler != null) {
            int nCartridges = 0, totalRounds = 0;
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack ammo = handler.getStackInSlot(i);
                if (!ammo.isEmpty()) {
                    nCartridges++;
                    totalRounds += ammo.getMaxDamage() - ammo.getItemDamage();
                }
            }
            infoList.add(I18n.format("gui.tooltip.minigun.ammoCount", totalRounds, nCartridges));
        }

        super.addInformation(stack, worldIn, infoList, par4);
    }

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
        } else {
            minigun.update(player.posX, player.posY, player.posZ);
        }
        if (world.isRemote && currentItem && minigun.getMinigunSpeed() > 0) {
            suppressSwitchAnimation();
        }
    }

    @SideOnly(Side.CLIENT)
    private void suppressSwitchAnimation() {
        Minecraft mc = Minecraft.getMinecraft();
        ItemRenderer renderer = mc.entityRenderer.itemRenderer;
        renderer.updateEquippedItem();
        renderer.equippedProgressMainHand = 1;
        renderer.prevEquippedProgressMainHand = 1;
    }

    private Minigun getMinigun(ItemStack stack, EntityPlayer player, ItemStack ammo) {
        minigun.setItemStack(stack).setAmmo(ammo).setPlayer(player).setPressurizable(this, 20).setWorld(player.world);
        return minigun;
    }

    public Minigun getMinigun(ItemStack stack, EntityPlayer player) {
        return getMinigun(stack, player, getMagazine(stack).getAmmo());
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand handIn) {
        ItemStack stack = player.getHeldItem(handIn);
        if (!world.isRemote) {
            if (player.isSneaking()) {
                player.openGui(PneumaticCraftRepressurized.instance, GuiHandler.EnumGuiId.MINIGUN_MAGAZINE.ordinal(), world,
                        (int) player.posX, (int) player.posY, (int) player.posZ);
            } else {
                MagazineHandler magazineHandler = getMagazine(stack);
                ItemStack ammo = magazineHandler.getAmmo();
                if (!ammo.isEmpty()) {
                    int prevDamage = ammo.getItemDamage();
                    boolean usedAmmo = getMinigun(stack, player, ammo).tryFireMinigun(null);
                    if (usedAmmo) ammo.setCount(0);
                    if (usedAmmo || ammo.getItemDamage() != prevDamage) {
                        magazineHandler.save();
                    }
                } else {
                    player.sendStatusMessage(new TextComponentTranslation("message.minigun.outOfAmmo"), true);
                }
            }
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    public static class MagazineHandler extends FilteredItemStackHandler {
        private final ItemStack gunStack;

        MagazineHandler(ItemStack gunStack) {
            super(MAGAZINE_SIZE);
            this.gunStack = gunStack;

            if (gunStack.hasTagCompound() && gunStack.getTagCompound().hasKey(NBT_MAGAZINE)) {
                deserializeNBT(gunStack.getTagCompound().getCompoundTag(NBT_MAGAZINE));
            }
        }

        public MagazineHandler() {
            super(MAGAZINE_SIZE);
            gunStack = ItemStack.EMPTY;
        }

        @Override
        public boolean test(Integer integer, ItemStack itemStack) {
            return itemStack.isEmpty() || itemStack.getItem() instanceof ItemGunAmmo;
        }

        public ItemStack getAmmo() {
            for (int i = 0; i < MAGAZINE_SIZE; i++) {
                if (getStackInSlot(i).getItem() instanceof ItemGunAmmo) {
                    return getStackInSlot(i);
                }
            }
            return ItemStack.EMPTY;
        }

        public void save() {
            if (!gunStack.isEmpty()) NBTUtil.setCompoundTag(gunStack, NBT_MAGAZINE, serializeNBT());
        }
    }

    private class MinigunItem extends Minigun {

        MinigunItem() {
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
            ItemStack ammo = ItemStack.EMPTY;
            if (NBTUtil.hasTag(stack, "ammoColorStack")) {
                NBTTagCompound tag = NBTUtil.getCompoundTag(stack, "ammoColorStack");
                ammo = new ItemStack(tag);
            }
            return getAmmoColor(ammo);
        }

        @Override
        public void playSound(SoundEvent soundName, float volume, float pitch) {
            NetworkHandler.sendToAllAround(new PacketPlaySound(soundName, SoundCategory.PLAYERS, player.getPosition(), volume, pitch, false), world);
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
