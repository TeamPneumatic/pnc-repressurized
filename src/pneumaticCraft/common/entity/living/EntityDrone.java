package pneumaticCraft.common.entity.living;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.client.C15PacketClientSettings;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.stats.StatBase;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.api.block.IPneumaticWrenchable;
import pneumaticCraft.api.client.pneumaticHelmet.IHackableEntity;
import pneumaticCraft.api.drone.IPathNavigator;
import pneumaticCraft.api.drone.IPathfindHandler;
import pneumaticCraft.api.tileentity.IManoMeasurable;
import pneumaticCraft.client.render.RenderProgressingLine;
import pneumaticCraft.common.NBTUtil;
import pneumaticCraft.common.PneumaticCraftAPIHandler;
import pneumaticCraft.common.ai.DroneAIManager;
import pneumaticCraft.common.ai.DroneAIManager.EntityAITaskEntry;
import pneumaticCraft.common.ai.DroneGoToChargingStation;
import pneumaticCraft.common.ai.DroneGoToOwner;
import pneumaticCraft.common.ai.DroneMoveHelper;
import pneumaticCraft.common.ai.EntityPathNavigateDrone;
import pneumaticCraft.common.ai.FakePlayerItemInWorldManager;
import pneumaticCraft.common.ai.IDroneBase;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.config.Config;
import pneumaticCraft.common.item.ItemGPSTool;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.item.ItemProgrammingPuzzle;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.minigun.Minigun;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketSendDroneDebugEntry;
import pneumaticCraft.common.network.PacketShowWireframe;
import pneumaticCraft.common.network.PacketSyncDroneEntityProgWidgets;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetGoToLocation;
import pneumaticCraft.common.recipes.AmadronOffer;
import pneumaticCraft.common.recipes.AmadronOfferCustom;
import pneumaticCraft.common.tileentity.TileEntityPlasticMixer;
import pneumaticCraft.common.tileentity.TileEntityProgrammer;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.NBTKeys;
import pneumaticCraft.lib.PneumaticValues;

