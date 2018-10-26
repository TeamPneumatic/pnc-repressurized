package me.desht.pneumaticcraft.common;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableBlock;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableEntity;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.UpgradeRenderHandlerList;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.hacking.HackableHandler;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.renderHandler.*;
import me.desht.pneumaticcraft.client.sound.MovingSounds;
import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.item.ItemMachineUpgrade;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.item.ItemRegistry;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.common.util.WorldAndCoord;
import me.desht.pneumaticcraft.lib.EnumCustomParticleType;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Sounds;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

public class CommonHUDHandler {
    private static final CommonHUDHandler clientHandler = new CommonHUDHandler();
    private static final CommonHUDHandler serverHandler = new CommonHUDHandler();

    private static final UUID PNEUMATIC_SPEED_ID = UUID.fromString("6ecaf25b-9619-4fd1-ae4c-c2f1521047d7");
    private static Potion nightVisionPotion;

    private final HashMap<String, CommonHUDHandler> playerHudHandlers = new HashMap<>();
    private int magnetRadius;
    private int magnetRadiusSq;
    private final boolean[][] upgradeRenderersInserted = new boolean[4][];
    private final boolean[][] upgradeRenderersEnabled = new boolean[4][];
    private final int[] ticksSinceEquip = new int[4];
    public final float[] armorPressure = new float[4];
    private final int[][] upgradeMatrix = new int [4][];

    private boolean isValid; // true if the handler is valid; gets invalidated if player disconnects

    private int hackTime;
    private WorldAndCoord hackedBlock;
    private Entity hackedEntity;

    private boolean armorEnabled;
    private boolean magnetEnabled;
    private boolean chargingEnabled;
    private boolean stepAssistEnabled;
    private boolean runSpeedEnabled;
    private boolean jumpBoostEnabled;
    private boolean entityTrackerEnabled;
    private boolean nightVisionEnabled;
    private boolean jetBootsEnabled;  // are jet boots switched on?
    private boolean jetBootsActive;  // are jet boots actually firing (player rising) ?
    private float flightAccel = 1.0F;  // increases while diving, decreases while climbing
    private int prevJetBootsAirUsage;  // so we know when the jet boots are starting up
    private int jetBootsActiveTicks;
    private boolean wasNightVisionEnabled;
    private float speedBoostMult;

    public CommonHUDHandler() {
        for (EntityEquipmentSlot slot : UpgradeRenderHandlerList.ARMOR_SLOTS) {
            List<IUpgradeRenderHandler> renderHandlers = UpgradeRenderHandlerList.instance().getHandlersForSlot(slot);
            upgradeRenderersInserted[slot.getIndex()] = new boolean[renderHandlers.size()];
            upgradeRenderersEnabled[slot.getIndex()] = new boolean[renderHandlers.size()];
            upgradeMatrix[slot.getIndex()] = new int[EnumUpgrade.values().length];
        }
        isValid = true;
    }

    private static CommonHUDHandler getManagerInstance(EntityPlayer player) {
        return player.world.isRemote ? clientHandler : serverHandler;
    }

    public static CommonHUDHandler getHandlerForPlayer(EntityPlayer player) {
        return getManagerInstance(player).playerHudHandlers.computeIfAbsent(player.getName(), v -> new CommonHUDHandler());
    }

    @SideOnly(Side.CLIENT)
    public static CommonHUDHandler getHandlerForPlayer() {
        return getHandlerForPlayer(FMLClientHandler.instance().getClient().player);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            getHandlerForPlayer(event.player).tick(event.player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        // called server side when player logs off
        clearHUDHandlerForPlayer(event.player);
    }

    @SubscribeEvent
    public static void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        // called client side when client disconnects
        EntityPlayer player = PneumaticCraftRepressurized.proxy.getClientPlayer();
        if (player != null) {
            clearHUDHandlerForPlayer(player);
        }
    }

    private static void clearHUDHandlerForPlayer(EntityPlayer player) {
        CommonHUDHandler h = getManagerInstance(player);
        h.playerHudHandlers.computeIfPresent(player.getName(), (name, val) -> { val.invalidate(); return null; } );
    }

    private static Potion getNightVisionPotion() {
        if (nightVisionPotion == null) nightVisionPotion = Potion.getPotionFromResourceLocation("night_vision");
        return nightVisionPotion;
    }

