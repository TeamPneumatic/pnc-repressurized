package me.desht.pneumaticcraft.common.entity.living;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.api.block.IPneumaticWrenchable;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableEntity;
import me.desht.pneumaticcraft.api.drone.IPathNavigator;
import me.desht.pneumaticcraft.api.drone.IPathfindHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.tileentity.IManoMeasurable;
import me.desht.pneumaticcraft.client.render.RenderDroneHeldItem;
import me.desht.pneumaticcraft.client.render.RenderProgressingLine;
import me.desht.pneumaticcraft.common.DamageSourcePneumaticCraft.DamageSourceDroneOverload;
import me.desht.pneumaticcraft.common.DroneRegistry;
import me.desht.pneumaticcraft.common.NBTUtil;
import me.desht.pneumaticcraft.common.ai.*;
import me.desht.pneumaticcraft.common.ai.DroneAIManager.EntityAITaskEntry;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.inventory.ChargeableItemHandler;
import me.desht.pneumaticcraft.common.item.ItemColorHandler;
import me.desht.pneumaticcraft.common.item.ItemGPSTool;
import me.desht.pneumaticcraft.common.item.ItemProgrammingPuzzle;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetGoToLocation;
import me.desht.pneumaticcraft.common.recipes.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferCustom;
import me.desht.pneumaticcraft.common.tileentity.PneumaticEnergyStorage;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPlasticMixer;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import me.desht.pneumaticcraft.common.util.FakeNetHandlerPlayerServer;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.NBTKeys;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Sounds;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityFlying;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class EntityDrone extends EntityDroneBase
        implements IManoMeasurable, IPneumaticWrenchable, IEntityAdditionalSpawnData, IHackableEntity, IDroneBase, EntityFlying {

    private static final HashMap<String, Integer> colorMap = new HashMap<>();

    static {
        colorMap.put("aureylian", 0xff69b4);
        colorMap.put("loneztar", 0x00a0a0);
        colorMap.put("jadedcat", 0xa020f0);
    }

    private ItemHandlerDrone inventory = new ItemHandlerDrone(1);
    private final FluidTank tank = new FluidTank(Integer.MAX_VALUE);
    private ItemStackHandler upgradeInventory = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            energy.setCapacity(100000 + 100000 * getUpgrades(EnumUpgrade.VOLUME));
        }
    };
    private final int[] emittingRedstoneValues = new int[6];
    private float propSpeed;
    private static final float LASER_EXTEND_SPEED = 0.05F;
    private final PneumaticEnergyStorage energy = new PneumaticEnergyStorage(100000);

    protected float currentAir; //the current held energy of the Drone;
    private float volume;
    private RenderProgressingLine targetLine;
    private RenderProgressingLine oldTargetLine;
    public List<IProgWidget> progWidgets = new ArrayList<IProgWidget>();

    private DroneFakePlayer fakePlayer;
    public String playerName = "Drone";
    private String playerUUID;

    public DroneGoToChargingStation chargeAI;
    public DroneGoToOwner gotoOwnerAI;
    private final DroneAIManager aiManager = new DroneAIManager(this);

    private boolean firstTick = true;
    public boolean naturallySpawned = true; //determines if it should drop a drone when it dies.
    private boolean hasLiquidImmunity;
    private double speed;
    private int lifeUpgrades;
    private int suffocationCounter = 40; //Drones are invincible for suffocation for this time.
    private boolean isSuffocating;
    private boolean disabledByHacking;
    private boolean standby; //If true, the drone's propellors stop, the drone will fall down, and won't use pressure.
    private Minigun minigun;

    private AmadronOffer handlingOffer;
    private int offerTimes;
    private ItemStack usedTablet;//Tablet used to place the order.
    private String buyingPlayer;
    private final SortedSet<DebugEntry> debugEntries = new TreeSet<DebugEntry>();
    private final Set<EntityPlayerMP> syncedPlayers = new HashSet<EntityPlayerMP>();

    public EntityDrone(World world) {
        super(world);
        setSize(0.7F, 0.35F);
        moveHelper = new DroneMoveHelper(this);
        tasks.addTask(1, chargeAI = new DroneGoToChargingStation(this));
    }

    public EntityDrone(World world, EntityPlayer player) {
        this(world);
        if(player != null){
            playerUUID = player.getGameProfile().getId().toString();
            playerName = player.getName();
        }else{
            playerUUID = getUniqueID().toString(); //Anonymous drone used for Amadron or spawned with a Dispenser
        }
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        EntityPathNavigateDrone nav = new EntityPathNavigateDrone(this, worldIn);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setCanEnterDoors(true);
        return nav;
    }

    private void initializeFakePlayer() {
        fakePlayer = new DroneFakePlayer(
                (WorldServer) world,
                new GameProfile(UUID.fromString(getOwnerUUID()), playerName),
                this);
        fakePlayer.connection = new FakeNetHandlerPlayerServer(FMLCommonHandler.instance().getMinecraftServerInstance(), fakePlayer);
        fakePlayer.inventory = new InventoryFakePlayer(fakePlayer);
    }

    private static final DataParameter<Boolean> ACCELERATING = EntityDataManager.createKey(EntityDrone.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float> PRESSURE = EntityDataManager.createKey(EntityDrone.class, DataSerializers.FLOAT);
    private static final DataParameter<String> PROGRAM_KEY = EntityDataManager.createKey(EntityDrone.class, DataSerializers.STRING);
    private static final DataParameter<BlockPos> DUG_POS = EntityDataManager.createKey(EntityDrone.class, DataSerializers.BLOCK_POS);
    private static final DataParameter<Boolean> GOING_TO_OWNER = EntityDataManager.createKey(EntityDrone.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> DRONE_COLOR = EntityDataManager.createKey(EntityDrone.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> MINIGUN_ACTIVE = EntityDataManager.createKey(EntityDrone.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> HAS_MINIGUN = EntityDataManager.createKey(EntityDrone.class, DataSerializers.BOOLEAN);
    private static final DataParameter<ItemStack> AMMO = EntityDataManager.createKey(EntityDrone.class, DataSerializers.ITEM_STACK);
    private static final DataParameter<String> LABEL = EntityDataManager.createKey(EntityDrone.class, DataSerializers.STRING);
    private static final DataParameter<Integer> ACTIVE_WIDGET = EntityDataManager.createKey(EntityDrone.class, DataSerializers.VARINT);
    private static final DataParameter<BlockPos> TARGET_POS = EntityDataManager.createKey(EntityDrone.class, DataSerializers.BLOCK_POS);
    private static final DataParameter<ItemStack> HELD_ITEM = EntityDataManager.createKey(EntityDrone.class, DataSerializers.ITEM_STACK);

    @Override
    protected void entityInit() {
        super.entityInit();

        dataManager.register(PRESSURE, 0.0f);
        dataManager.register(ACCELERATING, false);
        dataManager.register(PROGRAM_KEY, "");
        dataManager.register(DUG_POS, BlockPos.ORIGIN);
        dataManager.register(GOING_TO_OWNER, false);
        dataManager.register(DRONE_COLOR, 0);
        dataManager.register(MINIGUN_ACTIVE, false);
        dataManager.register(HAS_MINIGUN, false);
        dataManager.register(AMMO, ItemStack.EMPTY);
        dataManager.register(LABEL, "");
        dataManager.register(ACTIVE_WIDGET, 0);
        dataManager.register(TARGET_POS, BlockPos.ORIGIN);
        dataManager.register(HELD_ITEM, ItemStack.EMPTY);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40F);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(getRange());
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
                || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
                || capability == CapabilityEnergy.ENERGY
                || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
        } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(tank);
        } else if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(energy);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void writeSpawnData(ByteBuf data) {
        ByteBufUtils.writeUTF8String(data, getFakePlayer().getName());
    }

    @Override
    public void readSpawnData(ByteBuf data) {
        playerName = ByteBufUtils.readUTF8String(data);
    }

    /**
     * Determines if an entity can be despawned, used on idle far away entities
     */
    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    protected float getSoundVolume() {
        return 0.2F;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource p_184601_1_) {
        return Sounds.DRONE_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return Sounds.DRONE_DEATH;
    }

    @Override
    public void onUpdate() {
        if (firstTick) {
            firstTick = false;
            volume = PneumaticValues.DRONE_VOLUME + getUpgrades(EnumUpgrade.VOLUME) * PneumaticValues.VOLUME_VOLUME_UPGRADE;
            hasLiquidImmunity = getUpgrades(EnumUpgrade.SECURITY) > 0;
            if (hasLiquidImmunity) {
                ((EntityPathNavigateDrone) getPathNavigator()).pathThroughLiquid = true;
            }
            setPathPriority(PathNodeType.WATER, hasLiquidImmunity ? 0.0f : -1.0f);
            speed = 0.1 + Math.min(10, getUpgrades(EnumUpgrade.SPEED)) * 0.01;
            lifeUpgrades = getUpgrades(EnumUpgrade.ITEM_LIFE);
            if (!world.isRemote) setHasMinigun(getUpgrades(EnumUpgrade.ENTITY_TRACKER) > 0);
            aiManager.setWidgets(progWidgets);
            energy.setCapacity(100000 + 100000 * getUpgrades(EnumUpgrade.VOLUME));
        }
        boolean enabled = !disabledByHacking && getPressure(null) > 0.01F;
        if (!world.isRemote) {
            setAccelerating(!standby && enabled);
            if (isAccelerating()) {
                fallDistance = 0;
            }
            if (lifeUpgrades > 0) {
                int interval = 10 / lifeUpgrades;
                if (interval == 0 || ticksExisted % interval == 0) {
                    heal(1);
                }
            }
            if (!isSuffocating) {
                suffocationCounter = 40;
            }
            isSuffocating = false;
            Path path = getNavigator().getPath();
            if (path != null) {
                PathPoint target = path.getFinalPathPoint();
                if (target != null) {
                    setTargetedBlock(new BlockPos(target.x, target.y, target.z));
                } else {
                    setTargetedBlock(null);
                }
            } else {
                setTargetedBlock(null);
            }
            if (world.getTotalWorldTime() % 20 == 0) {
                updateSyncedPlayers();
            }
        } else {
            if (digLaser != null) digLaser.update();
            oldLaserExtension = laserExtension;
            if (getActiveProgramKey().equals("dig")) {
                laserExtension = Math.min(1, laserExtension + LASER_EXTEND_SPEED);
            } else {
                laserExtension = Math.max(0, laserExtension - LASER_EXTEND_SPEED);
            }

            if (isAccelerating()) {
                int x = (int) Math.floor(posX);
                int y = (int) Math.floor(posY - 1);
                int z = (int) Math.floor(posZ);
                BlockPos pos = new BlockPos(x, y, z);
                IBlockState state = null;
                for (int i = 0; i < 3; i++) {
                    state = world.getBlockState(pos);
                    if (state.getMaterial() != Material.AIR) break;
                    y--;
                }

                if (state.getMaterial() != Material.AIR) {
                    Vec3d vec = new Vec3d(posY - y, 0, 0);
                    vec = vec.rotateYaw((float) (rand.nextFloat() * Math.PI * 2));
                    world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, posX + vec.x, y + 1, posZ + vec.z, vec.x, 0, vec.z, Block.getStateId(world.getBlockState(pos)));
                }
            }
        }
        if (hasLiquidImmunity) {
            for (int x = (int) posX - 1; x <= (int) (posX + width); x++) {
                for (int y = (int) posY - 1; y <= (int) (posY + height + 1); y++) {
                    for (int z = (int) posZ - 2; z <= (int) (posZ + width); z++) {
                        if (PneumaticCraftUtils.isBlockLiquid(world.getBlockState(new BlockPos(x, y, z)).getBlock())) {
                            world.setBlockState(new BlockPos(x, y, z), Blocks.AIR.getDefaultState(), 2);
                        }
                    }
                }
            }
        }
        if (isAccelerating()) {
            motionX *= 0.3D;
            motionY *= 0.3D;
            motionZ *= 0.3D;
            propSpeed = Math.min(1, propSpeed + 0.04F);
            addAir(null, -1);
        } else {
            propSpeed = Math.max(0, propSpeed - 0.04F);
        }
        oldPropRotation = propRotation;
        propRotation += propSpeed;

        if (!world.isRemote && isEntityAlive()) {
            for (int i = 0; i < 4; i++) {
                getFakePlayer().interactionManager.updateBlockRemoving();
            }
        }
        super.onUpdate();
        if (hasMinigun()) getMinigun().setAttackTarget(getAttackTarget()).update(posX, posY, posZ);
        if (!world.isRemote && isEntityAlive()) {
            if (enabled) aiManager.onUpdateTasks();
            for (EnumFacing d : EnumFacing.VALUES) {
                if (getEmittingRedstone(d) > 0) {
                    if (world.isAirBlock(new BlockPos((int) Math.floor(posX + width / 2), (int) Math.floor(posY), (int) Math.floor(posZ + width / 2)))) {
                        world.setBlockState(new BlockPos((int) Math.floor(posX + width / 2), (int) Math.floor(posY), (int) Math.floor(posZ + width / 2)), Blockss.DRONE_REDSTONE_EMITTER.getDefaultState());
                    }
                    break;
                }
            }
        }
    }

    public BlockPos getTargetedBlock() {
        BlockPos pos = dataManager.get(TARGET_POS);
        return pos.equals(BlockPos.ORIGIN) ? null : pos;
    }

    private void setTargetedBlock(BlockPos pos) {
        dataManager.set(TARGET_POS, pos == null ? BlockPos.ORIGIN : pos);
    }

    @Override
    public int getLaserColor() {
        if (colorMap.containsKey(getCustomNameTag().toLowerCase())) {
            return colorMap.get(getCustomNameTag().toLowerCase());
        } else if (colorMap.containsKey(playerName.toLowerCase())) {
            return colorMap.get(playerName.toLowerCase());
        }
        return super.getLaserColor();
    }

    @Override
    protected BlockPos getDugBlock() {
        BlockPos pos = dataManager.get(DUG_POS);
        return pos.equals(BlockPos.ORIGIN) ? null : pos;
    }

    @Override
    public ItemStack getDroneHeldItem() {
        return ConfigHandler.client.dronesRenderHeldItem ? dataManager.get(HELD_ITEM) : ItemStack.EMPTY;
    }

    @Override
    public void setDugBlock(BlockPos pos) {
        dataManager.set(DUG_POS, pos == null ? BlockPos.ORIGIN : pos);
    }

    public List<EntityAITaskEntry> getRunningTasks() {
        return aiManager.getRunningTasks();
    }

    public EntityAIBase getRunningTargetAI() {
        return aiManager.getTargetAI();
    }

    public void setVariable(String varName, BlockPos pos) {
        aiManager.setCoordinate(varName, pos);
    }

    public BlockPos getVariable(String varName) {
        return aiManager.getCoordinate(varName);
    }

    @Nonnull
    public ItemStack getActiveProgram() {
        String key = getActiveProgramKey();
        if (key.equals("")) {
            return ItemStack.EMPTY;
        } else {
            return ItemProgrammingPuzzle.getStackForWidgetKey(key);
        }
    }

    private String getActiveProgramKey() {
        return dataManager.get(PROGRAM_KEY);
    }

    /**
     * Can only be called when the drone is being debugged, so the client has a synced progWidgets array.
     *
     * @return
     */
    public IProgWidget getActiveWidget() {
        int index = getActiveWidgetIndex();
        if (index >= 0 && index < progWidgets.size()) {
            return progWidgets.get(index);
        } else {
            return null;
        }
    }

    private int getActiveWidgetIndex() {
        return dataManager.get(ACTIVE_WIDGET);
    }

    @Override
    public void setActiveProgram(IProgWidget widget) {
        dataManager.set(PROGRAM_KEY, widget.getWidgetString());
        dataManager.set(ACTIVE_WIDGET, progWidgets.indexOf(widget));
    }

    private void setAccelerating(boolean accelerating) {
        dataManager.set(ACCELERATING, accelerating);
    }

    @Override
    public boolean isAccelerating() {
        return dataManager.get(ACCELERATING);
    }

    private void setDroneColor(int color) {
        dataManager.set(DRONE_COLOR, color);
    }

    @Override
    public int getDroneColor() {
        return dataManager.get(DRONE_COLOR);
    }

    private void setMinigunActivated(boolean activated) {
        dataManager.set(MINIGUN_ACTIVE, activated);
    }

    private boolean isMinigunActivated() {
        return dataManager.get(MINIGUN_ACTIVE);
    }

    private void setHasMinigun(boolean hasMinigun) {
        dataManager.set(HAS_MINIGUN, hasMinigun);
    }

    public boolean hasMinigun() {
        return dataManager.get(HAS_MINIGUN);
    }

    public int getAmmoColor() {
        ItemStack ammo = dataManager.get(AMMO);
        return ItemColorHandler.getAmmoColor(ammo);
    }

    public void setAmmoColor(ItemStack color) {
        dataManager.set(AMMO, color);
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
    public void travel(float par1, float par2, float par3) {
        if (world.isRemote) {
            EntityLivingBase targetEntity = getAttackTarget();
            if (targetEntity != null) {
                if (targetLine == null) targetLine = new RenderProgressingLine(0, -height / 2, 0, 0, 0, 0);
                if (oldTargetLine == null) oldTargetLine = new RenderProgressingLine(0, -height / 2, 0, 0, 0, 0);

                targetLine.endX = targetEntity.posX - posX;
                targetLine.endY = targetEntity.posY + targetEntity.height / 2 - posY;
                targetLine.endZ = targetEntity.posZ - posZ;
                oldTargetLine.endX = targetEntity.prevPosX - prevPosX;
                oldTargetLine.endY = targetEntity.prevPosY + targetEntity.height / 2 - prevPosY;
                oldTargetLine.endZ = targetEntity.prevPosZ - prevPosZ;

                oldTargetLine.setProgress(targetLine.getProgress());
                targetLine.incProgressByDistance(0.3D);
                ignoreFrustumCheck = true; //don't stop rendering the drone when it goes out of the camera frustrum, as we need to render the target lines as well.
            } else {
                ignoreFrustumCheck = false; //don't stop rendering the drone when it goes out of the camera frustrum, as we need to render the target lines as well.
            }
        }
        if (getRidingEntity() == null && isAccelerating()) {
            double d3 = motionY;
            super.travel(par1, par2, par3);
            motionY = d3 * 0.60D;
        } else {
            super.travel(par1, par2, par3);
        }
        onGround = true; //set onGround to true so AI pathfinding will keep updating.
    }

    /**
     * Method that's being called to render anything that has to for the Drone. The matrix is already translated to the drone's position.
     *
     * @param partialTicks
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void renderExtras(double transX, double transY, double transZ, float partialTicks) {
        if (targetLine != null && oldTargetLine != null) {
            GlStateManager.pushMatrix();
            GlStateManager.scale(1, -1, 1);
            GlStateManager.disableTexture2D();
            GlStateManager.color(1, 0, 0, 1);
            targetLine.renderInterpolated(oldTargetLine, partialTicks);
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.enableTexture2D();
            GlStateManager.popMatrix();
        }

        double x = lastTickPosX + (posX - lastTickPosX) * partialTicks;
        double y = lastTickPosY + (posY - lastTickPosY) * partialTicks;
        double z = lastTickPosZ + (posZ - lastTickPosZ) * partialTicks;
        getMinigun().render(x, y, z, 0.6);

        ItemStack held = getDroneHeldItem();
        if (!held.isEmpty()) {
            if (renderDroneHeldItem == null) {
                renderDroneHeldItem = new RenderDroneHeldItem(world);
            }
            renderDroneHeldItem.render(held);
        }
    }

    public double getRange() {
        return 75;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack equippedItem = player.getHeldItem(hand);
        if (!world.isRemote && !equippedItem.isEmpty()) {
            if (equippedItem.getItem() == Itemss.GPS_TOOL) {
                BlockPos gpsLoc = ItemGPSTool.getGPSLocation(equippedItem);
                if (gpsLoc != null) {
                    getNavigator().tryMoveToXYZ(gpsLoc.getX(), gpsLoc.getY(), gpsLoc.getZ(), 0.1D);
                }
            } else {
                int dyeIndex = TileEntityPlasticMixer.getDyeIndex(equippedItem);
                if (dyeIndex >= 0) {
                    setDroneColor(ItemDye.DYE_COLORS[dyeIndex]);
                    equippedItem.shrink(1);
                    if (equippedItem.getCount() <= 0) {
                        player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
                    }
                }
            }
        }
        return false;
    }

    /**
     * Called when a drone is hit by a Pneumatic Wrench.
     */
    @Override
    public boolean rotateBlock(World world, EntityPlayer player, BlockPos pos, EnumFacing side) {
        if (!naturallySpawned) {
            if (player.capabilities.isCreativeMode) naturallySpawned = true;//don't drop the drone in creative.
            attackEntityFrom(new DamageSourceDroneOverload("wrenched"), 2000.0F);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onDeath(DamageSource par1DamageSource) {
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                entityDropItem(inventory.getStackInSlot(i), 0);
                inventory.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
        if (!naturallySpawned) {
            ItemStack drone = getDroppedStack();
            if (hasCustomName()) drone.setStackDisplayName(getCustomNameTag());
            entityDropItem(drone, 0);

            if (!world.isRemote) {
                EntityPlayer owner = getOwner();
                if (owner != null) {
                    int x = (int) Math.floor(posX);
                    int y = (int) Math.floor(posY);
                    int z = (int) Math.floor(posZ);
                    ITextComponent msg = hasCustomName() ?
                            new TextComponentTranslation("death.drone.named", getCustomNameTag(), x, y, z) :
                            new TextComponentTranslation("death.drone", x, y, z);
                    msg = msg.appendSibling(new TextComponentString(" - ")).appendSibling(par1DamageSource.getDeathMessage(this));
                    owner.sendStatusMessage(msg, false);
                }
            }
        }
        if (!world.isRemote) getFakePlayer().interactionManager.cancelDestroyingBlock();
        super.onDeath(par1DamageSource);
    }

    protected ItemStack getDroppedStack() {
        NBTTagCompound tag = new NBTTagCompound();
        writeEntityToNBT(tag);
        ItemStack drone = new ItemStack(Itemss.DRONE);
        drone.setTagCompound(tag);
        return drone;
    }

    @Override
    public void setAttackTarget(EntityLivingBase entity) {
        super.setAttackTarget(entity);
        if (world.isRemote && targetLine != null && oldTargetLine != null) {
            targetLine.setProgress(0);
            oldTargetLine.setProgress(0);
        }
    }

    @Override
    public float getPressure(ItemStack iStack) {
        return dataManager.get(PRESSURE);
    }

    @Override
    public void addAir(ItemStack iStack, int amount) {
        if (!world().isRemote) {
            currentAir += amount;
            dataManager.set(PRESSURE, currentAir / volume);
        }
    }

    @Override
    public float maxPressure(ItemStack iStack) {
        return PneumaticValues.DRONE_MAX_PRESSURE;
    }

    @Override
    public void printManometerMessage(EntityPlayer player, List<String> curInfo) {
        if (hasCustomName()) curInfo.add(TextFormatting.AQUA + getCustomNameTag());
        curInfo.add("Owner: " + getFakePlayer().getName());
        curInfo.add("Current pressure: " + PneumaticCraftUtils.roundNumberTo(getPressure(null), 1) + " bar.");
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        TileEntityProgrammer.setWidgetsToNBT(progWidgets, tag);
        tag.setBoolean("naturallySpawned", naturallySpawned);
        tag.setFloat("currentAir", currentAir);
        tag.setFloat("propSpeed", propSpeed);
        tag.setBoolean("disabledByHacking", disabledByHacking);
        tag.setBoolean("hackedByOwner", gotoOwnerAI != null);
        tag.setInteger("color", getDroneColor());
        tag.setBoolean("standby", standby);
        tag.setFloat("volume", volume);

        NBTTagCompound variableTag = new NBTTagCompound();
        aiManager.writeToNBT(variableTag);
        tag.setTag("variables", variableTag);

        tag.setTag("Inventory", inventory.serializeNBT());
        tag.setTag(ChargeableItemHandler.NBT_UPGRADE_TAG, upgradeInventory.serializeNBT());

        tank.writeToNBT(tag);

        if (handlingOffer != null) {
            NBTTagCompound subTag = new NBTTagCompound();
            subTag.setBoolean("isCustom", handlingOffer instanceof AmadronOfferCustom);
            handlingOffer.writeToNBT(subTag);
            tag.setTag("amadronOffer", subTag);
            tag.setInteger("offerTimes", offerTimes);
            if (!usedTablet.isEmpty()) usedTablet.writeToNBT(subTag);
            tag.setString("buyingPlayer", buyingPlayer);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        progWidgets = TileEntityProgrammer.getWidgetsFromNBT(tag);
        naturallySpawned = tag.getBoolean("naturallySpawned");
        currentAir = tag.getFloat("currentAir");
        volume = tag.getFloat("volume");
        dataManager.set(PRESSURE, currentAir / volume);
        propSpeed = tag.getFloat("propSpeed");
        disabledByHacking = tag.getBoolean("disabledByHacking");
        setGoingToOwner(tag.getBoolean("hackedByOwner"));
        setDroneColor(tag.getInteger("color"));
        aiManager.readFromNBT(tag.getCompoundTag("variables"));
        standby = tag.getBoolean("standby");

        upgradeInventory.deserializeNBT(tag.getCompoundTag(ChargeableItemHandler.NBT_UPGRADE_TAG));

        // we can't just deserialize the saved inv directly into the inventory, since that
        // also affects its size, meaning any added dispenser upgrades wouldn't work
        inventory = new ItemHandlerDrone(1 + getUpgrades(EnumUpgrade.DISPENSER));
        ItemHandlerDrone tmpInv = new ItemHandlerDrone(1);
        tmpInv.deserializeNBT(tag.getCompoundTag("Inventory"));
        for (int i = 0; i < tmpInv.getSlots() && i < inventory.getSlots(); i++) {
            inventory.setStackInSlot(i, tmpInv.getStackInSlot(i).copy());
        }

        tank.setCapacity(PneumaticValues.DRONE_TANK_SIZE * (1 + getUpgrades(EnumUpgrade.DISPENSER)));
        tank.readFromNBT(tag);

        energy.setCapacity(100000 + 100000 * getUpgrades(EnumUpgrade.VOLUME));

        if (tag.hasKey("amadronOffer")) {
            NBTTagCompound subTag = tag.getCompoundTag("amadronOffer");
            if (subTag.getBoolean("isCustom")) {
                handlingOffer = AmadronOffer.loadFromNBT(subTag);
            } else {
                handlingOffer = AmadronOfferCustom.loadFromNBT(subTag);
            }
            if (subTag.hasKey("id")) {
                usedTablet = new ItemStack(subTag);
            } else {
                usedTablet = ItemStack.EMPTY;
            }
            buyingPlayer = subTag.getString("buyingPlayer");
        } else {
            handlingOffer = null;
            usedTablet = ItemStack.EMPTY;
            buyingPlayer = null;
        }
        offerTimes = tag.getInteger("offerTimes");
    }
    
    private String getOwnerUUID(){
        if(playerUUID == null){
            Log.warning(String.format("Drone with owner '%s' has no UUID! Substituting the Drone's UUID (%s).", playerName, getUniqueID().toString()));
            playerUUID = getUniqueID().toString();
        }
        return playerUUID;
    }

    /**
     * This and readFromNBT are _not_ being transfered from/to the Drone item.
     */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        if(playerName != null){
            tag.setString("owner", playerName);
            tag.setString("ownerUUID", getOwnerUUID());
        }
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey("owner")) {
            playerName = tag.getString("owner");
            playerUUID = tag.hasKey("ownerUUID") ? tag.getString("ownerUUID") : null;
        }
    }

    public int getUpgrades(EnumUpgrade upgrade) {
        return getUpgrades(Itemss.upgrades.get(upgrade));
    }

    @Override
    public int getUpgrades(Item upgrade) {
        int upgrades = 0;
        for (int i = 0; i < upgradeInventory.getSlots(); i++) {
            if (upgradeInventory.getStackInSlot(i).getItem() == upgrade) {
                upgrades += upgradeInventory.getStackInSlot(i).getCount();
            }
        }
        return upgrades;
    }

    @Override
    public DroneFakePlayer getFakePlayer() {
        if (fakePlayer == null && !world.isRemote) {
            initializeFakePlayer();
        }
        return fakePlayer;
    }

    public Minigun getMinigun() {
        if (minigun == null) {
            minigun = new MinigunDrone().setPlayer(getFakePlayer()).setWorld(world).setPressurizable(this, PneumaticValues.DRONE_USAGE_ATTACK);
        }
        return minigun;
    }

    @Override
    public boolean attackEntityAsMob(Entity entity) {
        getFakePlayer().attackTargetEntityWithCurrentItem(entity);
        addAir(null, -PneumaticValues.DRONE_USAGE_ATTACK);
        return true;
    }

    @Override
    public boolean attackEntityFrom(DamageSource damageSource, float damage) {
        if (damageSource == DamageSource.IN_WALL) {
            isSuffocating = true;
            if (suffocationCounter-- > 0 || !ConfigHandler.general.enableDroneSuffocationDamage) {
                return false;
            }
        }
        return super.attackEntityFrom(damageSource, damage);
    }

    @Override
    public IItemHandlerModifiable getInv() {
        return inventory;
    }

    public double getSpeed() {
        return speed;
    }

    public int getEmittingRedstone(EnumFacing side) {
        return emittingRedstoneValues[side.ordinal()];
    }

    @Override
    public void setEmittingRedstone(EnumFacing side, int value) {
        if (emittingRedstoneValues[side.ordinal()] != value) {
            emittingRedstoneValues[side.ordinal()] = value;
            BlockPos pos = new BlockPos((int) Math.floor(posX + width / 2), (int) Math.floor(posY), (int) Math.floor(posZ + width / 2));
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    @Override
    public boolean isBlockValidPathfindBlock(BlockPos pos) {
        if (world.isAirBlock(pos)) return true;
        Block block = world.getBlockState(pos).getBlock();
        if (PneumaticCraftUtils.isBlockLiquid(block)) {
            return hasLiquidImmunity;
        }
        if (block.isPassable(world, pos) && block != Blocks.LADDER) return true;
        if (DroneRegistry.getInstance().pathfindableBlocks.containsKey(block)) {
            IPathfindHandler pathfindHandler = DroneRegistry.getInstance().pathfindableBlocks.get(block);
            return pathfindHandler == null || pathfindHandler.canPathfindThrough(world, pos);
        } else {
            return false;
        }
    }

    @Override
    public void sendWireframeToClient(BlockPos pos) {
        NetworkHandler.sendToAllAround(new PacketShowWireframe(this, pos), world);
    }

    private class ItemHandlerDrone extends ItemStackHandler {
        ItemStack oldStack = ItemStack.EMPTY;

        ItemHandlerDrone(int size) {
            super(size);
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);

            if (slot == 0 && getFakePlayer() != null) {  // the "currently-held" item
                ItemStack newStack = inventory.getStackInSlot(slot);
                if (!oldStack.isEmpty()) {
                    for (EntityEquipmentSlot s : EntityEquipmentSlot.values()) {
                        getFakePlayer().getAttributeMap().removeAttributeModifiers(oldStack.getAttributeModifiers(s));
                    }
                }

                if (!newStack.isEmpty()) {
                    for (EntityEquipmentSlot s : EntityEquipmentSlot.values()) {
                        getFakePlayer().getAttributeMap().applyAttributeModifiers(newStack.getAttributeModifiers(s));
                    }
                }

                oldStack = newStack;

                if (ConfigHandler.client.dronesRenderHeldItem) dataManager.set(HELD_ITEM, newStack);
            }
        }
    }

    public static class DroneFakePlayer extends FakePlayer {
        private final IDroneBase drone;
        private boolean sneaking;

        public DroneFakePlayer(WorldServer world, GameProfile name, IDroneBase drone) {
            super(world, name);
            this.drone = drone;
        }

        @Override
        public void addExperience(int amount) {
            Vec3d pos = drone.getDronePos();
            EntityXPOrb orb = new EntityXPOrb(drone.world(), pos.x, pos.y, pos.z, amount);
            drone.world().spawnEntity(orb);
        }

        @Nonnull
        @Override
        public ItemStack getItemStackFromSlot(@Nonnull EntityEquipmentSlot slotIn) {
            switch (slotIn) {
                case MAINHAND:
                    return drone.getInv().getStackInSlot(0);
                default:
                    return ItemStack.EMPTY;
            }
        }

        @Override
        public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack) {
            if (slotIn == EntityEquipmentSlot.MAINHAND) {
                drone.getInv().setStackInSlot(0, stack);
            }
        }

        @Override
        public boolean isSneaking() {
            return sneaking;
        }

        @Override
        public void setSneaking(boolean sneaking) {
            this.sneaking = sneaking;
        }
    }

    /**
     * IHackableEntity
     */

    @Override
    public String getId() {
        return null;
    }

    @Override
    public boolean canHack(Entity entity, EntityPlayer player) {
        return isAccelerating();
    }

    @Override
    public void addInfo(Entity entity, List<String> curInfo, EntityPlayer player) {
        if (playerName.equals(player.getName())) {
            if (isGoingToOwner()) {
                curInfo.add("pneumaticHelmet.hacking.result.resumeTasks");
            } else {
                curInfo.add("pneumaticHelmet.hacking.result.callBack");
            }
        } else {
            curInfo.add("pneumaticHelmet.hacking.result.disable");
        }
    }

    @Override
    public void addPostHackInfo(Entity entity, List<String> curInfo, EntityPlayer player) {
        if (playerName.equals(player.getName())) {
            if (isGoingToOwner()) {
                curInfo.add("pneumaticHelmet.hacking.finished.calledBack");
            } else {
                curInfo.add("pneumaticHelmet.hacking.finished.resumedTasks");
            }
        } else {
            curInfo.add("pneumaticHelmet.hacking.finished.disabled");
        }
    }

    @Override
    public int getHackTime(Entity entity, EntityPlayer player) {
        return playerName.equals(player.getName()) ? 20 : 100;
    }

    @Override
    public void onHackFinished(Entity entity, EntityPlayer player) {
        if (!world.isRemote && player.getGameProfile().equals(getFakePlayer().getGameProfile())) {
            setGoingToOwner(gotoOwnerAI == null);//toggle the state
        } else {
            disabledByHacking = true;
        }
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }

    private void setGoingToOwner(boolean state) {
        if (state && gotoOwnerAI == null) {
            gotoOwnerAI = new DroneGoToOwner(this);
            tasks.addTask(2, gotoOwnerAI);
            dataManager.set(GOING_TO_OWNER, true);
            setActiveProgram(new ProgWidgetGoToLocation());
        } else if (!state && gotoOwnerAI != null) {
            tasks.removeTask(gotoOwnerAI);
            gotoOwnerAI = null;
            dataManager.set(GOING_TO_OWNER, false);
        }
    }

    private boolean isGoingToOwner() {
        return dataManager.get(GOING_TO_OWNER);
    }

    @Override
    public IFluidTank getTank() {
        return tank;
    }

    /**
     * Returns the owning player. Returns null when the player is not online.
     *
     * @return the owning player
     */
    public EntityPlayer getOwner() {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(playerName);
    }

    public void setStandby(boolean standby) {
        this.standby = standby;
    }

    @Override
    public World world() {
        return world;
    }

    @Override
    public Vec3d getDronePos() {
        return new Vec3d(posX, posY, posZ);
    }

    @Override
    public void dropItem(ItemStack stack) {
        entityDropItem(stack, 0);
    }

    @Override
    public List<IProgWidget> getProgWidgets() {
        return progWidgets;
    }

    @Override
    public EntityAITasks getTargetAI() {
        return targetTasks;
    }

    @Override
    public boolean isProgramApplicable(IProgWidget widget) {
        return true;
    }

    @Override
    public void setName(String string) {
        setCustomNameTag(string);
    }

    @Override
    public void setCarryingEntity(Entity entity) {
        if (entity == null) {
            for (Entity e : getCarryingEntities()) {
                e.dismountRidingEntity();
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
    public boolean isAIOverriden() {
        return chargeAI.isExecuting || gotoOwnerAI != null;
    }

    @Override
    public void onItemPickupEvent(EntityItem curPickingUpEntity, int stackSize) {
        onItemPickup(curPickingUpEntity, stackSize);
    }

    @Override
    public IPathNavigator getPathNavigator() {
        return (IPathNavigator) getNavigator();
    }

    public void tryFireMinigun(EntityLivingBase target) {
        ItemStack ammo = getAmmo();
        if (getMinigun().setAmmo(ammo).tryFireMinigun(target)) {
            for (int i = 0; i < inventory.getSlots(); i++) {
                if (inventory.getStackInSlot(i) == ammo) {
                    inventory.setStackInSlot(i, ItemStack.EMPTY);
                }
            }
        }
    }

    public ItemStack getAmmo() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.getItem() == Itemss.GUN_AMMO) {
                return stack;
            }
        }
        return null;
    }

    public void setHandlingOffer(AmadronOffer offer, int times, ItemStack usedTablet, String buyingPlayer) {
        handlingOffer = offer;
        offerTimes = times;
        this.usedTablet = usedTablet.copy();
        this.buyingPlayer = buyingPlayer;
    }

    public AmadronOffer getHandlingOffer() {
        return handlingOffer;
    }

    public int getOfferTimes() {
        return offerTimes;
    }

    public ItemStack getUsedTablet() {
        return usedTablet;
    }

    public String getBuyingPlayer() {
        return buyingPlayer;
    }

    @Override
    public void overload(String msgKey, Object... params) {
        attackEntityFrom(new DamageSourceDroneOverload(msgKey, params), 2000.0F);
    }

    @Override
    public DroneAIManager getAIManager() {
        return aiManager;
    }

    @Override
    public void updateLabel() {
        dataManager.set(LABEL, getAIManager() != null ? getAIManager().getLabel() : "Main");
    }

    public String getLabel() {
        return dataManager.get(LABEL);
    }

    public SortedSet<DebugEntry> getDebugEntries() {
        return debugEntries;
    }

    @Override
    public void addDebugEntry(String message) {
        addDebugEntry(message, null);
    }

    @Override
    public void addDebugEntry(String message, BlockPos pos) {

        DebugEntry entry = new DebugEntry(message, getActiveWidgetIndex(), pos);
        addDebugEntry(entry);

        PacketSendDroneDebugEntry packet = new PacketSendDroneDebugEntry(entry, this);
        for (EntityPlayerMP player : syncedPlayers) {
            NetworkHandler.sendTo(packet, player);
        }
    }

    public void addDebugEntry(DebugEntry entry) {
        if (!debugEntries.isEmpty()) {
            DebugEntry previous = debugEntries.last();
            if (previous.getProgWidgetId() != entry.getProgWidgetId()) {
                // When we've jumped to another piece, remove the data from the previous cycle
                debugEntries.removeIf(debugEntry -> debugEntry.getProgWidgetId() == entry.getProgWidgetId());
            }
        }
        debugEntries.add(entry);
    }

    public void trackAsDebugged(EntityPlayerMP player) {
        NetworkHandler.sendTo(new PacketSyncDroneEntityProgWidgets(this), player);

        for (DebugEntry entry : debugEntries) {
            NetworkHandler.sendTo(new PacketSendDroneDebugEntry(entry, this), player);
        }

        syncedPlayers.add(player);
    }

    private void updateSyncedPlayers() {
        syncedPlayers.removeIf(player -> player.isDead || player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty() || NBTUtil.getInteger(player.getItemStackFromSlot(EntityEquipmentSlot.HEAD), NBTKeys.PNEUMATIC_HELMET_DEBUGGING_DRONE) != getEntityId());
    }

    private class MinigunDrone extends Minigun {

        MinigunDrone() {
            super(true);
        }

        @Override
        public boolean isMinigunActivated() {
            return EntityDrone.this.isMinigunActivated();
        }

        @Override
        public void setMinigunActivated(boolean activated) {
            EntityDrone.this.setMinigunActivated(activated);
        }

        @Override
        public void setAmmoColorStack(@Nonnull ItemStack ammo) {
            setAmmoColor(ammo);
        }

        @Override
        public int getAmmoColor() {
            return EntityDrone.this.getAmmoColor();
        }

        @Override
        public void playSound(SoundEvent soundName, float volume, float pitch) {
            NetworkHandler.sendToAllAround(new PacketPlaySound(soundName, SoundCategory.NEUTRAL, posX, posY, posZ, volume, pitch, true), world);
        }

    }

    // Drone's fake player needs a custom inventory
    // This is so EntityPlayer#getDigSpeed() gets the right tool speed (i.e. the tool in the drone's inv. slot 0)
    // Overriding DroneFakePlayer#getItemStackFromSlot() is also necessary, but not sufficient on its own
    private class InventoryFakePlayer extends InventoryPlayer {
        InventoryFakePlayer(EntityPlayer fakePlayer) {
            super(fakePlayer);
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int index) {
            return getInv().getStackInSlot(index);
        }

        @Override
        public float getDestroySpeed(IBlockState state) {
            float f = 1.0f;

            if (!getInv().getStackInSlot(0).isEmpty()) {
                f *= getInv().getStackInSlot(0).getDestroySpeed(state);
            }

            return f;
        }
    }
}
