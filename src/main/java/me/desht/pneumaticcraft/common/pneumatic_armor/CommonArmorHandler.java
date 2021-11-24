/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.pneumatic_armor;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorExtensionData;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerItem;
import me.desht.pneumaticcraft.client.pneumatic_armor.ArmorUpgradeClientRegistry;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.item.ItemMachineUpgrade;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

public class CommonArmorHandler implements ICommonArmorHandler {

    private static final CommonArmorHandler clientHandler = new CommonArmorHandler(null);
    private static final CommonArmorHandler serverHandler = new CommonArmorHandler(null);

    private static final Vector3d FORWARD = new Vector3d(0, 0, 1);

    public static final float CRITICAL_PRESSURE = 0.1F;
    public static final float LOW_PRESSURE = 0.5F;

    private final HashMap<UUID, CommonArmorHandler> playerHandlers = new HashMap<>();
    private PlayerEntity player;
    private final boolean[][] upgradeRenderersInserted = new boolean[4][];
    private final boolean[][] upgradeRenderersEnabled = new boolean[4][];
    private final int[] ticksSinceEquip = new int[4];
    private final List<LazyOptional<IAirHandlerItem>> airHandlers = new ArrayList<>();
    private final List<EnumMap<EnumUpgrade, Integer>> upgradeMatrix = new ArrayList<>();
    private final int[] startupTimes = new int[4];
    private final IArmorExtensionData[][] extensionData = new IArmorExtensionData[4][];

    private boolean isValid; // true if the handler is valid; gets invalidated if player disconnects

    private CommonArmorHandler(PlayerEntity player) {
        this.player = player;
        for (EquipmentSlotType slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
            List<IArmorUpgradeHandler<?>> upgradeHandlers = ArmorUpgradeRegistry.getInstance().getHandlersForSlot(slot);
            upgradeRenderersInserted[slot.getIndex()] = new boolean[upgradeHandlers.size()];
            upgradeRenderersEnabled[slot.getIndex()] = new boolean[upgradeHandlers.size()];
            upgradeMatrix.add(new EnumMap<>(EnumUpgrade.class));
            airHandlers.add(LazyOptional.empty());
            extensionData[slot.getIndex()] = new IArmorExtensionData[upgradeHandlers.size()];
            for (IArmorUpgradeHandler<?> handler : upgradeHandlers) {
                extensionData[slot.getIndex()][handler.getIndex()] = handler.extensionData().get();
            }
        }
        Arrays.fill(startupTimes, 200);
        isValid = true;
    }

    private static CommonArmorHandler getManagerInstance(PlayerEntity player) {
        return player.level.isClientSide ? clientHandler : serverHandler;
    }

    public static CommonArmorHandler getHandlerForPlayer(PlayerEntity player) {
        return getManagerInstance(player).playerHandlers.computeIfAbsent(player.getUUID(), v -> new CommonArmorHandler(player));
    }

    public static CommonArmorHandler getHandlerForPlayer() {
        return getHandlerForPlayer(ClientUtils.getClientPlayer());
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Listeners {
        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                CommonArmorHandler handler = getHandlerForPlayer(event.player);
                for (EquipmentSlotType slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
                    handler.tickArmorPiece(slot);
                }
            }
        }

