package pneumaticCraft.common.tileentity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import pneumaticCraft.api.recipe.IPressureChamberRecipe;
import pneumaticCraft.api.recipe.PressureChamberRecipe;
import pneumaticCraft.api.tileentity.IAirHandler;
import pneumaticCraft.common.AchievementHandler;
import pneumaticCraft.common.DamageSourcePneumaticCraft;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.config.Config;
import pneumaticCraft.common.fluid.Fluids;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.DescSynced;
import pneumaticCraft.common.network.GuiSynced;
import pneumaticCraft.common.recipes.PneumaticRecipeRegistry;
import pneumaticCraft.lib.PneumaticValues;

public class TileEntityPressureChamberValve extends TileEntityPneumaticBase implements IInventory, IMinWorkingPressure{
    @DescSynced
    public int multiBlockX;
    @DescSynced
    public int multiBlockY;
    @DescSynced
    public int multiBlockZ;
    @DescSynced
    public int multiBlockSize;
    public List<TileEntityPressureChamberValve> accessoryValves;
    private final List<int[]> nbtValveList;
    private boolean readNBT = false;
    @GuiSynced
    public boolean isValidRecipeInChamber;
    @GuiSynced
    public boolean isSufficientPressureInChamber;
    @GuiSynced
    public boolean areEntitiesDoneMoving;
    @GuiSynced
    public float recipePressure;
    private final Item etchingAcid = Fluids.getBucket(Fluids.etchingAcid);

    private ItemStack[] inventory = new ItemStack[4];
    public static final int UPGRADE_SLOT_1 = 0;
    public static final int UPGRADE_SLOT_4 = 3;
    private final Random rand = new Random();

    // private float oldPressure;
    // private int pressureUpdateTimer = 60;