    private void tick(EntityPlayer player) {
        for (EntityEquipmentSlot slot : UpgradeRenderHandlerList.ARMOR_SLOTS) {
            tickArmorPiece(player, slot);
        }
        if (!player.world.isRemote) handleHacking(player);
    }

    private void tickArmorPiece(EntityPlayer player, EntityEquipmentSlot slot) {
        ItemStack armorStack = player.getItemStackFromSlot(slot);
        boolean armorActive = false;
        if (armorStack.getItem() instanceof ItemPneumaticArmor) {
            armorPressure[slot.getIndex()] = ((IPressurizable) armorStack.getItem()).getPressure(armorStack);
            if (ticksSinceEquip[slot.getIndex()] == 0) {
                initArmorInventory(player, slot);
            }
            ticksSinceEquip[slot.getIndex()]++;
            if (armorEnabled && armorPressure[slot.getIndex()] > 0F) {
                armorActive = true;
                if (!player.world.isRemote) {
                    if (isArmorReady(slot) && !player.capabilities.isCreativeMode) {
                        // use up air in the armor piece
                        float airUsage = UpgradeRenderHandlerList.instance().getAirUsage(player, slot, false);
                        if (airUsage != 0) {
                            float oldPressure = addAir(armorStack, slot, (int) -airUsage);
                            if (oldPressure > 0F && armorPressure[slot.getIndex()] == 0F) {
                                // out of air!
                                NetworkHandler.sendTo(new PacketPlaySound(Sounds.MINIGUN_STOP, SoundCategory.PLAYERS, player.posX, player.posY, player.posZ, 1.0f, 2.0f, false), (EntityPlayerMP) player);
                            }
                        }
                    }
                }
                doArmorActions(player, armorStack, slot);
            }
        }
        if (!armorActive) {
            if (ticksSinceEquip[slot.getIndex()] > 0) {
                onArmorRemoved(player, armorStack, slot);
            }
            ticksSinceEquip[slot.getIndex()] = 0;
        }
    }

    /*
     * Called when an armor piece is removed, or otherwise disabled - out of air, armor disabled
     */
    private void onArmorRemoved(EntityPlayer player, ItemStack armorStack, EntityEquipmentSlot slot) {
        switch (slot) {
            case HEAD:
                if (nightVisionEnabled) player.removeActivePotionEffect(getNightVisionPotion());
                break;
            case FEET:
                player.stepHeight = 0.6F;
                break;
            case LEGS:
                IAttributeInstance attr = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
                AttributeModifier modifier = attr.getModifier(PNEUMATIC_SPEED_ID);
                if (modifier != null) {
                    attr.removeModifier(modifier);
                }
                break;
        }
    }

    public float addAir(ItemStack armorStack, EntityEquipmentSlot slot, int air) {
        float oldPressure = armorPressure[slot.getIndex()];
        ((IPressurizable) armorStack.getItem()).addAir(armorStack, air);
        armorPressure[slot.getIndex()] = ((IPressurizable) armorStack.getItem()).getPressure(armorStack);
        return oldPressure;
    }

    private void doArmorActions(EntityPlayer player, ItemStack armorStack, EntityEquipmentSlot slot) {
        if (!isArmorReady(slot)) return;

        switch (slot) {
            case HEAD:
                handleNightVision(player, armorStack);
                break;
            case CHEST:
                handleChestplateMagnet(player, armorStack);
                handleChestplateCharging(player, armorStack);
                break;
            case LEGS:
                handleLeggingsSpeedBoost(player, armorStack);
                break;
            case FEET:
                if (getArmorPressure(EntityEquipmentSlot.FEET) > 0.0F && isStepAssistEnabled()) {
                    player.stepHeight = player.isSneaking() ? 0.6001F : 1.25F;
                } else {
                    player.stepHeight = 0.6F;
                }
                handleJetBoots(player, armorStack);
                break;
        }

        if (!player.world.isRemote && getUpgradeCount(slot, EnumUpgrade.ITEM_LIFE) > 0) {
            handleItemRepair(armorStack, slot);
        }
    }