        @SubscribeEvent
        public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
            // called server side when player logs off
            clearHandlerForPlayer(event.getPlayer());
        }

        @SubscribeEvent
        public static void onPlayerJoinWorld(EntityJoinWorldEvent event) {
            // this will happen when a player changes dimension; they get a new player entity, so the armor
            // handler must be updated to reflect that
            if (event.getEntity() instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) event.getEntity();
                CommonArmorHandler handler = getManagerInstance(player).playerHandlers.get(player.getUUID());
                if (handler != null) handler.player = player;
            }
        }
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientListeners {
        @SubscribeEvent
        public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggedOutEvent event) {
            // called client side when client disconnects
            PlayerEntity player = ClientUtils.getClientPlayer();
            if (player != null) {
                clearHandlerForPlayer(player);
            }
        }

        @SubscribeEvent
        public static void tickEnd(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                if (Minecraft.getInstance().player == null) {
                    for (EquipmentSlotType slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
                        for (IArmorUpgradeClientHandler<?> handler : ArmorUpgradeClientRegistry.getInstance().getHandlersForSlot(slot)) {
                            handler.reset();
                        }
                    }
                }
            }
        }
    }

    @Override
    public <T extends IArmorExtensionData> T getExtensionData(IArmorUpgradeHandler<T> handler) {
        //noinspection unchecked
        return (T) extensionData[handler.getEquipmentSlot().getIndex()][handler.getIndex()];
    }

    private static void clearHandlerForPlayer(PlayerEntity player) {
        CommonArmorHandler h = getManagerInstance(player);
        h.playerHandlers.computeIfPresent(player.getUUID(), (name, val) -> { val.invalidate(); return null; } );
    }

    public void tickArmorPiece(EquipmentSlotType slot) {
        ItemStack armorStack = player.getItemBySlot(slot);
        boolean armorActive = false;
        if (armorStack.getItem() instanceof ItemPneumaticArmor) {
            airHandlers.set(slot.getIndex(), armorStack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY));
            if (ticksSinceEquip[slot.getIndex()] == 0) {
                initArmorInventory(slot);
            }
            ticksSinceEquip[slot.getIndex()]++;
            if (isArmorEnabled() && getArmorPressure(slot) > 0F) {
                armorActive = true;
                if (!player.level.isClientSide && isArmorReady(slot) && !player.isCreative()) {
                    // use up air in the armor piece
                    float airUsage = getIdleAirUsage(slot, false);
                    if (airUsage != 0) {
                        addAir(slot, (int) -airUsage);
                    }
                }
                doArmorActions(slot);
            }
        } else {
            airHandlers.set(slot.getIndex(), LazyOptional.empty());
        }
        if (!armorActive) {
            if (ticksSinceEquip[slot.getIndex()] > 0) {
                onArmorRemoved(slot);
            }
            ticksSinceEquip[slot.getIndex()] = 0;
        }
    }

    public float getIdleAirUsage(EquipmentSlotType slot, boolean countDisabled) {
        float totalUsage = 0f;
        List<IArmorUpgradeHandler<?>> handlers = ArmorUpgradeRegistry.getInstance().getHandlersForSlot(slot);
        for (int i = 0; i < handlers.size(); i++) {
            if (isUpgradeInserted(slot, i) && (countDisabled || isUpgradeEnabled(slot, i)))
                totalUsage += handlers.get(i).getIdleAirUsage(this);
        }
        return totalUsage;
    }

    /*
     * Called when an armor piece is removed, or otherwise disabled - out of air, armor disabled
     */
    private void onArmorRemoved(EquipmentSlotType slot) {
        List<IArmorUpgradeHandler<?>> handlers = ArmorUpgradeRegistry.getInstance().getHandlersForSlot(slot);
        for (int i = 0; i < handlers.size(); i++) {
            if (isUpgradeInserted(slot, i)) {
                handlers.get(i).onShutdown(this);
            }
        }
    }

    @Override
    public float addAir(EquipmentSlotType slot, int airAmount) {
        float oldPressure = getArmorPressure(slot);
        if (!player.isCreative() || airAmount > 0) {
            airHandlers.get(slot.getIndex()).ifPresent(h -> h.addAir(airAmount));
        }
        return oldPressure;
    }

    private void doArmorActions(EquipmentSlotType slot) {
        if (!isArmorReady(slot)) return;

        List<IArmorUpgradeHandler<?>> handlers = ArmorUpgradeRegistry.getInstance().getHandlersForSlot(slot);
        for (int i = 0; i < handlers.size(); i++) {
            if (isUpgradeInserted(slot, i)) {
                handlers.get(i).tick(this, isUpgradeEnabled(slot, i));
            }
        }

        // flippers & repairing are special cases; they don't have upgrade handlers

        if (slot == EquipmentSlotType.FEET && player.level.isClientSide && player.isInWater() && player.zza > 0) {
            // doing this client-side only appears to be effective
            if (isArmorReady(EquipmentSlotType.FEET) && getUpgradeCount(EquipmentSlotType.FEET, EnumUpgrade.FLIPPERS) > 0) {
                player.moveRelative(player.isOnGround() ?
                        ConfigHelper.common().armor.flippersSpeedBoostGround.get().floatValue() :
                        ConfigHelper.common().armor.flippersSpeedBoostFloating.get().floatValue(),
                        FORWARD);
            }
        }

        if (!player.level.isClientSide && getUpgradeCount(slot, EnumUpgrade.ITEM_LIFE) > 0) {
            tryRepairArmor(slot);
        }
    }

    private void tryRepairArmor(EquipmentSlotType slot) {
        int upgrades = getUpgradeCount(slot, EnumUpgrade.ITEM_LIFE, PneumaticValues.ARMOR_REPAIR_MAX_UPGRADES);
        int interval = 120 - (20 * upgrades);
        int airUsage = ConfigHelper.common().armor.repairAirUsage.get() * upgrades;

        ItemStack armorStack = player.getItemBySlot(slot);
        if (armorStack.getDamageValue() > 0
                && hasMinPressure(slot)
                && ticksSinceEquip[slot.getIndex()] % interval == 0) {
            addAir(slot, -airUsage);
            armorStack.setDamageValue(armorStack.getDamageValue() - 1);
        }
    }

    /**
     * Called on the first tick after the armor piece is equipped.
     *
     * Scan the armor piece in the given slot, and record all installed upgrades for fast access later on.  Upgrades
     * can't be changed without removing and re-equipping the piece, so we can cache quite a lot of useful info.
     *
     * @param slot the equipment slot
     */
    public void initArmorInventory(EquipmentSlotType slot) {
        // armorStack has already been validated as a pneumatic armor piece at this point
        ItemStack armorStack = player.getItemBySlot(slot);

        // record which upgrades / render-handlers are inserted
        ItemStack[] upgradeStacks = UpgradableItemUtils.getUpgradeStacks(armorStack);
        Arrays.fill(upgradeRenderersInserted[slot.getIndex()], false);
        for (int i = 0; i < upgradeRenderersInserted[slot.getIndex()].length; i++) {
            upgradeRenderersInserted[slot.getIndex()][i] = isModuleEnabled(upgradeStacks, ArmorUpgradeRegistry.getInstance().getHandlersForSlot(slot).get(i));
        }

        // record the number of upgrades of every type
        upgradeMatrix.get(slot.getIndex()).clear();
        for (ItemStack stack : upgradeStacks) {
            if (stack.getItem() instanceof ItemMachineUpgrade) {
                ItemMachineUpgrade upgrade = (ItemMachineUpgrade) stack.getItem();
                upgradeMatrix.get(slot.getIndex()).put(upgrade.getUpgradeType(), stack.getCount() * upgrade.getTier());
            }
        }
        startupTimes[slot.getIndex()] = (int) (ConfigHelper.common().armor.armorStartupTime.get() * Math.pow(0.8, getSpeedFromUpgrades(slot) - 1));

        ArmorUpgradeRegistry.getInstance().getHandlersForSlot(slot).forEach(handler -> {
            if (isUpgradeInserted(slot, handler.getIndex())) {
                handler.onInit(this);
            }
        });
    }

    @Override
    public PlayerEntity getPlayer() {
        return player;
    }

    @Override
    public int getUpgradeCount(EquipmentSlotType slot, EnumUpgrade upgrade) {
        return upgradeMatrix.get(slot.getIndex()).getOrDefault(upgrade, 0);
    }

    public int getUpgradeCount(EquipmentSlotType slot, EnumUpgrade upgrade, int max) {
        return Math.min(max, getUpgradeCount(slot, upgrade));
    }

    public boolean isUpgradeInserted(EquipmentSlotType slot, int featureIndex) {
        return upgradeRenderersInserted[slot.getIndex()][featureIndex];
    }

    public boolean isUpgradeEnabled(EquipmentSlotType slot, int featureIndex) {
        return upgradeRenderersEnabled[slot.getIndex()][featureIndex];
    }

    public void setUpgradeEnabled(EquipmentSlotType slot, byte featureIndex, boolean state) {
        upgradeRenderersEnabled[slot.getIndex()][featureIndex] = state;
        IArmorUpgradeHandler<?> handler = ArmorUpgradeRegistry.getInstance().getHandlersForSlot(slot).get(featureIndex);
        handler.onToggle(this, state);
    }

    public int getTicksSinceEquipped(EquipmentSlotType slot) {
        return ticksSinceEquip[slot.getIndex()];
    }

    private boolean isModuleEnabled(ItemStack[] helmetStacks, IArmorUpgradeHandler<?> handler) {
        for (EnumUpgrade requiredUpgrade : handler.getRequiredUpgrades()) {
            boolean found = false;
            for (ItemStack stack : helmetStacks) {
                if (EnumUpgrade.from(stack) == requiredUpgrade) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    @Override
    public int getSpeedFromUpgrades(EquipmentSlotType slot) {
        return 1 + getUpgradeCount(slot, EnumUpgrade.SPEED);
    }

    public int getStartupTime(EquipmentSlotType slot) {
        return startupTimes[slot.getIndex()];
    }

    public boolean isArmorReady(EquipmentSlotType slot) {
        return getTicksSinceEquipped(slot) > getStartupTime(slot);
    }

    @Override
    public float getArmorPressure(EquipmentSlotType slot) {
        return airHandlers.get(slot.getIndex()).map(IAirHandler::getPressure).orElse(0F);
    }

    @Override
    public boolean isArmorEnabled() {
        return isUpgradeEnabled(EquipmentSlotType.HEAD, ArmorUpgradeRegistry.getInstance().coreComponentsHandler.getIndex());
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isValid() {
        return isValid;
    }

    public void invalidate() {
        isValid = false;
    }

    @Override
    public boolean hasMinPressure(EquipmentSlotType slot) {
        return getArmorPressure(slot) >= CRITICAL_PRESSURE;
    }

    /**
     * Validate that the given upgrade can currently be used. Also requires that the armor is enabled, and that the
     * associated armor piece has enough pressure and has finished initialising. For non-toggleable upgrades
     * (e.g. chestplate launcher), pass false for {@code mustBeActive}
     *
     * @param upgrade the upgrade to check
     * @param mustBeActive true if the upgrade must be switched on, false otherwise
     * @return true if the upgrade can currently be used
     */
    @Override
    public boolean upgradeUsable(IArmorUpgradeHandler<?> upgrade, boolean mustBeActive) {
        EquipmentSlotType slot = upgrade.getEquipmentSlot();
        int idx = upgrade.getIndex();
        return isArmorEnabled() && isArmorReady(slot) && hasMinPressure(slot)
                && isUpgradeInserted(slot, idx) && (!mustBeActive || isUpgradeEnabled(slot, idx));
    }
}
