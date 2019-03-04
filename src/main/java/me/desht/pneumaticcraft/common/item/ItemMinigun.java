package me.desht.pneumaticcraft.common.item;

import com.google.common.collect.ImmutableSet;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.client.IFOVModifierItem;
import me.desht.pneumaticcraft.api.item.IInventoryItem;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;
import me.desht.pneumaticcraft.common.inventory.ContainerMinigunMagazine;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.tileentity.FilteredItemStackHandler;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

public class ItemMinigun extends ItemPressurizable implements IChargingStationGUIHolderItem, IUpgradeAcceptor, IFOVModifierItem, IInventoryItem {
    private static final int MAGAZINE_SIZE = 4;

    private static Set<Item> applicableUpgrades;
    private static final String NBT_MAGAZINE = "Magazine";
    public static final String NBT_LOCKED_SLOT = "LockedSlot";

    // TODO this should be part of a more general "max upgrades" database - 1.13, probably
    private static final int[] MAX_UPGRADES = new int[EnumUpgrade.values().length];
    private static void setMaxUpgrades(EnumUpgrade upgrade, int max) {
        MAX_UPGRADES[upgrade.ordinal()] = max;
    }
    static {
        setMaxUpgrades(EnumUpgrade.SPEED, 3);
        setMaxUpgrades(EnumUpgrade.RANGE, 6);
        setMaxUpgrades(EnumUpgrade.DISPENSER, 3);
        setMaxUpgrades(EnumUpgrade.ITEM_LIFE, 4);
        setMaxUpgrades(EnumUpgrade.ENTITY_TRACKER, 4);
        setMaxUpgrades(EnumUpgrade.SECURITY, 1);
    }

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
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean currentItem) {
        super.onUpdate(stack, world, entity, slot, currentItem);
        EntityPlayer player = (EntityPlayer) entity;
        Minigun minigun = getMinigun(stack, player);
        if (!currentItem) {
            minigun.setMinigunSoundCounter(-1);
            minigun.setMinigunSpeed(0);
            minigun.setMinigunActivated(false);
            minigun.setMinigunTriggerTimeOut(0);
        } else {
            minigun.update(player.posX, player.posY, player.posZ);
        }

        if (world.isRemote && currentItem && minigun.getMinigunSpeed() > 0) {
            PneumaticCraftRepressurized.proxy.suppressItemEquipAnimation();
        }

        if (!world.isRemote && slot >= 0 && slot <= 8) {
            // if on hotbar, possibility of ammo replenishment via item life upgrades
            handleAmmoRepair(stack, world, minigun);
        }
    }

    private void handleAmmoRepair(ItemStack stack, World world, Minigun minigun) {
        if (minigun.getPlayer().openContainer instanceof ContainerMinigunMagazine) {
            return;  // avoid potential item duping or other shenanigans
        }
        int itemLife = minigun.getUpgrades(EnumUpgrade.ITEM_LIFE);
        if (itemLife > 0) {
            IPressurizable p = (IPressurizable) stack.getItem();
            MagazineHandler handler = getMagazine(stack);
            boolean repaired = false;
            for (int i = 0; i < handler.getSlots() && p.getPressure(stack) > 0.1; i++) {
                ItemStack ammo = handler.getStackInSlot(i);
                if (ammo.getItem() instanceof ItemGunAmmo && ammo.getItemDamage() > 0) {
                    if (world.getTotalWorldTime() % (475 - itemLife * 75) == 0) {
                        ammo.setItemDamage(ammo.getItemDamage() - 1);
                        p.addAir(stack, -(2 << itemLife));
                        repaired = true;
                    }
                }
            }
            if (repaired) {
                handler.save();
            }
        }
    }

    private Minigun getMinigun(ItemStack stack, EntityPlayer player, ItemStack ammo) {
        return new MinigunItem()
                .setItemStack(stack)
                .setAmmoStack(ammo)
                .setPlayer(player)
                .setPressurizable(this, PneumaticValues.USAGE_ITEM_MINIGUN)
                .setWorld(player.world);
    }

    public Minigun getMinigun(ItemStack stack, EntityPlayer player) {
        return getMinigun(stack, player, getMagazine(stack).getAmmo());
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand handIn) {
        ItemStack stack = player.getHeldItem(handIn);
        if (!world.isRemote) {
            if (player.isSneaking()) {
                player.openGui(PneumaticCraftRepressurized.instance, EnumGuiId.MINIGUN_MAGAZINE.ordinal(), world,
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
                    NetworkHandler.sendTo(new PacketPlaySound(SoundEvents.BLOCK_COMPARATOR_CLICK, SoundCategory.PLAYERS, player.posX, player.posY, player.posZ, 1.0f, 1.0f, false), (EntityPlayerMP) player);
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

    @Override
    public Set<Item> getApplicableUpgrades() {
        if (applicableUpgrades == null) {
            IItemRegistry r = PneumaticRegistry.getInstance().getItemRegistry();
            applicableUpgrades = ImmutableSet.of(
                    r.getUpgrade(EnumUpgrade.SPEED),
                    r.getUpgrade(EnumUpgrade.RANGE),
                    r.getUpgrade(EnumUpgrade.DISPENSER),
                    r.getUpgrade(EnumUpgrade.ENTITY_TRACKER),
                    r.getUpgrade(EnumUpgrade.ITEM_LIFE),
                    r.getUpgrade(EnumUpgrade.SECURITY)
            );
        }
        return applicableUpgrades;
    }

    @Override
    public String getName() {
        return getTranslationKey() + ".name";
    }

    @Override
    public EnumGuiId getGuiID() {
        return EnumGuiId.MINIGUN_UPGRADES;
    }

    @Override
    public float getFOVModifier(ItemStack stack, EntityPlayer player, EntityEquipmentSlot slot) {
        Minigun minigun = getMinigun(stack, player);
        int trackers = minigun.getUpgrades(EnumUpgrade.ENTITY_TRACKER);
        if (!minigun.isMinigunActivated() || trackers == 0) return 1.0f;
        return (float) (1 - (trackers * minigun.getMinigunSpeed() / 2));
    }

    @Override
    public void getStacksInItem(ItemStack stack, List<ItemStack> curStacks) {
        MagazineHandler handler = getMagazine(stack);
        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty()) {
                curStacks.add(handler.getStackInSlot(i));
            }
        }
    }

    @Override
    public String getInventoryHeader() {
        return TextFormatting.GREEN + I18n.format("gui.tooltip.gunAmmo.loaded");
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
            if (NBTUtil.hasTag(gunStack, NBT_LOCKED_SLOT)) {
                return getStackInSlot(NBTUtil.getInteger(gunStack, NBT_LOCKED_SLOT));
            }
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

        private int[] upgrades = null;

        MinigunItem() {
            super(false);
        }

        @Override
        public Minigun setAmmoStack(@Nonnull ItemStack ammoStack) {
            upgrades = null; // force a rescan of upgrades from the item nbt next time an upgrade is queried
            return super.setAmmoStack(ammoStack);
        }

        @Override
        public boolean isMinigunActivated() {
            return NBTUtil.getBoolean(minigunStack, "activated");
        }

        @Override
        public void setMinigunActivated(boolean activated) {
            NBTUtil.setBoolean(minigunStack, "activated", activated);
        }

        @Override
        public void setAmmoColorStack(@Nonnull ItemStack ammo) {
            if (!ammo.isEmpty() ) {
                NBTTagCompound tag = new NBTTagCompound();
                ammo.writeToNBT(tag);
                NBTUtil.setCompoundTag(minigunStack, "ammoColorStack", tag);
            } else {
                NBTUtil.removeTag(minigunStack, "ammoColorStack");
            }
        }

        @Override
        public int getAmmoColor() {
            ItemStack ammo = ItemStack.EMPTY;
            if (NBTUtil.hasTag(minigunStack, "ammoColorStack")) {
                NBTTagCompound tag = NBTUtil.getCompoundTag(minigunStack, "ammoColorStack");
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
            return NBTUtil.getDouble(minigunStack, "speed");
        }

        @Override
        public void setMinigunSpeed(double minigunSpeed) {
            NBTUtil.setDouble(minigunStack, "speed", minigunSpeed);
        }

        @Override
        public int getMinigunTriggerTimeOut() {
            return NBTUtil.getInteger(minigunStack, "triggerTimeout");
        }

        @Override
        public void setMinigunTriggerTimeOut(int minigunTriggerTimeOut) {
            NBTUtil.setInteger(minigunStack, "triggerTimeout", minigunTriggerTimeOut);
        }

        @Override
        public int getMinigunSoundCounter() {
            return NBTUtil.getInteger(minigunStack, "soundCounter");
        }

        @Override
        public void setMinigunSoundCounter(int minigunSoundCounter) {
            NBTUtil.setInteger(minigunStack, "soundCounter", minigunSoundCounter);
        }

        @Override
        public double getMinigunRotation() {
            return NBTUtil.getDouble(minigunStack, "rotation");
        }

        @Override
        public void setMinigunRotation(double minigunRotation) {
            NBTUtil.setDouble(minigunStack, "rotation", minigunRotation);
        }

        @Override
        public double getOldMinigunRotation() {
            return NBTUtil.getDouble(minigunStack, "oldRotation");
        }

        @Override
        public void setOldMinigunRotation(double oldMinigunRotation) {
            NBTUtil.setDouble(minigunStack, "oldRotation", oldMinigunRotation);
        }

        private void loadUpgrades() {
            upgrades = new int[EnumUpgrade.values().length];
            for (ItemStack stack : UpgradableItemUtils.getUpgradeStacks(minigunStack)) {
                if (stack.getItem() instanceof ItemMachineUpgrade) {
                    int idx = ((ItemMachineUpgrade) stack.getItem()).getUpgradeType().ordinal();
                    upgrades[idx] += stack.getCount();
                }
            }
        }

        @Override
        public int getUpgrades(EnumUpgrade upgrade) {
            if (upgrades == null) loadUpgrades();
            return Math.min(MAX_UPGRADES[upgrade.ordinal()], upgrades[upgrade.ordinal()]);
        }
    }

}
