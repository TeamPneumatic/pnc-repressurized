package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.math.IntMath;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.recipe.IPressureChamberRecipe;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IAirListener;
import me.desht.pneumaticcraft.common.DamageSourcePneumaticCraft;
import me.desht.pneumaticcraft.common.block.*;
import me.desht.pneumaticcraft.common.event.VillagerHandler;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.recipes.PressureChamberRecipe;
import me.desht.pneumaticcraft.common.util.ItemStackHandlerIterable;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.EnumCustomParticleType;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TileEntityPressureChamberValve extends TileEntityPneumaticBase implements IMinWorkingPressure, IAirListener {
    private static final int CHAMBER_INV_SIZE = 27;

    @DescSynced
    public int multiBlockX, multiBlockY, multiBlockZ;
    @DescSynced
    public int multiBlockSize;
    @DescSynced
    public boolean hasGlass;  // true if there is any glass in the multiblock (only the primary valve has this)
    @DescSynced
    private float roundedPressure; // rounded to multiples of 0.25 to avoid excessive server->client traffic

    public List<TileEntityPressureChamberValve> accessoryValves;
    private final List<BlockPos> nbtValveList;
    private boolean readNBT = false;
    @GuiSynced
    public boolean isValidRecipeInChamber;
    @GuiSynced
    public boolean isSufficientPressureInChamber;
    @GuiSynced
    public float recipePressure;
    @DescSynced
    private ItemStackHandler itemsInChamber = new ItemStackHandler(CHAMBER_INV_SIZE) {
        @Override
        protected void onContentsChanged(int slot) {
            recipeRecalcNeeded = true;
        }
    };

    // list of recipes which can be made from the current chamber contents, not considering the current pressure
    private final List<IPressureChamberRecipe> applicableRecipes = new ArrayList<>();
    private boolean recipeRecalcNeeded = true;

    private long lastSoundTick;  // to avoid excessive spamming of the pop sound
    private int nParticles;  // client-side: the number of particles to create each tick (dependent on chamber size & pressure)

    public TileEntityPressureChamberValve() {
        super(PneumaticValues.DANGER_PRESSURE_PRESSURE_CHAMBER, PneumaticValues.MAX_PRESSURE_PRESSURE_CHAMBER, PneumaticValues.VOLUME_PRESSURE_CHAMBER_PER_EMPTY, 4);
        accessoryValves = new ArrayList<>();
        nbtValveList = new ArrayList<>();
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

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

    @Override
    public void update() {
        if (readNBT && !getWorld().isRemote) {
            doPostNBTSetup();
        }

        if (!getWorld().isRemote) {
            checkForAirLeak();
        }

        if (multiBlockSize != 0 && !getWorld().isRemote) {
            roundedPressure = ((int) (getPressure() * 4.0f)) / 4.0f;

            if (recipeRecalcNeeded) {
                isValidRecipeInChamber = false;
                isSufficientPressureInChamber = false;
                recipePressure = Float.MAX_VALUE;
                applicableRecipes.clear();
                for (IPressureChamberRecipe recipe : PressureChamberRecipe.recipes) {
                    if (recipe.isValidRecipe(itemsInChamber)) {
                        applicableRecipes.add(recipe);
                    }
                }
                isValidRecipeInChamber = !applicableRecipes.isEmpty();
                recipeRecalcNeeded = false;
            }

            processApplicableRecipes();

            if (getPressure() > PneumaticValues.MAX_PRESSURE_LIVING_ENTITY) {
                handleEntitiesInChamber();
            }
        }

        super.update();

        // particles
        if (getWorld().isRemote && hasGlass && isPrimaryValve() && roundedPressure > 0.2D) {
            if (PneumaticCraftRepressurized.proxy.getClientPlayer().getDistanceSq(getPos()) < 256) {
                for (int i = 0; i < nParticles; i++) {
                    double posX = multiBlockX + 1D + getWorld().rand.nextDouble() * (multiBlockSize - 2D);
                    double posY = multiBlockY + 1.5D + getWorld().rand.nextDouble() * (multiBlockSize - 2.5D);
                    double posZ = multiBlockZ + 1D + getWorld().rand.nextDouble() * (multiBlockSize - 2D);
                    PneumaticCraftRepressurized.proxy.playCustomParticle(EnumCustomParticleType.AIR_PARTICLE, world, posX, posY, posZ, 0, 0, 0);
                }
            }
        }
    }

    /**
     * This setup can't be done in readFromNBT() because there may be multiple valve TE's in the multiblock,
     * and all of them need to be fully initialized before this code is run.
     */
    private void doPostNBTSetup() {
        readNBT = false;

        IBlockState state = getWorld().getBlockState(getPos());
        if (state.getBlock() instanceof BlockPressureChamberValve)
            getWorld().setBlockState(getPos(), state.withProperty(BlockPressureChamberValve.FORMED, isPrimaryValve()), 2);

        accessoryValves.clear();
        for (BlockPos valve : nbtValveList) {
            TileEntity te = getWorld().getTileEntity(valve);
            if (te instanceof TileEntityPressureChamberValve) {
                accessoryValves.add((TileEntityPressureChamberValve) te);
            }
        }

        if (isPrimaryValve()) {
            hasGlass = checkForGlass();
            sendDescriptionPacket();
        }
    }

    private void checkForAirLeak() {
        boolean[] connected = new boolean[]{ true, true, true, true, true, true };

        switch (getRotation()) {
            // take off the sides that tubes can connect to
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
        TileEntityPressureChamberValve primaryValve = accessoryValves.isEmpty() ? null : accessoryValves.get(accessoryValves.size() - 1);
        if (primaryValve != null) {
            // we can scratch one side (the side facing into the chamber) to be leaking air
            switch (getRotation()) {
                case UP: case DOWN:
                    if (primaryValve.multiBlockY == getPos().getY()) {
                        connected[EnumFacing.UP.ordinal()] = true;
                    } else {
                        connected[EnumFacing.DOWN.ordinal()] = true;
                    }
                    break;
                case NORTH: case SOUTH:
                    if (primaryValve.multiBlockZ == getPos().getZ()) {
                        connected[EnumFacing.SOUTH.ordinal()] = true;
                    } else {
                        connected[EnumFacing.NORTH.ordinal()] = true;
                    }
                    break;
                case EAST: case WEST:
                    if (primaryValve.multiBlockX == getPos().getX()) {
                        connected[EnumFacing.EAST.ordinal()] = true;
                    } else {
                        connected[EnumFacing.WEST.ordinal()] = true;
                    }
                    break;
            }
        }
        for (int i = 0; i < 6; i++) {
            if (!connected[i]) getAirHandler(null).airLeak(EnumFacing.byIndex(i));
        }
    }

    private void processApplicableRecipes() {
        for (IPressureChamberRecipe recipe : applicableRecipes) {
            boolean pressureOK = recipe.getCraftingPressure() <= getPressure() && recipe.getCraftingPressure() > 0F
                    || recipe.getCraftingPressure() >= getPressure() && recipe.getCraftingPressure() < 0F;
            if (Math.abs(recipe.getCraftingPressure()) < Math.abs(recipePressure)) {
                recipePressure = recipe.getCraftingPressure();
            }
            if (pressureOK) {
                isSufficientPressureInChamber = true;
                giveOutput(recipe.craftRecipe(itemsInChamber));
                if (getWorld().getTotalWorldTime() - lastSoundTick > 5) {
                    NetworkHandler.sendToAllAround(new PacketPlaySound(SoundEvents.ENTITY_CHICKEN_EGG, SoundCategory.BLOCKS, getPos(), 0.5f, 0.8f + getWorld().rand.nextFloat() * 0.4f, false), getWorld());
                    lastSoundTick = getWorld().getTotalWorldTime();
                }
                // Craft at most one recipe each tick; this is because crafting changes the contents of the
                // chamber, possibly invalidating other applicable recipes.  Modifying the chamber's contents
                // automatically triggers a rescan for applicable recipes on the next tick.
                break;
            }
        }
    }

    private void handleEntitiesInChamber() {
        AxisAlignedBB bbBox = new AxisAlignedBB(multiBlockX + 1, multiBlockY + 1, multiBlockZ + 1, multiBlockX + multiBlockSize - 1, multiBlockY + multiBlockSize - 1, multiBlockZ + multiBlockSize - 1);
        List<EntityLivingBase> entities = getWorld().getEntitiesWithinAABB(EntityLivingBase.class, bbBox);
        for (EntityLivingBase entity : entities) {
            if (entity instanceof EntityVillager) {
                EntityVillager villager = (EntityVillager) entity;
                if (villager.getProfessionForge() != VillagerHandler.mechanicProfession) {
                    villager.setDead();
                    EntityVillager mechanic = new EntityVillager(world);
                    mechanic.setProfession(VillagerHandler.mechanicProfession);
                    mechanic.setPosition(villager.posX, villager.posY, villager.posZ);
                    world.spawnEntity(mechanic);
                }
            }
            if (!(entity instanceof EntityVillager) || ((EntityVillager) entity).getProfessionForge() != VillagerHandler.mechanicProfession) {
                entity.attackEntityFrom(DamageSourcePneumaticCraft.PRESSURE, (int) (getPressure() * 2D));
            }
        }
    }

    private boolean checkForGlass() {
        MutableBlockPos mPos = new MutableBlockPos();
        for (int x = 0; x < multiBlockSize; x++) {
            for (int y = 0; y < multiBlockSize; y++) {
                for (int z = 0; z < multiBlockSize; z++) {
                    mPos = mPos.setPos(multiBlockX + x, multiBlockY + y, multiBlockZ + z);
                    if (world.getBlockState(mPos).getBlock() instanceof BlockPressureChamberGlass) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void giveOutput(NonNullList<ItemStack> stacks){
        for (ItemStack stack : stacks){
            stack = stack.copy();
            stack = ItemHandlerHelper.insertItem(itemsInChamber, stack, false);
            if (!stack.isEmpty()) dropItemOnGround(stack); //As a last resort, actually drop an item in the chamber.
        }
    }

    public ItemStackHandler getStacksInChamber() {
        return itemsInChamber;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        setMultiBlockCoords(tag.getInteger("multiBlockSize"), tag.getInteger("multiBlockX"), tag.getInteger("multiBlockY"), tag.getInteger("multiBlockZ"));
        isSufficientPressureInChamber = tag.getBoolean("sufPressure");
        isValidRecipeInChamber = tag.getBoolean("validRecipe");
        recipePressure = tag.getFloat("recipePressure");
        itemsInChamber.deserializeNBT(tag.getCompoundTag("itemsInChamber"));
        if (itemsInChamber.getSlots() > CHAMBER_INV_SIZE) {
            // in case we read in a larger item handler from previous save (used to be 100 items)
            ItemStackHandler newHandler = new ItemStackHandler(CHAMBER_INV_SIZE);
            for (int i = 0; i < CHAMBER_INV_SIZE; i++) {
                newHandler.setStackInSlot(i, itemsInChamber.getStackInSlot(i));
            }
            itemsInChamber = newHandler;
        }

        // Read in the accessory valves from NBT
        NBTTagList tagList2 = tag.getTagList("Valves", Constants.NBT.TAG_COMPOUND);
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
        tag.setTag("itemsInChamber", itemsInChamber.serializeNBT());

        // Write the accessory valve to NBT
        NBTTagList tagList2 = new NBTTagList();
        for (TileEntityPressureChamberValve valve : accessoryValves) {
            NBTTagCompound tagCompound = new NBTTagCompound();
            tagCompound.setInteger("x", valve.getPos().getX());
            tagCompound.setInteger("y", valve.getPos().getY());
            tagCompound.setInteger("z", valve.getPos().getZ());
            tagList2.appendTag(tagCompound);
        }

        tag.setTag("Valves", tagList2);
        return tag;
    }

    public void onMultiBlockBreak() {
        if (isPrimaryValve()) {
            Iterator<ItemStack> itemsInChamberIterator = new ItemStackHandlerIterable(itemsInChamber).iterator();
            while (itemsInChamberIterator.hasNext()) {
                ItemStack stack = itemsInChamberIterator.next();
                dropItemOnGround(stack);
                itemsInChamberIterator.remove();
            }
            invalidateMultiBlock();
        }

    }
    
    private void dropItemOnGround(ItemStack stack){
        PneumaticCraftUtils.dropItemOnGroundPrecisely(stack, getWorld(), multiBlockX + multiBlockSize / 2,
                                                                multiBlockY + 1,  
                                                                multiBlockZ + multiBlockSize / 2);
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
                    if (!getWorld().isRemote) valve.sendDescriptionPacket();
                }
            }
            accessoryValves.clear();
        }
        if (!getWorld().isRemote) sendDescriptionPacket();
    }

    private void setMultiBlockCoords(int size, int baseX, int baseY, int baseZ) {
        multiBlockSize = size;
        multiBlockX = baseX;
        multiBlockY = baseY;
        multiBlockZ = baseZ;
        getAirHandler(null).setDefaultVolume(getDefaultVolume());
    }

    @Override
    public void onDescUpdate() {
        super.onDescUpdate();
        nParticles = IntMath.pow(multiBlockSize - 2, 3);
        nParticles = Math.max(1, (int)(nParticles / (6 - roundedPressure)));
    }

    public static boolean checkIfProperlyFormed(World world, BlockPos pos) {
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
        List<TileEntityPressureChamberValve> valveList = new ArrayList<>();
        MutableBlockPos mPos = new MutableBlockPos();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    mPos = mPos.setPos(x + baseX, y + baseY, z + baseZ);
                    IBlockState state = world.getBlockState(mPos);
                    if (x != 0 && x != size - 1 && y != 0 && y != size - 1 && z != 0 && z != size - 1) {
                        if (!world.isAirBlock(mPos)) return false;
                    } else if (!(state.getBlock() instanceof IBlockPressureChamber)) {
                        return false;
                    } else if (state.getBlock() instanceof BlockPressureChamberValve) {
                        // this a valve; ensure it faces the right way for the face it's in
                        boolean xMid = x != 0 && x != size - 1;
                        boolean yMid = y != 0 && y != size - 1;
                        boolean zMid = z != 0 && z != size - 1;
                        EnumFacing facing = state.getValue(BlockPneumaticCraft.ROTATION); //(TileEntityBase) world.getTileEntity(mPos)).getRotation();
                        if (xMid && yMid && (facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH) || xMid && zMid && (facing == EnumFacing.UP || facing == EnumFacing.DOWN) || yMid && zMid && (facing == EnumFacing.EAST || facing == EnumFacing.WEST)) {
                            TileEntity te = world.getTileEntity(mPos);
                            if (te instanceof TileEntityPressureChamberValve) {
                                valveList.add((TileEntityPressureChamberValve) te);
                            }
                        } else {
                            return false;
                        }
                    } else {
                        // this is a wall or interface; ensure it doesn't belong to another pressure chamber
                        TileEntity te = world.getTileEntity(mPos);
                        if (te instanceof TileEntityPressureChamberWall && ((TileEntityPressureChamberWall) te).getCore() != null) {
                            return false;
                        }
                    }
                }
            }
        }

        // So the structure is valid; just check that we have at least one valid valve
        if (valveList.isEmpty()) return false;

        // primary valve is the last one scanned (which will be @ max X/Y/Z)
        TileEntityPressureChamberValve primaryValve = valveList.get(valveList.size() - 1);

        // every valve in the structure has a list of every valve, including itself
        valveList.forEach(valve -> valve.accessoryValves = new ArrayList<>(valveList));

        // set the multi-block coords in the primary valve only
        primaryValve.setMultiBlockCoords(size, baseX, baseY, baseZ);

        // note the core valve in every wall & interface so right clicking & block break work as expected
        primaryValve.hasGlass = false;
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    TileEntity te = world.getTileEntity(new BlockPos(x + baseX, y + baseY, z + baseZ));
                    if (te instanceof TileEntityPressureChamberWall) {
                        TileEntityPressureChamberWall teWall = (TileEntityPressureChamberWall) te;
                        teWall.setCore(primaryValve);  // this also forces re-rendering with the formed texture
                        if (world.getBlockState(te.getPos()).getBlock() instanceof BlockPressureChamberGlass) {
                            primaryValve.hasGlass = true;
                        }
                    } else if (te instanceof TileEntityPressureChamberValve) {
                        IBlockState state = world.getBlockState(te.getPos());
                        world.setBlockState(te.getPos(), state.withProperty(BlockPressureChamberValve.FORMED, ((TileEntityPressureChamberValve) te).isPrimaryValve()), 2);
                    }
                    if (te != null) {
                        double dx = x == 0 ? -0.1 : 0.1;
                        double dz = z == 0 ? -0.1 : 0.1;
                        NetworkHandler.sendToAllAround(
                                new PacketSpawnParticle(EnumParticleTypes.EXPLOSION_NORMAL,
                                        te.getPos().getX() + 0.5, te.getPos().getY() + 0.5, te.getPos().getZ() + 0.5,
                                        dx, 0.3, dz, 5, 0, 0, 0),
                                world);
                    }
                }
            }
        }

        // pick up any loose items into the chamber inventory
        primaryValve.captureEntityItemsInChamber();

        // force-sync primary valve details to clients for rendering purposes
        primaryValve.sendDescriptionPacket();

        return true;
    }

    private boolean isPrimaryValve() {
        return multiBlockSize > 0;
    }

    private AxisAlignedBB getChamberAABB() {
        return new AxisAlignedBB(multiBlockX, multiBlockY, multiBlockZ,
                multiBlockX + multiBlockSize, multiBlockY + multiBlockSize, multiBlockZ + multiBlockSize);
    }
    
    private void captureEntityItemsInChamber() {
        List<EntityItem> items = getWorld().getEntitiesWithinAABB(EntityItem.class, getChamberAABB(), EntitySelectors.IS_ALIVE);
        for (EntityItem item : items) {
            ItemStack stack = item.getItem();
            ItemStack leftover = ItemHandlerHelper.insertItem(itemsInChamber, stack, false);
            if (leftover.isEmpty()) item.setDead();
            else item.setItem(stack);
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox(){
        return getChamberAABB();
    }

    @Override
    public String getName() {
        return Blockss.PRESSURE_CHAMBER_VALVE.getTranslationKey();
    }

    @Override
    public float getMinWorkingPressure() {
        return isValidRecipeInChamber ? recipePressure : -Float.MAX_VALUE;
    }

    @Override
    public int getDefaultVolume() {
        int vol = super.getDefaultVolume();
        return multiBlockSize > 3 ? vol * IntMath.pow(multiBlockSize - 2, 3) : vol;
    }
}