    private void handleNightVision(EntityPlayer player, ItemStack armorStack) {
        // checking every 8 ticks should be enough
        if (!player.world.isRemote && (getTicksSinceEquipped(EntityEquipmentSlot.HEAD) & 0x7) == 0) {
            boolean shouldEnable = getArmorPressure(EntityEquipmentSlot.HEAD) > 0.0f
                    && getUpgradeCount(EntityEquipmentSlot.HEAD, EnumUpgrade.NIGHT_VISION) > 0
                    && nightVisionEnabled;
            if (shouldEnable) {
                ItemStack helmetStack = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
                player.addPotionEffect(new PotionEffect(getNightVisionPotion(), 500, 0, false, false));
                addAir(helmetStack, EntityEquipmentSlot.HEAD, -PneumaticValues.PNEUMATIC_NIGHT_VISION_USAGE * 8);
            } else if (!shouldEnable && wasNightVisionEnabled) {
                player.removePotionEffect(getNightVisionPotion());
            }
            wasNightVisionEnabled = shouldEnable;
        }
    }

    // track player movement across ticks on the server - very transient, a capability would be overkill here
    private static final Map<String,Vec3d> moveMap = new HashMap<>();

    private void handleLeggingsSpeedBoost(EntityPlayer player, ItemStack armorStack) {
        // check every 8 ticks
        if (!player.world.isRemote && (getTicksSinceEquipped(EntityEquipmentSlot.LEGS) & 0x7) == 0) {
            // we know it's pneumatic leggings at this point
            ItemStack legsStack = player.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
            int speedUpgrades = getUpgradeCount(EntityEquipmentSlot.LEGS, EnumUpgrade.SPEED, PneumaticValues.PNEUMATIC_LEGS_MAX_SPEED);
            if (getArmorPressure(EntityEquipmentSlot.LEGS) > 0.0F && speedUpgrades > 0) {
                double speedBoost = isRunSpeedEnabled() ? PneumaticValues.PNEUMATIC_LEGS_BOOST_PER_UPGRADE * speedUpgrades * speedBoostMult : 0.0;
                IAttributeInstance attr = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
                AttributeModifier modifier = attr.getModifier(PNEUMATIC_SPEED_ID);
                if (modifier == null || modifier.getAmount() != speedBoost) {
                    if (modifier != null) attr.removeModifier(modifier);
                    attr.applyModifier(new AttributeModifier(PNEUMATIC_SPEED_ID, SharedMonsterAttributes.MOVEMENT_SPEED.getName(), speedBoost, 1));
                }
                if (isRunSpeedEnabled()) {
                    Vec3d prev = moveMap.get(player.getName());
                    boolean moved = prev != null && (Math.abs(player.posX - prev.x) > 0.0001 || Math.abs(player.posZ - prev.z) > 0.0001);
                    if (moved && player.onGround && !player.isInsideOfMaterial(Material.WATER)) {
                        int airUsage = (int) Math.ceil(8 * PneumaticValues.PNEUMATIC_LEGS_SPEED_USAGE * speedUpgrades * speedBoostMult + 0.5f);
                        addAir(legsStack, EntityEquipmentSlot.LEGS, -airUsage);
                    }
                }
            }
            moveMap.put(player.getName(), new Vec3d(player.posX, player.posY, player.posZ));
        }
    }