    public TileEntityPressureChamberValve(){
        super(PneumaticValues.DANGER_PRESSURE_PRESSURE_CHAMBER, PneumaticValues.MAX_PRESSURE_PRESSURE_CHAMBER, PneumaticValues.VOLUME_PRESSURE_CHAMBER);
        accessoryValves = new ArrayList<TileEntityPressureChamberValve>();
        nbtValveList = new ArrayList<int[]>();
        setUpgradeSlots(new int[]{UPGRADE_SLOT_1, 1, 2, UPGRADE_SLOT_4});
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate(){
        return true;
    }

    // pneumatic methods-------------------------------------------------------
    // used in the air dispersion methods.
    @Override
    public boolean isConnectedTo(ForgeDirection side){
        switch(ForgeDirection.getOrientation(getBlockMetadata())){
            case UP:
            case DOWN:
                return side == ForgeDirection.UP || side == ForgeDirection.DOWN;
            case NORTH:
            case SOUTH:
                return side == ForgeDirection.NORTH || side == ForgeDirection.SOUTH;
            case EAST:
            case WEST:
                return side == ForgeDirection.EAST || side == ForgeDirection.WEST;
        }
        return false;
    }

    @Override
    public List<Pair<ForgeDirection, IAirHandler>> getConnectedPneumatics(){
        List<Pair<ForgeDirection, IAirHandler>> teList = super.getConnectedPneumatics();
        if(accessoryValves != null) {
            for(TileEntityPressureChamberValve valve : accessoryValves) {
                if(valve != this) teList.add(new ImmutablePair(ForgeDirection.UNKNOWN, valve.getAirHandler()));
            }
        }

        return teList;
    }

    @Override
    protected int getVolumeFromUpgrades(int[] upgradeSlots){
        return super.getVolumeFromUpgrades(getUpgradeSlots()) + (multiBlockSize > 0 ? (int)Math.pow(multiBlockSize - 2, 3) * PneumaticValues.VOLUME_PRESSURE_CHAMBER_PER_EMPTY : 0);
    }

    // main update method
    @Override
    public void updateEntity(){
        if(readNBT) {// this way of doing this is needed because other TE's are
                     // loaded after the reading of the NBT of this TE.
            readNBT = false;
            accessoryValves.clear();
            for(int[] valve : nbtValveList) {
                TileEntity te = worldObj.getTileEntity(valve[0], valve[1], valve[2]);
                if(te instanceof TileEntityPressureChamberValve) {

                    accessoryValves.add((TileEntityPressureChamberValve)te);
                }
            }
            if(accessoryValves.isEmpty()) {//Hacky solution for an unexplainable bug.
                invalidateMultiBlock();
                checkIfProperlyFormed(worldObj, xCoord, yCoord, zCoord);
            }
            if(worldObj.isRemote) worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
        }

        if(!worldObj.isRemote) {
            //code to check if we need to leak air.
            boolean[] connected = new boolean[]{true, true, true, true, true, true};//assume we are not leaking at any side
            switch(ForgeDirection.getOrientation(getBlockMetadata())){//take off the sides that tubes can connect to.
                case UP:
                case DOWN:
                    connected[ForgeDirection.UP.ordinal()] = false;
                    connected[ForgeDirection.DOWN.ordinal()] = false;
                    break;
                case NORTH:
                case SOUTH:
                    connected[ForgeDirection.NORTH.ordinal()] = false;
                    connected[ForgeDirection.SOUTH.ordinal()] = false;
                    break;
                case EAST:
                case WEST:
                    connected[ForgeDirection.EAST.ordinal()] = false;
                    connected[ForgeDirection.WEST.ordinal()] = false;
                    break;
            }
            List<Pair<ForgeDirection, IAirHandler>> teList = super.getConnectedPneumatics();//we need the super method, as the overridden method adds the other valves.
            for(Pair<ForgeDirection, IAirHandler> entry : teList) {
                connected[entry.getKey().ordinal()] = true;
            }
            //retrieve the valve that is controlling the (potential) chamber.
            TileEntityPressureChamberValve baseValve = null;
            for(TileEntityPressureChamberValve valve : accessoryValves) {
                if(valve.multiBlockSize > 0) {
                    baseValve = valve;
                    break;
                }
            }
            //if we found one, we can scratch one side to be leaking air.
            if(baseValve != null) {
                switch(ForgeDirection.getOrientation(getBlockMetadata())){
                    case UP:
                    case DOWN:
                        if(baseValve.multiBlockY == yCoord) connected[ForgeDirection.UP.ordinal()] = true;
                        else connected[ForgeDirection.DOWN.ordinal()] = true;
                        break;
                    case NORTH:
                    case SOUTH:
                        if(baseValve.multiBlockZ == zCoord) connected[ForgeDirection.SOUTH.ordinal()] = true;
                        else connected[ForgeDirection.NORTH.ordinal()] = true;
                        break;
                    case EAST:
                    case WEST:
                        if(baseValve.multiBlockX == xCoord) connected[ForgeDirection.EAST.ordinal()] = true;
                        else connected[ForgeDirection.WEST.ordinal()] = true;
                        break;
                }
            }
            for(int i = 0; i < 6; i++)
                if(!connected[i]) airLeak(ForgeDirection.getOrientation(i));
        }
        super.updateEntity();

        /*
         * FIXME pressure chamber particle effects not updating properly
         * if(!worldObj.isRemote){ pressureUpdateTimer++;
         * System.out.println("timer: " + pressureUpdateTimer);
         * if(pressureUpdateTimer > 60){ pressureUpdateTimer = 0;
         * updatePressuresToClient(); } }
         */

        if(multiBlockSize != 0 && !worldObj.isRemote) {
            ItemStack[] stacksInChamber = getStacksInChamber();
            isValidRecipeInChamber = false;
            isSufficientPressureInChamber = false;
            recipePressure = Float.MAX_VALUE;
            //simple recipes
            for(PressureChamberRecipe recipe : PressureChamberRecipe.chamberRecipes) {
                boolean isValidRecipeInChamberFlag = canBeCompressed(recipe, stacksInChamber);
                boolean isSufficientPressureInChamberFlag = recipe.pressure <= getPressure(ForgeDirection.UNKNOWN) && recipe.pressure > 0F || recipe.pressure >= getPressure(ForgeDirection.UNKNOWN) && recipe.pressure < 0F;
                if(isValidRecipeInChamberFlag) {
                    isValidRecipeInChamber = true;
                    if(Math.abs(recipe.pressure) < Math.abs(recipePressure)) {
                        recipePressure = recipe.pressure;
                    }
                }
                if(isSufficientPressureInChamberFlag) isSufficientPressureInChamber = true;
                if(isValidRecipeInChamberFlag && isSufficientPressureInChamberFlag && areEntitiesDoneMoving) {
                    double[] outputPosition = clearStacksInChamber(recipe.input);
                    giveOutput(recipe.output, outputPosition);
                }
            }
            //special recipes
            for(IPressureChamberRecipe recipe : PressureChamberRecipe.specialRecipes) {
                ItemStack[] removedStacks = recipe.isValidRecipe(stacksInChamber);
                boolean isValidRecipeInChamberFlag = removedStacks != null;
                boolean isSufficientPressureInChamberFlag = recipe.getCraftingPressure() <= getPressure(ForgeDirection.UNKNOWN) && recipe.getCraftingPressure() > 0F || recipe.getCraftingPressure() >= getPressure(ForgeDirection.UNKNOWN) && recipe.getCraftingPressure() < 0F;
                if(isValidRecipeInChamberFlag) {
                    isValidRecipeInChamber = true;
                    if(Math.abs(recipe.getCraftingPressure()) < Math.abs(recipePressure)) {
                        recipePressure = recipe.getCraftingPressure();
                    }
                }
                if(isSufficientPressureInChamberFlag) isSufficientPressureInChamber = true;
                if(isValidRecipeInChamberFlag && isSufficientPressureInChamberFlag && areEntitiesDoneMoving) {
                    double[] outputPosition = clearStacksInChamber(removedStacks);
                    giveOutput(recipe.craftRecipe(stacksInChamber, removedStacks), outputPosition);
                }
            }

            if(getPressure(ForgeDirection.UNKNOWN) > PneumaticValues.MAX_PRESSURE_LIVING_ENTITY) {
                AxisAlignedBB bbBox = AxisAlignedBB.getBoundingBox(multiBlockX + 1, multiBlockY + 1, multiBlockZ + 1, multiBlockX + multiBlockSize - 1, multiBlockY + multiBlockSize - 1, multiBlockZ + multiBlockSize - 1);
                List<EntityLivingBase> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, bbBox);
                for(EntityLivingBase entity : entities) {
                    if(entity instanceof EntityVillager) {
                        EntityVillager villager = (EntityVillager)entity;
                        if(villager.getProfession() != Config.villagerMechanicID) {
                            villager.setProfession(Config.villagerMechanicID);
                            NBTTagCompound tag = new NBTTagCompound();
                            villager.writeEntityToNBT(tag);
                            if(tag.hasKey("Offers")) {//reset the trade list
                                tag.removeTag("Offers");
                                villager.readEntityFromNBT(tag);
                            }
                        }
                    }
                    entity.attackEntityFrom(DamageSourcePneumaticCraft.pressure, (int)(getPressure(ForgeDirection.UNKNOWN) * 2D));
                }
            }
        }

        // move entities to eachother.
        AxisAlignedBB bbBox = AxisAlignedBB.getBoundingBox(multiBlockX, multiBlockY, multiBlockZ, multiBlockX + multiBlockSize, multiBlockY + multiBlockSize, multiBlockZ + multiBlockSize);
        List<EntityItem> entities = worldObj.getEntitiesWithinAABB(EntityItem.class, bbBox);
        areEntitiesDoneMoving = true; // set to true, set to false when one of
                                      // the entities is moving.
        for(int i = 0; i < entities.size() - 1; i++) {
            EntityItem lastEntity = entities.get(i);
            EntityItem entity = entities.get(i + 1);
            // XP Orb code snippet
            double d0 = 8.0D;
            double d1 = (lastEntity.posX - entity.posX) / d0;
            double d3 = (lastEntity.posZ - entity.posZ) / d0;
            double d4 = Math.sqrt(d1 * d1 + d3 * d3);
            double d5 = 1.0D - d4;

            if(d5 > 0.0D && d4 > 0.02D) {
                d5 *= d5;
                entity.motionX += d1 / d4 * d5 * 0.01D;
                entity.motionZ += d3 / d4 * d5 * 0.01D;
                lastEntity.motionX -= d1 / d4 * d5 * 0.01D;
                lastEntity.motionZ -= d3 / d4 * d5 * 0.01D;
                areEntitiesDoneMoving = false;
            }
        }

        boolean lifeUpgrade = getUpgrades(ItemMachineUpgrade.UPGRADE_ITEM_LIFE, getUpgradeSlots()) > 0;
        if(lifeUpgrade && !worldObj.isRemote) {
            for(EntityItem entity : entities) {
                entity.age--;
            }
        }

        // particles
        if(worldObj.isRemote && getPressure(ForgeDirection.UNKNOWN) > 0.2D) {
            int particles = (int)Math.pow(multiBlockSize - 2, 3);
            for(int i = 0; i < particles; i++) {
                if(rand.nextInt(Math.max(1, 8 - (int)(getPressure(ForgeDirection.UNKNOWN) * 2D))) == 0) {
                    double posX = multiBlockX + 1D + rand.nextDouble() * (multiBlockSize - 2D);
                    double posY = multiBlockY + 1D + rand.nextDouble() * (multiBlockSize - 2D);
                    double posZ = multiBlockZ + 1D + rand.nextDouble() * (multiBlockSize - 2D);
                    worldObj.spawnParticle("explode", posX, posY, posZ, 0D, 0D, 0D);
                }
            }
        }

    }

