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
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorExtensionData;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerItem;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.client.pneumatic_armor.ClientArmorRegistry;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketToggleArmorFeature;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.*;

public class CommonArmorHandler implements ICommonArmorHandler {

    private static final CommonArmorHandler clientHandler = new CommonArmorHandler(null);
    private static final CommonArmorHandler serverHandler = new CommonArmorHandler(null);

    private static final Vec3 FORWARD = new Vec3(0, 0, 1);

    public static final float CRITICAL_PRESSURE = 0.1F;
    public static final float LOW_PRESSURE = 0.5F;

    private final HashMap<UUID, CommonArmorHandler> playerHandlers = new HashMap<>();
    private Player player;
    private final boolean[][] upgradeRenderersInserted = new boolean[4][];
    private final boolean[][] upgradeRenderersEnabled = new boolean[4][];
    private final int[] ticksSinceEquip = new int[4];
    private final List<IAirHandlerItem> airHandlers = new ArrayList<>();
    private final List<Map<PNCUpgrade, Integer>> upgradeMatrix = new ArrayList<>();
    private final int[] startupTimes = new int[4];
    private final IArmorExtensionData[][] extensionData = new IArmorExtensionData[4][];

    private boolean isValid; // true if the handler is valid; gets invalidated if player disconnects

    private CommonArmorHandler(Player player) {
        this.player = player;
        for (EquipmentSlot slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
            List<IArmorUpgradeHandler<?>> upgradeHandlers = ArmorUpgradeRegistry.getInstance().getHandlersForSlot(slot);
            upgradeRenderersInserted[slot.getIndex()] = new boolean[upgradeHandlers.size()];
            upgradeRenderersEnabled[slot.getIndex()] = new boolean[upgradeHandlers.size()];
            upgradeMatrix.add(new HashMap<>());
            airHandlers.add(null);
            extensionData[slot.getIndex()] = new IArmorExtensionData[upgradeHandlers.size()];
            for (IArmorUpgradeHandler<?> handler : upgradeHandlers) {
                extensionData[slot.getIndex()][handler.getIndex()] = handler.extensionData().get();
            }
        }
        Arrays.fill(startupTimes, 200);
        isValid = true;
    }

    private static CommonArmorHandler getManagerInstance(Player player) {
        return player.level().isClientSide ? clientHandler : serverHandler;
    }

    public static CommonArmorHandler getHandlerForPlayer(Player player) {
        return getManagerInstance(player).playerHandlers.computeIfAbsent(player.getUUID(), v -> new CommonArmorHandler(player));
    }

    public static CommonArmorHandler getHandlerForPlayer() {
        return getHandlerForPlayer(ClientUtils.getClientPlayer());
    }

    public void armorSwitched(EquipmentSlot slot) {
        // called from LivingEntityMixin when a piece of pneumatic armor is equipped, replacing existing pneumatic armor
        // need to reset the init counter to force rescan of upgrades etc.
        if (ticksSinceEquip[slot.getIndex()] > 0) {
            airHandlers.set(slot.getIndex(), null);
            if (ticksSinceEquip[slot.getIndex()] > 1) {
                onArmorRemoved(slot);
            }
            ticksSinceEquip[slot.getIndex()] = 0;
        }
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Listeners {
        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                CommonArmorHandler handler = getHandlerForPlayer(event.player);
                for (EquipmentSlot slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
                    handler.tickArmorPiece(slot);
                }
            }
        }

