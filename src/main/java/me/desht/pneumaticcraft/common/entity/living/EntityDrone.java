package me.desht.pneumaticcraft.common.entity.living;

import com.mojang.authlib.GameProfile;
import me.desht.pneumaticcraft.api.DamageSourcePneumaticCraft.DamageSourceDroneOverload;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.block.IPneumaticWrenchable;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.drone.IPathNavigator;
import me.desht.pneumaticcraft.api.drone.IPathfindHandler;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.lib.NBTKeys;
import me.desht.pneumaticcraft.api.pressure.PressureHelper;
import me.desht.pneumaticcraft.api.semiblock.SemiblockEvent;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IManoMeasurable;
import me.desht.pneumaticcraft.client.util.ProgressingLine;
import me.desht.pneumaticcraft.common.DroneRegistry;
import me.desht.pneumaticcraft.common.ai.*;
import me.desht.pneumaticcraft.common.ai.DroneAIManager.EntityAITaskEntry;
import me.desht.pneumaticcraft.common.capabilities.BasicAirHandler;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModEntities;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.debug.DroneDebugger;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityLogisticsFrame;
import me.desht.pneumaticcraft.common.item.ItemDrone;
import me.desht.pneumaticcraft.common.item.ItemGPSTool;
import me.desht.pneumaticcraft.common.item.ItemGunAmmo;
import me.desht.pneumaticcraft.common.item.ItemRegistry;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlayMovingSound.SoundSource;
import me.desht.pneumaticcraft.common.network.PacketShowWireframe;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetGoToLocation;
import me.desht.pneumaticcraft.common.thirdparty.RadiationSourceCheck;
import me.desht.pneumaticcraft.common.tileentity.PneumaticEnergyStorage;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import me.desht.pneumaticcraft.common.util.*;
import me.desht.pneumaticcraft.common.util.fakeplayer.DroneFakePlayer;
import me.desht.pneumaticcraft.common.util.fakeplayer.DroneItemHandler;
import me.desht.pneumaticcraft.common.util.fakeplayer.FakeNetHandlerPlayerServer;
import me.desht.pneumaticcraft.common.util.upgrade.IUpgradeHolder;
import me.desht.pneumaticcraft.common.util.upgrade.UpgradeCache;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.passive.IFlyingAnimal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;
import static net.minecraft.util.Direction.UP;

