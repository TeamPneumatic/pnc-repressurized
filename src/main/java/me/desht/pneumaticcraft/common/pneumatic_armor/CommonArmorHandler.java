package me.desht.pneumaticcraft.common.pneumatic_armor;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableBlock;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerItem;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.entity_tracker.EntityTrackHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.JetBootsClientHandler;
import me.desht.pneumaticcraft.client.sound.MovingSounds;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.config.PNCConfig.Common.Armor;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.event.HackTickHandler;
import me.desht.pneumaticcraft.common.hacking.HackableHandler;
import me.desht.pneumaticcraft.common.hacking.WorldAndCoord;
import me.desht.pneumaticcraft.common.item.ItemMachineUpgrade;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.item.ItemRegistry;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.*;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.common.util.upgrade.ApplicableUpgradesDB;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

public class CommonArmorHandler implements ICommonArmorHandler {
    private static final UUID REACH_DIST_BOOST_ID = UUID.fromString("c9dce729-70c4-4c0f-95d4-31d2e50bc826");
    private static final AttributeModifier REACH_DIST_BOOST = new AttributeModifier(REACH_DIST_BOOST_ID, "Pneumatic Reach Boost", 3.5D, AttributeModifier.Operation.ADDITION);

    private static final CommonArmorHandler clientHandler = new CommonArmorHandler(null);
    private static final CommonArmorHandler serverHandler = new CommonArmorHandler(null);

    private static final Vector3d FORWARD = new Vector3d(0, 0, 1);

    private final HashMap<UUID, CommonArmorHandler> playerHandlers = new HashMap<>();
    private PlayerEntity player;
    private int magnetRadius;
    private int magnetRadiusSq;
    private final boolean[][] upgradeRenderersInserted = new boolean[4][];
    private final boolean[][] upgradeRenderersEnabled = new boolean[4][];
    private final int[] ticksSinceEquip = new int[4];
    private final List<LazyOptional<IAirHandlerItem>> airHandlers = new ArrayList<>();
    private final int[][] upgradeMatrix = new int [4][];
    private final int[] startupTimes = new int[4];

    private boolean isValid; // true if the handler is valid; gets invalidated if player disconnects

    private int hackTime;
    private WorldAndCoord hackedBlockPos;
    private Entity hackedEntity;

    private boolean armorEnabled;
    private boolean magnetEnabled;
    private boolean chargingEnabled;
    private boolean stepAssistEnabled;
    private boolean reachDistanceEnabled;
    private boolean runSpeedEnabled;
    private boolean jumpBoostEnabled;
    private boolean entityTrackerEnabled;
    private boolean nightVisionEnabled;
    private boolean scubaEnabled;
//    private boolean airConEnabled;
    private boolean jetBootsEnabled;  // are jet boots switched on?
    private boolean jetBootsActive;  // are jet boots actually firing (player rising) ?
    private float flightAccel = 1.0F;  // increases while diving, decreases while climbing
    private int prevJetBootsAirUsage;  // so we know when the jet boots are starting up
    private int jetBootsActiveTicks;
    private boolean wasNightVisionEnabled;
    private float speedBoostMult;
    private boolean jetBootsBuilderMode;
    private float jetBootsPower;

    private CommonArmorHandler(PlayerEntity player) {
        this.player = player;
        for (EquipmentSlotType slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
            List<IArmorUpgradeHandler> renderHandlers = ArmorUpgradeRegistry.getInstance().getHandlersForSlot(slot);
            upgradeRenderersInserted[slot.getIndex()] = new boolean[renderHandlers.size()];
            upgradeRenderersEnabled[slot.getIndex()] = new boolean[renderHandlers.size()];
            upgradeMatrix[slot.getIndex()] = new int[EnumUpgrade.values().length];
        }
        Arrays.fill(startupTimes, 200);
        for (int i = 0; i < 4; i++) {
            airHandlers.add(LazyOptional.empty());
        }
        isValid = true;
    }

    private static CommonArmorHandler getManagerInstance(PlayerEntity player) {
        return player.world.isRemote ? clientHandler : serverHandler;
    }