    private boolean canBeCompressed(PressureChamberRecipe recipe, ItemStack[] items){
        for(Object in : recipe.input) {
            if(in != null) {
                int amount = 0;
                for(ItemStack item : items) {
                    if(item != null && PneumaticRecipeRegistry.isItemEqual(in, item)) amount += item.stackSize;
                }
                if(amount < PneumaticRecipeRegistry.getItemAmount(in)) return false;
            }
        }
        return true;
    }

    public ItemStack[] getStacksInChamber(){
        List<ItemStack> stackList = new ArrayList<ItemStack>(); // first make a
                                                                // list, convert
                                                                // it to an
                                                                // array
                                                                // afterwards.
        /*for(int i = 0; i < multiBlockSize - 2; i++) {
            for(int j = 0; j < multiBlockSize - 2; j++) {
                for(int k = 0; k < multiBlockSize - 2; k++) {
                    Block blockID = worldObj.getBlock(i + multiBlockX + 1, j + multiBlockY + 1, k + multiBlockZ + 1);
                    if(!blockID.isAir(worldObj, i + multiBlockX + 1, j + multiBlockY + 1, k + multiBlockZ + 1)) {
                        ItemStack stack = new ItemStack(blockID, 1, worldObj.getBlockMetadata(i + multiBlockX + 1, j + multiBlockY + 1, k + multiBlockZ + 1));
                        if(stack.getItem() != null) addStackToList(stackList, stack);//getItem() shouldn't ever be able to be null, but still some defence cuz https://github.com/MineMaarten/PneumaticCraft-API/issues/60
                    }
                }
            }
        }*/

        // add Item entities lying on the ground.
        AxisAlignedBB bbBox = AxisAlignedBB.getBoundingBox(multiBlockX, multiBlockY, multiBlockZ, multiBlockX + multiBlockSize, multiBlockY + multiBlockSize, multiBlockZ + multiBlockSize);
        List<EntityItem> entities = worldObj.getEntitiesWithinAABB(EntityItem.class, bbBox);
        for(EntityItem entity : entities) {
            if(entity.isDead) continue;
            stackList.add(entity.getEntityItem());
        }

        return stackList.toArray(new ItemStack[stackList.size()]); // return an
                                                                   // into an
                                                                   // array
                                                                   // converted
                                                                   // list
    }