        @SubscribeEvent
        public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
            // called server side when player logs off
            clearHandlerForPlayer(event.getEntity());
        }

        @SubscribeEvent
        public static void onPlayerJoinWorld(EntityJoinLevelEvent event) {
            // this will happen when a player changes dimension; they get a new player entity, so the armor
            // handler must be updated to reflect that
            if (event.getEntity() instanceof Player player) {
                CommonArmorHandler handler = getManagerInstance(player).playerHandlers.get(player.getUUID());
                if (handler != null) handler.player = player;
            }
        }
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientListeners {
        @SubscribeEvent
        public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
            // called client side when client disconnects
            ClientUtils.getOptionalClientPlayer().ifPresent(CommonArmorHandler::clearHandlerForPlayer);
        }

        @SubscribeEvent
        public static void tickEnd(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                if (ClientUtils.getOptionalClientPlayer().isEmpty() && ArmorUpgradeRegistry.getInstance().isFrozen()) {
                    for (EquipmentSlot slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
                        ClientArmorRegistry.getInstance().getHandlersForSlot(slot).forEach(IArmorUpgradeClientHandler::reset);
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

    private static void clearHandlerForPlayer(Player player) {
        CommonArmorHandler h = getManagerInstance(player);
        h.playerHandlers.computeIfPresent(player.getUUID(), (name, val) -> { val.invalidate(); return null; } );
    }

    public void tickArmorPiece(EquipmentSlot slot) {
        ItemStack armorStack = player.getItemBySlot(slot);
        boolean armorActive = false;
        if (armorStack.getItem() instanceof PneumaticArmorItem) {
            airHandlers.set(slot.getIndex(), armorStack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM));
            if (ticksSinceEquip[slot.getIndex()] == 0) {
                initArmorInventory(slot);
            }
            ticksSinceEquip[slot.getIndex()]++;
            if (isArmorEnabled() && getArmorPressure(slot) > 0F) {
                armorActive = true;
                if (!player.level().isClientSide && isArmorReady(slot) && !player.isCreative()) {
                    // use up air in the armor piece
                    float airUsage = getIdleAirUsage(slot, false);
                    if (airUsage != 0) {
                        addAir(slot, (int) -airUsage);
                    }
                }
                doArmorActions(slot);
            }
        } else {
            airHandlers.set(slot.getIndex(), null);
        }
        if (!armorActive) {
            if (ticksSinceEquip[slot.getIndex()] > 1) {
                onArmorRemoved(slot);
            }
            ticksSinceEquip[slot.getIndex()] = 0;
        }
    }

    public float getIdleAirUsage(EquipmentSlot slot, boolean countDisabled) {
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
    private void onArmorRemoved(EquipmentSlot slot) {
        List<IArmorUpgradeHandler<?>> handlers = ArmorUpgradeRegistry.getInstance().getHandlersForSlot(slot);
        for (int i = 0; i < handlers.size(); i++) {
            if (isUpgradeInserted(slot, i)) {
                handlers.get(i).onShutdown(this);
            }
        }
    }

    @Override
    public float addAir(EquipmentSlot slot, int airAmount) {
        float oldPressure = getArmorPressure(slot);
        if ((!player.isCreative() || airAmount > 0) && getUpgradeCount(slot, ModUpgrades.CREATIVE.get()) == 0) {
            IAirHandlerItem handler = airHandlers.get(slot.getIndex());
            if (handler != null) handler.addAir(airAmount);
        }
        return oldPressure;
    }

    private void doArmorActions(EquipmentSlot slot) {
        if (!isArmorReady(slot)) return;

        List<IArmorUpgradeHandler<?>> handlers = ArmorUpgradeRegistry.getInstance().getHandlersForSlot(slot);
        for (int i = 0; i < handlers.size(); i++) {
            if (isUpgradeInserted(slot, i)) {
                handlers.get(i).tick(this, isUpgradeEnabled(slot, i));
            }
        }

        // flippers & repairing are special cases; they don't have upgrade handlers

        if (slot == EquipmentSlot.FEET && player.level().isClientSide && player.isInWater() && player.zza > 0) {
            // doing this client-side only appears to be effective
            if (isArmorReady(EquipmentSlot.FEET) && getUpgradeCount(EquipmentSlot.FEET, ModUpgrades.FLIPPERS.get()) > 0) {
                player.moveRelative(player.onGround() ? ConfigHelper.common().armor.flippersSpeedBoostGround.get().floatValue() : ConfigHelper.common().armor.flippersSpeedBoostFloating.get().floatValue(), FORWARD);
            }
        }

        if (!player.level().isClientSide && getUpgradeCount(slot, ModUpgrades.ITEM_LIFE.get()) > 0) {
            tryRepairArmor(slot);
        }
    }

    private void tryRepairArmor(EquipmentSlot slot) {
        int upgrades = getUpgradeCount(slot, ModUpgrades.ITEM_LIFE.get(), PneumaticValues.ARMOR_REPAIR_MAX_UPGRADES);
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
    public void initArmorInventory(EquipmentSlot slot) {
        // armorStack has already been validated as a pneumatic armor piece at this point
        ItemStack armorStack = player.getItemBySlot(slot);

        // record which upgrades / render-handlers are inserted
        Map<PNCUpgrade,Integer> upgrades = UpgradableItemUtils.getUpgrades(armorStack);
        Arrays.fill(upgradeRenderersInserted[slot.getIndex()], false);
        for (int i = 0; i < upgradeRenderersInserted[slot.getIndex()].length; i++) {
            upgradeRenderersInserted[slot.getIndex()][i] = isModuleEnabled(upgrades.keySet(), ArmorUpgradeRegistry.getInstance().getHandlersForSlot(slot).get(i));
        }

        // record the number of upgrades of every type
        upgradeMatrix.get(slot.getIndex()).clear();
        upgrades.forEach((upgrade, count) -> upgradeMatrix.get(slot.getIndex()).put(upgrade, count));

        startupTimes[slot.getIndex()] = (int) (ConfigHelper.common().armor.armorStartupTime.get() * Math.pow(0.8, getSpeedFromUpgrades(slot) - 1));

        ArmorUpgradeRegistry.getInstance().getHandlersForSlot(slot).forEach(handler -> {
            if (isUpgradeInserted(slot, handler.getIndex())) {
                handler.onInit(this);
            }
        });
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public int getUpgradeCount(EquipmentSlot slot, PNCUpgrade upgrade) {
        return upgradeMatrix.get(slot.getIndex()).getOrDefault(upgrade, 0);
    }

    public int getUpgradeCount(EquipmentSlot slot, PNCUpgrade upgrade, int max) {
        return Math.min(max, getUpgradeCount(slot, upgrade));
    }

    public boolean isUpgradeInserted(EquipmentSlot slot, int featureIndex) {
        return upgradeRenderersInserted[slot.getIndex()][featureIndex];
    }

    public boolean isUpgradeEnabled(EquipmentSlot slot, int featureIndex) {
        return upgradeRenderersEnabled[slot.getIndex()][featureIndex];
    }

    public void setUpgradeEnabled(EquipmentSlot slot, byte featureIndex, boolean state) {
        upgradeRenderersEnabled[slot.getIndex()][featureIndex] = state;
        IArmorUpgradeHandler<?> handler = ArmorUpgradeRegistry.getInstance().getHandlersForSlot(slot).get(featureIndex);
        handler.onToggle(this, state);
    }

    public int getTicksSinceEquipped(EquipmentSlot slot) {
        return ticksSinceEquip[slot.getIndex()];
    }

    private boolean isModuleEnabled(Set<PNCUpgrade> upgrades, IArmorUpgradeHandler<?> handler) {
        return Arrays.stream(handler.getRequiredUpgrades()).allMatch(upgrades::contains);
    }

    @Override
    public int getSpeedFromUpgrades(EquipmentSlot slot) {
        return 1 + getUpgradeCount(slot, ModUpgrades.SPEED.get());
    }

    public int getStartupTime(EquipmentSlot slot) {
        return startupTimes[slot.getIndex()];
    }

    public boolean isArmorReady(EquipmentSlot slot) {
        return getTicksSinceEquipped(slot) > getStartupTime(slot);
    }

    @Override
    public float getArmorPressure(EquipmentSlot slot) {
        IAirHandlerItem handler = airHandlers.get(slot.getIndex());
        return handler == null ? 0F : handler.getPressure();
    }

    @Override
    public int getAir(EquipmentSlot slot) {
        IAirHandlerItem handler = airHandlers.get(slot.getIndex());
        return handler == null ? 0 : handler.getAir();
    }

    @Override
    public boolean isArmorEnabled() {
        return isUpgradeEnabled(EquipmentSlot.HEAD, CommonUpgradeHandlers.coreComponentsHandler.getIndex());
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isValid() {
        return isValid;
    }

    public void invalidate() {
        isValid = false;
    }

    @Override
    public boolean hasMinPressure(EquipmentSlot slot) {
        return getArmorPressure(slot) >= CRITICAL_PRESSURE;
    }

    @Override
    public boolean upgradeUsable(IArmorUpgradeHandler<?> upgrade, boolean mustBeActive) {
        EquipmentSlot slot = upgrade.getEquipmentSlot();
        int idx = upgrade.getIndex();
        return isArmorEnabled() && isArmorReady(slot) && hasMinPressure(slot)
                && isUpgradeInserted(slot, idx) && (!mustBeActive || isUpgradeEnabled(slot, idx));
    }

    @Override
    public boolean isUpgradeEnabled(IArmorUpgradeHandler<?> upgrade) {
        return isUpgradeEnabled(upgrade.getEquipmentSlot(), upgrade.getIndex());
    }

    @Override
    public void setUpgradeEnabled(IArmorUpgradeHandler<?> upgrade, boolean enabled) {
        if (isUpgradeEnabled(upgrade) != enabled && upgradeUsable(upgrade, false)) {
            if (player.level().isClientSide) {
                ClientUtils.setArmorUpgradeEnabled(upgrade.getEquipmentSlot(), (byte) upgrade.getIndex(), enabled);
            } else if (player instanceof ServerPlayer sp) {
                NetworkHandler.sendToPlayer(new PacketToggleArmorFeature(upgrade.getEquipmentSlot(), (byte) upgrade.getIndex(), enabled), sp);
            }
        }
    }

    public boolean isOnCooldown(EquipmentSlot slot) {
        return player.getCooldowns().isOnCooldown(player.getItemBySlot(slot).getItem());
    }
}
