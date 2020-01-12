package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.math.IntMath;
import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipes;
import me.desht.pneumaticcraft.api.crafting.recipe.IPressureChamberRecipe;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.api.tileentity.IAirListener;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.block.BlockPressureChamberGlass;
import me.desht.pneumaticcraft.common.block.BlockPressureChamberValve;
import me.desht.pneumaticcraft.common.block.IBlockPressureChamber;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerPressureChamberValve;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.particle.AirParticleData;
import me.desht.pneumaticcraft.common.util.ItemStackHandlerIterable;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TileEntityPressureChamberValve extends TileEntityPneumaticBase implements IMinWorkingPressure, IAirListener, INamedContainerProvider {
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
        super(ModTileEntities.PRESSURE_CHAMBER_VALVE.get(), PneumaticValues.DANGER_PRESSURE_PRESSURE_CHAMBER, PneumaticValues.MAX_PRESSURE_PRESSURE_CHAMBER, PneumaticValues.VOLUME_PRESSURE_CHAMBER_PER_EMPTY, 4);
        accessoryValves = new ArrayList<>();
        nbtValveList = new ArrayList<>();
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return side.getAxis() == getRotation().getAxis();
    }

    @Override
    public List<IAirHandlerMachine> addConnectedPneumatics(List<IAirHandlerMachine> airHandlers) {
        if (accessoryValves != null) {
            for (TileEntityPressureChamberValve valve : accessoryValves) {
                if (valve != this) {
                    airHandlers.add(valve.airHandler);
                }
            }
        }
        return airHandlers;
    }

    @Override
    public void tick() {
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
                for (IPressureChamberRecipe recipe : PneumaticCraftRecipes.pressureChamberRecipes.values()) {
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

        super.tick();

        // particles
        if (getWorld().isRemote && hasGlass && isPrimaryValve() && roundedPressure > 0.2D) {
            if (ClientUtils.getClientPlayer().getDistanceSq(getPos().getX(), getPos().getY(), getPos().getZ()) < 256) {
                for (int i = 0; i < nParticles; i++) {
                    double posX = multiBlockX + 1D + getWorld().rand.nextDouble() * (multiBlockSize - 2D);
                    double posY = multiBlockY + 1.5D + getWorld().rand.nextDouble() * (multiBlockSize - 2.5D);
                    double posZ = multiBlockZ + 1D + getWorld().rand.nextDouble() * (multiBlockSize - 2D);
                    world.addParticle(AirParticleData.NORMAL, posX, posY, posZ, 0, 0, 0);
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

        BlockState state = getWorld().getBlockState(getPos());
        if (state.getBlock() instanceof BlockPressureChamberValve)
            getWorld().setBlockState(getPos(), state.with(BlockPressureChamberValve.FORMED, isPrimaryValve()), 2);

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
                connected[Direction.UP.ordinal()] = connected[Direction.DOWN.ordinal()] = false;
                break;
            case NORTH: case SOUTH:
                connected[Direction.NORTH.ordinal()] = connected[Direction.SOUTH.ordinal()] = false;
                break;
            case EAST: case WEST:
                connected[Direction.EAST.ordinal()] = connected[Direction.WEST.ordinal()] = false;
                break;
        }

        List<IAirHandlerMachine.Connection> l = airHandler.getConnectedAirHandlers(this);
        for (IAirHandlerMachine.Connection c : l) {
            if (c.getDirection() != null) connected[c.getDirection().ordinal()] = true;
        }

        // retrieve the valve that is controlling the (potential) chamber
        TileEntityPressureChamberValve primaryValve = accessoryValves.isEmpty() ? null : accessoryValves.get(accessoryValves.size() - 1);
        if (primaryValve != null) {
            // we can scratch one side (the side facing into the chamber) to be leaking air
            switch (getRotation()) {
                case UP: case DOWN:
                    if (primaryValve.multiBlockY == getPos().getY()) {
                        connected[Direction.UP.ordinal()] = true;
                    } else {
                        connected[Direction.DOWN.ordinal()] = true;
                    }
                    break;
                case NORTH: case SOUTH:
                    if (primaryValve.multiBlockZ == getPos().getZ()) {
                        connected[Direction.SOUTH.ordinal()] = true;
                    } else {
                        connected[Direction.NORTH.ordinal()] = true;
                    }
                    break;
                case EAST: case WEST:
                    if (primaryValve.multiBlockX == getPos().getX()) {
                        connected[Direction.EAST.ordinal()] = true;
                    } else {
                        connected[Direction.WEST.ordinal()] = true;
                    }
                    break;
            }
        }
        for (int i = 0; i < 6; i++) {
            if (!connected[i]) airHandler.airLeak(this, Direction.byIndex(i));
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
                if (getWorld().getGameTime() - lastSoundTick > 5) {
                    NetworkHandler.sendToAllAround(new PacketPlaySound(SoundEvents.ENTITY_CHICKEN_EGG, SoundCategory.BLOCKS, getPos(), 0.7f, 0.8f + getWorld().rand.nextFloat() * 0.4f, false), getWorld());
                    lastSoundTick = getWorld().getGameTime();
                }
                // Craft at most one recipe each tick; this is because crafting changes the contents of the
                // chamber, possibly invalidating other applicable recipes.  Modifying the chamber's contents
                // automatically triggers a rescan for applicable recipes on the next tick.
                break;
            }
        }
    }

    // todo 1.14 villagers
    private void handleEntitiesInChamber() {
//        AxisAlignedBB bbBox = new AxisAlignedBB(multiBlockX + 1, multiBlockY + 1, multiBlockZ + 1, multiBlockX + multiBlockSize - 1, multiBlockY + multiBlockSize - 1, multiBlockZ + multiBlockSize - 1);
//        List<LivingEntity> entities = getWorld().getEntitiesWithinAABB(LivingEntity.class, bbBox);
//        for (LivingEntity entity : entities) {
//            if (entity instanceof VillagerEntity) {
//                VillagerEntity villager = (VillagerEntity) entity;
//                if (villager.getProfessionForge() != VillagerHandler.mechanicProfession) {
//                    villager.remove();
//                    VillagerEntity mechanic = new VillagerEntity(world);
//                    mechanic.setProfession(VillagerHandler.mechanicProfession);
//                    mechanic.setPosition(villager.posX, villager.posY, villager.posZ);
//                    world.addEntity(mechanic);
//                }
//            }
//            if (!(entity instanceof VillagerEntity) || ((VillagerEntity) entity).getProfessionForge() != VillagerHandler.mechanicProfession) {
//                entity.attackEntityFrom(DamageSourcePneumaticCraft.PRESSURE, (int) (getPressure() * 2D));
//            }
//        }
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
    public void read(CompoundNBT tag) {
        super.read(tag);

        setupMultiBlock(tag.getInt("multiBlockSize"), tag.getInt("multiBlockX"), tag.getInt("multiBlockY"), tag.getInt("multiBlockZ"));
        isSufficientPressureInChamber = tag.getBoolean("sufPressure");
        isValidRecipeInChamber = tag.getBoolean("validRecipe");
        recipePressure = tag.getFloat("recipePressure");
        itemsInChamber.deserializeNBT(tag.getCompound("itemsInChamber"));
        if (itemsInChamber.getSlots() > CHAMBER_INV_SIZE) {
            // in case we read in a larger item handler from previous save (used to be 100 items)
            ItemStackHandler newHandler = new ItemStackHandler(CHAMBER_INV_SIZE);
            for (int i = 0; i < CHAMBER_INV_SIZE; i++) {
                newHandler.setStackInSlot(i, itemsInChamber.getStackInSlot(i));
            }
            itemsInChamber = newHandler;
        }

        // Read in the accessory valves from NBT
        ListNBT tagList2 = tag.getList("Valves", Constants.NBT.TAG_COMPOUND);
        nbtValveList.clear();
        for (int i = 0; i < tagList2.size(); ++i) {
            CompoundNBT tagCompound = tagList2.getCompound(i);
            nbtValveList.add(NBTUtil.getPos(tagCompound));
        }
        
        readNBT = true;
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.putInt("multiBlockX", multiBlockX);
        tag.putInt("multiBlockY", multiBlockY);
        tag.putInt("multiBlockZ", multiBlockZ);
        tag.putInt("multiBlockSize", multiBlockSize);
        tag.putBoolean("sufPressure", isSufficientPressureInChamber);
        tag.putBoolean("validRecipe", isValidRecipeInChamber);
        tag.putFloat("recipePressure", recipePressure);
        tag.put("itemsInChamber", itemsInChamber.serializeNBT());

        // Write the accessory valve to NBT
        ListNBT tagList2 = new ListNBT();
        for (TileEntityPressureChamberValve valve : accessoryValves) {
            CompoundNBT tagCompound = new CompoundNBT();
            tagCompound.putInt("x", valve.getPos().getX());
            tagCompound.putInt("y", valve.getPos().getY());
            tagCompound.putInt("z", valve.getPos().getZ());
            tagList2.add(tagCompound);
        }

        tag.put("Valves", tagList2);
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
        PneumaticCraftUtils.dropItemOnGroundPrecisely(stack, getWorld(),
                multiBlockX + multiBlockSize / 2.0, multiBlockY + 1.0, multiBlockZ + multiBlockSize / 2.0);
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
                valve.setupMultiBlock(0, 0, 0, 0);
                if (valve != this) {
                    valve.accessoryValves.clear();
                    if (!getWorld().isRemote) valve.sendDescriptionPacket();
                }
            }
            accessoryValves.clear();
        }
        if (!getWorld().isRemote) sendDescriptionPacket();
    }

    private void setupMultiBlock(int size, int baseX, int baseY, int baseZ) {
        multiBlockSize = size;
        multiBlockX = baseX;
        multiBlockY = baseY;
        multiBlockZ = baseZ;
        airHandler.setBaseVolume(getDefaultVolume());
    }

    @Override
    public void onDescUpdate() {
        super.onDescUpdate();
        nParticles = IntMath.pow(multiBlockSize - 2, 3);
        nParticles = Math.max(1, (int)(nParticles / ((dangerPressure + 1) - Math.min(dangerPressure, roundedPressure))));
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return null;
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
                    BlockState state = world.getBlockState(mPos);
                    if (x != 0 && x != size - 1 && y != 0 && y != size - 1 && z != 0 && z != size - 1) {
                        if (!world.isAirBlock(mPos)) return false;
                    } else if (!(state.getBlock() instanceof IBlockPressureChamber)) {
                        return false;
                    } else if (state.getBlock() instanceof BlockPressureChamberValve) {
                        // this a valve; ensure it faces the right way for the face it's in
                        boolean xMid = x != 0 && x != size - 1;
                        boolean yMid = y != 0 && y != size - 1;
                        boolean zMid = z != 0 && z != size - 1;
                        Direction facing = state.get(BlockStateProperties.FACING);
                        if (xMid && yMid && facing.getAxis() == Direction.Axis.Z
                                || xMid && zMid && facing.getAxis() == Direction.Axis.Y
                                || yMid && zMid && facing.getAxis() == Direction.Axis.X) {
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
        primaryValve.setupMultiBlock(size, baseX, baseY, baseZ);

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
                        BlockState state = world.getBlockState(te.getPos());
                        world.setBlockState(te.getPos(), state.with(BlockPressureChamberValve.FORMED, ((TileEntityPressureChamberValve) te).isPrimaryValve()), 2);
                    }
                    if (te != null) {
                        double dx = x == 0 ? -0.1 : 0.1;
                        double dz = z == 0 ? -0.1 : 0.1;
                        NetworkHandler.sendToAllAround(
                                new PacketSpawnParticle(ParticleTypes.POOF,
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
        List<ItemEntity> items = getWorld().getEntitiesWithinAABB(ItemEntity.class, getChamberAABB(), EntityPredicates.IS_ALIVE);
        for (ItemEntity item : items) {
            ItemStack stack = item.getItem();
            ItemStack leftover = ItemHandlerHelper.insertItem(itemsInChamber, stack, false);
            if (leftover.isEmpty()) item.remove();
            else item.setItem(stack);
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox(){
        return getChamberAABB();
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

    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerPressureChamberValve(i, playerInventory, getPos());
    }
}