    private void handleJetBoots(EntityPlayer player, ItemStack bootsStack) {
        int jetbootsCount = getUpgradeCount(EntityEquipmentSlot.FEET, EnumUpgrade.JET_BOOTS, PneumaticValues.PNEUMATIC_JET_BOOTS_MAX_UPGRADES);
        if (jetbootsCount == 0) return;

        int jetbootsAirUsage = 0;
        Vec3d lookVec = new Vec3d(0, 0.5, 0);
        if (getArmorPressure(EntityEquipmentSlot.FEET) > 0.0F) {
            if (isJetBootsActive()) {
                // jetboots firing - move in direction of looking
                lookVec = player.getLookVec().normalize().scale(0.15 * jetbootsCount);
                flightAccel += lookVec.y / -20.0;
                flightAccel = MathHelper.clamp(flightAccel, 0.8F, 4.0F);
                lookVec = lookVec.scale(flightAccel);
                if (jetBootsActiveTicks < 10) lookVec = lookVec.scale(jetBootsActiveTicks * 0.1);
                player.motionX = lookVec.x;
                player.motionY = player.onGround ? 0 : lookVec.y;
                player.motionZ = lookVec.z;
                jetbootsAirUsage = PneumaticValues.PNEUMATIC_JET_BOOTS_USAGE * jetbootsCount;
                jetBootsActiveTicks++;
            } else if (isJetBootsEnabled() && !player.onGround) {
                // jetboots not firing, but enabled - slowly descend (or hover)
                player.motionY = player.isSneaking() ? -0.45 : -0.15 + 0.015 * jetbootsCount;
                player.fallDistance = 0;
                jetbootsAirUsage = (int) (PneumaticValues.PNEUMATIC_JET_BOOTS_USAGE * (player.isSneaking() ? 0.25F : 0.5F));
                flightAccel = 1.0F;
            } else {
                flightAccel = 1.0F;
            }
        }
        if (jetbootsAirUsage != 0) {
            if (!player.world.isRemote) {
                if (prevJetBootsAirUsage == 0) {
                    NetworkHandler.sendToDimension(new PacketPlayMovingSound(MovingSounds.Sound.JET_BOOTS, player), player.world.provider.getDimension());
                    AdvancementTriggers.FLIGHT.trigger((EntityPlayerMP) player);
                }
                if (player.collidedHorizontally) {
                    double vel = Math.sqrt(player.motionZ * player.motionZ + player.motionX * player.motionX);
                    if (player.world.getDifficulty() == EnumDifficulty.HARD) {
                        vel *= 2;
                    } else if (player.world.getDifficulty() == EnumDifficulty.NORMAL) {
                        vel *= 1.5;
                    }
                    if (vel > 2) {
                        player.playSound(vel > 2.5 ? SoundEvents.ENTITY_GENERIC_BIG_FALL : SoundEvents.ENTITY_GENERIC_SMALL_FALL, 1.0F, 1.0F);
                        player.attackEntityFrom(DamageSource.FLY_INTO_WALL, (float) vel);
                        AdvancementTriggers.FLY_INTO_WALL.trigger((EntityPlayerMP) player);
                    }
                }
                Vec3d jetVec = lookVec.scale(-0.5);
                NetworkHandler.sendToAllAround(
                        new PacketSpawnParticle(EnumCustomParticleType.AIR_PARTICLE_DENSE, player.posX, player.posY, player.posZ,
                                jetVec.x, jetVec.y * 0.75, jetVec.z, isJetBootsActive() ? 8 : 1, 0, 0, 0),
                        player.world);
                addAir(bootsStack, EntityEquipmentSlot.FEET, -jetbootsAirUsage);
            }
        }
        prevJetBootsAirUsage = jetbootsAirUsage;
    }

