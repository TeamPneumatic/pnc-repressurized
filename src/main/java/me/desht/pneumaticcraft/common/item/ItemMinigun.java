package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.client.IFOVModifierItem;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IInventoryItem;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.client.render.RenderItemMinigun;
import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.inventory.ContainerMinigunMagazine;
import me.desht.pneumaticcraft.common.inventory.ContainerPneumaticBase;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.minigun.MinigunPlayerTracker;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketMinigunStop;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.common.util.NBTUtils;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.common.util.upgrade.ApplicableUpgradesDB;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.NetworkHooks;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemMinigun extends ItemPressurizable implements
        IChargeableContainerProvider, IUpgradeAcceptor, IFOVModifierItem,
        IInventoryItem, IShiftScrollable {
    public static final int MAGAZINE_SIZE = 4;

    private static final String NBT_MAGAZINE = "Magazine";
    public static final String NBT_LOCKED_SLOT = "LockedSlot";
    public static final String OWNING_PLAYER_ID = "owningPlayerId";

    public ItemMinigun() {
        super(ModItems.toolProps().setISTER(() -> RenderItemMinigun::new), PneumaticValues.AIR_CANISTER_MAX_AIR, PneumaticValues.AIR_CANISTER_VOLUME);
    }

    @Nonnull
    public MagazineHandler getMagazine(ItemStack stack) {
        Validate.isTrue(stack.getItem() instanceof ItemMinigun);
        return new MagazineHandler(stack);
    }

    /**
     * Called on server only, when player equips or unequips a minigun
     * @param player the player
     * @param stack the minigun item
     * @param equipping true if equipping, false if unequipping
     */
    public void onEquipmentChange(ServerPlayerEntity player, ItemStack stack, boolean equipping) {
        if (equipping) {
            // tag the minigun with the player's entity ID - it's sync'd to clients
            // so other clients will know who's wielding it, and render appropriately
            // See RenderItemMinigun
            stack.getOrCreateTag().putInt(OWNING_PLAYER_ID, player.getEntityId());
        } else {
            stack.getOrCreateTag().remove(OWNING_PLAYER_ID);
            Minigun minigun = getMinigun(stack, player);
            if (minigun.getMinigunSpeed() > 0 || minigun.isMinigunActivated()) {
                NetworkHandler.sendToPlayer(new PacketMinigunStop(stack), player);
            }
            minigun.setMinigunSpeed(0);
            minigun.setMinigunActivated(false);
            minigun.setMinigunTriggerTimeOut(0);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean currentItem) {
        super.inventoryTick(stack, world, entity, slot, currentItem);

        PlayerEntity player = (PlayerEntity) entity;

        Minigun minigun = null;
        if (currentItem) {
            minigun = getMinigun(stack, player);
            minigun.update(player.getPosX(), player.getPosY(), player.getPosZ());
        }
        if (!world.isRemote && slot >= 0 && slot <= 8) {
            // if on hotbar, possibility of ammo replenishment via item life upgrades
            if (minigun == null) minigun = getMinigun(stack, player);
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
            float pressure = minigun.getAirCapability().map(IAirHandler::getPressure).orElse(0f);
            for (int i = 0; i < handler.getSlots() && pressure > 0.25f; i++) {
                ItemStack ammo = handler.getStackInSlot(i);
                if (ammo.getItem() instanceof ItemGunAmmo && ammo.getDamage() > 0) {
                    if (world.getGameTime() % (475 - itemLife * 75L) == 0) {
                        ammo.setDamage(ammo.getDamage() - 1);
                        minigun.getAirCapability().ifPresent(h -> h.addAir(-(50 * itemLife)));
                        pressure = minigun.getAirCapability().map(IAirHandler::getPressure).orElse(0f);
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
        return new MinigunItem(player, stack)
                .setAmmoStack(ammo)
                .setAirHandler(stack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY), PneumaticValues.USAGE_ITEM_MINIGUN)
                .setWorld(player.world);
    }

    public Minigun getMinigun(ItemStack stack, PlayerEntity player) {
        return getMinigun(stack, player, getMagazine(stack).getAmmo());
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand handIn) {
        ItemStack stack = player.getHeldItem(handIn);
        if (player.isSneaking()) {
            if (!world.isRemote && stack.getCount() == 1) {
                NetworkHooks.openGui((ServerPlayerEntity) player, new INamedContainerProvider() {
                    @Override
                    public ITextComponent getDisplayName() {
                        return stack.getDisplayName();
                    }

                    @Override
                    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                        return new ContainerMinigunMagazine(i, playerInventory, handIn);
                    }
                }, buf -> ContainerPneumaticBase.putHand(buf, handIn));
            }
            return ActionResult.resultConsume(stack);
        } else {
            MagazineHandler magazineHandler = getMagazine(stack);
            ItemStack ammo = magazineHandler.getAmmo();
            if (!ammo.isEmpty()) {
                player.setActiveHand(handIn);
                return ActionResult.func_233538_a_(stack, world.isRemote);
            }
            if (player.world.isRemote) {
                player.playSound(SoundEvents.BLOCK_COMPARATOR_CLICK, 1f, 1f);
                player.sendStatusMessage(new TranslationTextComponent("pneumaticcraft.message.minigun.outOfAmmo"), true);
            }
            return ActionResult.resultFail(stack);
        }
    }

    @Override
    public void onUsingTick(ItemStack stack, LivingEntity entity, int count) {
        if (!(entity instanceof PlayerEntity)) return;
        PlayerEntity player = (PlayerEntity) entity;

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
            if (player.world.isRemote) {
                player.playSound(SoundEvents.BLOCK_COMPARATOR_CLICK, 1f, 1f);
                player.sendStatusMessage(new TranslationTextComponent("pneumaticcraft.message.minigun.outOfAmmo"), true);
            }
            player.stopActiveHand();
        }
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, LivingEntity entityLiving) {
        return super.onItemUseFinish(stack, worldIn, entityLiving);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public Map<EnumUpgrade,Integer> getApplicableUpgrades() {
        return ApplicableUpgradesDB.getInstance().getApplicableUpgrades(this);
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
        return 1 - (trackers * minigun.getMinigunSpeed() / 2);
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
        return xlate("pneumaticcraft.gui.tooltip.gunAmmo.loaded").mergeStyle(TextFormatting.GREEN);
    }

    @Override
    public INamedContainerProvider getContainerProvider(TileEntityChargingStation te) {
        return new IChargeableContainerProvider.Provider(te, ModContainers.CHARGING_MINIGUN.get());
    }

    @Override
    public void onShiftScrolled(PlayerEntity player, boolean forward, Hand hand) {
        // cycle the locked slot to the next valid ammo type (assuming any valid ammo)
        ItemStack stack = player.getHeldItem(hand);
        if (stack.getItem() instanceof ItemMinigun) {
            MagazineHandler handler = getMagazine(stack);
            int newSlot = Math.max(0, getLockedSlot(stack));
            for (int i = 0; i < MAGAZINE_SIZE - 1; i++) {
                newSlot = (newSlot + (forward ? 1 : -1));
                if (newSlot < 0) newSlot = MAGAZINE_SIZE - 1;
                else if (newSlot >= MAGAZINE_SIZE) newSlot = 0;
                if (handler.getStackInSlot(newSlot).getItem() instanceof ItemGunAmmo) {
                    // found one!
                    NBTUtils.setInteger(stack, ItemMinigun.NBT_LOCKED_SLOT, newSlot);
                    return;
                }
            }
        }
    }

    public static int getLockedSlot(ItemStack stack) {
        if (NBTUtils.hasTag(stack, NBT_LOCKED_SLOT)) {
            int slot = NBTUtils.getInteger(stack, NBT_LOCKED_SLOT);
            if (slot >= 0 && slot < MAGAZINE_SIZE) {
                return slot;
            } else {
                Log.warning("removed out of range saved ammo slot: " + slot);
                NBTUtils.removeTag(stack, NBT_LOCKED_SLOT);
            }
        }
        return -1;
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID)
    public static class Listener {
        @SubscribeEvent
        public static void onLivingAttack(LivingAttackEvent event) {
            if (event.getEntityLiving() instanceof PlayerEntity
                    && event.getSource() instanceof EntityDamageSource
                    && ((EntityDamageSource) event.getSource()).getIsThornsDamage()) {
                // don't take thorns damage when attacking with minigun (it applies direct damage, but it's effectively ranged...)
                PlayerEntity player = (PlayerEntity) event.getEntityLiving();
                ItemStack stack = player.getHeldItemMainhand();
                if (stack.getItem() instanceof ItemMinigun) {
                    Minigun minigun = ((ItemMinigun) stack.getItem()).getMinigun(stack, player);
                    if (minigun != null && minigun.getMinigunSpeed() >= Minigun.MAX_GUN_SPEED) {
                        event.setCanceled(true);
                    }
                }
            }
        }
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
            int slot = getLockedSlot(gunStack);
            if (slot >= 0) {
                return getStackInSlot(slot);
            }
            for (int i = 0; i < MAGAZINE_SIZE; i++) {
                if (getStackInSlot(i).getItem() instanceof ItemGunAmmo) {
                    return getStackInSlot(i);
                }
            }
            return ItemStack.EMPTY;
        }

        public void save() {
            if (!gunStack.isEmpty()) NBTUtils.setCompoundTag(gunStack, NBT_MAGAZINE, serializeNBT());
        }
    }

    private static class MinigunItem extends Minigun {
        private final ItemStack minigunStack;
        private final MinigunPlayerTracker tracker;

        MinigunItem(PlayerEntity player, ItemStack stack) {
            super(player, false);
            tracker = MinigunPlayerTracker.getInstance(player);
            this.minigunStack = stack;
        }

        @Override
        public boolean isMinigunActivated() {
            return tracker.isActivated();
        }

        @Override
        public void setMinigunActivated(boolean activated) {
            tracker.setActivated(activated);
        }

        @Override
        public void setAmmoColorStack(@Nonnull ItemStack ammo) {
            if (!ammo.isEmpty() ) {
                tracker.setAmmoColor(getAmmoColor(ammo));
            } else {
                tracker.setAmmoColor(0);
            }
        }

        @Override
        public int getAmmoColor() {
            return tracker.getAmmoColor();
        }

        @Override
        public void playSound(SoundEvent soundName, float volume, float pitch) {
            if (!player.world.isRemote) {
                NetworkHandler.sendToAllTracking(new PacketPlaySound(soundName, SoundCategory.PLAYERS, player.getPosition(), volume, pitch, false), player.world, player.getPosition());
            }
        }

        @Override
        public float getMinigunSpeed() {
            return tracker.getRotationSpeed();
        }

        @Override
        public void setMinigunSpeed(float minigunSpeed) {
            tracker.setRotationSpeed(minigunSpeed);
        }

        @Override
        public int getMinigunTriggerTimeOut() {
            return tracker.getTriggerTimeout();
        }

        @Override
        public void setMinigunTriggerTimeOut(int minigunTriggerTimeOut) {
            tracker.setTriggerTimeout(minigunTriggerTimeOut);
        }

        @Override
        public float getMinigunRotation() {
            return tracker.getBarrelRotation();
        }

        @Override
        public void setMinigunRotation(float minigunRotation) {
            tracker.setBarrelRotation(minigunRotation);
        }

        @Override
        public float getOldMinigunRotation() {
            return tracker.getPrevBarrelRotation();
        }

        @Override
        public void setOldMinigunRotation(float oldMinigunRotation) {
            tracker.setPrevBarrelRotation(oldMinigunRotation);
        }

        @Override
        public int getUpgrades(EnumUpgrade upgrade) {
            return Math.min(ApplicableUpgradesDB.getInstance().getMaxUpgrades(minigunStack.getItem(), upgrade),
                    UpgradableItemUtils.getUpgrades(minigunStack, upgrade));
        }
    }
}