    public double[] clearStacksInChamber(Object... stacksToClear){
        int[] stackSizes = new int[stacksToClear.length];
        for(int i = 0; i < stacksToClear.length; i++) {
            stackSizes[i] = PneumaticRecipeRegistry.getItemAmount(stacksToClear[i]);
        }
        // default the output position to the middle of the chamber.
        double[] outputPosition = new double[]{multiBlockX + multiBlockSize / 2D, multiBlockY + 1.2D, multiBlockZ + multiBlockSize / 2D};

        // get the in world EntityItems
        AxisAlignedBB bbBox = AxisAlignedBB.getBoundingBox(multiBlockX, multiBlockY, multiBlockZ, multiBlockX + multiBlockSize, multiBlockY + multiBlockSize, multiBlockZ + multiBlockSize);
        List<EntityItem> entities = worldObj.getEntitiesWithinAABB(EntityItem.class, bbBox);
        for(EntityItem entity : entities) {
            if(entity.isDead) continue;
            ItemStack entityStack = entity.getEntityItem();
            for(int l = 0; l < stacksToClear.length; l++) {
                if(PneumaticRecipeRegistry.isItemEqual(stacksToClear[l], entityStack) && stackSizes[l] > 0) {
                    outputPosition[0] = entity.posX;
                    outputPosition[1] = entity.posY;
                    outputPosition[2] = entity.posZ;
                    int removedItems = Math.min(stackSizes[l], entityStack.stackSize);
                    stackSizes[l] -= removedItems;
                    entityStack.stackSize -= removedItems;
                    if(entityStack.stackSize <= 0) entity.setDead();
                    break;
                }
            }
        }
        return outputPosition;
    }

