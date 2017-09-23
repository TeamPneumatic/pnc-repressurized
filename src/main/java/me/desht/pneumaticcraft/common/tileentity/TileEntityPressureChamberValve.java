package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.recipe.IPressureChamberRecipe;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IAirListener;
import me.desht.pneumaticcraft.common.DamageSourcePneumaticCraft;
import me.desht.pneumaticcraft.common.NBTUtil;
import me.desht.pneumaticcraft.common.block.BlockPressureChamberValve;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.block.IBlockPressureChamber;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.recipes.PneumaticRecipeRegistry;
import me.desht.pneumaticcraft.common.recipes.PressureChamberRecipe;
import me.desht.pneumaticcraft.common.util.Reflections;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class TileEntityPressureChamberValve extends TileEntityPneumaticBase implements IMinWorkingPressure, IAirListener {
    public int multiBlockX;
    public int multiBlockY;
    public int multiBlockZ;
    @GuiSynced
    public int multiBlockSize;
    public List<TileEntityPressureChamberValve> accessoryValves;
    private final List<BlockPos> nbtValveList;
    private boolean readNBT = false;
    @GuiSynced
    public boolean isValidRecipeInChamber;
    @GuiSynced
    public boolean isSufficientPressureInChamber;
    @GuiSynced
    public boolean areEntitiesDoneMoving;
    @GuiSynced
    public float recipePressure;
//    private final Item etchingAcid = Fluids.getBucket(Fluids.ETCHING_ACID);

    private final Random rand = new Random();

    public TileEntityPressureChamberValve() {
        super(PneumaticValues.DANGER_PRESSURE_PRESSURE_CHAMBER, PneumaticValues.MAX_PRESSURE_PRESSURE_CHAMBER, PneumaticValues.VOLUME_PRESSURE_CHAMBER, 4);
        accessoryValves = new ArrayList<>();
        nbtValveList = new ArrayList<>();
        addApplicableUpgrade(EnumUpgrade.ITEM_LIFE);
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    // pneumatic methods-------------------------------------------------------
    // used in the air dispersion methods.
    @Override
    public boolean isConnectedTo(EnumFacing side) {
        switch (getRotation()) {
            case UP:
            case DOWN:
                return side == EnumFacing.UP || side == EnumFacing.DOWN;
            case NORTH:
            case SOUTH:
                return side == EnumFacing.NORTH || side == EnumFacing.SOUTH;
            case EAST:
            case WEST:
                return side == EnumFacing.EAST || side == EnumFacing.WEST;
        }
        return false;
    }

    @Override
    public void addConnectedPneumatics(List<Pair<EnumFacing, IAirHandler>> teList) {
        if (accessoryValves != null) {
            for (TileEntityPressureChamberValve valve : accessoryValves) {
                if (valve != this) teList.add(new ImmutablePair<>(null, valve.getAirHandler(null)));
            }
        }
    }

    @Override
    public void onAirDispersion(IAirHandler handler, EnumFacing dir, int airAdded) {
    }

    @Override
    public int getMaxDispersion(IAirHandler handler, EnumFacing dir) {
        return Integer.MAX_VALUE;
    }

    // main update method
    @Override
    public void update() {
        if (readNBT) {
            // this way of doing this is needed because other TE's are loaded after the reading of the NBT of this TE
            readNBT = false;

            // moved this from setMultiBlockCoords() - desht
            IBlockState state = getWorld().getBlockState(getPos());
            if (state.getBlock() == Blockss.PRESSURE_CHAMBER_VALVE)
                getWorld().setBlockState(getPos(), state.withProperty(BlockPressureChamberValve.FORMED, multiBlockSize > 0), 2);

            accessoryValves.clear();
            for (BlockPos valve : nbtValveList) {
                TileEntity te = getWorld().getTileEntity(valve);
                if (te instanceof TileEntityPressureChamberValve) {
                    accessoryValves.add((TileEntityPressureChamberValve) te);
                }
            }
            if (accessoryValves.isEmpty()) {//Hacky solution for an unexplainable bug.
                invalidateMultiBlock();
                checkIfProperlyFormed(getWorld(), getPos());
            }
            if (getWorld().isRemote) {
                getWorld().markBlockRangeForRenderUpdate(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX(), getPos().getY(), getPos().getZ());
            }
        }
        if (!getWorld().isRemote) {
            checkForAirLeak();

        }
        super.update();

        /*
         * FIXME pressure chamber particle effects not updating properly
         * if(!getWorld().isRemote){ pressureUpdateTimer++;
         * System.out.println("timer: " + pressureUpdateTimer);
         * if(pressureUpdateTimer > 60){ pressureUpdateTimer = 0;
         * updatePressuresToClient(); } }
         */

        if (multiBlockSize != 0 && !getWorld().isRemote) {
            ItemStack[] stacksInChamber = getStacksInChamber();
            isValidRecipeInChamber = false;
            isSufficientPressureInChamber = false;
            recipePressure = Float.MAX_VALUE;

            // simple recipes
            for (PressureChamberRecipe recipe : PressureChamberRecipe.chamberRecipes) {
                boolean isValidRecipeInChamberFlag = canBeCompressed(recipe, stacksInChamber);
                boolean isSufficientPressureInChamberFlag = recipe.pressure <= getPressure() && recipe.pressure > 0F || recipe.pressure >= getPressure() && recipe.pressure < 0F;
                if (isValidRecipeInChamberFlag) {
                    isValidRecipeInChamber = true;
                    if (Math.abs(recipe.pressure) < Math.abs(recipePressure)) {
                        recipePressure = recipe.pressure;
                    }
                }
                if (isSufficientPressureInChamberFlag) isSufficientPressureInChamber = true;
                if (isValidRecipeInChamberFlag && isSufficientPressureInChamberFlag && areEntitiesDoneMoving) {
                    double[] outputPosition = clearStacksInChamber(recipe.input);
                    giveOutput(recipe.output, outputPosition);
                }
            }

            // special recipes
            for (IPressureChamberRecipe recipe : PressureChamberRecipe.specialRecipes) {
                ItemStack[] removedStacks = recipe.isValidRecipe(stacksInChamber);
                boolean isValidRecipeInChamberFlag = removedStacks != null;
                boolean isSufficientPressureInChamberFlag = recipe.getCraftingPressure() <= getPressure() && recipe.getCraftingPressure() > 0F || recipe.getCraftingPressure() >= getPressure() && recipe.getCraftingPressure() < 0F;
                if (isValidRecipeInChamberFlag) {
                    isValidRecipeInChamber = true;
                    if (Math.abs(recipe.getCraftingPressure()) < Math.abs(recipePressure)) {
                        recipePressure = recipe.getCraftingPressure();
                    }
                }
                if (isSufficientPressureInChamberFlag) isSufficientPressureInChamber = true;
                if (isValidRecipeInChamberFlag && isSufficientPressureInChamberFlag && areEntitiesDoneMoving) {
                    double[] outputPosition = clearStacksInChamber((Object[]) removedStacks);
                    giveOutput(recipe.craftRecipe(stacksInChamber, removedStacks), outputPosition);
                }
            }

            if (getPressure() > PneumaticValues.MAX_PRESSURE_LIVING_ENTITY) {
                AxisAlignedBB bbBox = new AxisAlignedBB(multiBlockX + 1, multiBlockY + 1, multiBlockZ + 1, multiBlockX + multiBlockSize - 1, multiBlockY + multiBlockSize - 1, multiBlockZ + multiBlockSize - 1);
                List<EntityLivingBase> entities = getWorld().getEntitiesWithinAABB(EntityLivingBase.class, bbBox);
                for (EntityLivingBase entity : entities) {
                    if (entity instanceof EntityVillager) {
                        EntityVillager villager = (EntityVillager) entity;
                        // FIXME migrate to getProfessionForge()
                        if (villager.getProfession() != ConfigHandler.general.villagerMechanicID) {
                            villager.setProfession(ConfigHandler.general.villagerMechanicID);
                            NBTTagCompound tag = new NBTTagCompound();
                            villager.writeEntityToNBT(tag);
                            if (tag.hasKey("Offers")) {//reset the trade list
                                tag.removeTag("Offers");
                                villager.readEntityFromNBT(tag);
                            }
                        }
                    }
                    entity.attackEntityFrom(DamageSourcePneumaticCraft.PRESSURE, (int) (getPressure() * 2D));
                }
            }
        }

        // move entities to eachother.
        AxisAlignedBB bbBox = new AxisAlignedBB(multiBlockX, multiBlockY, multiBlockZ, multiBlockX + multiBlockSize, multiBlockY + multiBlockSize, multiBlockZ + multiBlockSize);
        List<EntityItem> entities = getWorld().getEntitiesWithinAABB(EntityItem.class, bbBox);
        areEntitiesDoneMoving = true; // set to true, set to false when one of
        // the entities is moving.
        for (int i = 0; i < entities.size() - 1; i++) {
            EntityItem lastEntity = entities.get(i);
            EntityItem entity = entities.get(i + 1);
            // XP Orb code snippet
            double d0 = 8.0D;
            double d1 = (lastEntity.posX - entity.posX) / d0;
            double d3 = (lastEntity.posZ - entity.posZ) / d0;
            double d4 = Math.sqrt(d1 * d1 + d3 * d3);
            double d5 = 1.0D - d4;

            if (d5 > 0.0D && d4 > 0.02D) {
                d5 *= d5;
                entity.motionX += d1 / d4 * d5 * 0.01D;
                entity.motionZ += d3 / d4 * d5 * 0.01D;
                lastEntity.motionX -= d1 / d4 * d5 * 0.01D;
                lastEntity.motionZ -= d3 / d4 * d5 * 0.01D;
                areEntitiesDoneMoving = false;
            }
        }

        boolean lifeUpgrade = getUpgrades(EnumUpgrade.ITEM_LIFE) > 0;
        if (lifeUpgrade && !getWorld().isRemote) {
            for (EntityItem entityItem : entities) {
                Reflections.setItemAge(entityItem, Reflections.getItemAge(entityItem) - 1);
            }
        }

//        if (lifeUpgrade && !getWorld().isRemote && ageField != null) {
//            try {
//                for (EntityItem entity : entities) {
//                    ageField.setInt(entity, ageField.getInt(entity));
//                }
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//                ageField = null;
//            }
//
////                entity.age--;
//        }

        // particles
        if (getWorld().isRemote && getPressure() > 0.2D) {
            int particles = (int) Math.pow(multiBlockSize - 2, 3);
            for (int i = 0; i < particles; i++) {
                if (rand.nextInt(Math.max(1, 8 - (int) (getPressure() * 2D))) == 0) {
                    double posX = multiBlockX + 1D + rand.nextDouble() * (multiBlockSize - 2D);
                    double posY = multiBlockY + 1D + rand.nextDouble() * (multiBlockSize - 2D);
                    double posZ = multiBlockZ + 1D + rand.nextDouble() * (multiBlockSize - 2D);
                    getWorld().spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, posX, posY, posZ, 0D, 0D, 0D);
                }
            }
        }

    }

    private void checkForAirLeak() {
        boolean[] connected = new boolean[]{ true, true, true, true, true, true };

        switch (getRotation()) {
            //take off the sides that tubes can connect to
            case UP: case DOWN:
                connected[EnumFacing.UP.ordinal()] = connected[EnumFacing.DOWN.ordinal()] = false;
                break;
            case NORTH: case SOUTH:
                connected[EnumFacing.NORTH.ordinal()] = connected[EnumFacing.SOUTH.ordinal()] = false;
                break;
            case EAST: case WEST:
                connected[EnumFacing.EAST.ordinal()] = connected[EnumFacing.WEST.ordinal()] = false;
                break;
        }

        List<Pair<EnumFacing, IAirHandler>> teList = getAirHandler(null).getConnectedPneumatics();
        for (Pair<EnumFacing, IAirHandler> entry : teList) {
            if (entry.getKey() != null) connected[entry.getKey().ordinal()] = true;
        }

        // retrieve the valve that is controlling the (potential) chamber
        TileEntityPressureChamberValve baseValve = null;
        for (TileEntityPressureChamberValve valve : accessoryValves) {
            if (valve.multiBlockSize > 0) {
                baseValve = valve;
                break;
            }
        }
        // if we found one, we can scratch one side to be leaking air
        if (baseValve != null) {
            switch (getRotation()) {
                case UP: case DOWN:
                    if (baseValve.multiBlockY == getPos().getY()) {
                        connected[EnumFacing.UP.ordinal()] = true;
                    } else {
                        connected[EnumFacing.DOWN.ordinal()] = true;
                    }
                    break;
                case NORTH: case SOUTH:
                    if (baseValve.multiBlockZ == getPos().getZ()) {
                        connected[EnumFacing.SOUTH.ordinal()] = true;
                    } else {
                        connected[EnumFacing.NORTH.ordinal()] = true;
                    }
                    break;
                case EAST: case WEST:
                    if (baseValve.multiBlockX == getPos().getX()) {
                        connected[EnumFacing.EAST.ordinal()] = true;
                    } else {
                        connected[EnumFacing.WEST.ordinal()] = true;
                    }
                    break;
            }
        }
        for (int i = 0; i < 6; i++) {
            if (!connected[i]) getAirHandler(null).airLeak(EnumFacing.getFront(i));
        }
    }

    private boolean canBeCompressed(PressureChamberRecipe recipe, ItemStack[] items) {
        for (Object in : recipe.input) {
            if (in != null) {
                int amount = 0;
                for (ItemStack item : items) {
                    if (!item.isEmpty() && PneumaticRecipeRegistry.isItemEqual(in, item)) amount += item.getCount();
                }
                if (amount < PneumaticRecipeRegistry.getItemAmount(in)) return false;
            }
        }
        return true;
    }

    ItemStack[] getStacksInChamber() {
        // add item entities lying on the ground
        AxisAlignedBB bbBox = new AxisAlignedBB(multiBlockX, multiBlockY, multiBlockZ,
                multiBlockX + multiBlockSize, multiBlockY + multiBlockSize, multiBlockZ + multiBlockSize);
        List<ItemStack> stackList = getWorld().getEntitiesWithinAABB(EntityItem.class, bbBox).stream().
                filter(entityItem -> !entityItem.isDead).
                map(EntityItem::getItem).
                collect(Collectors.toList());

        return stackList.toArray(new ItemStack[stackList.size()]);
    }

    double[] clearStacksInChamber(Object... stacksToClear) {
        int[] stackSizes = new int[stacksToClear.length];
        for (int i = 0; i < stacksToClear.length; i++) {
            stackSizes[i] = PneumaticRecipeRegistry.getItemAmount(stacksToClear[i]);
        }
        // default the output position to the middle of the chamber.
        double[] outputPosition = new double[]{multiBlockX + multiBlockSize / 2D, multiBlockY + 1.2D, multiBlockZ + multiBlockSize / 2D};

        // get the in world EntityItems
        AxisAlignedBB bbBox = new AxisAlignedBB(multiBlockX, multiBlockY, multiBlockZ, multiBlockX + multiBlockSize, multiBlockY + multiBlockSize, multiBlockZ + multiBlockSize);
        List<EntityItem> entities = getWorld().getEntitiesWithinAABB(EntityItem.class, bbBox);
        for (EntityItem entity : entities) {
            if (entity.isDead) continue;
            ItemStack entityStack = entity.getItem();
            for (int l = 0; l < stacksToClear.length; l++) {
                if (PneumaticRecipeRegistry.isItemEqual(stacksToClear[l], entityStack) && stackSizes[l] > 0) {
                    outputPosition[0] = entity.posX;
                    outputPosition[1] = entity.posY;
                    outputPosition[2] = entity.posZ;
                    int removedItems = Math.min(stackSizes[l], entityStack.getCount());
                    stackSizes[l] -= removedItems;
                    entityStack.shrink(removedItems);
                    if (entityStack.getCount() <= 0) entity.setDead();
                    break;
                }
            }
        }
        return outputPosition;
    }

    private void giveOutput(ItemStack[] output, double[] outputPosition) {
        for (ItemStack iStack : output) {
//            if (iStack.getItem() == etchingAcid) {
//                for (EntityPlayer player : getWorld().getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(getPos().getX() - 32, getPos().getY() - 32, getPos().getZ() - 32, getPos().getX() + 32, getPos().getY() + 32, getPos().getZ() + 32))) {
//                    AchievementHandler.giveAchievement(player, new ItemStack(etchingAcid));
//                }
//            }
            EntityItem item = new EntityItem(getWorld(), outputPosition[0], outputPosition[1], outputPosition[2], iStack.copy());
            getWorld().spawnEntity(item);
        }
    }

    // NBT methods-----------------------------------------------
    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        setMultiBlockCoords(tag.getInteger("multiBlockSize"), tag.getInteger("multiBlockX"), tag.getInteger("multiBlockY"), tag.getInteger("multiBlockZ"));
        isSufficientPressureInChamber = tag.getBoolean("sufPressure");
        isValidRecipeInChamber = tag.getBoolean("validRecipe");
        recipePressure = tag.getFloat("recipePressure");

        // Read in the accessory valves from NBT
        NBTTagList tagList2 = tag.getTagList("Valves", 10);
        nbtValveList.clear();
        for (int i = 0; i < tagList2.tagCount(); ++i) {
            NBTTagCompound tagCompound = tagList2.getCompoundTagAt(i);
            nbtValveList.add(NBTUtil.getPos(tagCompound));
        }
        readNBT = true;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("multiBlockX", multiBlockX);
        tag.setInteger("multiBlockY", multiBlockY);
        tag.setInteger("multiBlockZ", multiBlockZ);
        tag.setInteger("multiBlockSize", multiBlockSize);
        tag.setBoolean("sufPressure", isSufficientPressureInChamber);
        tag.setBoolean("validRecipe", isValidRecipeInChamber);
        tag.setFloat("recipePressure", recipePressure);

        // Write the accessory valve to NBT
        NBTTagList tagList2 = new NBTTagList();
        for (TileEntityPressureChamberValve valve : accessoryValves) {
            NBTTagCompound tagCompound = new NBTTagCompound();
            tagCompound.setInteger("getPos().getX()", valve.getPos().getX());
            tagCompound.setInteger("getPos().getY()", valve.getPos().getY());
            tagCompound.setInteger("getPos().getZ()", valve.getPos().getZ());
            tagList2.appendTag(tagCompound);
        }

        tag.setTag("Valves", tagList2);
        return tag;
    }

    public void onMultiBlockBreak() {
//        dropInventory(getWorld(), multiBlockX + multiBlockSize / 2D, multiBlockY + multiBlockSize / 2D, multiBlockZ + multiBlockSize / 2D);
        invalidateMultiBlock();
    }

    private void invalidateMultiBlock() {
        for (int x = 0; x < multiBlockSize; x++) {
            for (int y = 0; y < multiBlockSize; y++) {
                for (int z = 0; z < multiBlockSize; z++) {
                    TileEntity te = getWorld().getTileEntity(new BlockPos(x + multiBlockX, y + multiBlockY, z + multiBlockZ));
                    if (te instanceof TileEntityPressureChamberWall) {
                        // Clear the base TE's, so that the walls can be used in a new MultiBlock
                        TileEntityPressureChamberWall teWall = (TileEntityPressureChamberWall) te;
                        teWall.setCore(null);
                    }
                }
            }
        }
        if (accessoryValves != null) {
            for (TileEntityPressureChamberValve valve : accessoryValves) {
                valve.setMultiBlockCoords(0, 0, 0, 0);
                if (valve != this) {
                    valve.accessoryValves.clear();
                    valve.sendDescriptionPacket();
                }
            }
            accessoryValves.clear();
        }
        sendDescriptionPacket();
    }

    public void setMultiBlockCoords(int size, int baseX, int baseY, int baseZ) {
        multiBlockSize = size;
        multiBlockX = baseX;
        multiBlockY = baseY;
        multiBlockZ = baseZ;
        getAirHandler(null).setDefaultVolume(PneumaticValues.VOLUME_PRESSURE_CHAMBER + (multiBlockSize > 0 ? (int) Math.pow(multiBlockSize - 2, 3) * PneumaticValues.VOLUME_PRESSURE_CHAMBER_PER_EMPTY : 0));
//        IBlockState state = getWorld().getBlockState(getPos());
//        if (state.getBlock() == Blockss.PRESSURE_CHAMBER_VALVE)
//            getWorld().setBlockState(getPos(), state.withProperty(BlockPressureChamberValve.FORMED, multiBlockSize > 0), 2);

    }

    public static boolean checkIfProperlyFormed(World world, BlockPos pos) {
        // ArrayList<int[]> blockList = getAccessoryTiles(new
        // ArrayList<int[]>(), world, x,y,z);

        for (int i = 3; i < 6; i++) {
            if (checkForShiftedCubeOfSize(i, world, pos.getX(), pos.getY(), pos.getZ())) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkForShiftedCubeOfSize(int size, World world, int baseX, int baseY, int baseZ) {
        for (int wallX = 0; wallX < size; wallX++) {
            for (int wallY = 0; wallY < size; wallY++) {
                // check every possible configuration the block can be in.
                if (checkForCubeOfSize(size, world, baseX, baseY - wallY, baseZ - wallX)) return true;
                if (checkForCubeOfSize(size, world, baseX, baseY + wallY, baseZ + wallX)) return true;
                if (checkForCubeOfSize(size, world, baseX - wallX, baseY - wallY, baseZ)) return true;
                if (checkForCubeOfSize(size, world, baseX + wallX, baseY + wallY, baseZ)) return true;
                if (checkForCubeOfSize(size, world, baseX - wallX, baseY, baseZ - wallY)) return true;
                if (checkForCubeOfSize(size, world, baseX + wallX, baseY, baseZ + wallY)) return true;

                if (checkForCubeOfSize(size, world, baseX - size + 1, baseY - wallY, baseZ - wallX)) return true;
                if (checkForCubeOfSize(size, world, baseX - size + 1, baseY + wallY, baseZ + wallX)) return true;
                if (checkForCubeOfSize(size, world, baseX - wallX, baseY - wallY, baseZ - size + 1)) return true;
                if (checkForCubeOfSize(size, world, baseX + wallX, baseY + wallY, baseZ - size + 1)) return true;
                if (checkForCubeOfSize(size, world, baseX - wallX, baseY - size + 1, baseZ - wallY)) return true;
                if (checkForCubeOfSize(size, world, baseX + wallX, baseY - size + 1, baseZ + wallY)) return true;
            }
        }
        return false;
    }

    private static boolean checkForCubeOfSize(int size, World world, int baseX, int baseY, int baseZ) {
        boolean validValveFound = false;
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    if (x != 0 && x != size - 1 && y != 0 && y != size - 1 && z != 0 && z != size - 1) continue;
                    BlockPos pos = new BlockPos(x + baseX, y + baseY, z + baseZ);
                    Block block = world.getBlockState(pos).getBlock();
                    if (!(block instanceof IBlockPressureChamber)) {
                        return false;
                    } else if (block == Blockss.PRESSURE_CHAMBER_VALVE) {
                        boolean xMid = x != 0 && x != size - 1;
                        boolean yMid = y != 0 && y != size - 1;
                        boolean zMid = z != 0 && z != size - 1;
                        EnumFacing facing = ((TileEntityBase) world.getTileEntity(pos)).getRotation();
                        if (xMid && yMid && (facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH) || xMid && zMid && (facing == EnumFacing.UP || facing == EnumFacing.DOWN) || yMid && zMid && (facing == EnumFacing.EAST || facing == EnumFacing.WEST)) {
                            validValveFound = true;
                        } else {
                            return false;
                        }
                    } else {// when blockID == wall/interface
                        TileEntity te = world.getTileEntity(pos);
                        if (te instanceof TileEntityPressureChamberWall && ((TileEntityPressureChamberWall) te).getCore() != null) {
                            return false;
                        }
                    }
                }
            }
        }

        // when the code makes it to here we've found a valid structure. it only
        // depends on whether there is a valid valve in the structure now.
        if (!validValveFound) return false;

        TileEntityPressureChamberValve teValve = null;
        List<TileEntityPressureChamberValve> valveList = new ArrayList<TileEntityPressureChamberValve>();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    BlockPos pos = new BlockPos(x + baseX, y + baseY, z + baseZ);
                    TileEntity te = world.getTileEntity(pos);
                    if (te instanceof TileEntityPressureChamberValve) {
                        boolean xMid = x != 0 && x != size - 1;
                        boolean yMid = y != 0 && y != size - 1;
                        boolean zMid = z != 0 && z != size - 1;
                        EnumFacing facing = ((TileEntityBase) world.getTileEntity(pos)).getRotation();
                        if (xMid && yMid && (facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH) || xMid && zMid && (facing == EnumFacing.UP || facing == EnumFacing.DOWN) || yMid && zMid && (facing == EnumFacing.EAST || facing == EnumFacing.WEST)) {
                            teValve = (TileEntityPressureChamberValve) te;
                            valveList.add(teValve);
                        }
                    }
                }
            }
        }
        // this line shouldn't be triggered ever.
        if (teValve == null) return false;

        // put the list of all the valves in the structure in all the valve TE's.
        for (TileEntityPressureChamberValve valve : valveList) {
            valve.accessoryValves = new ArrayList<>(valveList);
            valve.sendDescriptionPacket();
        }

        // set the multi-block coords in the valve TE
        teValve.setMultiBlockCoords(size, baseX, baseY, baseZ);

        // set the redirections of right clicking and breaking a wall block to
        // the valve.
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    TileEntity te = world.getTileEntity(new BlockPos(x + baseX, y + baseY, z + baseZ));
                    if (te instanceof TileEntityPressureChamberWall) {
                        TileEntityPressureChamberWall teWall = (TileEntityPressureChamberWall) te;
                        teWall.setCore(teValve); // set base TE to the valve found.
                    }
                }
            }
        }

        teValve.sendDescriptionPacket();
        return true;
    }

    public boolean isCoordWithinChamber(World world, BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        return x > multiBlockX && x < multiBlockX + multiBlockSize - 1 && y > multiBlockY && y < multiBlockY + multiBlockSize - 1 && z > multiBlockZ && z < multiBlockZ + multiBlockSize - 1;
    }

    @Override
    public String getName() {
        return Blockss.PRESSURE_CHAMBER_VALVE.getUnlocalizedName();
    }

    @Override
    public float getMinWorkingPressure() {
        return isValidRecipeInChamber ? recipePressure : -1;
    }

}