public class EntityDrone extends EntityDroneBase implements
        IManoMeasurable, IPneumaticWrenchable, IEntityAdditionalSpawnData,
        IHackableEntity, IDroneBase, IFlyingAnimal, IUpgradeHolder {

    private static final float LASER_EXTEND_SPEED = 0.05F;

    private static final DataParameter<Boolean> ACCELERATING = EntityDataManager.defineId(EntityDrone.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float> PRESSURE = EntityDataManager.defineId(EntityDrone.class, DataSerializers.FLOAT);
    private static final DataParameter<String> PROGRAM_KEY = EntityDataManager.defineId(EntityDrone.class, DataSerializers.STRING);
    private static final DataParameter<BlockPos> DUG_POS = EntityDataManager.defineId(EntityDrone.class, DataSerializers.BLOCK_POS);
    private static final DataParameter<Boolean> GOING_TO_OWNER = EntityDataManager.defineId(EntityDrone.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> DRONE_COLOR = EntityDataManager.defineId(EntityDrone.class, DataSerializers.INT);
    private static final DataParameter<Boolean> MINIGUN_ACTIVE = EntityDataManager.defineId(EntityDrone.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> HAS_MINIGUN = EntityDataManager.defineId(EntityDrone.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> AMMO = EntityDataManager.defineId(EntityDrone.class, DataSerializers.INT);
    private static final DataParameter<String> LABEL = EntityDataManager.defineId(EntityDrone.class, DataSerializers.STRING);
    private static final DataParameter<Integer> ACTIVE_WIDGET = EntityDataManager.defineId(EntityDrone.class, DataSerializers.INT);
    private static final DataParameter<BlockPos> TARGET_POS = EntityDataManager.defineId(EntityDrone.class, DataSerializers.BLOCK_POS);
    private static final DataParameter<ItemStack> HELD_ITEM = EntityDataManager.defineId(EntityDrone.class, DataSerializers.ITEM_STACK);
    private static final DataParameter<Integer> TARGET_ID = EntityDataManager.defineId(EntityDrone.class, DataSerializers.INT);

    private static final StringTextComponent DEF_DRONE_NAME = new StringTextComponent("Drone");

    public static final String NBT_DRONE_COLOR = "color";

    private static final HashMap<ITextComponent, Integer> LASER_COLOR_MAP = new HashMap<>();
    static {
        LASER_COLOR_MAP.put(new StringTextComponent("aureylian"), 0xff69b4);
        LASER_COLOR_MAP.put(new StringTextComponent("loneztar"), 0x00a0a0);
        LASER_COLOR_MAP.put(new StringTextComponent("jadedcat"), 0xa020f0);
        LASER_COLOR_MAP.put(new StringTextComponent("desht"), 0xff6000);
    }

    private final EntityDroneItemHandler droneItemHandler = new EntityDroneItemHandler(this);
    private final LazyOptional<IItemHandlerModifiable> droneItemHandlerCap = LazyOptional.of(() -> droneItemHandler);

    private final FluidTank fluidTank = new FluidTank(Integer.MAX_VALUE);
    private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(() -> fluidTank);

    private final PneumaticEnergyStorage energy = new PneumaticEnergyStorage(100000);
    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);

    private final ItemStackHandler upgradeInventory = new ItemStackHandler(9);
    private final UpgradeCache upgradeCache = new UpgradeCache(this);

    private BasicAirHandler airHandler;
    private final LazyOptional<IAirHandler> airCap = LazyOptional.of(this::getAirHandler);

    private final Map<Direction,Integer> emittingRedstoneValues = new EnumMap<>(Direction.class);
    private float propSpeed;

    private ProgressingLine targetLine;
    private ProgressingLine oldTargetLine;

    public List<IProgWidget> progWidgets = new ArrayList<>();

    private DroneFakePlayer fakePlayer;
    public ITextComponent ownerName = DEF_DRONE_NAME;
    private UUID ownerUUID;

    private final DroneGoToChargingStation chargeAI;
    private DroneGoToOwner gotoOwnerAI;
    private final DroneAIManager aiManager = new DroneAIManager(this);

    private double droneSpeed;  // not to be confused with LivingEntity#speed
    private int healingInterval;
    private int suffocationCounter = 40; // Drones are immune to suffocation for this time.
    private boolean isSuffocating;
    private boolean disabledByHacking;
    private boolean standby; // If true, the drone's propellors stop, the drone will fall down, and won't use pressure.
    private Minigun minigun;
    private int attackCount; // tracks number of times drone has starting attacking something
    private BlockPos deployPos; // where the drone was deployed, accessible to programs as '$deploy_pos'

    private final DroneDebugger debugger = new DroneDebugger(this);

    private int securityUpgradeCount; // for liquid immunity: 1 = breathe in water, 2 = temporary air bubble, 3+ = permanent water removal
    private final Map<BlockPos, BlockState> displacedLiquids = new HashMap<>();  // liquid blocks displaced by security upgrade

    // Although this is only used by DroneAILogistics, it is here rather than there
    // so it can persist, for performance reasons; DroneAILogistics is a short-lived object
    private LogisticsManager logisticsManager;
    private final Map<Enchantment,Integer> stackEnchants = new HashMap<>();

    public EntityDrone(EntityType<? extends EntityDrone> type, World world) {
        super(type, world);
        moveControl = new DroneMovementController(this);
        goalSelector.addGoal(1, chargeAI = new DroneGoToChargingStation(this));
    }

    EntityDrone(EntityType<? extends EntityDrone> type, World world, PlayerEntity player) {
        this(type, world);
        if (player != null) {
            ownerUUID = player.getGameProfile().getId();
            ownerName = player.getName();
        } else {
            ownerUUID = getUUID(); // Anonymous drone used for Amadron or spawned with a Dispenser
            ownerName = DEF_DRONE_NAME;
        }
    }

    public EntityDrone(World world, PlayerEntity player) {
        this(ModEntities.DRONE.get(), world, player);
    }

    @SubscribeEvent
    public void onSemiblockEvent(SemiblockEvent event) {
        if (!event.getWorld().isClientSide && event.getWorld() == getCommandSenderWorld()
                && event.getSemiblock() instanceof EntityLogisticsFrame) {
            // semiblock has been added or removed; clear the cached logistics manager
            // next DroneAILogistics operation will search the area again
            logisticsManager = null;
        }
    }

    @Override
    protected PathNavigator createNavigation(World worldIn) {
        EntityPathNavigateDrone nav = new EntityPathNavigateDrone(this, worldIn);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    /**
     * Deserialize stored itemstack data when a drone item is deployed.
     * @param droneStack the drone itemstack
     */
    public void readFromItemStack(ItemStack droneStack) {
        Validate.isTrue(droneStack.getItem() instanceof ItemDrone);

        CompoundNBT stackTag = droneStack.getTag();
        if (stackTag != null) {
            upgradeInventory.deserializeNBT(stackTag.getCompound(UpgradableItemUtils.NBT_UPGRADE_TAG));
            stackEnchants.putAll(EnchantmentHelper.getEnchantments(droneStack));
            int air = droneStack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).orElseThrow(RuntimeException::new).getAir();
            getAirHandler().addAir(air);
            if (((ItemDrone) droneStack.getItem()).canProgram(droneStack)) {
                progWidgets = TileEntityProgrammer.getWidgetsFromNBT(stackTag);
                TileEntityProgrammer.updatePuzzleConnections(progWidgets);
            }
            setDroneColor(stackTag.getInt(NBT_DRONE_COLOR));
            fluidTank.setCapacity(PneumaticValues.DRONE_TANK_SIZE * (1 + getUpgrades(EnumUpgrade.INVENTORY)));
            droneItemHandler.setUseableSlots(1 + getUpgrades(EnumUpgrade.INVENTORY));
            if (stackTag.contains("Tank")) {
                fluidTank.readFromNBT(stackTag.getCompound("Tank"));
            }
        }
        if (droneStack.hasCustomHoverName()) setCustomName(droneStack.getHoverName());
    }

    /**
     * Serialize the necessary data to a drone item when the drone dies.
     * @param droneStack the drone itemstack
     */
    private void writeToItemStack(ItemStack droneStack) {
        Validate.isTrue(droneStack.getItem() instanceof ItemDrone);

        CompoundNBT tag = new CompoundNBT();
        tag.put(UpgradableItemUtils.NBT_UPGRADE_TAG, upgradeInventory.serializeNBT());
        if (((ItemDrone) droneStack.getItem()).canProgram(droneStack)) {
            TileEntityProgrammer.putWidgetsToNBT(progWidgets, tag);
        }
        tag.putInt("color", getDroneColor());
        if (!fluidTank.isEmpty()) {
            tag.put("Tank", fluidTank.writeToNBT(new CompoundNBT()));
        }
        droneStack.setTag(tag);
        EnchantmentHelper.setEnchantments(stackEnchants, droneStack);

        droneStack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).orElseThrow(RuntimeException::new).addAir(getAirHandler().getAir());

        if (hasCustomName()) droneStack.setHoverName(getCustomName());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        entityData.define(PRESSURE, 0.0f);
        entityData.define(ACCELERATING, false);
        entityData.define(PROGRAM_KEY, "");
        entityData.define(DUG_POS, BlockPos.ZERO);
        entityData.define(GOING_TO_OWNER, false);
        entityData.define(DRONE_COLOR, DyeColor.BLACK.getId());
        entityData.define(MINIGUN_ACTIVE, false);
        entityData.define(HAS_MINIGUN, false);
        entityData.define(AMMO, 0xFFFFFF00);
        entityData.define(LABEL, "");
        entityData.define(ACTIVE_WIDGET, 0);
        entityData.define(TARGET_POS, BlockPos.ZERO);
        entityData.define(HELD_ITEM, ItemStack.EMPTY);
        entityData.define(TARGET_ID, 0);
    }

    public static AttributeModifierMap.MutableAttribute prepareAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.FOLLOW_RANGE, 75.0D);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return droneItemHandlerCap.cast();
        } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return fluidCap.cast();
        } else if (capability == CapabilityEnergy.ENERGY) {
            return energyCap.cast();
        } else if (capability == PNCCapabilities.AIR_HANDLER_CAPABILITY) {
            return airCap.cast();
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeLong(ownerUUID.getMostSignificantBits());
        buffer.writeLong(ownerUUID.getLeastSignificantBits());
        buffer.writeComponent(ownerName);
        buffer.writeVarInt(getUpgrades(EnumUpgrade.SECURITY));
    }

    @Override
    public void readSpawnData(PacketBuffer buffer) {
        ownerUUID = new UUID(buffer.readLong(), buffer.readLong());
        ownerName = buffer.readComponent();
        securityUpgradeCount = buffer.readVarInt();
    }

    /**
     * Determines if an entity can be despawned, used on idle far away entities
     */
    @Override
    public boolean removeWhenFarAway(double dist) {
        return false;
    }

    @Override
    protected float getSoundVolume() {
        return 0.2F;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource p_184601_1_) {
        return ModSounds.DRONE_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.DRONE_DEATH.get();
    }

    @Override
    public void tick() {
        if (tickCount == 1) {
            onFirstTick();
        }

        boolean enabled = !disabledByHacking && getAirHandler().getPressure() > 0.01F;
        if (!level.isClientSide) {
            entityData.set(PRESSURE, ((int) (getAirHandler().getPressure() * 10.0f)) / 10.0f);  // rounded for client

            setAccelerating(!standby && enabled);
            if (isAccelerating()) {
                fallDistance = 0;
            }

            if (healingInterval != 0 && getHealth() < getMaxHealth() && tickCount % healingInterval == 0) {
                heal(1);
                airHandler.addAir(-healingInterval);
            }

            if (!isSuffocating) {
                suffocationCounter = 40;
            }
            isSuffocating = false;

            Path path = getNavigation().getPath();
            if (path != null) {
                PathPoint target = path.getEndNode();
                if (target != null) {
                    setTargetedBlock(new BlockPos(target.x, target.y, target.z));
                } else {
                    setTargetedBlock(null);
                }
            } else {
                setTargetedBlock(null);
            }

            if (level.getGameTime() % 20 == 0) {
                debugger.updateDebuggingPlayers();
            }

            FakePlayer fp = getFakePlayer();
            fp.setPos(getX(), getY(), getZ());
            fp.tick();
            if (isAlive()) {
                for (int i = 0; i < 4; i++) {
                    fp.gameMode.tick();
                }
            }

            if (securityUpgradeCount > 1 && getHealth() > 0F) {
                handleFluidDisplacement();
            }
        } else {
            oldLaserExtension = laserExtension;
            if (getActiveProgramKey().getPath().equals("dig")) {
                laserExtension = Math.min(1, laserExtension + LASER_EXTEND_SPEED);
            } else {
                laserExtension = Math.max(0, laserExtension - LASER_EXTEND_SPEED);
            }

            if (isAccelerating() && level.random.nextBoolean()) {
                int x = (int) Math.floor(getX());
                int y = (int) Math.floor(getY() - 1);
                int z = (int) Math.floor(getZ());
                BlockPos pos = new BlockPos(x, y, z);
                BlockState state = null;
                for (int i = 0; i < 3; i++, y--) {
                    state = level.getBlockState(pos);
                    if (state.getMaterial() != Material.AIR) break;
                }

                if (state.getMaterial() != Material.AIR) {
                    Vector3d vec = new Vector3d(getY() - y, 0, 0);
                    vec = vec.yRot((float) (random.nextFloat() * Math.PI * 2));
                    IParticleData data = new BlockParticleData(ParticleTypes.BLOCK, state);
                    level.addParticle(data, getX() + vec.x, y + 1, getZ() + vec.z, vec.x, 0, vec.z);
                }
            }
        }
        if (isAccelerating()) {
            setDeltaMovement(getDeltaMovement().scale(0.3));
            propSpeed = Math.min(1, propSpeed + 0.04F);
            getAirHandler().addAir(-1);
        } else {
            propSpeed = Math.max(0, propSpeed - 0.04F);
        }
        oldPropRotation = propRotation;
        propRotation += propSpeed;

        super.tick();

        if (hasMinigun()) {
            getMinigun().setAttackTarget(getTarget()).update(getX(), getY(), getZ());
        }

        if (!level.isClientSide && isAlive()) {
            if (enabled) aiManager.onUpdateTasks();
            handleRedstoneEmission();
        }
    }

    private void onFirstTick() {
        if (!level.isClientSide) {
            MinecraftForge.EVENT_BUS.register(this);

            double newDroneSpeed = 0.15f + Math.min(10, getUpgrades(EnumUpgrade.SPEED)) * 0.015f;
            if (getUpgrades(EnumUpgrade.ARMOR) > 6) {
                newDroneSpeed -= 0.01f * (getUpgrades(EnumUpgrade.ARMOR) - 6);
            }
            setDroneSpeed(newDroneSpeed);

            healingInterval = getUpgrades(EnumUpgrade.ITEM_LIFE) > 0 ? 100 / getUpgrades(EnumUpgrade.ITEM_LIFE) : 0;

            securityUpgradeCount = getUpgrades(EnumUpgrade.SECURITY);
            setPathfindingMalus(PathNodeType.WATER, securityUpgradeCount > 0 ? 0.0f : -1.0f);

            energy.setCapacity(100000 + 100000 * getUpgrades(EnumUpgrade.VOLUME));

            setHasMinigun(getUpgrades(EnumUpgrade.MINIGUN) > 0);

            droneItemHandler.setFakePlayerReady();

            aiManager.setWidgets(progWidgets);
        }
    }

    private void handleRedstoneEmission() {
        for (Direction d : DirectionUtil.VALUES) {
            if (getEmittingRedstone(d) > 0) {
                BlockPos emitterPos = new BlockPos((int) Math.floor(getX() + getBbWidth() / 2), (int) Math.floor(getY()), (int) Math.floor(getZ() + getBbWidth() / 2));
                if (level.isEmptyBlock(emitterPos)) {
                    level.setBlockAndUpdate(emitterPos, ModBlocks.DRONE_REDSTONE_EMITTER.get().defaultBlockState());
                }
                break;
            }
        }
    }

    private void handleFluidDisplacement() {
        restoreFluidBlocks(true);

        for (int x = (int) getX() - 1; x <= (int) (getX() + getBbWidth()); x++) {
            for (int y = (int) getY() - 1; y <= (int) (getY() + getBbHeight() + 1); y++) {
                for (int z = (int) getZ() - 2; z <= (int) (getZ() + getBbWidth()); z++) {
                    if (PneumaticCraftUtils.isBlockLiquid(level.getBlockState(new BlockPos(x, y, z)).getBlock())) {
                        BlockPos pos = new BlockPos(x, y, z);
                        if (securityUpgradeCount == 2) displacedLiquids.put(pos, level.getBlockState(pos));
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
                    }
                }
            }
        }
    }

    @Override
    public boolean canBreatheUnderwater() {
        return securityUpgradeCount > 0;
    }

    @Override
    public BlockPos getTargetedBlock() {
        BlockPos pos = entityData.get(TARGET_POS);
        return pos.equals(BlockPos.ZERO) ? null : pos;
    }

    private void setTargetedBlock(BlockPos pos) {
        entityData.set(TARGET_POS, pos == null ? BlockPos.ZERO : pos);
    }

    @Override
    public int getLaserColor() {
        ITextComponent name = hasCustomName() ? getCustomName() : ownerName;
        return LASER_COLOR_MAP.getOrDefault(name, super.getLaserColor());
    }

    @Override
    public BlockPos getDugBlock() {
        BlockPos pos = entityData.get(DUG_POS);
        return pos.equals(BlockPos.ZERO) ? null : pos;
    }

    @Override
    public ItemStack getDroneHeldItem() {
        return PNCConfig.Common.General.dronesRenderHeldItem ? entityData.get(HELD_ITEM) : ItemStack.EMPTY;
    }

    @Override
    public void setDugBlock(BlockPos pos) {
        entityData.set(DUG_POS, pos == null ? BlockPos.ZERO : pos);
    }

    // drone interface (computercraft)
    public List<EntityAITaskEntry> getRunningTasks() {
        return aiManager.getRunningTasks();
    }

    // drone interface (computercraft)
    public Goal getRunningTargetAI() {
        return aiManager.getTargetAI();
    }

    public void setVariable(String varName, BlockPos pos) {
        aiManager.setCoordinate(varName, pos);
    }

    public BlockPos getVariable(String varName) {
        return aiManager.getCoordinate(varName);
    }

    private ResourceLocation getActiveProgramKey() {
        return new ResourceLocation(entityData.get(PROGRAM_KEY));
    }


    @Override
    public int getActiveWidgetIndex() {
        return entityData.get(ACTIVE_WIDGET);
    }

    @Override
    public void setActiveProgram(IProgWidget widget) {
        entityData.set(PROGRAM_KEY, widget.getTypeID().toString());
        entityData.set(ACTIVE_WIDGET, progWidgets.indexOf(widget));
    }

    private void setAccelerating(boolean accelerating) {
        entityData.set(ACCELERATING, accelerating);
    }

    @Override
    public boolean isAccelerating() {
        return entityData.get(ACCELERATING);
    }

    private void setDroneColor(int color) {
        entityData.set(DRONE_COLOR, color);
    }

    @Override
    public int getDroneColor() {
        return entityData.get(DRONE_COLOR);
    }

    private void setMinigunActivated(boolean activated) {
        entityData.set(MINIGUN_ACTIVE, activated);
    }

    private boolean isMinigunActivated() {
        return entityData.get(MINIGUN_ACTIVE);
    }

    private void setHasMinigun(boolean hasMinigun) {
        entityData.set(HAS_MINIGUN, hasMinigun);
    }

    public boolean hasMinigun() {
        return entityData.get(HAS_MINIGUN);
    }

    public int getAmmoColor() {
        return entityData.get(AMMO);
    }

    private void setAmmoColor(ItemStack ammo) {
        int color = ammo.getItem() instanceof ItemGunAmmo ? ((ItemGunAmmo) ammo.getItem()).getAmmoColor(ammo) : 0xFFFF0000;
        entityData.set(AMMO, color);
    }

    @Override
    public BlockPos getDeployPos() {
        return deployPos;
    }

    public void setDeployPos(BlockPos deployPos) {
        if (this.deployPos != null) {
            throw new IllegalStateException("deployPos has already been set!");
        }
        this.deployPos = deployPos;
    }

    /**
     * Decrements the entity's air supply when underwater
     */
    @Override
    protected int decreaseAirSupply(int par1) {
        return -20; // make drones insta drown
    }

    /**
     * Moves the entity based on the specified heading.  Args: strafe, forward
     */
    @Override
    public void travel(Vector3d travelVec) {
        if (level.isClientSide) {
            LivingEntity targetEntity = getTarget();
            if (targetEntity != null && !targetEntity.isAlive()) {
                setTarget(null);
                targetEntity = null;
            }
            if (targetEntity != null) {
                if (targetLine == null) targetLine = new ProgressingLine(0, getBbHeight() / 2, 0, 0, 0, 0);
                if (oldTargetLine == null) oldTargetLine = new ProgressingLine(0, getBbHeight() / 2, 0, 0, 0, 0);

                targetLine.endX = (float) (targetEntity.getX() - getX());
                targetLine.endY = (float) (targetEntity.getY() + targetEntity.getBbHeight() / 2 - getY());
                targetLine.endZ = (float) (targetEntity.getZ() - getZ());
                oldTargetLine.endX = (float) (targetEntity.xo - xo);
                oldTargetLine.endY = (float) (targetEntity.yo + targetEntity.getBbHeight() / 2 - yo);
                oldTargetLine.endZ = (float) (targetEntity.zo - zo);

                oldTargetLine.setProgress(targetLine.getProgress());
                targetLine.incProgressByDistance(0.2D);
                noCulling = true; //don't stop rendering the drone when it goes out of the camera frustrum, as we need to render the target lines as well.
            } else {
                targetLine = oldTargetLine = null;
                noCulling = false;
            }
        }
        if (getVehicle() == null && isAccelerating()) {
            double d3 = getDeltaMovement().y;
            super.travel(travelVec);
            setDeltaMovement(getDeltaMovement().x, d3 * 0.6D, getDeltaMovement().z);
        } else {
            super.travel(travelVec);
        }
        onGround = true; //set onGround to true so AI pathfinding will keep updating.
    }

    public ProgressingLine getTargetLine() {
        return targetLine;
    }

    public ProgressingLine getOldTargetLine() {
        return oldTargetLine;
    }

    @Override
    public ActionResultType mobInteract(PlayerEntity player, Hand hand) {
        // mobInteract = onEntityRightClick() ?
        if (!getOwnerUUID().equals(player.getUUID())) return ActionResultType.PASS;

        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() == ModItems.GPS_TOOL.get()) {
            if (!level.isClientSide) {
                BlockPos gpsLoc = ItemGPSTool.getGPSLocation(level, stack);
                if (gpsLoc != null) {
                    getNavigation().moveTo(gpsLoc.getX(), gpsLoc.getY(), gpsLoc.getZ(), 0.1D);
                    return ActionResultType.SUCCESS;
                } else {
                    return ActionResultType.PASS;
                }
            }
            return ActionResultType.SUCCESS;
        } else if (stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).isPresent()) {
            if (player.level.isClientSide) return ActionResultType.CONSUME;
            return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).map(handler -> {
                if (handler.getFluidInTank(0).isEmpty()) {
                    boolean ok = player.level.isClientSide || FluidUtil.interactWithFluidHandler(player, hand, fluidTank);
                    return ok ? ActionResultType.CONSUME : ActionResultType.PASS;
                } else {
                    return ActionResultType.PASS;
                }
            }).orElseThrow(RuntimeException::new);
        } else {
            DyeColor color = DyeColor.getColor(stack);
            if (color != null) {
                if (!level.isClientSide) {
                    setDroneColor(color.getId());
                    if (PNCConfig.Common.General.useUpDyesWhenColoring && !player.isCreative()) {
                        stack.shrink(1);
                        if (stack.getCount() <= 0) {
                            player.setItemInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                        }
                    }
                }
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }

    public boolean shouldDropAsItem() {
        return true;
    }

    /**
     * Called when a drone is right-clicked by a Pneumatic Wrench.
     */
    @Override
    public boolean onWrenched(World world, PlayerEntity player, BlockPos pos, Direction side, Hand hand) {
        if (shouldDropAsItem()) {
            hurt(new DamageSourceDroneOverload("wrenched"), 2000.0F);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Restore any liquids that may have been displaced by the drone (security upgrade)
     *
     * @param distCheck if true, only restore liquids in blocks > 1 block distance away from the drone
     */
    private void restoreFluidBlocks(boolean distCheck) {
        Iterator<Map.Entry<BlockPos, BlockState>> iter = displacedLiquids.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<BlockPos, BlockState> entry = iter.next();
            BlockPos pos = entry.getKey();
            if (!distCheck || pos.distSqr(getX(), getY(), getZ(), true) > 1) {
                if (level.isEmptyBlock(pos) || PneumaticCraftUtils.isBlockLiquid(level.getBlockState(pos).getBlock())) {
                    level.setBlock(pos, entry.getValue(), Constants.BlockFlags.BLOCK_UPDATE);
                }
                iter.remove();
            }
        }
    }

    @Nullable
    @Override
    public Entity changeDimension(ServerWorld p_241206_1_, ITeleporter teleporter) {
        Entity entity = super.changeDimension(p_241206_1_, teleporter);
        if (entity != null) {
            restoreFluidBlocks(false);
        }
        return entity;
    }

    @Override
    protected void dropEquipment() {
        for (int i = 0; i < droneItemHandler.getSlots(); i++) {
            if (!droneItemHandler.getStackInSlot(i).isEmpty()) {
                spawnAtLocation(droneItemHandler.getStackInSlot(i), 0);
                droneItemHandler.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        restoreFluidBlocks(false);

        if (shouldDropAsItem()) {
            ItemStack stack = new ItemStack(getDroneItem());
            writeToItemStack(stack);
            spawnAtLocation(stack, 0);
            if (!level.isClientSide) {
                reportDroneDeath(getOwner(), damageSource);
            }
        }

        if (!level.isClientSide && getDugBlock() != null) {
            // stop any in-progress digging - 3rd & 4th parameters are unimportant here
            getFakePlayer().gameMode.handleBlockBreakAction(getDugBlock(), CPlayerDiggingPacket.Action.ABORT_DESTROY_BLOCK, UP, 0);
        }

        setCustomName(new StringTextComponent(""));  // keep other mods (like CoFH Core) quiet about death message broadcasts

        MinecraftForge.EVENT_BUS.unregister(this);
    }

    private void reportDroneDeath(PlayerEntity owner, DamageSource damageSource) {
        if (owner != null) {
            int x = (int) Math.floor(getX());
            int y = (int) Math.floor(getY());
            int z = (int) Math.floor(getZ());
            ITextComponent msg = hasCustomName() ?
                    new TranslationTextComponent("pneumaticcraft.death.drone.named", getCustomName().getString(), x, y, z) :
                    new TranslationTextComponent("pneumaticcraft.death.drone", x, y, z);
            msg = msg.plainCopy().append(" - ").append(damageSource.getLocalizedDeathMessage(this));
            owner.displayClientMessage(msg, false);
        }
    }

    private Item getDroneItem() {
        // return the item which has the same name as our entity type
        return ForgeRegistries.ITEMS.getValue(getType().getRegistryName());
    }

    @Override
    public void onSyncedDataUpdated(DataParameter<?> key) {
        if (level.isClientSide) {
            if (TARGET_ID.equals(key)) {
                int id = entityData.get(TARGET_ID);
                if (id > 0) {
                    Entity e = getCommandSenderWorld().getEntity(id);
                    if (e instanceof LivingEntity) {
                        setTarget((LivingEntity) e);
                    }
                }
                if (targetLine != null && oldTargetLine != null) {
                    targetLine.setProgress(0);
                    oldTargetLine.setProgress(0);
                }
            } else if (PRESSURE.equals(key)) {
                int newAir = (int) (entityData.get(PRESSURE) * getAirHandler().getVolume());
                getAirHandler().addAir(newAir - airHandler.getAir());
            }
        }
        super.onSyncedDataUpdated(key);
    }

    @Override
    public void setTarget(LivingEntity entity) {
        super.setTarget(entity);
        if (!level.isClientSide) {
            entityData.set(TARGET_ID, entity == null ? 0 : entity.getId());
        }
    }

    @Override
    public boolean startRiding(Entity entity, boolean force) {
        return PNCConfig.Common.General.dronesCanBePickedUp && super.startRiding(entity, force);
    }

    protected BasicAirHandler getAirHandler() {
        if (airHandler == null) {
            int vol = PressureHelper.getUpgradedVolume(PneumaticValues.DRONE_VOLUME, getUpgrades(EnumUpgrade.VOLUME));
            ItemStack stack = new ItemStack(getDroneItem());
            EnchantmentHelper.setEnchantments(stackEnchants, stack);
            vol = ItemRegistry.getInstance().getModifiedVolume(stack, vol);
            airHandler = new BasicAirHandler(vol);
        }
        return airHandler;
    }

    @Override
    public void printManometerMessage(PlayerEntity player, List<ITextComponent> curInfo) {
        if (hasCustomName()) {
            curInfo.add(getDroneName().copy().withStyle(TextFormatting.AQUA, TextFormatting.ITALIC));
        } else {
            curInfo.add(getDroneName().copy().withStyle(TextFormatting.AQUA));
        }
        curInfo.add(xlate("pneumaticcraft.entityTracker.info.tamed", getOwnerName().getString()));
        curInfo.add(xlate("pneumaticcraft.gui.tooltip.pressure", PneumaticCraftUtils.roundNumberTo(getAirHandler().getPressure(), 2)));
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT tag) {
        super.addAdditionalSaveData(tag);

        TileEntityProgrammer.putWidgetsToNBT(progWidgets, tag);
        tag.put("airHandler", getAirHandler().serializeNBT());
        tag.putFloat("propSpeed", propSpeed);
        tag.putBoolean("disabledByHacking", disabledByHacking);
        tag.putBoolean("hackedByOwner", gotoOwnerAI != null);
        tag.putInt("color", getDroneColor());
        tag.putBoolean("standby", standby);
        tag.put("variables", aiManager.writeToNBT(new CompoundNBT()));

        ItemStackHandler tmpHandler = new ItemStackHandler(droneItemHandler.getSlots());
        PneumaticCraftUtils.copyItemHandler(droneItemHandler, tmpHandler, droneItemHandler.getSlots());
        tag.put("Inventory", tmpHandler.serializeNBT());
        tag.put(UpgradableItemUtils.NBT_UPGRADE_TAG, upgradeInventory.serializeNBT());

        fluidTank.writeToNBT(tag);

        tag.putString("owner", ownerName.getString());
        if (ownerUUID != null) {
            tag.putLong("ownerUUID_M", ownerUUID.getMostSignificantBits());
            tag.putLong("ownerUUID_L", ownerUUID.getLeastSignificantBits());
        }

        if (!stackEnchants.isEmpty()) {
            CompoundNBT eTag = new CompoundNBT();
            stackEnchants.forEach((ench, lvl) -> eTag.putInt(ench.getRegistryName().toString(), lvl));
            tag.put("stackEnchants", eTag);
        }

        if (!displacedLiquids.isEmpty()) {
            ListNBT disp = new ListNBT();
            for (Map.Entry<BlockPos, BlockState> entry : displacedLiquids.entrySet()) {
                CompoundNBT p = NBTUtil.writeBlockPos(entry.getKey());
                CompoundNBT s = NBTUtil.writeBlockState(entry.getValue());
                ListNBT l = new ListNBT();
                l.add(0, p);
                l.add(1, s);
                disp.add(0, l);
            }
            tag.put("displacedLiquids", disp);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT tag) {
        super.readAdditionalSaveData(tag);

        progWidgets = TileEntityProgrammer.getWidgetsFromNBT(tag);
        TileEntityProgrammer.updatePuzzleConnections(progWidgets);
        propSpeed = tag.getFloat("propSpeed");
        disabledByHacking = tag.getBoolean("disabledByHacking");
        setGoingToOwner(tag.getBoolean("hackedByOwner"));
        setDroneColor(tag.getInt("color"));
        aiManager.readFromNBT(tag.getCompound("variables"));
        standby = tag.getBoolean("standby");
        upgradeInventory.deserializeNBT(tag.getCompound(UpgradableItemUtils.NBT_UPGRADE_TAG));
        upgradeCache.invalidate();
        getAirHandler().deserializeNBT(tag.getCompound("airHandler"));

        ItemStackHandler tmpInv = new ItemStackHandler();
        tmpInv.deserializeNBT(tag.getCompound("Inventory"));
        PneumaticCraftUtils.copyItemHandler(tmpInv, droneItemHandler);
        droneItemHandler.setUseableSlots(1 + getUpgrades(EnumUpgrade.INVENTORY));

        fluidTank.setCapacity(PneumaticValues.DRONE_TANK_SIZE * (1 + getUpgrades(EnumUpgrade.INVENTORY)));
        fluidTank.readFromNBT(tag);

        energy.setCapacity(100000 + 100000 * getUpgrades(EnumUpgrade.VOLUME));

        if (tag.contains("owner")) ownerName = new StringTextComponent(tag.getString("owner"));
        if (tag.contains("ownerUUID_M")) ownerUUID = new UUID(tag.getLong("ownerUUID_M"), tag.getLong("ownerUUID_L"));

        if (tag.contains("stackEnchants", Constants.NBT.TAG_COMPOUND)) {
            CompoundNBT eTag = tag.getCompound("stackEnchants");
            for (String name : eTag.getAllKeys()) {
                Enchantment e = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(name));
                if (e != null) {
                    stackEnchants.put(e, eTag.getInt(name));
                }
            }
        }

        if (tag.contains("displacedLiquids")) {
            for (INBT inbt : tag.getList("displacedLiquids", Constants.NBT.TAG_LIST)) {
                ListNBT l = (ListNBT) inbt;
                CompoundNBT p = l.getCompound(0);
                CompoundNBT s = l.getCompound(1);
                BlockPos pos = NBTUtil.readBlockPos(p);
                BlockState state = NBTUtil.readBlockState(s);
                displacedLiquids.put(pos, state);
            }
        }
    }

    // computercraft ("getOwnerName" method)
    @Override
    public ITextComponent getOwnerName() {
        return ownerName;
    }

    @Override
    public UUID getOwnerUUID() {
        if (ownerUUID == null) {
            Log.warning("Drone with owner '%s' has no UUID! Substituting the Drone's UUID (%s).", ownerName, getUUID());
            Log.warning("If you use any protection mods, the drone might not be able to operate in protected areas.");
            ownerUUID = getUUID();
        }
        return ownerUUID;
    }

    @Override
    public int getUpgrades(EnumUpgrade upgrade) {
        return upgradeCache.getUpgrades(upgrade);
    }

    @Override
    public FakePlayer getFakePlayer() {
        if (fakePlayer == null && !level.isClientSide) {
            // using the owner's UUID for the fake player should be fine in Forge 35.0.12 and up
            // see https://github.com/MinecraftForge/MinecraftForge/pull/7454
            fakePlayer = new DroneFakePlayer((ServerWorld) level, new GameProfile(getOwnerUUID(), ownerName.getString()), this);
            fakePlayer.connection = new FakeNetHandlerPlayerServer(ServerLifecycleHooks.getCurrentServer(), fakePlayer);
        }
        return fakePlayer;
    }

    public Minigun getMinigun() {
        if (minigun == null) {
            minigun = new MinigunDrone(level.isClientSide ? null : getFakePlayer())
                    .setWorld(level)
                    .setAirHandler(this.getCapability(PNCCapabilities.AIR_HANDLER_CAPABILITY), PneumaticValues.DRONE_USAGE_ATTACK);
        }
        return minigun;
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        getFakePlayer().attack(entity);
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            if (livingEntity.isAlive() && livingEntity.getLastHurtByMob() == getFakePlayer()) {
                livingEntity.setLastHurtByMob(this);
            }
        }
        getAirHandler().addAir(-PneumaticValues.DRONE_USAGE_ATTACK);
        return true;
    }

    @Override
    public int getArmorValue() {
        return getUpgrades(EnumUpgrade.ARMOR);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float damage) {
        if (damageSource == DamageSource.IN_WALL) {
            if (suffocationCounter > 0) suffocationCounter--;
        }
        return super.hurt(damageSource, damage);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (source == DamageSource.IN_WALL) {
            return suffocationCounter > 0 || !PNCConfig.Common.General.enableDroneSuffocation;
        }
        if (RadiationSourceCheck.INSTANCE.isRadiation(source)) {
            return true;
        }
        if (source instanceof EntityDamageSource) {
            Entity e = source.getEntity();
            if (e != null && !level.isClientSide && e.getId() == getFakePlayer().getId()) {
                // don't allow the drone's own fake player to damage the drone
                // e.g. if the drone is wielding an infinity hammer
                return true;
            }
        }
        return super.isInvulnerableTo(source);
    }

    @Override
    public IItemHandlerModifiable getInv() {
        return droneItemHandler;
    }

//    public double getSpeed() {
//        return speed;
//    }

    public int getEmittingRedstone(Direction side) {
        return emittingRedstoneValues.getOrDefault(side, 0);
    }

    @Override
    public void setEmittingRedstone(Direction side, int value) {
        if (emittingRedstoneValues.getOrDefault(side, 0) != value) {
            emittingRedstoneValues.put(side, value);
            BlockPos pos = new BlockPos((int) Math.floor(getX() + getBbWidth() / 2), (int) Math.floor(getY()), (int) Math.floor(getZ() + getBbWidth() / 2));
            BlockState state = level.getBlockState(pos);
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    @Override
    public boolean isBlockValidPathfindBlock(BlockPos pos) {
        if (level.isEmptyBlock(pos)) return true;
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (PneumaticCraftUtils.isBlockLiquid(block)) {
            return securityUpgradeCount > 0;
        }
        if (state.isPathfindable(level, pos, PathType.LAND)) return true;
        if (!state.getMaterial().blocksMotion() && block != Blocks.LADDER) return true;
        if (DroneRegistry.getInstance().pathfindableBlocks.containsKey(block)) {
            IPathfindHandler pathfindHandler = DroneRegistry.getInstance().pathfindableBlocks.get(block);
            return pathfindHandler == null || pathfindHandler.canPathfindThrough(level, pos);
        } else {
            return false;
        }
    }

    @Override
    public void sendWireframeToClient(BlockPos pos) {
        NetworkHandler.sendToAllTracking(new PacketShowWireframe(this, pos), this);
    }

    /**
     * IHackableEntity
     */

    @Override
    public ResourceLocation getHackableId() {
        return RL("drone");
    }

    @Override
    public boolean canHack(Entity entity, PlayerEntity player) {
        return isAccelerating();
    }

    @Override
    public void addHackInfo(Entity entity, List<ITextComponent> curInfo, PlayerEntity player) {
        if (ownerUUID.equals(player.getUUID())) {
            if (isGoingToOwner()) {
                curInfo.add(xlate("pneumaticcraft.armor.hacking.result.resumeTasks"));
            } else {
                curInfo.add(xlate("pneumaticcraft.armor.hacking.result.callBack"));
            }
        } else {
            curInfo.add(xlate("pneumaticcraft.armor.hacking.result.disable"));
        }
    }

    @Override
    public void addPostHackInfo(Entity entity, List<ITextComponent> curInfo, PlayerEntity player) {
        if (ownerUUID.equals(player.getUUID())) {
            if (isGoingToOwner()) {
                curInfo.add(xlate("pneumaticcraft.armor.hacking.finished.calledBack"));
            } else {
                curInfo.add(xlate("pneumaticcraft.armor.hacking.finished.resumedTasks"));
            }
        } else {
            curInfo.add(xlate("pneumaticcraft.armor.hacking.finished.disabled"));
        }
    }

    @Override
    public int getHackTime(Entity entity, PlayerEntity player) {
        return ownerUUID.equals(player.getUUID()) ? 20 : 100;
    }

    @Override
    public void onHackFinished(Entity entity, PlayerEntity player) {
        if (!level.isClientSide && player.getUUID().equals(ownerUUID)) {
            setGoingToOwner(gotoOwnerAI == null); //toggle the state
        } else {
            disabledByHacking = true;
        }
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }

    private void setGoingToOwner(boolean state) {
        if (!level.isClientSide) {
            if (state && gotoOwnerAI == null) {
                gotoOwnerAI = new DroneGoToOwner(this);
                goalSelector.addGoal(2, gotoOwnerAI);
                entityData.set(GOING_TO_OWNER, true);
                setActiveProgram(new ProgWidgetGoToLocation());
            } else if (!state && gotoOwnerAI != null) {
                goalSelector.removeGoal(gotoOwnerAI);
                gotoOwnerAI = null;
                entityData.set(GOING_TO_OWNER, false);
            }
        }
    }

    private boolean isGoingToOwner() {
        return entityData.get(GOING_TO_OWNER);
    }

    @Override
    public IFluidTank getFluidTank() {
        return fluidTank;
    }

    @Override
    public PlayerEntity getOwner() {
        MinecraftServer server = level.getServer();
        return server != null ? server.getPlayerList().getPlayer(ownerUUID) : null;
    }

    public void setStandby(boolean standby) {
        this.standby = standby;
    }

    @Override
    public World world() {
        return level;
    }

    @Override
    public Vector3d getDronePos() {
        return position();
    }

    @Override
    public BlockPos getControllerPos() {
        return BlockPos.ZERO;
    }

    @Override
    public void dropItem(ItemStack stack) {
        spawnAtLocation(stack, 0);
    }

    @Override
    public List<IProgWidget> getProgWidgets() {
        return progWidgets;
    }

    @Override
    public GoalSelector getTargetAI() {
        return targetSelector;
    }

    @Override
    public boolean isProgramApplicable(ProgWidgetType<?> widgetType) {
        return true;
    }

    @Override
    public void setName(ITextComponent string) {
        setCustomName(string);
    }

    @Override
    public void setCarryingEntity(Entity entity) {
        if (entity == null) {
            for (Entity e : getCarryingEntities()) {
                e.stopRiding();
                double y = e.getY();
                if (PNCConfig.Common.General.dronesCanBePickedUp && (e instanceof AbstractMinecartEntity || e instanceof BoatEntity)) {
                    // little kludge to prevent the dropped minecart/boat immediately picking up the drone
                    y -= 2;
                    BlockPos pos = PneumaticCraftUtils.getPosForEntity(e);
                    if (level.getBlockState(pos).isRedstoneConductor(level, pos)) {
                        y++;
                    }
                    // minecarts have their own tick() which doesn't decrement rideCooldown
                    if (e instanceof AbstractMinecartEntity) e.boardingCooldown = 0;
                }
                if (y != e.getY()) e.setPos(e.getX(), y, e.getZ());
            }
        } else {
            entity.startRiding(this);
        }
    }

    @Override
    public List<Entity> getCarryingEntities() {
        return getPassengers();
    }

    @Override
    public boolean isAIOverridden() {
        return chargeAI.isExecuting || gotoOwnerAI != null;
    }

    @Override
    public void onItemPickupEvent(ItemEntity curPickingUpEntity, int stackSize) {
        take(curPickingUpEntity, stackSize);
    }

    @Override
    public IPathNavigator getPathNavigator() {
        return (IPathNavigator) getNavigation();
    }

    public void tryFireMinigun(LivingEntity target) {
        int slot = getSlotForAmmo();
        if (slot >= 0) {
            ItemStack ammo = droneItemHandler.getStackInSlot(slot);
            if (getMinigun().setAmmoStack(ammo).tryFireMinigun(target)) {
                droneItemHandler.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    /**
     * Get the first slot which has any ammo in it.
     *
     * @return a slot number, or -1 if no ammo
     */
    public int getSlotForAmmo() {
        for (int i = 0; i < droneItemHandler.getSlots(); i++) {
            if (droneItemHandler.getStackInSlot(i).getItem() instanceof ItemGunAmmo) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void overload(String msgKey, Object... params) {
        hurt(new DamageSourceDroneOverload(msgKey, params), 2000.0F);
    }

    @Override
    public DroneAIManager getAIManager() {
        return aiManager;
    }

    @Override
    public LogisticsManager getLogisticsManager() {
        return logisticsManager;
    }

    @Override
    public void setLogisticsManager(LogisticsManager logisticsManager) {
        this.logisticsManager = logisticsManager;
    }

    @Override
    public void playSound(SoundEvent soundEvent, SoundCategory category, float volume, float pitch) {
        level.playSound(null, blockPosition(), soundEvent, category, volume, pitch);
    }

    @Override
    public void addAirToDrone(int air) {
        airHandler.addAir(air);
    }

    @Override
    public void updateLabel() {
        entityData.set(LABEL, getAIManager() != null ? getAIManager().getLabel() : "Main");
    }

    @Override
    public String getLabel() {
        return entityData.get(LABEL);
    }

    @Override
    public boolean isTeleportRangeLimited() {
        return true;
    }

    @Override
    public ITextComponent getDroneName() {
        return getName();
    }

    @Override
    public DroneDebugger getDebugger() {
        return debugger;
    }

    @Override
    public void storeTrackerData(ItemStack stack) {
        NBTUtils.setInteger(stack, NBTKeys.PNEUMATIC_HELMET_DEBUGGING_DRONE, getId());
        NBTUtils.removeTag(stack, NBTKeys.PNEUMATIC_HELMET_DEBUGGING_PC);
    }

    @Override
    public IItemHandler getUpgradeHandler() {
        return upgradeInventory;
    }

    @Override
    public void onUpgradesChanged() {
        energy.setCapacity(100000 + 100000 * getUpgrades(EnumUpgrade.VOLUME));
    }

    @Override
    public boolean isDroneStillValid() {
        return isAlive();
    }

    @Override
    public boolean canMoveIntoFluid(Fluid fluid) {
        if (fluid.getAttributes().getTemperature() > 373) {
            return false;
        } else {
            return canBreatheUnderwater();
        }
    }

    @Override
    public DroneItemHandler getDroneItemHandler() {
        return droneItemHandler;
    }

    /**
     * Add an initial program to a drone that's about to be placed. This is called right before the entity is spawned.
     * Programmable drones don't do anything here (program is created by the programmer and stored in item NBT), but
     * subclasses will add their static program here.
     *
     * @param clickPos block the drone item is clicked against
     * @param facing side of the clicked block
     * @param placePos blockpos the drone will appear in
     * @param droneStack the drone itemstack
     * @param progWidgets add the program to this list, ideally using {@link DroneProgramBuilder}
     * @return true if a program was added, false otherwise
     */
    public boolean addProgram(BlockPos clickPos, Direction facing, BlockPos placePos, ItemStack droneStack, List<IProgWidget> progWidgets) {
        return false;
    }

    public void incAttackCount() {
        attackCount++;
    }

    public int getAttackCount() {
        return attackCount;
    }

    @Override
    public void resetAttackCount() {
        attackCount = 0;
    }

    public void setDroneSpeed(double droneSpeed) {
        this.droneSpeed = droneSpeed;
    }

    public double getDroneSpeed() {
        return droneSpeed;
    }

    public class MinigunDrone extends Minigun {
        MinigunDrone(FakePlayer fakePlayer) {
            super(fakePlayer, true);
        }

        @Override
        public SoundSource getSoundSource() {
            return SoundSource.of(EntityDrone.this);
        }

        @Override
        public boolean isMinigunActivated() {
            return EntityDrone.this.isMinigunActivated();
        }

        @Override
        public void setMinigunActivated(boolean activated) {
            if (!world.isClientSide) {
                // only set server-side; drone sync's the activation state to client
                EntityDrone.this.setMinigunActivated(activated);
            }
        }

        @Override
        public void setAmmoColorStack(@Nonnull ItemStack ammo) {
            if (!world.isClientSide) {
                // only set server-side; drone sync's the activation state to client
                setAmmoColor(ammo);
            }
        }

        @Override
        public int getAmmoColor() {
            return EntityDrone.this.getAmmoColor();
        }

        @Override
        public void playSound(SoundEvent soundName, float volume, float pitch) {
            world.playSound(null, blockPosition(), soundName, SoundCategory.NEUTRAL, volume, pitch);
        }

        @Override
        public boolean isValid() {
            return EntityDrone.this.isAlive();
        }
    }

    private class EntityDroneItemHandler extends DroneItemHandler {
        EntityDroneItemHandler(IDrone holder) {
            super(holder, 1);
        }

        @Override
        public void copyItemToFakePlayer(int slot) {
            super.copyItemToFakePlayer(slot);

            if (isFakePlayerReady() && slot == getFakePlayer().inventory.selected && PNCConfig.Common.General.dronesRenderHeldItem) {
                entityData.set(HELD_ITEM, getStackInSlot(slot));
            }
        }
    }
}
