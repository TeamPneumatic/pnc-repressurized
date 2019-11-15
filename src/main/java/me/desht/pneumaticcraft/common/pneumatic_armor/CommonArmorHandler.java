package me.desht.pneumaticcraft.common.pneumatic_armor;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableBlock;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.UpgradeRenderHandlerList;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.*;
import me.desht.pneumaticcraft.client.sound.MovingSounds;
import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.hacking.HackableHandler;
import me.desht.pneumaticcraft.common.item.ItemMachineUpgrade;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.item.ItemRegistry;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
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
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.*;

public class CommonArmorHandler {
    private static final CommonArmorHandler clientHandler = new CommonArmorHandler(null);
    private static final CommonArmorHandler serverHandler = new CommonArmorHandler(null);

    private final HashMap<UUID, CommonArmorHandler> playerHudHandlers = new HashMap<>();
    private PlayerEntity player;
    private int magnetRadius;
    private int magnetRadiusSq;
    private final boolean[][] upgradeRenderersInserted = new boolean[4][];
    private final boolean[][] upgradeRenderersEnabled = new boolean[4][];
    private final int[] ticksSinceEquip = new int[4];
    public final float[] armorPressure = new float[4];
    private final int[][] upgradeMatrix = new int [4][];
    private final int[] startupTimes = new int[4];

    private boolean isValid; // true if the handler is valid; gets invalidated if player disconnects

    private int hackTime;
    private GlobalPos hackedBlockPos;
    private Entity hackedEntity;

    private boolean armorEnabled;
    private boolean magnetEnabled;
    private boolean chargingEnabled;
    private boolean stepAssistEnabled;
    private boolean runSpeedEnabled;
    private boolean jumpBoostEnabled;
    private boolean entityTrackerEnabled;
    private boolean nightVisionEnabled;
    private boolean scubaEnabled;
    private boolean airConEnabled;
    private boolean jetBootsEnabled;  // are jet boots switched on?
    private boolean jetBootsActive;  // are jet boots actually firing (player rising) ?
    private float flightAccel = 1.0F;  // increases while diving, decreases while climbing
    private int prevJetBootsAirUsage;  // so we know when the jet boots are starting up
    private int jetBootsActiveTicks;
    private boolean wasNightVisionEnabled;
    private float speedBoostMult;
    private boolean jetBootsBuilderMode;

    private CommonArmorHandler(PlayerEntity player) {
        this.player = player;
        for (EquipmentSlotType slot : UpgradeRenderHandlerList.ARMOR_SLOTS) {
            List<IUpgradeRenderHandler> renderHandlers = UpgradeRenderHandlerList.instance().getHandlersForSlot(slot);
            upgradeRenderersInserted[slot.getIndex()] = new boolean[renderHandlers.size()];
            upgradeRenderersEnabled[slot.getIndex()] = new boolean[renderHandlers.size()];
            upgradeMatrix[slot.getIndex()] = new int[EnumUpgrade.values().length];
        }
        Arrays.fill(startupTimes, 200);
        isValid = true;
    }

    private static CommonArmorHandler getManagerInstance(PlayerEntity player) {
        return player.world.isRemote ? clientHandler : serverHandler;
    }

    public static CommonArmorHandler getHandlerForPlayer(PlayerEntity player) {
        return getManagerInstance(player).playerHudHandlers.computeIfAbsent(player.getUniqueID(), v -> new CommonArmorHandler(player));
    }

