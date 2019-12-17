package me.desht.pneumaticcraft.common.item;

import com.google.common.collect.ImmutableSet;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.client.IFOVModifierItem;
import me.desht.pneumaticcraft.api.item.IInventoryItem;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerBase;
import me.desht.pneumaticcraft.client.render.RenderItemMinigun;
import me.desht.pneumaticcraft.common.capabilities.CapabilityAirHandler;
import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.inventory.ContainerMinigunMagazine;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemMinigun extends ItemPressurizable implements IChargeableContainerProvider, IUpgradeAcceptor, IFOVModifierItem, IInventoryItem {
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
        super(defaultProps().setTEISR(() -> RenderItemMinigun::new), "minigun", PneumaticValues.AIR_CANISTER_MAX_AIR, PneumaticValues.AIR_CANISTER_VOLUME);
    }

    public static MagazineHandler getMagazine(ItemStack stack) {
        if (stack.getItem() instanceof ItemMinigun) {
            return new MagazineHandler(stack);
        }
        return null;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean currentItem) {
        super.inventoryTick(stack, world, entity, slot, currentItem);
        PlayerEntity player = (PlayerEntity) entity;
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
            MagazineHandler handler = getMagazine(stack);
            boolean repaired = false;
            float pressure = minigun.getAirCapability().map(IAirHandlerBase::getPressure).orElse(0f);
            for (int i = 0; i < handler.getSlots() && pressure > 0.1f; i++) {
                ItemStack ammo = handler.getStackInSlot(i);
                if (ammo.getItem() instanceof ItemGunAmmo && ammo.getDamage() > 0) {
                    if (world.getGameTime() % (475 - itemLife * 75) == 0) {
                        ammo.setDamage(ammo.getDamage() - 1);
                        minigun.getAirCapability().ifPresent(h -> h.addAir(-(2 << itemLife)));
                        pressure = minigun.getAirCapability().map(IAirHandlerBase::getPressure).orElse(0f);
                        repaired = true;
                    }
                }
            }
            if (repaired) {
                handler.save();
            }
        }
    }

    private Minigun getMinigun(ItemStack stack, PlayerEntity player, ItemStack ammo) {
        return new MinigunItem()
                .setItemStack(stack)
                .setAmmoStack(ammo)
                .setPlayer(player)
                .setAirHandler(stack.getCapability(CapabilityAirHandler.AIR_HANDLER_ITEM_CAPABILITY), PneumaticValues.USAGE_ITEM_MINIGUN)
                .setWorld(player.world);
    }

    public Minigun getMinigun(ItemStack stack, PlayerEntity player) {
        return getMinigun(stack, player, getMagazine(stack).getAmmo());
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand handIn) {
        ItemStack stack = player.getHeldItem(handIn);
        if (!world.isRemote) {
            if (player.isSneaking()) {
                NetworkHooks.openGui((ServerPlayerEntity) player, new INamedContainerProvider() {
                    @Override
                    public ITextComponent getDisplayName() {
                        return stack.getDisplayName();
                    }

                    @Nullable
                    @Override
                    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                        return new ContainerMinigunMagazine(i, playerInventory);
                    }
                });
            } else {
                MagazineHandler magazineHandler = getMagazine(stack);
                ItemStack ammo = magazineHandler.getAmmo();
                if (!ammo.isEmpty()) {
                    int prevDamage = ammo.getDamage();
                    Minigun minigun = getMinigun(stack, player, ammo);
                    // an item life upgrade will prevent the stack from being destroyed
                    boolean usedUpAmmo = minigun.tryFireMinigun(null) && minigun.getUpgrades(EnumUpgrade.ITEM_LIFE) == 0;
                    if (usedUpAmmo) ammo.setCount(0);
                    if (usedUpAmmo || ammo.getDamage() != prevDamage) {
                        magazineHandler.save();
                    }
                } else {
                    NetworkHandler.sendToPlayer(new PacketPlaySound(SoundEvents.BLOCK_COMPARATOR_CLICK, SoundCategory.PLAYERS, player.posX, player.posY, player.posZ, 1.0f, 1.0f, false), (ServerPlayerEntity) player);
                    player.sendStatusMessage(new TranslationTextComponent("message.minigun.outOfAmmo"), true);
                }
            }
        }
        return ActionResult.newResult(ActionResultType.SUCCESS, stack);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public Set<Item> getApplicableUpgrades() {
        if (applicableUpgrades == null) {
            applicableUpgrades = ImmutableSet.of(
                    EnumUpgrade.SPEED.getItem(),
                    EnumUpgrade.RANGE.getItem(),
                    EnumUpgrade.DISPENSER.getItem(),
                    EnumUpgrade.ENTITY_TRACKER.getItem(),
                    EnumUpgrade.ITEM_LIFE.getItem(),
                    EnumUpgrade.SECURITY.getItem()
            );
        }
        return applicableUpgrades;
    }

    @Override
    public String getUpgradeAcceptorTranslationKey() {
        return getTranslationKey();
    }

    @Override
    public float getFOVModifier(ItemStack stack, PlayerEntity player, EquipmentSlotType slot) {
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
    public ITextComponent getInventoryHeader() {
        return xlate("gui.tooltip.gunAmmo.loaded").applyTextStyle(TextFormatting.GREEN);
    }

    @Override
    public INamedContainerProvider getContainerProvider(TileEntityChargingStation te) {
        return new IChargeableContainerProvider.Provider(te, ModContainerTypes.CHARGING_MINIGUN);
    }

    public static class MagazineHandler extends BaseItemStackHandler {
        private final ItemStack gunStack;

        MagazineHandler(ItemStack gunStack) {
            super(MAGAZINE_SIZE);
            this.gunStack = gunStack;

            if (gunStack.hasTag() && gunStack.getTag().contains(NBT_MAGAZINE)) {
                deserializeNBT(gunStack.getTag().getCompound(NBT_MAGAZINE));
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
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
                CompoundNBT tag = new CompoundNBT();
                ammo.write(tag);
                NBTUtil.setCompoundTag(minigunStack, "ammoColorStack", tag);
            } else {
                NBTUtil.removeTag(minigunStack, "ammoColorStack");
            }
        }

        @Override
        public int getAmmoColor() {
            ItemStack ammo = ItemStack.EMPTY;
            if (NBTUtil.hasTag(minigunStack, "ammoColorStack")) {
                CompoundNBT tag = NBTUtil.getCompoundTag(minigunStack, "ammoColorStack");
                ammo = ItemStack.read(tag);
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