    public static CommonArmorHandler getHandlerForPlayer(PlayerEntity player) {
        return getManagerInstance(player).playerHandlers.computeIfAbsent(player.getUniqueID(), v -> new CommonArmorHandler(player));
    }

    public static CommonArmorHandler getHandlerForPlayer() {
        return getHandlerForPlayer(ClientUtils.getClientPlayer());
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Listeners {
        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                getHandlerForPlayer(event.player).tick();
            }
        }

        @SubscribeEvent
        public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
            // called server side when player logs off
            clearHandlerForPlayer(event.getPlayer());
        }

        @SubscribeEvent
        public static void onPlayerJoinWorld(EntityJoinWorldEvent event) {
            if (event.getEntity() instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) event.getEntity();
                CommonArmorHandler handler = getManagerInstance(player).playerHandlers.get(player.getUniqueID());
                if (handler != null) handler.player = player;
            }
        }
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientListeners {
        @SubscribeEvent
        public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggedOutEvent event) {
            // called client side when client disconnects
            PlayerEntity player = ClientUtils.getClientPlayer();
            if (player != null) {
                clearHandlerForPlayer(player);
            }
        }
    }

    private static void clearHandlerForPlayer(PlayerEntity player) {
        CommonArmorHandler h = getManagerInstance(player);
        h.playerHandlers.computeIfPresent(player.getUniqueID(), (name, val) -> { val.invalidate(); return null; } );
    }

    private void tick() {
        for (EquipmentSlotType slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
            tickArmorPiece(slot);
        }
        if (!player.world.isRemote) {
            handleHacking();
        }
    }

    private void tickArmorPiece(EquipmentSlotType slot) {
        ItemStack armorStack = player.getItemStackFromSlot(slot);
        boolean armorActive = false;
        if (armorStack.getItem() instanceof ItemPneumaticArmor) {
            airHandlers.set(slot.getIndex(), armorStack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY));
            if (ticksSinceEquip[slot.getIndex()] == 0) {
                initArmorInventory(slot);
            }
            ticksSinceEquip[slot.getIndex()]++;
            if (armorEnabled && getArmorPressure(slot) > 0F) {
                armorActive = true;
                if (!player.world.isRemote) {
                    if (isArmorReady(slot) && !player.isCreative()) {
                        // use up air in the armor piece
                        float airUsage = getIdleAirUsage(slot, false);
                        if (airUsage != 0) {
                            float oldPressure = addAir(slot, (int) -airUsage);
                            if (oldPressure > 0F && getArmorPressure(slot) == 0F) {
                                // out of air!
                                NetworkHandler.sendToPlayer(new PacketPlaySound(ModSounds.MINIGUN_STOP.get(), SoundCategory.PLAYERS, player.getPosX(), player.getPosY(), player.getPosZ(), 1.0f, 2.0f, false), (ServerPlayerEntity) player);
                            }
                        }
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
        List<IArmorUpgradeHandler> handlers = ArmorUpgradeRegistry.getInstance().getHandlersForSlot(slot);
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
        switch (slot) {
            case HEAD:
                if (nightVisionEnabled) player.removeActivePotionEffect(Effects.NIGHT_VISION);
                break;
            case CHEST:
                ModifiableAttributeInstance attr = player.getAttribute(ForgeMod.REACH_DISTANCE.get());
                if (attr != null) {
                    attr.removeModifier(REACH_DIST_BOOST);
                }
                break;
            case FEET:
                player.stepHeight = 0.6F;
                break;
        }
    }

    public float addAir(EquipmentSlotType slot, int airAmount) {
        float oldPressure = getArmorPressure(slot);
        if (!player.isCreative() || airAmount > 0) {
            airHandlers.get(slot.getIndex()).ifPresent(h -> h.addAir(airAmount));
        }
        return oldPressure;
    }

    private void doArmorActions(EquipmentSlotType slot) {
        if (!isArmorReady(slot)) return;

        switch (slot) {
            case HEAD:
                handleNightVision();
                handleScuba();
                break;
            case CHEST:
                handleChestplateMagnet();
                handleChestplateCharging();
                handleReachDistance();
                break;
            case LEGS:
                handleLeggingsSpeedBoost();
                break;
            case FEET:
                handleStepAssist();
                handleJetBoots();
                handleFlippersSpeedBoost();
                break;
        }

        if (!player.world.isRemote && getUpgradeCount(slot, EnumUpgrade.ITEM_LIFE) > 0) {
            handleItemRepair(slot);
        }
    }

    private void handleNightVision() {
        // checking every 8 ticks should be enough
        if (!player.world.isRemote && (getTicksSinceEquipped(EquipmentSlotType.HEAD) & 0x7) == 0) {
            boolean shouldEnable = getArmorPressure(EquipmentSlotType.HEAD) > 0.0f
                    && getUpgradeCount(EquipmentSlotType.HEAD, EnumUpgrade.NIGHT_VISION) > 0
                    && nightVisionEnabled;
            if (shouldEnable) {
                player.addPotionEffect(new EffectInstance(Effects.NIGHT_VISION, 500, 0, false, false));
            } else if (wasNightVisionEnabled) {
                player.removePotionEffect(Effects.NIGHT_VISION);
            }
            wasNightVisionEnabled = shouldEnable;
        }
    }

    private void handleScuba() {
        // checking every 16 ticks
        if (!player.world.isRemote
                && scubaEnabled && getUpgradeCount(EquipmentSlotType.HEAD, EnumUpgrade.SCUBA) > 0
                && getArmorPressure(EquipmentSlotType.HEAD) > 0.1f
                && player.getAir() < 200) {

            ItemStack helmetStack = player.getItemStackFromSlot(EquipmentSlotType.HEAD);

            int vol = ApplicableUpgradesDB.getInstance().getUpgradedVolume(((ItemPneumaticArmor) helmetStack.getItem()).getBaseVolume(), getUpgradeCount(EquipmentSlotType.HEAD, EnumUpgrade.VOLUME));
            float airInHelmet = getArmorPressure(EquipmentSlotType.HEAD) * vol;
            int playerAir = (int) Math.min(300 - player.getAir(), airInHelmet / Armor.scubaMultiplier);
            player.setAir(player.getAir() + playerAir);

            int airUsed = playerAir * Armor.scubaMultiplier;
            addAir(EquipmentSlotType.HEAD, -airUsed);
            NetworkHandler.sendToPlayer(new PacketPlaySound(ModSounds.SCUBA.get(), SoundCategory.PLAYERS, player.getPosition(), 1.5f, 1.0f, false), (ServerPlayerEntity) player);
            Vector3d eyes = player.getEyePosition(1.0f).add(player.getLookVec().scale(0.5));
            NetworkHandler.sendToAllTracking(new PacketSpawnParticle(ParticleTypes.BUBBLE, eyes.x - 0.5, eyes.y, eyes.z -0.5, 0.0, 0.2, 0.0, 10, 1.0, 1.0, 1.0), player.world, player.getPosition());
        }
    }

    private void handleReachDistance() {
        if ((getTicksSinceEquipped(EquipmentSlotType.CHEST) & 0xf) == 0) {
            ModifiableAttributeInstance attr = player.getAttribute(ForgeMod.REACH_DISTANCE.get());
            if (attr != null) {
                attr.removeModifier(REACH_DIST_BOOST);
                if (getArmorPressure(EquipmentSlotType.CHEST) > 0f && getUpgradeCount(EquipmentSlotType.CHEST, EnumUpgrade.RANGE) > 0 && armorEnabled && reachDistanceEnabled) {
                    attr.applyNonPersistentModifier(REACH_DIST_BOOST);
                }
            }
        }
    }

    private void handleStepAssist() {
        if (getArmorPressure(EquipmentSlotType.FEET) > 0.0F && isStepAssistEnabled()) {
            player.stepHeight = player.isSneaking() ? 0.6001F : 1.25F;
        } else {
            player.stepHeight = 0.6F;
        }
    }

    // track player movement across ticks on the server - very transient, a capability would be overkill here
    private static final Map<UUID,Vector3d> moveMap = new HashMap<>();

    private void handleLeggingsSpeedBoost() {
        double speedBoost = getSpeedBoostFromLegs();
        if (player.world.isRemote) {
            // doing this client-side only appears to be effective
            if (player.moveForward > 0) {
                if (!player.isOnGround() && isJetBootsEnabled() && jetBootsBuilderMode) {
                    player.moveRelative(getUpgradeCount(EquipmentSlotType.FEET, EnumUpgrade.JET_BOOTS) / 250f, FORWARD);
                }
                if (player.isOnGround() && !player.isInWater()) {
                    player.moveRelative((float) speedBoost, FORWARD);
                }
            }
        }
        if (!player.world.isRemote && speedBoost > 0) {
            Vector3d prev = moveMap.get(player.getUniqueID());
            boolean moved = prev != null && (Math.abs(player.getPosX() - prev.x) > 0.0001 || Math.abs(player.getPosZ() - prev.z) > 0.0001);
            if (moved && player.isOnGround() && !player.isInWater()) {
                int airUsage = (int) Math.ceil(PneumaticValues.PNEUMATIC_LEGS_SPEED_USAGE * speedBoost * 8);
                addAir(EquipmentSlotType.LEGS, -airUsage);
            }
            moveMap.put(player.getUniqueID(), player.getPositionVec());
        }
    }

    public double getSpeedBoostFromLegs() {
        int speedUpgrades = getUpgradeCount(EquipmentSlotType.LEGS, EnumUpgrade.SPEED, PneumaticValues.PNEUMATIC_LEGS_MAX_SPEED);
        if (isArmorReady(EquipmentSlotType.LEGS) && speedUpgrades > 0 && isRunSpeedEnabled() && getArmorPressure(EquipmentSlotType.LEGS) > 0.0F) {
            return PneumaticValues.PNEUMATIC_LEGS_BOOST_PER_UPGRADE * speedUpgrades * speedBoostMult;
        } else {
            return 0.0;
        }
    }

    private void handleFlippersSpeedBoost() {
        if (player.world.isRemote && player.isInWater() && player.moveForward > 0) {
            // doing this client-side only appears to be effective
            if (isArmorReady(EquipmentSlotType.FEET) && getUpgradeCount(EquipmentSlotType.FEET, EnumUpgrade.FLIPPERS) > 0) {
                player.moveRelative((float) (player.isOnGround() ? Armor.flippersSpeedBoostGround : Armor.flippersSpeedBoostFloating), FORWARD);
            }
        }
    }

    private void setYMotion(Entity entity, double y) {
        Vector3d v = entity.getMotion();
        v = v.add(0, y - v.y, 0);
        entity.setMotion(v);
    }

    private void handleJetBoots() {
        int jetbootsCount = getUpgradeCount(EquipmentSlotType.FEET, EnumUpgrade.JET_BOOTS, PneumaticValues.PNEUMATIC_JET_BOOTS_MAX_UPGRADES);
        if (jetbootsCount == 0) return;

        int jetbootsAirUsage = 0;
        if (getArmorPressure(EquipmentSlotType.FEET) > 0.0F) {
            if (isJetBootsActive()) {
                if (jetBootsBuilderMode && jetbootsCount >= JetBootsClientHandler.BUILDER_MODE_LEVEL) {
                    // builder mode - rise vertically (or hover if sneaking and firing)
                    setYMotion(player, player.isSneaking() ? 0 : 0.15 + 0.15 * (jetbootsCount - 3));
                    jetbootsAirUsage = (int) (Armor.jetBootsAirUsage * jetbootsCount / 2.5F);
                } else {
                    // jetboots firing - move in direction of looking
                    Vector3d lookVec = player.getLookVec().scale(0.3 * jetbootsCount);
                    float div = lookVec.y > 0 ? -64f : -16f;
                    flightAccel = MathHelper.clamp(flightAccel + (float)lookVec.y / div, 0.8F, 4.2F);
                    lookVec = lookVec.scale(flightAccel * jetBootsPower);
                    if (jetBootsActiveTicks < 10) lookVec = lookVec.scale(jetBootsActiveTicks * 0.1);
                    player.setMotion(lookVec.x, player.isOnGround() ? 0 : lookVec.y, lookVec.z);
                    jetbootsAirUsage = (int) (Armor.jetBootsAirUsage * jetbootsCount * jetBootsPower);
                }
                if (player.isInWater()) jetbootsAirUsage *= 4;
                jetBootsActiveTicks++;
            } else if (isJetBootsEnabled() && !player.isOnGround()) {
                // jetboots not firing, but enabled - slowly descend (or hover if enough upgrades)
                setYMotion(player, player.isSneaking() ? -0.45 : -0.1 + 0.02 * jetbootsCount);
                player.fallDistance = 0;
                jetbootsAirUsage = (int) (Armor.jetBootsAirUsage * (player.isSneaking() ? 0.25F : 0.5F));
                flightAccel = 1.0F;
            } else {
                flightAccel = 1.0F;
            }
        }
        if (jetbootsAirUsage != 0 && !player.world.isRemote) {
            if (prevJetBootsAirUsage == 0) {
                // jet boots starting up
                NetworkHandler.sendToAllTracking(new PacketPlayMovingSound(MovingSounds.Sound.JET_BOOTS, player), player.world, player.getPosition());
                AdvancementTriggers.FLIGHT.trigger((ServerPlayerEntity) player);
            }
            if (player.collidedHorizontally) {
                double vel = player.getMotion().length();
                if (player.world.getDifficulty() == Difficulty.HARD) {
                    vel *= 2;
                } else if (player.world.getDifficulty() == Difficulty.NORMAL) {
                    vel *= 1.5;
                }
                if (vel > 2) {
                    player.playSound(vel > 2.5 ? SoundEvents.ENTITY_GENERIC_BIG_FALL : SoundEvents.ENTITY_GENERIC_SMALL_FALL, 1.0F, 1.0F);
                    player.attackEntityFrom(DamageSource.FLY_INTO_WALL, (float) vel);
                    AdvancementTriggers.FLY_INTO_WALL.trigger((ServerPlayerEntity) player);
                }
            }
            addAir(EquipmentSlotType.FEET, -jetbootsAirUsage);
        }
        prevJetBootsAirUsage = jetbootsAirUsage;
    }

    private void handleChestplateCharging() {
        if (player.world.isRemote || !chargingEnabled
                || getUpgradeCount(EquipmentSlotType.CHEST, EnumUpgrade.CHARGING) == 0
                || getTicksSinceEquipped(EquipmentSlotType.CHEST) % PneumaticValues.ARMOR_CHARGER_INTERVAL != 5)
            return;

        int upgrades = getUpgradeCount(EquipmentSlotType.CHEST, EnumUpgrade.CHARGING, PneumaticValues.ARMOR_CHARGING_MAX_UPGRADES);
        int airAmount = upgrades * 100 + 100;

        for (EquipmentSlotType slot : EquipmentSlotType.values()) {
            if (slot == EquipmentSlotType.CHEST) continue;
            if (getArmorPressure(EquipmentSlotType.CHEST) < 0.1F) return;
            ItemStack stack = player.getItemStackFromSlot(slot);
            tryPressurize(airAmount, stack);
        }
        for (ItemStack stack : player.inventory.mainInventory) {
            if (getArmorPressure(EquipmentSlotType.CHEST) < 0.1F) return;
            tryPressurize(airAmount, stack);
        }
    }

    private void tryPressurize(int airAmount, ItemStack destStack) {
        destStack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).ifPresent(destHandler -> {
            float pressure = destHandler.getPressure();
            if (pressure < destHandler.maxPressure() && pressure < getArmorPressure(EquipmentSlotType.CHEST)) {
                float currentAir = pressure * destHandler.getVolume();
                float targetAir = getArmorPressure(EquipmentSlotType.CHEST) * destHandler.getVolume();
                int amountToMove = Math.min((int)(targetAir - currentAir), airAmount);
                destHandler.addAir(amountToMove);
                addAir(EquipmentSlotType.CHEST, -amountToMove);
            }
        });
    }

    private void handleItemRepair(EquipmentSlotType slot) {
        int upgrades = getUpgradeCount(slot, EnumUpgrade.ITEM_LIFE, PneumaticValues.ARMOR_REPAIR_MAX_UPGRADES);
        int interval = 120 - (20 * upgrades);
        int airUsage = Armor.repairAirUsage * upgrades;

        ItemStack armorStack = player.getItemStackFromSlot(slot);
        if (armorStack.getDamage() > 0
                && getArmorPressure(slot) > 0.1F
                && ticksSinceEquip[slot.getIndex()] % interval == 0) {
            addAir(slot, -airUsage);
            armorStack.setDamage(armorStack.getDamage() - 1);
        }
    }

    private void handleChestplateMagnet() {
        if (player.world.isRemote || !magnetEnabled || (getTicksSinceEquipped(EquipmentSlotType.CHEST) & 0x3) != 0
                || getUpgradeCount(EquipmentSlotType.CHEST, EnumUpgrade.MAGNET) == 0)
            return;

        AxisAlignedBB box = new AxisAlignedBB(player.getPosition()).grow(magnetRadius);
        List<Entity> itemList = player.getEntityWorld().getEntitiesWithinAABB(Entity.class, box,
                e -> (e instanceof ExperienceOrbEntity || e instanceof ItemEntity) && e.isAlive());

        Vector3d playerVec = player.getPositionVec();
        for (Entity item : itemList) {
            if (item instanceof ItemEntity && ((ItemEntity) item).cannotPickup()) continue;

            if (item.getPositionVec().squareDistanceTo(playerVec) <= magnetRadiusSq
                    && !ItemRegistry.getInstance().shouldSuppressMagnet(item)
                    && !item.getPersistentData().getBoolean(Names.PREVENT_REMOTE_MOVEMENT)) {
                if (getArmorPressure(EquipmentSlotType.CHEST) < 0.1F) break;
                item.setPosition(player.getPosX(), player.getPosY(), player.getPosZ());
                if (item instanceof ItemEntity) ((ItemEntity) item).setPickupDelay(0);
                addAir(EquipmentSlotType.CHEST, -Armor.magnetAirUsage);
            }
        }
    }

    // only called server-side
    private void handleHacking() {
        if (hackedBlockPos != null) {
            IHackableBlock hackableBlock = HackableHandler.getHackableForBlock(hackedBlockPos.world, hackedBlockPos.pos, player);
            if (hackableBlock != null) {
                IBlockReader world = hackedBlockPos.world;
                if (world != null && ++hackTime >= hackableBlock.getHackTime(world, hackedBlockPos.pos, player)) {
                    hackableBlock.onHackComplete(player.world, hackedBlockPos.pos, player);
                    HackTickHandler.instance().trackBlock(player.world, hackedBlockPos.pos, hackableBlock);
                    NetworkHandler.sendToAllTracking(new PacketHackingBlockFinish(hackedBlockPos), player.world, player.getPosition());
                    setHackedBlockPos(null);
                    AdvancementTriggers.BLOCK_HACK.trigger((ServerPlayerEntity) player);  // safe to cast, this is server-side
                }
            } else {
                setHackedBlockPos(null);
            }
        } else if (hackedEntity != null) {
            IHackableEntity hackableEntity = HackableHandler.getHackableForEntity(hackedEntity, player);
            if (hackableEntity != null) {
                if (++hackTime >= hackableEntity.getHackTime(hackedEntity, player)) {
                    if (hackedEntity.isAlive()) {
                        hackableEntity.onHackFinished(hackedEntity, player);
                        HackTickHandler.instance().trackEntity(hackedEntity, hackableEntity);
                        NetworkHandler.sendToAllTracking(new PacketHackingEntityFinish(hackedEntity), hackedEntity);
                        AdvancementTriggers.ENTITY_HACK.trigger((ServerPlayerEntity) player);  // safe to cast, this is server-side
                    }
                    setHackedEntity(null);
                }
            } else {
                setHackedEntity(null);
            }
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
        ItemStack armorStack = player.getItemStackFromSlot(slot);

        // record which upgrades / render-handlers are inserted
        ItemStack[] upgradeStacks = UpgradableItemUtils.getUpgradeStacks(armorStack);
        Arrays.fill(upgradeRenderersInserted[slot.getIndex()], false);
        for (int i = 0; i < upgradeRenderersInserted[slot.getIndex()].length; i++) {
            upgradeRenderersInserted[slot.getIndex()][i] = isModuleEnabled(upgradeStacks, ArmorUpgradeRegistry.getInstance().getHandlersForSlot(slot).get(i));
        }

        // record the number of upgrades of every type
        Arrays.fill(upgradeMatrix[slot.getIndex()], 0);
        for (ItemStack stack : upgradeStacks) {
            if (stack.getItem() instanceof ItemMachineUpgrade) {
                ItemMachineUpgrade item = (ItemMachineUpgrade) stack.getItem();
                upgradeMatrix[slot.getIndex()][item.getUpgradeType().ordinal()] += stack.getCount() * item.getTier();
            }
        }
        startupTimes[slot.getIndex()] = (int) (Armor.armorStartupTime * Math.pow(0.8, getSpeedFromUpgrades(slot) - 1));

        // some slot-specific setup
        switch (slot) {
            case CHEST:
                magnetRadius = PneumaticValues.MAGNET_BASE_RANGE
                        + Math.min(getUpgradeCount(EquipmentSlotType.CHEST, EnumUpgrade.MAGNET), PneumaticValues.MAGNET_MAX_UPGRADES);
                magnetRadiusSq = magnetRadius * magnetRadius;
                break;
            case LEGS:
                speedBoostMult = ItemPneumaticArmor.getIntData(armorStack, ItemPneumaticArmor.NBT_SPEED_BOOST, 100, 0, 100) / 100f;
                break;
            case FEET:
                jetBootsBuilderMode = ItemPneumaticArmor.getBooleanData(armorStack, ItemPneumaticArmor.NBT_BUILDER_MODE, false);
                jetBootsPower = ItemPneumaticArmor.getIntData(armorStack, ItemPneumaticArmor.NBT_JET_BOOTS_POWER, 100, 0, 100) / 100f;
                JetBootsStateTracker.getTracker(player).setJetBootsState(player, isJetBootsEnabled(), isJetBootsActive(), jetBootsBuilderMode);
                break;
        }
    }

    @Override
    public PlayerEntity getPlayer() {
        return player;
    }

    @Override
    public int getUpgradeCount(EquipmentSlotType slot, EnumUpgrade upgrade) {
        return upgradeMatrix[slot.getIndex()][upgrade.ordinal()];
    }

    public int getUpgradeCount(EquipmentSlotType slot, EnumUpgrade upgrade, int max) {
        return Math.min(max, upgradeMatrix[slot.getIndex()][upgrade.ordinal()]);
    }

    public boolean isUpgradeInserted(EquipmentSlotType slot, int featureIndex) {
        return upgradeRenderersInserted[slot.getIndex()][featureIndex];
    }

    public boolean isUpgradeEnabled(EquipmentSlotType slot, int featureIndex) {
        return upgradeRenderersEnabled[slot.getIndex()][featureIndex];
    }

    public void setUpgradeEnabled(EquipmentSlotType slot, byte featureIndex, boolean state) {
        upgradeRenderersEnabled[slot.getIndex()][featureIndex] = state;
        IArmorUpgradeHandler handler = ArmorUpgradeRegistry.getInstance().getHandlersForSlot(slot).get(featureIndex);
        // bit of a code smell here, but caching the enablement of various features is important for performance
        if (handler instanceof MagnetHandler) {
            magnetEnabled = state;
        } else if (handler instanceof ChargingHandler) {
            chargingEnabled = state;
        } else if (handler instanceof StepAssistHandler) {
            stepAssistEnabled = state;
            if (!stepAssistEnabled) player.stepHeight = 0.6F;
        } else if (handler instanceof SpeedBoostHandler) {
            runSpeedEnabled = state;
        } else if (handler instanceof JumpBoostHandler) {
            jumpBoostEnabled = state;
        } else if (handler instanceof JetBootsHandler) {
            jetBootsEnabled = state;
            JetBootsStateTracker.getTracker(player).setJetBootsState(player, jetBootsEnabled, isJetBootsActive(), isJetBootsBuilderMode());
        } else if (handler instanceof CoreComponentsHandler) {
            armorEnabled = state;
        } else if (handler instanceof EntityTrackHandler) {
            entityTrackerEnabled = state;
        } else if (handler instanceof NightVisionHandler) {
            nightVisionEnabled = state;
            if (!nightVisionEnabled) player.removeActivePotionEffect(Effects.NIGHT_VISION);
        } else if (handler instanceof ScubaHandler) {
            scubaEnabled = state;
        } else if (handler instanceof ReachDistanceHandler) {
            reachDistanceEnabled = state;
        }
        /*else if (handler instanceof AirConUpgradeHandler) {
            airConEnabled = state;
        }*/
    }

    public int getTicksSinceEquipped(EquipmentSlotType slot) {
        return ticksSinceEquip[slot.getIndex()];
    }

    private boolean isModuleEnabled(ItemStack[] helmetStacks, IArmorUpgradeHandler handler) {
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

    public void setHackedBlockPos(WorldAndCoord blockPos) {
        hackedBlockPos = blockPos;
        hackedEntity = null;
        hackTime = 0;
    }

    public void setHackedEntity(Entity entity) {
        hackedEntity = entity;
        hackedBlockPos = null;
        hackTime = 0;
    }

    public boolean isArmorReady(EquipmentSlotType slot) {
        return getTicksSinceEquipped(slot) > getStartupTime(slot);
    }

    public boolean isStepAssistEnabled() {
        return stepAssistEnabled;
    }

    public boolean isRunSpeedEnabled() {
        return runSpeedEnabled;
    }

    public boolean isJumpBoostEnabled() {
        return jumpBoostEnabled;
    }

//    public boolean isAirConEnabled() {
//        return airConEnabled;
//    }

    @Override
    public float getArmorPressure(EquipmentSlotType slot) {
        return airHandlers.get(slot.getIndex()).map(IAirHandler::getPressure).orElse(0F);
    }

    public void setJetBootsActive(boolean jetBootsActive) {
        if (!jetBootsActive) {
            jetBootsActiveTicks = 0;
        }
        this.jetBootsActive = jetBootsActive;
        JetBootsStateTracker.getTracker(player).setJetBootsState(player, isJetBootsEnabled(), jetBootsActive, isJetBootsBuilderMode());
    }

    public boolean isJetBootsActive() {
        return jetBootsActive;
    }

    public boolean isJetBootsEnabled() {
        return jetBootsEnabled;
    }

    public boolean isArmorEnabled() {
        return armorEnabled;
    }

    public boolean isEntityTrackerEnabled() {
        return entityTrackerEnabled;
    }

    public boolean isScubaEnabled() {
        return scubaEnabled;
    }

    public boolean isValid() {
        return isValid;
    }

    public void invalidate() {
        isValid = false;
    }

    public boolean isJetBootsBuilderMode() {
        return jetBootsBuilderMode;
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
    public boolean upgradeUsable(IArmorUpgradeHandler upgrade, boolean mustBeActive) {
        EquipmentSlotType slot = upgrade.getEquipmentSlot();
        int idx = ArmorUpgradeRegistry.getInstance().getIndexForHandler(upgrade);
        return armorEnabled && isArmorReady(slot) && getArmorPressure(slot) > 0f
                && isUpgradeInserted(slot, idx) && (!mustBeActive || isUpgradeEnabled(slot, idx));
    }

    /**
     * Called both client- and server-side when a custom NBT field in an armor item has been updated.  Used to
     * cache data (e.g. legs speed boost %) for performance reasons.
     *
     * @param key the data key
     * @param dataTag the NBT data, to be interpreted depending on the key
     */
    public void onDataFieldUpdated(String key, INBT dataTag) {
        switch (key) {
            case ItemPneumaticArmor.NBT_SPEED_BOOST:
                speedBoostMult = MathHelper.clamp(((IntNBT) dataTag).getInt() / 100f, 0f, 1f);
                break;
            case ItemPneumaticArmor.NBT_BUILDER_MODE:
                jetBootsBuilderMode = ((ByteNBT) dataTag).getByte() == 1;
                JetBootsStateTracker.getTracker(player).getJetBootsState(player).setBuilderMode(jetBootsBuilderMode);
                break;
            case ItemPneumaticArmor.NBT_JET_BOOTS_POWER:
                jetBootsPower = MathHelper.clamp(((IntNBT) dataTag).getInt() / 100f, 0f, 1f);
                break;
        }
    }
}