    private void giveOutput(ItemStack[] output, double[] outputPosition){
        for(ItemStack iStack : output) {
            if(iStack.getItem() == etchingAcid) {
                for(EntityPlayer player : (List<EntityPlayer>)worldObj.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(xCoord - 32, yCoord - 32, zCoord - 32, xCoord + 32, yCoord + 32, zCoord + 32))) {
                    AchievementHandler.giveAchievement(player, new ItemStack(etchingAcid));
                }
            }
            EntityItem item = new EntityItem(worldObj, outputPosition[0], outputPosition[1], outputPosition[2], iStack.copy());
            worldObj.spawnEntityInWorld(item);
        }
    }

    // NBT methods-----------------------------------------------
    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        multiBlockX = tag.getInteger("multiBlockX");
        multiBlockY = tag.getInteger("multiBlockY");
        multiBlockZ = tag.getInteger("multiBlockZ");
        multiBlockSize = tag.getInteger("multiBlockSize");
        isSufficientPressureInChamber = tag.getBoolean("sufPressure");
        isValidRecipeInChamber = tag.getBoolean("validRecipe");
        recipePressure = tag.getFloat("recipePressure");
        // Read in the ItemStacks in the inventory from NBT
        NBTTagList tagList = tag.getTagList("Items", 10);
        inventory = new ItemStack[getSizeInventory()];
        for(int i = 0; i < tagList.tagCount(); ++i) {
            NBTTagCompound tagCompound = tagList.getCompoundTagAt(i);
            byte slot = tagCompound.getByte("Slot");
            if(slot >= 0 && slot < inventory.length) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(tagCompound);
            }
        }

        // Read in the accessory valves from NBT
        NBTTagList tagList2 = tag.getTagList("Valves", 10);
        nbtValveList.clear();
        for(int i = 0; i < tagList2.tagCount(); ++i) {
            NBTTagCompound tagCompound = tagList2.getCompoundTagAt(i);
            if(tagCompound != null) {
                nbtValveList.add(new int[]{tagCompound.getInteger("xCoord"), tagCompound.getInteger("yCoord"), tagCompound.getInteger("zCoord")});
            }
        }
        readNBT = true;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setInteger("multiBlockX", multiBlockX);
        tag.setInteger("multiBlockY", multiBlockY);
        tag.setInteger("multiBlockZ", multiBlockZ);
        tag.setInteger("multiBlockSize", multiBlockSize);
        tag.setBoolean("sufPressure", isSufficientPressureInChamber);
        tag.setBoolean("validRecipe", isValidRecipeInChamber);
        tag.setFloat("recipePressure", recipePressure);
        // Write the ItemStacks in the inventory to NBT
        NBTTagList tagList = new NBTTagList();
        for(int currentIndex = 0; currentIndex < inventory.length; ++currentIndex) {
            if(inventory[currentIndex] != null) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setByte("Slot", (byte)currentIndex);
                inventory[currentIndex].writeToNBT(tagCompound);
                tagList.appendTag(tagCompound);
            }
        }
        tag.setTag("Items", tagList);

        // Write the accessory valve to NBT
        NBTTagList tagList2 = new NBTTagList();
        for(TileEntityPressureChamberValve valve : accessoryValves) {
            NBTTagCompound tagCompound = new NBTTagCompound();
            tagCompound.setInteger("xCoord", valve.xCoord);
            tagCompound.setInteger("yCoord", valve.yCoord);
            tagCompound.setInteger("zCoord", valve.zCoord);
            tagList2.appendTag(tagCompound);
        }

        tag.setTag("Valves", tagList2);
    }

    // Multi-block structure methods
    // --------------------------------------------------------

    public void onMultiBlockBreak(){
        dropInventory(worldObj, multiBlockX + multiBlockSize / 2D, multiBlockY + multiBlockSize / 2D, multiBlockZ + multiBlockSize / 2D);
        invalidateMultiBlock();
    }

    private void dropInventory(World world, double x, double y, double z){

        IInventory inventory = this;
        Random rand = new Random();
        for(int i = 0; i < inventory.getSizeInventory(); i++) {

            ItemStack itemStack = inventory.getStackInSlot(i);

            if(itemStack != null && itemStack.stackSize > 0) {
                float dX = rand.nextFloat() * 0.8F - 0.4F;
                float dY = rand.nextFloat() * 0.8F - 0.4F;
                float dZ = rand.nextFloat() * 0.8F - 0.4F;

                EntityItem entityItem = new EntityItem(world, x + dX, y + dY, z + dZ, new ItemStack(itemStack.getItem(), itemStack.stackSize, itemStack.getItemDamage()));

                if(itemStack.hasTagCompound()) {
                    entityItem.getEntityItem().setTagCompound((NBTTagCompound)itemStack.getTagCompound().copy());
                }

                float factor = 0.05F;
                entityItem.motionX = rand.nextGaussian() * factor;
                entityItem.motionY = rand.nextGaussian() * factor + 0.2F;
                entityItem.motionZ = rand.nextGaussian() * factor;
                world.spawnEntityInWorld(entityItem);
                // itemStack.stackSize = 0;
            }
        }
        this.inventory = new ItemStack[4];
    }

    private void invalidateMultiBlock(){
        for(int x = 0; x < multiBlockSize; x++) {
            for(int y = 0; y < multiBlockSize; y++) {
                for(int z = 0; z < multiBlockSize; z++) {
                    TileEntity te = worldObj.getTileEntity(x + multiBlockX, y + multiBlockY, z + multiBlockZ);
                    if(te instanceof TileEntityPressureChamberWall) {
                        TileEntityPressureChamberWall teWall = (TileEntityPressureChamberWall)te;
                        teWall.setCore(null); // Clear the base TE's, so that
                                              // the walls can be used in a new
                                              // MultiBlock
                    }
                }
            }
        }
        if(accessoryValves != null) {
            for(TileEntityPressureChamberValve valve : accessoryValves) {
                valve.setMultiBlockCoords(0, 0, 0, 0);
                if(valve != this) {
                    valve.accessoryValves.clear();
                    valve.sendDescriptionPacket();
                }
            }
            accessoryValves.clear();
        }
        sendDescriptionPacket();
    }

    public void setMultiBlockCoords(int size, int baseX, int baseY, int baseZ){
        multiBlockSize = size;
        multiBlockX = baseX;
        multiBlockY = baseY;
        multiBlockZ = baseZ;
    }

    public static boolean checkIfProperlyFormed(World world, int x, int y, int z){
        // ArrayList<int[]> blockList = getAccessoryTiles(new
        // ArrayList<int[]>(), world, x,y,z);

        for(int i = 3; i < 6; i++) {
            if(checkForShiftedCubeOfSize(i, world, x, y, z)) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkForShiftedCubeOfSize(int size, World world, int baseX, int baseY, int baseZ){
        for(int wallX = 0; wallX < size; wallX++) {
            for(int wallY = 0; wallY < size; wallY++) {
                // check every possible configuration the block can be in.
                if(checkForCubeOfSize(size, world, baseX, baseY - wallY, baseZ - wallX)) return true;
                if(checkForCubeOfSize(size, world, baseX, baseY + wallY, baseZ + wallX)) return true;
                if(checkForCubeOfSize(size, world, baseX - wallX, baseY - wallY, baseZ)) return true;
                if(checkForCubeOfSize(size, world, baseX + wallX, baseY + wallY, baseZ)) return true;
                if(checkForCubeOfSize(size, world, baseX - wallX, baseY, baseZ - wallY)) return true;
                if(checkForCubeOfSize(size, world, baseX + wallX, baseY, baseZ + wallY)) return true;

                if(checkForCubeOfSize(size, world, baseX - size + 1, baseY - wallY, baseZ - wallX)) return true;
                if(checkForCubeOfSize(size, world, baseX - size + 1, baseY + wallY, baseZ + wallX)) return true;
                if(checkForCubeOfSize(size, world, baseX - wallX, baseY - wallY, baseZ - size + 1)) return true;
                if(checkForCubeOfSize(size, world, baseX + wallX, baseY + wallY, baseZ - size + 1)) return true;
                if(checkForCubeOfSize(size, world, baseX - wallX, baseY - size + 1, baseZ - wallY)) return true;
                if(checkForCubeOfSize(size, world, baseX + wallX, baseY - size + 1, baseZ + wallY)) return true;
            }
        }
        return false;
    }

    private static boolean checkForCubeOfSize(int size, World world, int baseX, int baseY, int baseZ){
        boolean validValveFound = false;
        for(int x = 0; x < size; x++) {
            for(int y = 0; y < size; y++) {
                for(int z = 0; z < size; z++) {
                    if(x != 0 && x != size - 1 && y != 0 && y != size - 1 && z != 0 && z != size - 1) continue;
                    if(world.getBlock(x + baseX, y + baseY, z + baseZ) != Blockss.pressureChamberWall && world.getBlock(x + baseX, y + baseY, z + baseZ) != Blockss.pressureChamberValve && world.getBlock(x + baseX, y + baseY, z + baseZ) != Blockss.pressureChamberInterface) {
                        return false;
                    } else if(world.getBlock(x + baseX, y + baseY, z + baseZ) == Blockss.pressureChamberValve) {
                        boolean xMid = x != 0 && x != size - 1;
                        boolean yMid = y != 0 && y != size - 1;
                        boolean zMid = z != 0 && z != size - 1;
                        ForgeDirection facing = ForgeDirection.getOrientation(world.getBlockMetadata(x + baseX, y + baseY, z + baseZ));
                        if(xMid && yMid && (facing == ForgeDirection.NORTH || facing == ForgeDirection.SOUTH) || xMid && zMid && (facing == ForgeDirection.UP || facing == ForgeDirection.DOWN) || yMid && zMid && (facing == ForgeDirection.EAST || facing == ForgeDirection.WEST)) {
                            validValveFound = true;
                        } else {
                            return false;
                        }
                    } else {// when blockID == wall/interface
                        TileEntity te = world.getTileEntity(x + baseX, y + baseY, z + baseZ);
                        if(te instanceof TileEntityPressureChamberWall && ((TileEntityPressureChamberWall)te).getCore() != null) {
                            return false;
                        }
                    }
                }
            }
        }

        // when the code makes it to here we've found a valid structure. it only
        // depends on whether there is a valid valve in the structure now.
        if(!validValveFound) return false;

        TileEntityPressureChamberValve teValve = null;
        List<TileEntityPressureChamberValve> valveList = new ArrayList<TileEntityPressureChamberValve>();
        for(int x = 0; x < size; x++) {
            for(int y = 0; y < size; y++) {
                for(int z = 0; z < size; z++) {
                    TileEntity te = world.getTileEntity(x + baseX, y + baseY, z + baseZ);
                    if(te instanceof TileEntityPressureChamberValve) {
                        boolean xMid = x != 0 && x != size - 1;
                        boolean yMid = y != 0 && y != size - 1;
                        boolean zMid = z != 0 && z != size - 1;
                        ForgeDirection facing = ForgeDirection.getOrientation(world.getBlockMetadata(x + baseX, y + baseY, z + baseZ));
                        if(xMid && yMid && (facing == ForgeDirection.NORTH || facing == ForgeDirection.SOUTH) || xMid && zMid && (facing == ForgeDirection.UP || facing == ForgeDirection.DOWN) || yMid && zMid && (facing == ForgeDirection.EAST || facing == ForgeDirection.WEST)) {
                            teValve = (TileEntityPressureChamberValve)te;
                            valveList.add(teValve);
                        }
                    }
                }
            }
        }
        if(teValve == null) return false;// this line shouldn't be triggered
                                         // ever.

        // put the list of all the valves in the structure in all the valve
        // TE's.
        for(TileEntityPressureChamberValve valve : valveList) {
            valve.accessoryValves = new ArrayList<TileEntityPressureChamberValve>(valveList);
            valve.sendDescriptionPacket();
        }

        // set the redirections of right clicking and breaking a wall block to
        // the valve.
        for(int x = 0; x < size; x++) {
            for(int y = 0; y < size; y++) {
                for(int z = 0; z < size; z++) {
                    TileEntity te = world.getTileEntity(x + baseX, y + baseY, z + baseZ);
                    if(te instanceof TileEntityPressureChamberWall) {
                        TileEntityPressureChamberWall teWall = (TileEntityPressureChamberWall)te;
                        teWall.setCore(teValve); // set base TE to the valve
                                                 // found.
                    }
                }
            }
        }

        // set the multi-block coords in the valve TE
        teValve.setMultiBlockCoords(size, baseX, baseY, baseZ);
        teValve.sendDescriptionPacket();
        return true;
    }

    public boolean isCoordWithinChamber(World world, int x, int y, int z){
        if(x > multiBlockX && x < multiBlockX + multiBlockSize - 1 && y > multiBlockY && y < multiBlockY + multiBlockSize - 1 && z > multiBlockZ && z < multiBlockZ + multiBlockSize - 1) {
            return true;
        }
        return false;
    }

    // INVENTORY METHODS- && NBT
    // ------------------------------------------------------------

    /**
     * Returns the number of slots in the inventory.
     */
    @Override
    public int getSizeInventory(){

        return inventory.length;
    }

    /**
     * Returns the stack in slot i
     */
    @Override
    public ItemStack getStackInSlot(int slot){

        return inventory[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount){

        ItemStack itemStack = getStackInSlot(slot);
        if(itemStack != null) {
            if(itemStack.stackSize <= amount) {
                setInventorySlotContents(slot, null);
            } else {
                itemStack = itemStack.splitStack(amount);
                if(itemStack.stackSize == 0) {
                    setInventorySlotContents(slot, null);
                }
            }
        }

        return itemStack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot){

        ItemStack itemStack = getStackInSlot(slot);
        if(itemStack != null) {
            setInventorySlotContents(slot, null);
        }
        return itemStack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack itemStack){
        // super.setInventorySlotContents(slot, itemStack);
        inventory[slot] = itemStack;
        if(itemStack != null && itemStack.stackSize > getInventoryStackLimit()) {
            itemStack.stackSize = getInventoryStackLimit();
        }
    }

    @Override
    public int getInventoryStackLimit(){

        return 64;
    }

    @Override
    public String getInventoryName(){

        return Blockss.pressureChamberValve.getUnlocalizedName();
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack){
        return itemstack.getItem() == Itemss.machineUpgrade;
    }

    @Override
    public boolean hasCustomInventoryName(){
        return false;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer var1){
        return isGuiUseableByPlayer(var1);
    }

    @Override
    public void openInventory(){}

    @Override
    public void closeInventory(){}

    @Override
    public float getMinWorkingPressure(){
        return isValidRecipeInChamber ? recipePressure : -1;
    }

}