    private void handleChestplateCharging(EntityPlayer player, ItemStack chestplateStack) {
        if (player.world.isRemote || !chargingEnabled
                || getUpgradeCount(EntityEquipmentSlot.CHEST, EnumUpgrade.CHARGING) == 0
                || getTicksSinceEquipped(EntityEquipmentSlot.CHEST) % PneumaticValues.ARMOR_CHARGER_INTERVAL != 5)
            return;

        int upgrades = getUpgradeCount(EntityEquipmentSlot.CHEST, EnumUpgrade.CHARGING, PneumaticValues.ARMOR_CHARGING_MAX_UPGRADES);
        int airAmount = upgrades * 100 + 100;

        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            if (slot == EntityEquipmentSlot.CHEST) continue;
            if (armorPressure[EntityEquipmentSlot.CHEST.getIndex()] < 0.1F) return;
            ItemStack stack = player.getItemStackFromSlot(slot);
            tryPressurize(chestplateStack, airAmount, stack);
        }
        for (ItemStack stack : player.inventory.mainInventory) {
            if (armorPressure[EntityEquipmentSlot.CHEST.getIndex()] < 0.1F) return;
            tryPressurize(chestplateStack, airAmount, stack);
        }
    }

    private void tryPressurize(ItemStack chestplateStack, int airAmount, ItemStack destStack) {
        if (destStack.getItem() instanceof IPressurizable) {
            IPressurizable p = (IPressurizable) destStack.getItem();
            float pressure = p.getPressure(destStack);
            if (pressure < p.maxPressure(destStack) && pressure < armorPressure[EntityEquipmentSlot.CHEST.getIndex()]) {
                float currentAir = pressure * p.getVolume(destStack);
                float targetAir = armorPressure[EntityEquipmentSlot.CHEST.getIndex()] * p.getVolume(destStack);
                int amountToMove = Math.min((int)(targetAir - currentAir), airAmount);
                p.addAir(destStack, amountToMove);
                addAir(chestplateStack, EntityEquipmentSlot.CHEST, -amountToMove);
            }
        }
    }

    private void handleItemRepair(ItemStack armorStack, EntityEquipmentSlot slot) {
        int upgrades = getUpgradeCount(slot, EnumUpgrade.ITEM_LIFE, PneumaticValues.ARMOR_REPAIR_MAX_UPGRADES);
        int interval = 120 - (20 * upgrades);
        int airUsage = PneumaticValues.PNEUMATIC_ARMOR_REPAIR_USAGE * upgrades;

        if (armorStack.getItemDamage() > 0
                && armorPressure[slot.getIndex()] > 0.1F
                && ticksSinceEquip[slot.getIndex()] % interval == 0) {
            addAir(armorStack, slot, -airUsage);
            armorStack.setItemDamage(armorStack.getItemDamage() - 1);
        }
    }

    private void handleChestplateMagnet(EntityPlayer player, ItemStack chestplateStack) {
        if (player.world.isRemote || !magnetEnabled || (getTicksSinceEquipped(EntityEquipmentSlot.CHEST) & 0x7) != 0
                || getUpgradeCount(EntityEquipmentSlot.CHEST, EnumUpgrade.MAGNET) == 0)
            return;

        AxisAlignedBB box = new AxisAlignedBB(player.getPosition()).grow(magnetRadius);
        List<EntityItem> itemList = player.getEntityWorld().getEntitiesWithinAABB(EntityItem.class, box, EntitySelectors.IS_ALIVE);

        Vec3d playerVec = player.getPositionVector();
        for (EntityItem item : itemList) {
            if (!item.cannotPickup()
                    && item.getPositionVector().squareDistanceTo(playerVec) <= magnetRadiusSq
                    && !ItemRegistry.getInstance().shouldSuppressMagnet(item)
                    && !item.getEntityData().getBoolean(Names.PREVENT_REMOTE_MOVEMENT)) {
                if (armorPressure[EntityEquipmentSlot.CHEST.getIndex()] < 0.1F) break;
                item.setPosition(player.posX, player.posY, player.posZ);
                item.setPickupDelay(0);
                addAir(chestplateStack, EntityEquipmentSlot.CHEST, -PneumaticValues.MAGNET_AIR_USAGE);
            }
        }
    }

    private void handleHacking(EntityPlayer player) {
        if (hackedBlock != null) {
            IHackableBlock hackableBlock = HackableHandler.getHackableForCoord(hackedBlock, player);
            if (hackableBlock != null) {
                if (++hackTime >= hackableBlock.getHackTime(hackedBlock.world, hackedBlock.pos, player)) {
                    hackableBlock.onHackFinished(player.world, hackedBlock.pos, player);
                    PneumaticCraftRepressurized.proxy.getHackTickHandler().trackBlock(hackedBlock, hackableBlock);
                    NetworkHandler.sendToAllAround(new PacketHackingBlockFinish(hackedBlock), player.world);
                    setHackedBlock(null);
                    AdvancementTriggers.BLOCK_HACK.trigger((EntityPlayerMP) player);  // safe to cast, this is server-side
                }
            } else {
                setHackedBlock(null);
            }
        } else if (hackedEntity != null) {
            IHackableEntity hackableEntity = HackableHandler.getHackableForEntity(hackedEntity, player);
            if (hackableEntity != null) {
                if (++hackTime >= hackableEntity.getHackTime(hackedEntity, player)) {
                    hackableEntity.onHackFinished(hackedEntity, player);
                    PneumaticCraftRepressurized.proxy.getHackTickHandler().trackEntity(hackedEntity, hackableEntity);
                    NetworkHandler.sendToAllAround(new PacketHackingEntityFinish(hackedEntity), new TargetPoint(hackedEntity.world.provider.getDimension(), hackedEntity.posX, hackedEntity.posY, hackedEntity.posZ, 64));
                    setHackedEntity(null);
                    AdvancementTriggers.ENTITY_HACK.trigger((EntityPlayerMP) player);  // safe to cast, this is server-side
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
     * @param player player wearing the armor piece
     * @param slot the equipment slot
     */
    public void initArmorInventory(EntityPlayer player, EntityEquipmentSlot slot) {
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

        // some slot-specific setup
        switch (slot) {
            case CHEST:
                magnetRadius = PneumaticValues.MAGNET_BASE_RANGE
                        + Math.min(getUpgradeCount(EntityEquipmentSlot.CHEST, EnumUpgrade.MAGNET), PneumaticValues.MAGNET_MAX_UPGRADES);
                magnetRadiusSq = magnetRadius * magnetRadius;
                break;
            case LEGS:
                speedBoostMult = ItemPneumaticArmor.getIntData(armorStack, "speedBoost", 100) / 100f;
                break;
        }
    }

    public int getUpgradeCount(EntityEquipmentSlot slot, EnumUpgrade upgrade) {
        return upgradeMatrix[slot.getIndex()][upgrade.ordinal()];
    }

    public int getUpgradeCount(EntityEquipmentSlot slot, EnumUpgrade upgrade, int max) {
        return Math.min(max, upgradeMatrix[slot.getIndex()][upgrade.ordinal()]);
    }

    public boolean isUpgradeRendererInserted(EntityEquipmentSlot slot, int i) {
        return upgradeRenderersInserted[slot.getIndex()][i];
    }

    public boolean isUpgradeRendererEnabled(EntityEquipmentSlot slot, int i) {
        return upgradeRenderersEnabled[slot.getIndex()][i];
    }

    public void setUpgradeRenderEnabled(EntityEquipmentSlot slot, byte featureIndex, boolean state) {
        upgradeRenderersEnabled[slot.getIndex()][featureIndex] = state;
        IUpgradeRenderHandler handler = UpgradeRenderHandlerList.instance().getHandlersForSlot(slot).get(featureIndex);
        // bit of a code smell here, but caching the enablement of various features is important for performance
        if (handler instanceof MagnetUpgradeRenderHandler) {
            magnetEnabled = state;
        } else if (handler instanceof ChargingUpgradeRenderHandler) {
            chargingEnabled = state;
        } else if (handler instanceof StepAssistUpgradeHandler) {
            stepAssistEnabled = state;
        } else if (handler instanceof RunSpeedUpgradeHandler) {
            runSpeedEnabled = state;
        } else if (handler instanceof JumpBoostUpgradeHandler) {
            jumpBoostEnabled = state;
        } else if (handler instanceof JetBootsUpgradeHandler) {
            jetBootsEnabled = state;
        } else if (handler instanceof MainHelmetHandler) {
            armorEnabled = state;
        } else if (handler instanceof EntityTrackUpgradeHandler) {
            entityTrackerEnabled = state;
        } else if (handler instanceof NightVisionUpgradeHandler) {
            nightVisionEnabled = state;
        }
    }

    public int getTicksSinceEquipped(EntityEquipmentSlot slot) {
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

    public int getSpeedFromUpgrades() {
        return 1 + getUpgradeCount(EntityEquipmentSlot.HEAD, EnumUpgrade.SPEED);
    }

    public int getStartupTime(EntityEquipmentSlot slot) {
        int baseTime = slot == EntityEquipmentSlot.HEAD ? 200 : 100;
        return baseTime / getSpeedFromUpgrades();
    }

    public void setHackedBlock(WorldAndCoord blockPos) {
        hackedBlock = blockPos;
        hackedEntity = null;
        hackTime = 0;
    }

    public void setHackedEntity(Entity entity) {
        hackedEntity = entity;
        hackedBlock = null;
        hackTime = 0;
    }

    public boolean isArmorReady(EntityEquipmentSlot slot) {
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

    public float getArmorPressure(EntityEquipmentSlot slot) {
        return armorPressure[slot.getIndex()];
    }

    public void setJetBootsActive(boolean jetBootsActive, EntityPlayer player) {
        if (!player.world.isRemote && jetBootsActive != this.jetBootsActive) {
            NetworkHandler.sendToDimension(new PacketMarkPlayerJetbootsActive(player, jetBootsActive), player.world.provider.getDimension());
        }
        if (!jetBootsActive) jetBootsActiveTicks = 0;

        this.jetBootsActive = jetBootsActive;
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

    public boolean isValid() {
        return isValid;
    }

    public void invalidate() {
        isValid = false;
    }

    /**
     * Called server-side when a custom NBT field in an armor item has been updated.  Used to
     * cache data (e.g. legs speed boost %) for performance reasons.
     *
     * @param slot the armor slot
     * @param key the data key
     * @param dataTag the data item, to be interpreted depending on the key
     */
    public void onDataFieldUpdated(EntityEquipmentSlot slot, String key, NBTBase dataTag) {
        switch (key) {
            case "speedBoost":
                speedBoostMult = MathHelper.clamp(((NBTTagInt) dataTag).getInt() / 100f, 0.0f, 1.0f);
                break;
        }
    }
}