import com.mojang.authlib.GameProfile;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityDrone extends EntityDroneBase implements IManoMeasurable, IInventoryHolder, IPneumaticWrenchable,
        IEntityAdditionalSpawnData, IHackableEntity, IDroneBase{

    private static final HashMap<String, Integer> colorMap = new HashMap<String, Integer>();

    static {
        colorMap.put("aureylian", 0xff69b4);
        colorMap.put("loneztar", 0x00a0a0);
        colorMap.put("jadedcat", 0xa020f0);
    }

    public boolean isChangingCurrentStack;//used when syncing up the stacks of the drone with the fake player. Without it they'll keep syncing resulting in a stackoverflow.
    private IInventory inventory = new InventoryDrone("Drone Inventory", true, 0);
    private final FluidTank tank = new FluidTank(Integer.MAX_VALUE);
    private ItemStack[] upgradeInventory = new ItemStack[9];
    private final int[] emittingRedstoneValues = new int[6];
    private float propSpeed;
    private static final float LASER_EXTEND_SPEED = 0.05F;

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
    public boolean naturallySpawned = true;//determines if it should drop a drone when it dies.
    public boolean hasLiquidImmunity;
    private double speed;
    private int lifeUpgrades;
    private int suffocationCounter = 40;//Drones are invincible for suffocation for this time.
    private boolean isSuffocating;
    private boolean disabledByHacking;
    private boolean standby;//If true, the drone's propellors stop, the drone will fall down, and won't use pressure.
    private Minigun minigun;

    private AmadronOffer handlingOffer;
    private int offerTimes;
    private ItemStack usedTablet;//Tablet used to place the order.
    private String buyingPlayer;
    private final SortedSet<DebugEntry> debugEntries = new TreeSet<DebugEntry>();
    private final Set<EntityPlayerMP> syncedPlayers = new HashSet<EntityPlayerMP>();

    public EntityDrone(World world){
        super(world);
        setSize(0.7F, 0.35F);
        ReflectionHelper.setPrivateValue(EntityLiving.class, this, new EntityPathNavigateDrone(this, world), "navigator", "field_70699_by");
        ReflectionHelper.setPrivateValue(EntityLiving.class, this, new DroneMoveHelper(this), "moveHelper", "field_70765_h");
        tasks.addTask(1, chargeAI = new DroneGoToChargingStation(this));
    }

    public EntityDrone(World world, EntityPlayer player){
        this(world);
        playerUUID = player.getGameProfile().getId().toString();
        playerName = player.getCommandSenderName();
    }

    private void initializeFakePlayer(){
        fakePlayer = new DroneFakePlayer((WorldServer)worldObj, new GameProfile(playerUUID != null ? UUID.fromString(playerUUID) : null, playerName), new FakePlayerItemInWorldManager(worldObj, fakePlayer, this), this);
        fakePlayer.playerNetServerHandler = new NetHandlerPlayServer(MinecraftServer.getServer(), new NetworkManager(false), fakePlayer);
        fakePlayer.inventory = new InventoryFakePlayer(fakePlayer);
    }

    @Override
    protected void entityInit(){
        super.entityInit();
        dataWatcher.addObject(12, 0F);
        dataWatcher.addObject(13, (byte)0);
        dataWatcher.addObject(14, 0);
        dataWatcher.addObject(15, 0);
        dataWatcher.addObject(16, 0);
        dataWatcher.addObject(17, "");
        dataWatcher.addObject(18, 0);
        dataWatcher.addObject(19, 0);
        dataWatcher.addObject(20, 0);
        dataWatcher.addObject(21, (byte)0);
        dataWatcher.addObject(22, 0);
        dataWatcher.addObject(23, (byte)0);
        dataWatcher.addObject(24, (byte)0);
        dataWatcher.addObjectByDataType(25, 5);
        dataWatcher.addObject(26, "Main");
        dataWatcher.addObject(27, 0);
    }

    @Override
    protected void applyEntityAttributes(){
        super.applyEntityAttributes();
        getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(3.0D);
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(40F);
        getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(getRange());
    }

    @Override
    public void writeSpawnData(ByteBuf data){
        ByteBufUtils.writeUTF8String(data, getFakePlayer().getCommandSenderName());
    }

    @Override
    public void readSpawnData(ByteBuf data){
        playerName = ByteBufUtils.readUTF8String(data);
    }

    /**
     * Determines if an entity can be despawned, used on idle far away entities
     */
    @Override
    protected boolean canDespawn(){
        return false;
    }

    @Override
    protected float getSoundVolume(){
        return 0.2F;
    }

    @Override
    protected String getHurtSound(){
        return "pneumaticcraft:drone.hurt";
    }

    /**
     * Returns the sound this mob makes on death.
     */
    @Override
    protected String getDeathSound(){
        return "pneumaticcraft:drone.death";
    }

    @Override
    public void onUpdate(){
        if(firstTick) {
            firstTick = false;
            volume = PneumaticValues.DRONE_VOLUME + getUpgrades(ItemMachineUpgrade.UPGRADE_VOLUME_DAMAGE) * PneumaticValues.VOLUME_VOLUME_UPGRADE;
            hasLiquidImmunity = getUpgrades(ItemMachineUpgrade.UPGRADE_SECURITY) > 0;
            if(hasLiquidImmunity) {
                ((EntityPathNavigateDrone)getPathNavigator()).pathThroughLiquid = true;
            }
            speed = 0.1 + Math.min(10, getUpgrades(ItemMachineUpgrade.UPGRADE_SPEED_DAMAGE)) * 0.01;
            lifeUpgrades = getUpgrades(ItemMachineUpgrade.UPGRADE_ITEM_LIFE);
            if(!worldObj.isRemote) setHasMinigun(getUpgrades(ItemMachineUpgrade.UPGRADE_ENTITY_TRACKER) > 0);
            aiManager.setWidgets(progWidgets);
        }
        boolean enabled = !disabledByHacking && getPressure(null) > 0.01F;
        if(!worldObj.isRemote) {
            setAccelerating(!standby && enabled);
            if(isAccelerating()) {
                fallDistance = 0;
            }
            if(lifeUpgrades > 0) {
                int interval = 10 / lifeUpgrades;
                if(interval == 0 || ticksExisted % interval == 0) {
                    heal(1);
                }
            }
            if(!isSuffocating) {
                suffocationCounter = 40;
            }
            isSuffocating = false;
            PathEntity path = getNavigator().getPath();
            if(path != null) {
                PathPoint target = path.getFinalPathPoint();
                if(target != null) {
                    setTargetedBlock(target.xCoord, target.yCoord, target.zCoord);
                } else {
                    setTargetedBlock(0, 0, 0);
                }
            } else {
                setTargetedBlock(0, 0, 0);
            }
            if(worldObj.getTotalWorldTime() % 20 == 0) {
                updateSyncedPlayers();
            }
        } else {
            if(digLaser != null) digLaser.update();
            oldLaserExtension = laserExtension;
            if(getActiveProgramKey().equals("dig")) {
                laserExtension = Math.min(1, laserExtension + LASER_EXTEND_SPEED);
            } else {
                laserExtension = Math.max(0, laserExtension - LASER_EXTEND_SPEED);
            }

            if(isAccelerating()) {
                int x = (int)Math.floor(posX);
                int y = (int)Math.floor(posY - 1);
                int z = (int)Math.floor(posZ);
                Block block = null;
                for(int i = 0; i < 3; i++) {
                    block = worldObj.getBlock(x, y, z);
                    if(block.getMaterial() != Material.air) break;
                    y--;
                }

                if(block.getMaterial() != Material.air) {
                    Vec3 vec = Vec3.createVectorHelper(posY - y, 0, 0);
                    vec.rotateAroundY((float)(rand.nextFloat() * Math.PI * 2));
                    worldObj.spawnParticle("blockcrack_" + Block.getIdFromBlock(block) + "_" + worldObj.getBlockMetadata(x, y, z), posX + vec.xCoord, y + 1, posZ + vec.zCoord, vec.xCoord, 0, vec.zCoord);
                }
            }
        }
        if(hasLiquidImmunity) {
            for(int x = (int)posX - 1; x <= (int)(posX + width); x++) {
                for(int y = (int)posY - 1; y <= (int)(posY + height + 1); y++) {
                    for(int z = (int)posZ - 2; z <= (int)(posZ + width); z++) {
                        if(PneumaticCraftUtils.isBlockLiquid(worldObj.getBlock(x, y, z))) {
                            worldObj.setBlock(x, y, z, Blocks.air, 0, 2);
                        }
                    }
                }
            }
        }
        if(isAccelerating()) {
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

        if(!worldObj.isRemote && isEntityAlive()/*((FakePlayerItemInWorldManager)fakePlayer.theItemInWorldManager).isDigging()*/) {
            for(int i = 0; i < 4; i++) {
                getFakePlayer().theItemInWorldManager.updateBlockRemoving();
            }
        }
        super.onUpdate();
        if(hasMinigun()) getMinigun().setAttackTarget(getAttackTarget()).update(posX, posY, posZ);
        if(!worldObj.isRemote && isEntityAlive()) {
            if(enabled) aiManager.onUpdateTasks();
            for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
                if(getEmittingRedstone(d) > 0) {
                    if(worldObj.isAirBlock((int)Math.floor(posX + width / 2), (int)Math.floor(posY), (int)Math.floor(posZ + width / 2))) {
                        worldObj.setBlock((int)Math.floor(posX + width / 2), (int)Math.floor(posY), (int)Math.floor(posZ + width / 2), Blockss.droneRedstoneEmitter);
                    }
                    break;
                }
            }
        }
    }

    public ChunkPosition getTargetedBlock(){
        int x = dataWatcher.getWatchableObjectInt(14);
        int y = dataWatcher.getWatchableObjectInt(15);
        int z = dataWatcher.getWatchableObjectInt(16);
        return x != 0 || y != 0 || z != 0 ? new ChunkPosition(x, y, z) : null;
    }

    private void setTargetedBlock(int x, int y, int z){
        dataWatcher.updateObject(14, x);
        dataWatcher.updateObject(15, y);
        dataWatcher.updateObject(16, z);
    }

    @Override
    public int getLaserColor(){
        if(colorMap.containsKey(getCustomNameTag().toLowerCase())) {
            return colorMap.get(getCustomNameTag().toLowerCase());
        } else if(colorMap.containsKey(playerName.toLowerCase())) {
            return colorMap.get(playerName.toLowerCase());
        }
        return super.getLaserColor();
    }

    @Override
    protected ChunkPosition getDugBlock(){
        int x = dataWatcher.getWatchableObjectInt(18);
        int y = dataWatcher.getWatchableObjectInt(19);
        int z = dataWatcher.getWatchableObjectInt(20);
        return x != 0 || y != 0 || z != 0 ? new ChunkPosition(x, y, z) : null;
    }

    @Override
    public void setDugBlock(int x, int y, int z){
        dataWatcher.updateObject(18, x);
        dataWatcher.updateObject(19, y);
        dataWatcher.updateObject(20, z);
    }

    public List<EntityAITaskEntry> getRunningTasks(){
        return aiManager.getRunningTasks();
    }

    public EntityAIBase getRunningTargetAI(){
        return aiManager.getTargetAI();
    }

    public void setVariable(String varName, ChunkPosition pos){
        aiManager.setCoordinate(varName, pos);
    }

    public ChunkPosition getVariable(String varName){
        return aiManager.getCoordinate(varName);
    }

    public ItemStack getActiveProgram(){
        String key = getActiveProgramKey();
        if(key.equals("")) {
            return null;
        } else {
            return ItemProgrammingPuzzle.getStackForWidgetKey(key);
        }
    }

    private String getActiveProgramKey(){
        return dataWatcher.getWatchableObjectString(17);
    }

    /**
     * Can only be called when the drone is being debugged, so the client has a synced progWidgets array.
     * @return
     */
    public IProgWidget getActiveWidget(){
        int index = getActiveWidgetIndex();
        if(index >= 0 && index < progWidgets.size()) {
            return progWidgets.get(index);
        } else {
            return null;
        }
    }

    private int getActiveWidgetIndex(){
        return dataWatcher.getWatchableObjectInt(27);
    }

    @Override
    public void setActiveProgram(IProgWidget widget){
        dataWatcher.updateObject(17, widget.getWidgetString());
        dataWatcher.updateObject(27, progWidgets.indexOf(widget));
    }

    private void setAccelerating(boolean accelerating){
        dataWatcher.updateObject(13, (byte)(accelerating ? 1 : 0));
    }

    @Override
    public boolean isAccelerating(){
        return dataWatcher.getWatchableObjectByte(13) == 1;
    }

    private void setDroneColor(int color){
        dataWatcher.updateObject(22, color);
    }

    @Override
    public int getDroneColor(){
        return dataWatcher.getWatchableObjectInt(22);
    }

    public void setMinigunActivated(boolean activated){
        dataWatcher.updateObject(23, (byte)(activated ? 1 : 0));
    }

    public boolean isMinigunActivated(){
        return dataWatcher.getWatchableObjectByte(23) == 1;
    }

    public void setHasMinigun(boolean hasMinigun){
        dataWatcher.updateObject(24, (byte)(hasMinigun ? 1 : 0));
    }

    public boolean hasMinigun(){
        return dataWatcher.getWatchableObjectByte(24) == 1;
    }

    public int getAmmoColor(){
        ItemStack ammo = dataWatcher.getWatchableObjectItemStack(25);
        return ammo != null ? ammo.getItem().getColorFromItemStack(ammo, 1) : 0xFF313131;
    }

    public void setAmmoColor(ItemStack color){
        dataWatcher.updateObject(25, color);
    }

    /**
     * Returns true if the newer Entity AI code should be run
     */
    @Override
    public boolean isAIEnabled(){
        return true;
    }

    /**
     * Decrements the entity's air supply when underwater
     */
    @Override
    protected int decreaseAirSupply(int par1){
        return -20;//make drones insta drown.
    }

    /**
     * Moves the entity based on the specified heading.  Args: strafe, forward
     */
    @Override
    public void moveEntityWithHeading(float par1, float par2){
        if(worldObj.isRemote) {
            EntityLivingBase targetEntity = getAttackTarget();
            if(targetEntity != null) {
                if(targetLine == null) targetLine = new RenderProgressingLine(0, -height / 2, 0, 0, 0, 0);
                if(oldTargetLine == null) oldTargetLine = new RenderProgressingLine(0, -height / 2, 0, 0, 0, 0);

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
        if(ridingEntity == null && isAccelerating()) {
            double d3 = motionY;
            super.moveEntityWithHeading(par1, par2);
            motionY = d3 * 0.60D;
        } else {
            super.moveEntityWithHeading(par1, par2);
        }
        onGround = true;//set onGround to true so AI pathfinding will keep updating.
    }

    /**
     * Method that's being called to render anything that has to for the Drone. The matrix is already translated to the drone's position.
     * @param partialTicks
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void renderExtras(double transX, double transY, double transZ, float partialTicks){
        if(targetLine != null && oldTargetLine != null) {
            GL11.glPushMatrix();
            GL11.glScaled(1, -1, 1);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            //GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glColor4d(1, 0, 0, 1);
            targetLine.renderInterpolated(oldTargetLine, partialTicks);
            GL11.glColor4d(1, 1, 1, 1);
            // GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glPopMatrix();
        }

        double x = lastTickPosX + (posX - lastTickPosX) * partialTicks;
        double y = lastTickPosY + (posY - lastTickPosY) * partialTicks;
        double z = lastTickPosZ + (posZ - lastTickPosZ) * partialTicks;
        getMinigun().render(x, y, z, 0.6);
    }

    public double getRange(){
        return 75;
    }

    @Override
    public boolean interact(EntityPlayer player){
        ItemStack equippedItem = player.getCurrentEquippedItem();
        if(!worldObj.isRemote && equippedItem != null) {
            if(equippedItem.getItem() == Itemss.GPSTool) {
                ChunkPosition gpsLoc = ItemGPSTool.getGPSLocation(equippedItem);
                if(gpsLoc != null) {
                    getNavigator().tryMoveToXYZ(gpsLoc.chunkPosX, gpsLoc.chunkPosY, gpsLoc.chunkPosZ, 0.1D);
                }
            } else {
                int dyeIndex = TileEntityPlasticMixer.getDyeIndex(equippedItem);
                if(dyeIndex >= 0) {
                    setDroneColor(ItemDye.field_150922_c[dyeIndex]);
                    equippedItem.stackSize--;
                    if(equippedItem.stackSize <= 0) {
                        player.setCurrentItemOrArmor(0, null);
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
    public boolean rotateBlock(World world, EntityPlayer player, int x, int y, int z, ForgeDirection side){
        if(!naturallySpawned) {
            if(player.capabilities.isCreativeMode) naturallySpawned = true;//don't drop the drone in creative.
            attackEntityFrom(DamageSource.outOfWorld, 2000.0F);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onDeath(DamageSource par1DamageSource){
        for(int i = 0; i < inventory.getSizeInventory(); i++) {
            if(inventory.getStackInSlot(i) != null) {
                entityDropItem(inventory.getStackInSlot(i), 0);
                inventory.setInventorySlotContents(i, null);
            }
        }
        if(naturallySpawned) {

        } else {
            ItemStack drone = getDroppedStack();
            if(hasCustomNameTag()) drone.setStackDisplayName(getCustomNameTag());

            entityDropItem(drone, 0);

            if(!worldObj.isRemote) {
                EntityPlayer owner = getOwner();
                if(owner != null) {
                    int x = (int)Math.floor(posX);
                    int y = (int)Math.floor(posY);
                    int z = (int)Math.floor(posZ);
                    if(hasCustomNameTag()) {
                        owner.addChatComponentMessage(new ChatComponentTranslation("death.drone.named", getCustomNameTag(), x, y, z));
                    } else {
                        owner.addChatComponentMessage(new ChatComponentTranslation("death.drone", x, y, z));
                    }
                }
            }
        }
        if(!worldObj.isRemote) ((FakePlayerItemInWorldManager)getFakePlayer().theItemInWorldManager).cancelDigging();
        super.onDeath(par1DamageSource);
    }

    protected ItemStack getDroppedStack(){
        NBTTagCompound tag = new NBTTagCompound();
        writeEntityToNBT(tag);
        tag.setTag("UpgradeInventory", tag.getTag("Inventory"));
        tag.removeTag("Inventory");
        ItemStack drone = new ItemStack(Itemss.drone);
        drone.setTagCompound(tag);
        return drone;
    }

    @Override
    public void setAttackTarget(EntityLivingBase entity){
        super.setAttackTarget(entity);
        if(worldObj.isRemote && targetLine != null && oldTargetLine != null) {
            targetLine.setProgress(0);
            oldTargetLine.setProgress(0);
        }
    }

    @Override
    public float getPressure(ItemStack iStack){
        return dataWatcher.getWatchableObjectFloat(12);
    }

    @Override
    public void addAir(ItemStack iStack, int amount){
        if(!worldObj.isRemote) {
            currentAir += amount;
            dataWatcher.updateObject(12, currentAir / volume);
        }
    }

    @Override
    public float maxPressure(ItemStack iStack){
        return PneumaticValues.DRONE_MAX_PRESSURE;
    }

    @Override
    public void printManometerMessage(EntityPlayer player, List<String> curInfo){
        if(hasCustomNameTag()) curInfo.add(EnumChatFormatting.AQUA + getCustomNameTag());
        curInfo.add("Owner: " + getFakePlayer().getCommandSenderName());
        curInfo.add("Current pressure: " + PneumaticCraftUtils.roundNumberTo(getPressure(null), 1) + " bar.");
        /*for(int i = 0; i < 9; i++) {
            if(upgradeInventory[i] != null) {
                player.addChatMessage("inv " + i + ": " + upgradeInventory[i].stackSize + "x " + upgradeInventory[i].getDisplayName());
            }
        }*/
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag){
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

        NBTTagCompound inv = new NBTTagCompound();
        // Write the ItemStacks in the inventory to NBT
        NBTTagList tagList = new NBTTagList();
        for(int currentIndex = 0; currentIndex < inventory.getSizeInventory(); ++currentIndex) {
            if(inventory.getStackInSlot(currentIndex) != null) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setByte("Slot", (byte)currentIndex);
                inventory.getStackInSlot(currentIndex).writeToNBT(tagCompound);
                tagList.appendTag(tagCompound);
            }
        }
        inv.setTag("Inv", tagList);

        NBTTagList upgradeList = new NBTTagList();
        for(int i = 0; i < 9; i++) {
            if(upgradeInventory[i] != null) {
                NBTTagCompound slotEntry = new NBTTagCompound();
                slotEntry.setByte("Slot", (byte)i);
                upgradeInventory[i].writeToNBT(slotEntry);
                upgradeList.appendTag(slotEntry);
            }
        }
        // save content in Inventory->Items
        inv.setTag("Items", upgradeList);
        tag.setTag("Inventory", inv);

        tank.writeToNBT(tag);

        if(handlingOffer != null) {
            NBTTagCompound subTag = new NBTTagCompound();
            subTag.setBoolean("isCustom", handlingOffer instanceof AmadronOfferCustom);
            handlingOffer.writeToNBT(subTag);
            tag.setTag("amadronOffer", subTag);
            tag.setInteger("offerTimes", offerTimes);
            if(usedTablet != null) usedTablet.writeToNBT(subTag);
            tag.setString("buyingPlayer", buyingPlayer);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag){
        super.readEntityFromNBT(tag);
        progWidgets = TileEntityProgrammer.getWidgetsFromNBT(tag);
        naturallySpawned = tag.getBoolean("naturallySpawned");
        currentAir = tag.getFloat("currentAir");
        volume = tag.getFloat("volume");
        dataWatcher.updateObject(12, currentAir / volume);
        propSpeed = tag.getFloat("propSpeed");
        disabledByHacking = tag.getBoolean("disabledByHacking");
        setGoingToOwner(tag.getBoolean("hackedByOwner"));
        setDroneColor(tag.getInteger("color"));
        aiManager.readFromNBT(tag.getCompoundTag("variables"));
        standby = tag.getBoolean("standby");

        // Read in the ItemStacks in the inventory from NBT
        NBTTagCompound inv = tag.getCompoundTag("Inventory");
        if(inv != null) {
            NBTTagList tagList = inv.getTagList("Inv", 10);

            upgradeInventory = new ItemStack[9];
            NBTTagList upgradeList = inv.getTagList("Items", 10);
            for(int i = 0; i < upgradeList.tagCount(); i++) {
                NBTTagCompound slotEntry = upgradeList.getCompoundTagAt(i);
                int j = slotEntry.getByte("Slot");

                if(j >= 0 && j < 9) {
                    upgradeInventory[j] = ItemStack.loadItemStackFromNBT(slotEntry);
                }
            }

            inventory = new InventoryDrone("Drone Inventory", true, 1 + getUpgrades(ItemMachineUpgrade.UPGRADE_DISPENSER_DAMAGE));
            for(int i = 0; i < tagList.tagCount(); ++i) {
                NBTTagCompound tagCompound = tagList.getCompoundTagAt(i);
                byte slot = tagCompound.getByte("Slot");
                if(slot >= 0 && slot < inventory.getSizeInventory()) {
                    inventory.setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(tagCompound));
                }
            }
        }

        tank.setCapacity(PneumaticValues.DRONE_TANK_SIZE * (1 + getUpgrades(ItemMachineUpgrade.UPGRADE_DISPENSER_DAMAGE)));
        tank.readFromNBT(tag);

        if(tag.hasKey("amadronOffer")) {
            NBTTagCompound subTag = tag.getCompoundTag("amadronOffer");
            if(subTag.getBoolean("isCustom")) {
                handlingOffer = AmadronOffer.loadFromNBT(subTag);
            } else {
                handlingOffer = AmadronOfferCustom.loadFromNBT(subTag);
            }
            if(subTag.hasKey("id")) {
                usedTablet = ItemStack.loadItemStackFromNBT(subTag);
            } else {
                usedTablet = null;
            }
            buyingPlayer = subTag.getString("buyingPlayer");
        } else {
            handlingOffer = null;
            usedTablet = null;
            buyingPlayer = null;
        }
        offerTimes = tag.getInteger("offerTimes");
    }

    /**
     * This and readFromNBT are _not_ being transfered from/to the Drone item.
     */
    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        if(fakePlayer != null) {
            tag.setString("owner", fakePlayer.getCommandSenderName());
            if(fakePlayer.getGameProfile().getId() != null) tag.setString("ownerUUID", fakePlayer.getGameProfile().getId().toString());
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        if(tag.hasKey("owner")) {
            playerName = tag.getString("owner");
            playerUUID = tag.hasKey("ownerUUID") ? tag.getString("ownerUUID") : null;
        }
    }

    @Override
    public int getUpgrades(int upgradeDamage){
        int upgrades = 0;
        for(ItemStack stack : upgradeInventory) {
            if(stack != null && stack.getItem() == Itemss.machineUpgrade && stack.getItemDamage() == upgradeDamage) {
                upgrades += stack.stackSize;
            }
        }
        return upgrades;
    }

    @Override
    public DroneFakePlayer getFakePlayer(){
        if(fakePlayer == null && !worldObj.isRemote) {
            initializeFakePlayer();
        }
        return fakePlayer;
    }

    public Minigun getMinigun(){
        if(minigun == null) {
            minigun = new MinigunDrone().setPlayer(getFakePlayer()).setWorld(worldObj).setPressurizable(this, PneumaticValues.DRONE_USAGE_ATTACK);
        }
        return minigun;
    }

    @Override
    public boolean attackEntityAsMob(Entity entity){
        getFakePlayer().attackTargetEntityWithCurrentItem(entity);
        addAir(null, -PneumaticValues.DRONE_USAGE_ATTACK);
        return true;
    }

    @Override
    public boolean attackEntityFrom(DamageSource damageSource, float damage){
        if(damageSource == DamageSource.inWall) {
            isSuffocating = true;
            if(suffocationCounter-- > 0 || !Config.enableDroneSuffocationDamage) {
                return false;
            }
        }
        return super.attackEntityFrom(damageSource, damage);
    }

    @Override
    public IInventory getInventory(){
        return inventory;
    }

    public double getSpeed(){
        return speed;
    }

    public int getEmittingRedstone(ForgeDirection side){
        return emittingRedstoneValues[side.ordinal()];
    }

    @Override
    public void setEmittingRedstone(ForgeDirection side, int value){
        if(emittingRedstoneValues[side.ordinal()] != value) {
            emittingRedstoneValues[side.ordinal()] = value;
            worldObj.notifyBlocksOfNeighborChange((int)Math.floor(posX + width / 2), (int)Math.floor(posY), (int)Math.floor(posZ + width / 2), Blockss.droneRedstoneEmitter);
        }
    }

    @Override
    public boolean isBlockValidPathfindBlock(int x, int y, int z){
        if(worldObj.isAirBlock(x, y, z)) return true;
        Block block = worldObj.getBlock(x, y, z);
        if(PneumaticCraftUtils.isBlockLiquid(block)) {
            return hasLiquidImmunity;
        }
        if(block.getBlocksMovement(worldObj, x, y, z) && block != Blocks.ladder) return true;
        if(PneumaticCraftAPIHandler.getInstance().pathfindableBlocks.containsKey(block)) {
            IPathfindHandler pathfindHandler = PneumaticCraftAPIHandler.getInstance().pathfindableBlocks.get(block);
            return pathfindHandler == null || pathfindHandler.canPathfindThrough(worldObj, x, y, z);
        } else {
            return false;
        }
    }

    @Override
    public void sendWireframeToClient(int x, int y, int z){
        NetworkHandler.sendToAllAround(new PacketShowWireframe(this, x, y, z), worldObj);
    }

    private class InventoryDrone extends InventoryBasic{
        ItemStack oldStack;

        public InventoryDrone(String inventoryName, boolean isNameLocalized, int slots){
            super(inventoryName, isNameLocalized, slots);
        }

        @Override
        public void setInventorySlotContents(int slot, ItemStack stack){
            super.setInventorySlotContents(slot, stack);
            if(slot == 0 && !isChangingCurrentStack) {
                isChangingCurrentStack = true;
                getFakePlayer().inventory.setInventorySlotContents(slot, stack);
                isChangingCurrentStack = false;
                if(oldStack != null) {
                    getFakePlayer().getAttributeMap().removeAttributeModifiers(oldStack.getAttributeModifiers());
                }

                if(stack != null) {
                    getFakePlayer().getAttributeMap().applyAttributeModifiers(stack.getAttributeModifiers());
                }
                oldStack = stack;
            }
        }
    }

    private class InventoryFakePlayer extends InventoryPlayer{
        public InventoryFakePlayer(EntityPlayer par1EntityPlayer){
            super(par1EntityPlayer);
        }

        @Override
        public void setInventorySlotContents(int slot, ItemStack stack){
            super.setInventorySlotContents(slot, stack);
            if(slot == 0 && !isChangingCurrentStack) {
                isChangingCurrentStack = true;
                getInventory().setInventorySlotContents(slot, stack);
                isChangingCurrentStack = false;
            }
        }
    }

    public static class DroneFakePlayer extends EntityPlayerMP{
        private final IDroneBase drone;
        private boolean sneaking;

        public DroneFakePlayer(WorldServer world, GameProfile name, ItemInWorldManager itemManager, IDroneBase drone){
            super(FMLCommonHandler.instance().getMinecraftServerInstance(), world, name, itemManager);
            this.drone = drone;
        }

        @Override
        public void addExperience(int amount){
            Vec3 pos = drone.getPosition();
            EntityXPOrb orb = new EntityXPOrb(drone.getWorld(), pos.xCoord, pos.yCoord, pos.zCoord, amount);
            drone.getWorld().spawnEntityInWorld(orb);
        }

        @Override
        public void setCurrentItemOrArmor(int p_70062_1_, ItemStack p_70062_2_){

            if(p_70062_1_ == 0) {
                inventory.setInventorySlotContents(inventory.currentItem, p_70062_2_);
            } else {
                inventory.armorInventory[p_70062_1_ - 1] = p_70062_2_;
            }
        }

        @Override
        public boolean canCommandSenderUseCommand(int i, String s){
            return false;
        }

        @Override
        public ChunkCoordinates getPlayerCoordinates(){
            return new ChunkCoordinates(0, 0, 0);
        }

        @Override
        public void addChatComponentMessage(IChatComponent chatmessagecomponent){}

        @Override
        public void addStat(StatBase par1StatBase, int par2){}

        @Override
        public void openGui(Object mod, int modGuiId, World world, int x, int y, int z){}

        @Override
        public boolean isEntityInvulnerable(){
            return true;
        }

        @Override
        public boolean canAttackPlayer(EntityPlayer player){
            return false;
        }

        @Override
        public void onDeath(DamageSource source){
            return;
        }

        @Override
        public void onUpdate(){
            return;
        }

        @Override
        public void travelToDimension(int dim){
            return;
        }

        @Override
        public void func_147100_a(C15PacketClientSettings pkt){
            return;
        }

        @Override
        public boolean isSneaking(){
            return sneaking;
        }

        @Override
        public void setSneaking(boolean sneaking){
            this.sneaking = sneaking;
        }
    }

    /**
     * IHackableEntity
     */

    @Override
    public String getId(){
        return null;
    }

    @Override
    public boolean canHack(Entity entity, EntityPlayer player){
        return isAccelerating();
    }

    @Override
    public void addInfo(Entity entity, List<String> curInfo, EntityPlayer player){
        if(playerName.equals(player.getCommandSenderName())) {
            if(isGoingToOwner()) {
                curInfo.add("pneumaticHelmet.hacking.result.resumeTasks");
            } else {
                curInfo.add("pneumaticHelmet.hacking.result.callBack");
            }
        } else {
            curInfo.add("pneumaticHelmet.hacking.result.disable");
        }
    }

    @Override
    public void addPostHackInfo(Entity entity, List<String> curInfo, EntityPlayer player){
        if(playerName.equals(player.getCommandSenderName())) {
            if(isGoingToOwner()) {
                curInfo.add("pneumaticHelmet.hacking.finished.calledBack");
            } else {
                curInfo.add("pneumaticHelmet.hacking.finished.resumedTasks");
            }
        } else {
            curInfo.add("pneumaticHelmet.hacking.finished.disabled");
        }
    }

    @Override
    public int getHackTime(Entity entity, EntityPlayer player){
        return playerName.equals(player.getCommandSenderName()) ? 20 : 100;
    }

    @Override
    public void onHackFinished(Entity entity, EntityPlayer player){
        if(!worldObj.isRemote && player.getGameProfile().equals(getFakePlayer().getGameProfile())) {
            setGoingToOwner(gotoOwnerAI == null);//toggle the state
        } else {
            disabledByHacking = true;
        }
    }

    @Override
    public boolean afterHackTick(Entity entity){
        return false;
    }

    private void setGoingToOwner(boolean state){
        if(state && gotoOwnerAI == null) {
            gotoOwnerAI = new DroneGoToOwner(this);
            tasks.addTask(2, gotoOwnerAI);
            dataWatcher.updateObject(21, (byte)1);
            setActiveProgram(new ProgWidgetGoToLocation());
        } else if(!state && gotoOwnerAI != null) {
            tasks.removeTask(gotoOwnerAI);
            gotoOwnerAI = null;
            dataWatcher.updateObject(21, (byte)0);
        }
    }

    private boolean isGoingToOwner(){
        return dataWatcher.getWatchableObjectByte(21) == (byte)1;
    }

    @Override
    public IFluidTank getTank(){
        return tank;
    }

    /**
     * Returns the owning player. Returns null when the player is not online.
     * @return
     */
    public EntityPlayer getOwner(){
        return MinecraftServer.getServer().getConfigurationManager().func_152612_a(playerName);
    }

    public void setStandby(boolean standby){
        this.standby = standby;
    }

    @Override
    public World getWorld(){
        return worldObj;
    }

    @Override
    public Vec3 getPosition(){
        return Vec3.createVectorHelper(posX, posY, posZ);
    }

    @Override
    public void dropItem(ItemStack stack){
        entityDropItem(stack, 0);
    }

    @Override
    public List<IProgWidget> getProgWidgets(){
        return progWidgets;
    }

    @Override
    public EntityAITasks getTargetAI(){
        return targetTasks;
    }

    @Override
    public boolean isProgramApplicable(IProgWidget widget){
        return true;
    }

    @Override
    public IExtendedEntityProperties getProperty(String key){
        return getExtendedProperties(key);
    }

    @Override
    public void setProperty(String key, IExtendedEntityProperties property){
        registerExtendedProperties(key, property);
    }

    @Override
    public void setName(String string){
        setCustomNameTag(string);
    }

    @Override
    public void setCarryingEntity(Entity entity){
        if(entity == null) {
            if(getCarryingEntity() != null) getCarryingEntity().mountEntity(null);
        } else {
            entity.mountEntity(this);
        }
    }

    @Override
    public Entity getCarryingEntity(){
        return riddenByEntity;
    }

    @Override
    public boolean isAIOverriden(){
        return chargeAI.isExecuting || gotoOwnerAI != null;
    }

    @Override
    public void onItemPickupEvent(EntityItem curPickingUpEntity, int stackSize){
        onItemPickup(curPickingUpEntity, stackSize);
    }

    @Override
    public IPathNavigator getPathNavigator(){
        return (IPathNavigator)getNavigator();
    }

    public void tryFireMinigun(EntityLivingBase target){
        ItemStack ammo = getAmmo();
        if(getMinigun().setAmmo(ammo).tryFireMinigun(target)) {
            for(int i = 0; i < getInventory().getSizeInventory(); i++) {
                if(getInventory().getStackInSlot(i) == ammo) {
                    getInventory().setInventorySlotContents(i, null);
                }
            }
        }
    }

    public ItemStack getAmmo(){
        for(int i = 0; i < getInventory().getSizeInventory(); i++) {
            ItemStack stack = getInventory().getStackInSlot(i);
            if(stack != null && stack.getItem() == Itemss.gunAmmo) {
                return stack;
            }
        }
        return null;
    }

    public void setHandlingOffer(AmadronOffer offer, int times, ItemStack usedTablet, String buyingPlayer){
        handlingOffer = offer;
        offerTimes = times;
        this.usedTablet = usedTablet != null ? usedTablet.copy() : null;
        this.buyingPlayer = buyingPlayer;
    }

    public AmadronOffer getHandlingOffer(){
        return handlingOffer;
    }

    public int getOfferTimes(){
        return offerTimes;
    }

    public ItemStack getUsedTablet(){
        return usedTablet;
    }

    public String getBuyingPlayer(){
        return buyingPlayer;
    }

    @Override
    public void overload(){
        attackEntityFrom(DamageSource.outOfWorld, 2000.0F);
    }

    @Override
    public DroneAIManager getAIManager(){
        return aiManager;
    }

    @Override
    public void updateLabel(){
        dataWatcher.updateObject(26, getAIManager() != null ? getAIManager().getLabel() : "Main");
    }

    public String getLabel(){
        return dataWatcher.getWatchableObjectString(26);
    }

    public SortedSet<DebugEntry> getDebugEntries(){
        return debugEntries;
    }

    @Override
    public void addDebugEntry(String message){
        addDebugEntry(message, null);
    }

    @Override
    public void addDebugEntry(String message, ChunkPosition pos){

        DebugEntry entry = new DebugEntry(message, getActiveWidgetIndex(), pos);
        addDebugEntry(entry);

        PacketSendDroneDebugEntry packet = new PacketSendDroneDebugEntry(entry, this);
        for(EntityPlayerMP player : syncedPlayers) {
            NetworkHandler.sendTo(packet, player);
        }
    }

    public void addDebugEntry(DebugEntry entry){
        if(!debugEntries.isEmpty()) {
            DebugEntry previous = debugEntries.last();
            if(previous.getProgWidgetId() != entry.getProgWidgetId()) {//When we've jumped to another piece
                Iterator<DebugEntry> iterator = debugEntries.iterator();
                while(iterator.hasNext()) {
                    if(iterator.next().getProgWidgetId() == entry.getProgWidgetId()) {
                        iterator.remove(); //Remove the data from last cycle.
                    }
                }
            }
        }
        debugEntries.add(entry);
    }

    public void trackAsDebugged(EntityPlayerMP player){
        NetworkHandler.sendTo(new PacketSyncDroneEntityProgWidgets(this), player);

        for(DebugEntry entry : debugEntries) {
            NetworkHandler.sendTo(new PacketSendDroneDebugEntry(entry, this), player);
        }

        syncedPlayers.add(player);
    }

    public void updateSyncedPlayers(){
        Iterator<EntityPlayerMP> iterator = syncedPlayers.iterator();
        while(iterator.hasNext()) {
            EntityPlayerMP player = iterator.next();
            if(player.isDead || player.getCurrentArmor(3) == null || NBTUtil.getInteger(player.getCurrentArmor(3), NBTKeys.PNEUMATIC_HELMET_DEBUGGING_DRONE) != getEntityId()) {
                iterator.remove();
            }
        }
    }

    private class MinigunDrone extends Minigun{

        public MinigunDrone(){
            super(true);
        }

        @Override
        public boolean isMinigunActivated(){
            return EntityDrone.this.isMinigunActivated();
        }

        @Override
        public void setMinigunActivated(boolean activated){
            EntityDrone.this.setMinigunActivated(activated);
        }

        @Override
        public void setAmmoColorStack(ItemStack ammo){
            setAmmoColor(ammo);
        }

        @Override
        public int getAmmoColor(){
            return EntityDrone.this.getAmmoColor();
        }

        @Override
        public void playSound(String soundName, float volume, float pitch){
            worldObj.playSoundAtEntity(EntityDrone.this, soundName, volume, pitch);
        }

    }
}