    public static CommonArmorHandler getHandlerForPlayer() {
        return getHandlerForPlayer(PneumaticCraftRepressurized.proxy.getClientPlayer());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            getHandlerForPlayer(event.player).tick();
        }
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        // called server side when player logs off
        clearHUDHandlerForPlayer(event.getPlayer());
    }

    @SubscribeEvent
    public static void onPlayerJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntity();
            CommonArmorHandler handler = getManagerInstance(player).playerHudHandlers.get(player.getUniqueID());
            if (handler != null) handler.player = player;
        }
    }

    @SubscribeEvent
    public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        // called client side when client disconnects
        PlayerEntity player = PneumaticCraftRepressurized.proxy.getClientPlayer();
        if (player != null) {
            clearHUDHandlerForPlayer(player);
        }
    }

    private static void clearHUDHandlerForPlayer(PlayerEntity player) {
        CommonArmorHandler h = getManagerInstance(player);
        h.playerHudHandlers.computeIfPresent(player.getUniqueID(), (name, val) -> { val.invalidate(); return null; } );
    }

    private void tick() {
        for (EquipmentSlotType slot : UpgradeRenderHandlerList.ARMOR_SLOTS) {
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
            armorPressure[slot.getIndex()] = ((IPressurizable) armorStack.getItem()).getPressure(armorStack);
            if (ticksSinceEquip[slot.getIndex()] == 0) {
                initArmorInventory(slot);
            }
            ticksSinceEquip[slot.getIndex()]++;
            if (armorEnabled && armorPressure[slot.getIndex()] > 0F) {
                armorActive = true;
                if (!player.world.isRemote) {
                    if (isArmorReady(slot) && !player.isCreative()) {
                        // use up air in the armor piece
                        float airUsage = UpgradeRenderHandlerList.instance().getAirUsage(player, slot, false);
                        if (airUsage != 0) {
                            float oldPressure = addAir(slot, (int) -airUsage);
                            if (oldPressure > 0F && armorPressure[slot.getIndex()] == 0F) {
                                // out of air!
                                NetworkHandler.sendToPlayer(new PacketPlaySound(ModSounds.MINIGUN_STOP, SoundCategory.PLAYERS, player.posX, player.posY, player.posZ, 1.0f, 2.0f, false), (ServerPlayerEntity) player);
                            }
                        }
                    }
                }
                doArmorActions(slot);
            }
        }
        if (!armorActive) {
            if (ticksSinceEquip[slot.getIndex()] > 0) {
                onArmorRemoved(slot);
            }
            ticksSinceEquip[slot.getIndex()] = 0;
        }
    }

    /*
     * Called when an armor piece is removed, or otherwise disabled - out of air, armor disabled
     */
    private void onArmorRemoved(EquipmentSlotType slot) {
        switch (slot) {
            case HEAD:
                if (nightVisionEnabled) player.removeActivePotionEffect(Effects.NIGHT_VISION);
                break;
            case FEET:
                player.stepHeight = 0.6F;
                break;
        }
    }

    public float addAir(EquipmentSlotType slot, int air) {
        ItemStack armorStack = player.getItemStackFromSlot(slot);
        float oldPressure = armorPressure[slot.getIndex()];
        if (armorStack.getItem() instanceof IPressurizable) {
            ((IPressurizable) armorStack.getItem()).addAir(armorStack, air);
            armorPressure[slot.getIndex()] = ((IPressurizable) armorStack.getItem()).getPressure(armorStack);
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
                break;
            case LEGS:
                handleLeggingsSpeedBoost();
                break;
            case FEET:
                if (getArmorPressure(EquipmentSlotType.FEET) > 0.0F && isStepAssistEnabled()) {
                    player.stepHeight = player.isSneaking() ? 0.6001F : 1.25F;
                } else {
                    player.stepHeight = 0.6F;
                }
                handleJetBoots();
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
                addAir(EquipmentSlotType.HEAD, -PneumaticValues.PNEUMATIC_NIGHT_VISION_USAGE * 8);
            } else if (!shouldEnable && wasNightVisionEnabled) {
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

            int vol = ((ItemPneumaticArmor) helmetStack.getItem()).getBaseVolume() + PneumaticValues.VOLUME_VOLUME_UPGRADE * getUpgradeCount(EquipmentSlotType.HEAD, EnumUpgrade.VOLUME);
            float airInHelmet = getArmorPressure(EquipmentSlotType.HEAD) * vol;
            int playerAir = (int) Math.min(300 - player.getAir(), airInHelmet / PneumaticValues.PNEUMATIC_HELMET_SCUBA_MULTIPLIER);
            player.setAir(player.getAir() + playerAir);

            int airUsed = playerAir * PneumaticValues.PNEUMATIC_HELMET_SCUBA_MULTIPLIER;
            addAir(EquipmentSlotType.HEAD, -airUsed);
            NetworkHandler.sendToPlayer(new PacketPlaySound(ModSounds.SCUBA, SoundCategory.PLAYERS, player.getPosition(), 1.5f, 1.0f, false), (ServerPlayerEntity) player);
            Vec3d eyes = player.getEyePosition(1.0f).add(player.getLookVec().scale(0.5));
            NetworkHandler.sendToAllAround(new PacketSpawnParticle(ParticleTypes.BUBBLE, eyes.x - 0.5, eyes.y, eyes.z -0.5, 0.0, 0.2, 0.0, 10, 1.0, 1.0, 1.0), player.world);
        }
    }

    // track player movement across ticks on the server - very transient, a capability would be overkill here
    private static final Map<UUID,Vec3d> moveMap = new HashMap<>();

    private void handleLeggingsSpeedBoost() {
        double speedBoost = getSpeedBoostFromLegs();
        if (player.world.isRemote) {
            // doing this client-side only appears to be effective
            if (player.moveForward > 0) {
                if (!player.onGround && isJetBootsEnabled() && jetBootsBuilderMode) {
                    player.moveRelative(getUpgradeCount(EquipmentSlotType.FEET, EnumUpgrade.JET_BOOTS) / 400f, new Vec3d(0, 0, 1));
                }
                if (player.onGround && !player.isInWater()) {
                    player.moveRelative((float) speedBoost, new Vec3d(0, 0, 1));
                }
            }
        }
        if (!player.world.isRemote && speedBoost > 0) {
            Vec3d prev = moveMap.get(player.getUniqueID());
            boolean moved = prev != null && (Math.abs(player.posX - prev.x) > 0.0001 || Math.abs(player.posZ - prev.z) > 0.0001);
            if (moved && player.onGround && !player.isInWater()) {
                int airUsage = (int) Math.ceil(PneumaticValues.PNEUMATIC_LEGS_SPEED_USAGE * speedBoost * 4);
                addAir(EquipmentSlotType.LEGS, -airUsage);
            }
            moveMap.put(player.getUniqueID(), new Vec3d(player.posX, player.posY, player.posZ));
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

    private void setYMotion(Entity entity, double y) {
        Vec3d v = entity.getMotion();
        v = v.add(0, y - v.y, 0);
        entity.setMotion(v);
    }

    private void handleJetBoots() {
        int jetbootsCount = getUpgradeCount(EquipmentSlotType.FEET, EnumUpgrade.JET_BOOTS, PneumaticValues.PNEUMATIC_JET_BOOTS_MAX_UPGRADES);
        if (jetbootsCount == 0) return;

        int jetbootsAirUsage = 0;
        if (getArmorPressure(EquipmentSlotType.FEET) > 0.0F) {
            if (isJetBootsActive()) {
                if (jetBootsBuilderMode && jetbootsCount >= 8) {
                    // builder mode - rise vertically (or hover if sneaking and firing)
                    setYMotion(player, player.isSneaking() ? 0 : 0.15 + 0.15 * (jetbootsCount - 8));
                    jetbootsAirUsage = (int) (PNCConfig.Common.Armor.jetBootsAirUsage * jetbootsCount / 5F);
                } else {
                    // jetboots firing - move in direction of looking
                    Vec3d lookVec = player.getLookVec().scale(0.15 * jetbootsCount);
                    flightAccel += lookVec.y / -20.0;
                    flightAccel = MathHelper.clamp(flightAccel, 0.8F, 4.0F);
                    lookVec = lookVec.scale(flightAccel);
                    if (jetBootsActiveTicks < 10) lookVec = lookVec.scale(jetBootsActiveTicks * 0.1);
                    player.setMotion(lookVec.x, player.onGround ? 0 : lookVec.y, lookVec.z);
                    jetbootsAirUsage = PNCConfig.Common.Armor.jetBootsAirUsage * jetbootsCount;
                }
                jetBootsActiveTicks++;
            } else if (isJetBootsEnabled() && !player.onGround) {
                // jetboots not firing, but enabled - slowly descend (or hover if enough upgrades)
                if (jetbootsCount > 6 && !player.isSneaking()) setYMotion(player, 0.0);
                else setYMotion(player, player.isSneaking() ? -0.45 : -0.15 + 0.015 * jetbootsCount);
                player.fallDistance = 0;
                jetbootsAirUsage = (int) (PNCConfig.Common.Armor.jetBootsAirUsage * (player.isSneaking() ? 0.25F : 0.5F));
                flightAccel = 1.0F;
            } else {
                flightAccel = 1.0F;
            }
        }
        if (jetbootsAirUsage != 0 && !player.world.isRemote) {
            if (prevJetBootsAirUsage == 0) {
                NetworkHandler.sendToDimension(new PacketPlayMovingSound(MovingSounds.Sound.JET_BOOTS, player), player.world.getDimension().getType());
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
            if (armorPressure[EquipmentSlotType.CHEST.getIndex()] < 0.1F) return;
            ItemStack stack = player.getItemStackFromSlot(slot);
            tryPressurize(airAmount, stack);
        }
        for (ItemStack stack : player.inventory.mainInventory) {
            if (armorPressure[EquipmentSlotType.CHEST.getIndex()] < 0.1F) return;
            tryPressurize(airAmount, stack);
        }
    }

    private void tryPressurize(int airAmount, ItemStack destStack) {
        if (destStack.getItem() instanceof IPressurizable) {
            IPressurizable p = (IPressurizable) destStack.getItem();
            float pressure = p.getPressure(destStack);
            if (pressure < p.maxPressure(destStack) && pressure < armorPressure[EquipmentSlotType.CHEST.getIndex()]) {
                float currentAir = pressure * p.getVolume(destStack);
                float targetAir = armorPressure[EquipmentSlotType.CHEST.getIndex()] * p.getVolume(destStack);
                int amountToMove = Math.min((int)(targetAir - currentAir), airAmount);
                p.addAir(destStack, amountToMove);
                addAir(EquipmentSlotType.CHEST, -amountToMove);
            }
        }
    }

    private void handleItemRepair(EquipmentSlotType slot) {
        int upgrades = getUpgradeCount(slot, EnumUpgrade.ITEM_LIFE, PneumaticValues.ARMOR_REPAIR_MAX_UPGRADES);
        int interval = 120 - (20 * upgrades);
        int airUsage = PneumaticValues.PNEUMATIC_ARMOR_REPAIR_USAGE * upgrades;

        ItemStack armorStack = player.getItemStackFromSlot(slot);
        if (armorStack.getDamage() > 0
                && armorPressure[slot.getIndex()] > 0.1F
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

        Vec3d playerVec = player.getPositionVector();
        for (Entity item : itemList) {
            if (item instanceof ItemEntity && ((ItemEntity) item).cannotPickup()) continue;

            if (item.getPositionVector().squareDistanceTo(playerVec) <= magnetRadiusSq
                    && !ItemRegistry.getInstance().shouldSuppressMagnet(item)
                    && !item.getPersistentData().getBoolean(Names.PREVENT_REMOTE_MOVEMENT)) {
                if (armorPressure[EquipmentSlotType.CHEST.getIndex()] < 0.1F) break;
                item.setPosition(player.posX, player.posY, player.posZ);
                if (item instanceof ItemEntity) ((ItemEntity) item).setPickupDelay(0);
                addAir(EquipmentSlotType.CHEST, -PneumaticValues.MAGNET_AIR_USAGE);
            }
        }
    }

    private void handleHacking() {
        if (hackedBlockPos != null) {
            IHackableBlock hackableBlock = HackableHandler.getHackableForCoord(hackedBlockPos, player);
            if (hackableBlock != null) {
                World world = PneumaticCraftUtils.getWorldForGlobalPos(hackedBlockPos);
                if (world != null && ++hackTime >= hackableBlock.getHackTime(world, hackedBlockPos.getPos(), player)) {
                    hackableBlock.onHackFinished(player.world, hackedBlockPos.getPos(), player);
                    PneumaticCraftRepressurized.proxy.getHackTickHandler().trackBlock(hackedBlockPos, hackableBlock);
                    NetworkHandler.sendToAllAround(new PacketHackingBlockFinish(hackedBlockPos), player.world);
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
                    hackableEntity.onHackFinished(hackedEntity, player);
                    PneumaticCraftRepressurized.proxy.getHackTickHandler().trackEntity(hackedEntity, hackableEntity);
                    NetworkHandler.sendToAllAround(new PacketHackingEntityFinish(hackedEntity), new PacketDistributor.TargetPoint(hackedEntity.posX, hackedEntity.posY, hackedEntity.posZ, 64, hackedEntity.world.getDimension().getType()));
                    setHackedEntity(null);
                    AdvancementTriggers.ENTITY_HACK.trigger((ServerPlayerEntity) player);  // safe to cast, this is server-side
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
            upgradeRenderersInserted[slot.getIndex()][i] = isModuleEnabled(upgradeStacks, UpgradeRenderHandlerList.instance().getHandlersForSlot(slot).get(i));
        }

        // record the number of upgrades of every type
        Arrays.fill(upgradeMatrix[slot.getIndex()], 0);
        for (ItemStack stack : upgradeStacks) {
            if (stack.getItem() instanceof ItemMachineUpgrade) {
                upgradeMatrix[slot.getIndex()][((ItemMachineUpgrade) stack.getItem()).getUpgradeType().ordinal()] += stack.getCount();
            }
        }
        startupTimes[slot.getIndex()] = (int) (PNCConfig.Common.Armor.armorStartupTime * Math.pow(0.8, getSpeedFromUpgrades(slot) - 1));

        // some slot-specific setup
        switch (slot) {
            case CHEST:
                magnetRadius = PneumaticValues.MAGNET_BASE_RANGE
                        + Math.min(getUpgradeCount(EquipmentSlotType.CHEST, EnumUpgrade.MAGNET), PneumaticValues.MAGNET_MAX_UPGRADES);
                magnetRadiusSq = magnetRadius * magnetRadius;
                break;
            case LEGS:
                speedBoostMult = ItemPneumaticArmor.getIntData(armorStack, ItemPneumaticArmor.NBT_SPEED_BOOST, 100) / 100f;
                break;
            case FEET:
                jetBootsBuilderMode = ItemPneumaticArmor.getBooleanData(armorStack, ItemPneumaticArmor.NBT_BUILDER_MODE, false);
                JetBootsStateTracker.getTracker(player).setJetBootsState(player, isJetBootsEnabled(), isJetBootsActive(), jetBootsBuilderMode);
                break;
        }
    }

    public int getUpgradeCount(EquipmentSlotType slot, EnumUpgrade upgrade) {
        return upgradeMatrix[slot.getIndex()][upgrade.ordinal()];
    }

    public int getUpgradeCount(EquipmentSlotType slot, EnumUpgrade upgrade, int max) {
        return Math.min(max, upgradeMatrix[slot.getIndex()][upgrade.ordinal()]);
    }

    public boolean isUpgradeRendererInserted(EquipmentSlotType slot, int i) {
        return upgradeRenderersInserted[slot.getIndex()][i];
    }

    public boolean isUpgradeRendererEnabled(EquipmentSlotType slot, int i) {
        return upgradeRenderersEnabled[slot.getIndex()][i];
    }

    public void setUpgradeRenderEnabled(EquipmentSlotType slot, byte featureIndex, boolean state) {
        upgradeRenderersEnabled[slot.getIndex()][featureIndex] = state;
        IUpgradeRenderHandler handler = UpgradeRenderHandlerList.instance().getHandlersForSlot(slot).get(featureIndex);
        // bit of a code smell here, but caching the enablement of various features is important for performance
        if (handler instanceof MagnetUpgradeHandler) {
            magnetEnabled = state;
        } else if (handler instanceof ChargingUpgradeHandler) {
            chargingEnabled = state;
        } else if (handler instanceof StepAssistUpgradeHandler) {
            stepAssistEnabled = state;
        } else if (handler instanceof RunSpeedUpgradeHandler) {
            runSpeedEnabled = state;
        } else if (handler instanceof JumpBoostUpgradeHandler) {
            jumpBoostEnabled = state;
        } else if (handler instanceof JetBootsUpgradeHandler) {
            jetBootsEnabled = state;
            JetBootsStateTracker.getTracker(player).setJetBootsState(player, jetBootsEnabled, isJetBootsActive(), isJetBootsBuilderMode());
        } else if (handler instanceof MainHelmetHandler) {
            armorEnabled = state;
        } else if (handler instanceof EntityTrackUpgradeHandler) {
            entityTrackerEnabled = state;
        } else if (handler instanceof NightVisionUpgradeHandler) {
            nightVisionEnabled = state;
        } else if (handler instanceof ScubaUpgradeHandler) {
            scubaEnabled = state;
        } else if (handler instanceof AirConUpgradeHandler) {
            airConEnabled = state;
        }
    }

    public int getTicksSinceEquipped(EquipmentSlotType slot) {
        return ticksSinceEquip[slot.getIndex()];
    }

    private boolean isModuleEnabled(ItemStack[] helmetStacks, IUpgradeRenderHandler handler) {
        for (Item requiredUpgrade : handler.getRequiredUpgrades()) {
            boolean found = false;
            for (ItemStack stack : helmetStacks) {
                if (stack.getItem() == requiredUpgrade) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    public int getSpeedFromUpgrades(EquipmentSlotType slot) {
        return 1 + getUpgradeCount(slot, EnumUpgrade.SPEED);
    }

    public int getStartupTime(EquipmentSlotType slot) {
        return startupTimes[slot.getIndex()];
    }

    public void setHackedBlockPos(GlobalPos blockPos) {
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

    public boolean isAirConEnabled() {
        return airConEnabled;
    }

    public float getArmorPressure(EquipmentSlotType slot) {
        return armorPressure[slot.getIndex()];
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
     * Called both client- and server-side when a custom NBT field in an armor item has been updated.  Used to
     * cache data (e.g. legs speed boost %) for performance reasons.
     *
     * @param slot the armor slot
     * @param key the data key
     * @param dataTag the data item, to be interpreted depending on the key
     */
    public void onDataFieldUpdated(EquipmentSlotType slot, String key, INBT dataTag) {
        switch (key) {
            case ItemPneumaticArmor.NBT_SPEED_BOOST:
                speedBoostMult = MathHelper.clamp(((IntNBT) dataTag).getInt() / 100f, 0.0f, 1.0f);
                break;
            case ItemPneumaticArmor.NBT_BUILDER_MODE:
                jetBootsBuilderMode = ((ByteNBT) dataTag).getByte() == 1;
                JetBootsStateTracker.getTracker(player).getJetBootsState(player).setBuilderMode(jetBootsBuilderMode);
                break;
        }
    }
}
