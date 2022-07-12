/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableList;
import com.google.common.math.IntMath;
import me.desht.pneumaticcraft.api.DamageSourcePneumaticCraft;
import me.desht.pneumaticcraft.api.crafting.recipe.PressureChamberRecipe;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.api.tileentity.IAirListener;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.block.BlockPressureChamberGlass;
import me.desht.pneumaticcraft.common.block.BlockPressureChamberValve;
import me.desht.pneumaticcraft.common.block.IBlockPressureChamber;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerPressureChamberValve;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import me.desht.pneumaticcraft.common.particle.AirParticleData;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.util.CountedItemStacks;
import me.desht.pneumaticcraft.common.util.ItemStackHandlerIterable;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.SizeLimitedItemHandlerWrapper;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class TileEntityPressureChamberValve extends TileEntityPneumaticBase
        implements IMinWorkingPressure, IAirListener, INamedContainerProvider {
    private static final int CHAMBER_INV_SIZE = 18;
    private static final int OUTPUT_INV_SIZE = 9;

    @DescSynced
    public int multiBlockX, multiBlockY, multiBlockZ;
    @DescSynced
    public int multiBlockSize;
    @DescSynced
    public boolean hasGlass;  // true if there is any glass in the multiblock (only the primary valve has this)
    @DescSynced
    private float roundedPressure; // rounded to multiples of 0.25 to avoid excessive server->client traffic

    @GuiSynced
    public boolean isValidRecipeInChamber;
    @GuiSynced
    public boolean isSufficientPressureInChamber;
    @GuiSynced
    public float recipePressure;

    private final ItemStackHandler itemsInChamber = new ChamberStackHandler();
    final ItemStackHandler craftedItems = new OutputStackHandler();
    @DescSynced
    final CombinedInvWrapper allItems = new CombinedInvWrapper(itemsInChamber, craftedItems);

    public final List<ItemStack> renderedItems = new ArrayList<>(); // list of non-empty stacks from allItems

    public List<TileEntityPressureChamberValve> accessoryValves;
    private final List<BlockPos> nbtValveList;  // temp store for positions of other valves read from NBT

    // list of recipes which can be made from the current chamber contents, not considering the current pressure
    private final List<ApplicableRecipe> applicableRecipes = new ArrayList<>();
    private boolean recipeRecalcNeeded = true;

    private long lastSoundTick;  // to avoid excessive spamming of the pop sound
    private int nParticles;  // client-side: the number of particles to create each tick (dependent on chamber size & pressure)
    private boolean triedRebuild;

    // Used to short-term track the multiblock size after it is broken
    // Does not get persisted in any way: only intended to prevent exploits around breaking a small multiblock and
    //   reforming the valve into a large multiblock to get free air
    // Since the pressure is preserved (other than any normal leakage) when the multiblock is broken/reformed and
    //   the base volume of the air handler changes
    private int savedMultiblockSize = 0;

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
        super.tick();

        if (!getLevel().isClientSide) {
            checkForAirLeak();
        }

        if (multiBlockSize != 0 && !getLevel().isClientSide) {
            roundedPressure = ((int) (getPressure() * 4.0f)) / 4.0f;

            if (recipeRecalcNeeded) {
                isValidRecipeInChamber = false;
                isSufficientPressureInChamber = false;
                recipePressure = Float.MAX_VALUE;
                applicableRecipes.clear();
                final SizeLimitedItemHandlerWrapper h = new SizeLimitedItemHandlerWrapper(itemsInChamber);
                if (h.getSlots() > 0) {
                    PneumaticCraftRecipeType.PRESSURE_CHAMBER.stream(level).forEach(recipe -> {
                        Collection<Integer> slots = recipe.findIngredients(h);
                        if (!slots.isEmpty()) {
                            applicableRecipes.add(new ApplicableRecipe(recipe, slots));
                        }
                    });
                }
                isValidRecipeInChamber = !applicableRecipes.isEmpty();
                // if we can't find a valid recipe, try coalescing itemstack in the chamber
                // it's possible we have the right ingredients, but split across stacks
                recipeRecalcNeeded = !isValidRecipeInChamber && coalesceItems();
            }

            processApplicableRecipes();

            if (getPressure() > PneumaticValues.MAX_PRESSURE_LIVING_ENTITY) {
                handleEntitiesInChamber();
            }
        }

        // particles
        if (getLevel().isClientSide && hasGlass && isPrimaryValve() && roundedPressure > 0.2D) {
            if (ClientUtils.getClientPlayer().distanceToSqr(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ()) < 256) {
                for (int i = 0; i < nParticles; i++) {
                    double posX = multiBlockX + 1D + getLevel().random.nextDouble() * (multiBlockSize - 2D);
                    double posY = multiBlockY + 1.5D + getLevel().random.nextDouble() * (multiBlockSize - 2.5D);
                    double posZ = multiBlockZ + 1D + getLevel().random.nextDouble() * (multiBlockSize - 2D);
                    getLevel().addParticle(AirParticleData.NORMAL, posX, posY, posZ, 0, 0, 0);
                }
            }
        }
    }

    private boolean coalesceItems() {
        CountedItemStacks count = new CountedItemStacks(itemsInChamber);
        if (!count.canCoalesce()) return false;

        NonNullList<ItemStack> coalesced = count.coalesce();
        for (int i = 0; i < itemsInChamber.getSlots(); i++) {
            if (i < coalesced.size()) {
                itemsInChamber.setStackInSlot(i, coalesced.get(i));
            } else {
                itemsInChamber.setStackInSlot(i, ItemStack.EMPTY);
            }
        }

        return true;
    }

    @Override
    protected void onFirstServerTick() {
        super.onFirstServerTick();

        doPostNBTSetup();
    }

    /**
     * Initialize the accessory valve list from saved NBT data.
     * This can't be done in readFromNBT() because there may be multiple valve TE's in the multiblock,
     * and all of them need to be fully initialized before this is run.
     */
    private void doPostNBTSetup() {
        if (!nbtValveList.isEmpty()) {
            BlockState state = getLevel().getBlockState(getBlockPos());
            if (state.getBlock() instanceof BlockPressureChamberValve)
                getLevel().setBlock(getBlockPos(), state.setValue(BlockPressureChamberValve.FORMED, isPrimaryValve()), 2);

            accessoryValves.clear();
            for (BlockPos valve : nbtValveList) {
                TileEntity te = getLevel().getBlockEntity(valve);
                if (te instanceof TileEntityPressureChamberValve) {
                    accessoryValves.add((TileEntityPressureChamberValve) te);
                }
            }

            if (isPrimaryValve()) {
                hasGlass = checkForGlass();
                sendDescriptionPacket();
            }

            nbtValveList.clear();
        }
    }

    private void checkForAirLeak() {
        BitSet disconnected = new BitSet(6);

        switch (getRotation().getAxis()) {
            case X:
                disconnected.set(Direction.WEST.get3DDataValue());
                disconnected.set(Direction.EAST.get3DDataValue());
                break;
            case Y:
                disconnected.set(Direction.UP.get3DDataValue());
                disconnected.set(Direction.DOWN.get3DDataValue());
                break;
            case Z:
                disconnected.set(Direction.NORTH.get3DDataValue());
                disconnected.set(Direction.SOUTH.get3DDataValue());
                break;
        }

        List<IAirHandlerMachine.Connection> l = airHandler.getConnectedAirHandlers(this);
        for (IAirHandlerMachine.Connection c : l) {
            if (c.getDirection() != null) disconnected.clear(c.getDirection().get3DDataValue());
        }

        // workaround for odd case where multiblock init sometimes fails after reloading
        // could be caused by something else throwing an exception during NBT loading?
        if (accessoryValves.isEmpty() && !triedRebuild) {
            if (checkIfProperlyFormed(level, worldPosition, true)) {
                Log.warning("Rebuilt damaged pressure chamber multiblock: valve pos = " + worldPosition);
            }
            triedRebuild = true;
        }

        // retrieve the valve that is controlling the (potential) chamber
        TileEntityPressureChamberValve primaryValve = accessoryValves.isEmpty() ? null : accessoryValves.get(accessoryValves.size() - 1);
        if (primaryValve != null) {
            // the side of the valve facing into a formed chamber doesn't leak, even though it's not connected
            switch (getRotation().getAxis()) {
                case X:
                    if (primaryValve.multiBlockX == getBlockPos().getX()) {
                        disconnected.clear(Direction.EAST.get3DDataValue());
                    } else {
                        disconnected.clear(Direction.WEST.get3DDataValue());
                    }
                    break;
                case Y:
                    if (primaryValve.multiBlockY == getBlockPos().getY()) {
                        disconnected.clear(Direction.UP.get3DDataValue());
                    } else {
                        disconnected.clear(Direction.DOWN.get3DDataValue());
                    }
                    break;
                case Z:
                    if (primaryValve.multiBlockZ == getBlockPos().getZ()) {
                        disconnected.clear(Direction.SOUTH.get3DDataValue());
                    } else {
                        disconnected.clear(Direction.NORTH.get3DDataValue());
                    }
                    break;
            }
        }
        airHandler.setSideLeaking(disconnected.isEmpty() ? null : getRotation());
    }

    private void processApplicableRecipes() {
        for (ApplicableRecipe applicableRecipe : applicableRecipes) {
            PressureChamberRecipe recipe = applicableRecipe.recipe;
            float requiredPressure = recipe.getCraftingPressure(itemsInChamber, applicableRecipe.slots);
            boolean pressureOK = requiredPressure <= getPressure() && requiredPressure > 0F
                    || requiredPressure >= getPressure() && requiredPressure < 0F;
            if (Math.abs(requiredPressure) < Math.abs(recipePressure)) {
                recipePressure = requiredPressure;
            }
            if (pressureOK) {
                // let's craft! (although if the output handler is full, we won't...)
                isSufficientPressureInChamber = true;
                if (giveOutput(recipe.craftRecipe(itemsInChamber, applicableRecipe.slots, true), true)
                        && giveOutput(recipe.craftRecipe(itemsInChamber, applicableRecipe.slots, false), false)) {
                    if (getLevel().getGameTime() - lastSoundTick > 5) {
                        getLevel().playSound(null, getBlockPos(), SoundEvents.CHICKEN_EGG, SoundCategory.BLOCKS, 0.7f, 0.8f);
                        lastSoundTick = getLevel().getGameTime();
                    }
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
        List<LivingEntity> entities = getLevel().getEntitiesOfClass(LivingEntity.class, bbBox);
        for (LivingEntity entity : entities) {
            // Note: villager conversion is no longer a thing, since due to new 1.14+ villager mechanics,
            // the converted villager will just lose his progression. Instead, just place down a
            // charging station, and an unemployed villager will claim it.
            entity.hurt(DamageSourcePneumaticCraft.PRESSURE, (int) (getPressure() * 2D));
        }
    }

    private boolean checkForGlass() {
        BlockPos.Mutable mPos = new BlockPos.Mutable();
        for (int x = 0; x < multiBlockSize; x++) {
            for (int y = 0; y < multiBlockSize; y++) {
                for (int z = 0; z < multiBlockSize; z++) {
                    mPos = mPos.set(multiBlockX + x, multiBlockY + y, multiBlockZ + z);
                    if (level.getBlockState(mPos).getBlock() instanceof BlockPressureChamberGlass) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Try to insert all the given stacks into the output handler.
     * @param stacks list of item stacks
     * @param simulate true to simulate insertion
     * @return true if all can be (were) inserted, false if not
     */
    private boolean giveOutput(NonNullList<ItemStack> stacks, boolean simulate) {
        if (stacks.isEmpty()) return false;
        for (ItemStack stack : stacks) {
            stack = stack.copy();
            ItemStack result = ItemHandlerHelper.insertItem(craftedItems, stack, simulate);
            if (!result.isEmpty()) return false;
        }
        return true;
    }

    /**
     * Insert the given item into the crafting chamber.
     *
     * @param stack the item to insert
     * @return what could not be inserted
     */
    ItemStack insertItemToChamber(ItemStack stack) {
        return ItemHandlerHelper.insertItem(itemsInChamber, stack.copy(), false);
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);

        setupMultiBlock(tag.getInt("multiBlockSize"), tag.getInt("multiBlockX"), tag.getInt("multiBlockY"), tag.getInt("multiBlockZ"));
        ItemStackHandler handler = new ItemStackHandler();
        handler.deserializeNBT(tag.getCompound("itemsInChamber"));
        for (int i = 0; i < handler.getSlots() && i < CHAMBER_INV_SIZE; i++) {
            itemsInChamber.setStackInSlot(i, handler.getStackInSlot(i));
        }
        ItemStackHandler outHandler = new ItemStackHandler();
        outHandler.deserializeNBT(tag.getCompound("craftedItems"));
        for (int i = 0; i < outHandler.getSlots() && i < OUTPUT_INV_SIZE; i++) {
            craftedItems.setStackInSlot(i, outHandler.getStackInSlot(i));
        }

        // Read in the accessory valves from NBT
        ListNBT accList = tag.getList("Valves", Constants.NBT.TAG_COMPOUND);
        nbtValveList.clear();
        for (int i = 0; i < accList.size(); ++i) {
            CompoundNBT tagCompound = accList.getCompound(i);
            nbtValveList.add(NBTUtil.readBlockPos(tagCompound));
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        super.save(tag);
        tag.putInt("multiBlockX", multiBlockX);
        tag.putInt("multiBlockY", multiBlockY);
        tag.putInt("multiBlockZ", multiBlockZ);
        tag.putInt("multiBlockSize", multiBlockSize);
        tag.put("itemsInChamber", itemsInChamber.serializeNBT());
        tag.put("craftedItems", craftedItems.serializeNBT());

        // Write the accessory valve list to NBT
        ListNBT accList = accessoryValves.stream()
                .map(valve -> NBTUtil.writeBlockPos(valve.getBlockPos()))
                .collect(Collectors.toCollection(ListNBT::new));
        tag.put("Valves", accList);

        return tag;
    }

    public void onMultiBlockBreak() {
        if (isPrimaryValve()) {
            Iterator<ItemStack> itemsInChamberIterator = new ItemStackHandlerIterable(allItems).iterator();
            while (itemsInChamberIterator.hasNext()) {
                ItemStack stack = itemsInChamberIterator.next();
                if (!stack.isEmpty()) {
                    dropItemOnGround(stack);
                    itemsInChamberIterator.remove();
                }
            }
            invalidateMultiBlock();
        }

    }
    
    private void dropItemOnGround(ItemStack stack){
        PneumaticCraftUtils.dropItemOnGroundPrecisely(stack, getLevel(),
                multiBlockX + multiBlockSize / 2.0, multiBlockY + 1.0, multiBlockZ + multiBlockSize / 2.0);
    }

    private void invalidateMultiBlock() {
        for (int x = 0; x < multiBlockSize; x++) {
            for (int y = 0; y < multiBlockSize; y++) {
                for (int z = 0; z < multiBlockSize; z++) {
                    TileEntity te = getLevel().getBlockEntity(new BlockPos(x + multiBlockX, y + multiBlockY, z + multiBlockZ));
                    if (te instanceof TileEntityPressureChamberWall) {
                        // Clear the base TE's, so that the walls can be used in a new MultiBlock
                        TileEntityPressureChamberWall teWall = (TileEntityPressureChamberWall) te;
                        teWall.setPrimaryValve(null);
                    }
                }
            }
        }
        if (accessoryValves != null) {
            for (TileEntityPressureChamberValve valve : accessoryValves) {
                valve.savedMultiblockSize = valve.multiBlockSize;
                // keep the valve pressure constant after multi-block break (4x4x4 or 5x5x5 will cause base volume to drop)
                float pressure = valve.getPressure();
                valve.setupMultiBlock(0, 0, 0, 0);
                valve.airHandler.setPressure(pressure);
                if (valve != this) {
                    valve.accessoryValves.clear();
                    if (!getLevel().isClientSide) valve.sendDescriptionPacket();
                }
                valve.setChanged();
            }
            accessoryValves.clear();
        }
        if (!getLevel().isClientSide) sendDescriptionPacket();
    }

    private void setupMultiBlock(int size, int baseX, int baseY, int baseZ) {
        multiBlockSize = size;
        multiBlockX = baseX;
        multiBlockY = baseY;
        multiBlockZ = baseZ;
        int vol = PneumaticValues.VOLUME_PRESSURE_CHAMBER_PER_EMPTY;
        airHandler.setBaseVolume(multiBlockSize > 3 ? vol * IntMath.pow(multiBlockSize - 2, 3) : vol);
    }

    @Override
    public void onDescUpdate() {
        super.onDescUpdate();

        nParticles = (int) (Math.min(1, (roundedPressure / getDangerPressure()) * (multiBlockSize - 2) * (multiBlockSize - 2)) * 2);
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return null;
    }

    public static boolean checkIfProperlyFormed(World world, BlockPos pos) {
        return checkIfProperlyFormed(world, pos, false);
    }

    public static boolean checkIfProperlyFormed(World world, BlockPos pos, boolean forceRebuild) {
        for (int i = 3; i < 6; i++) {
            if (checkForShiftedCubeOfSize(i, world, pos, forceRebuild)) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkForShiftedCubeOfSize(int size, World world, BlockPos pos, boolean forceRebuild) {
        int baseX = pos.getX();
        int baseY = pos.getY();
        int baseZ = pos.getZ();
        BlockPos rebuildPos = forceRebuild ? pos : null;
        for (int wallX = 0; wallX < size; wallX++) {
            for (int wallY = 0; wallY < size; wallY++) {
                // check every possible configuration the block can be in.
                if (checkForCubeOfSize(size, world, baseX, baseY - wallY, baseZ - wallX, rebuildPos)) return true;
                if (checkForCubeOfSize(size, world, baseX, baseY + wallY, baseZ + wallX, rebuildPos)) return true;
                if (checkForCubeOfSize(size, world, baseX - wallX, baseY - wallY, baseZ, rebuildPos)) return true;
                if (checkForCubeOfSize(size, world, baseX + wallX, baseY + wallY, baseZ, rebuildPos)) return true;
                if (checkForCubeOfSize(size, world, baseX - wallX, baseY, baseZ - wallY, rebuildPos)) return true;
                if (checkForCubeOfSize(size, world, baseX + wallX, baseY, baseZ + wallY, rebuildPos)) return true;

                if (checkForCubeOfSize(size, world, baseX - size + 1, baseY - wallY, baseZ - wallX, rebuildPos)) return true;
                if (checkForCubeOfSize(size, world, baseX - size + 1, baseY + wallY, baseZ + wallX, rebuildPos)) return true;
                if (checkForCubeOfSize(size, world, baseX - wallX, baseY - wallY, baseZ - size + 1, rebuildPos)) return true;
                if (checkForCubeOfSize(size, world, baseX + wallX, baseY + wallY, baseZ - size + 1, rebuildPos)) return true;
                if (checkForCubeOfSize(size, world, baseX - wallX, baseY - size + 1, baseZ - wallY, rebuildPos)) return true;
                if (checkForCubeOfSize(size, world, baseX + wallX, baseY - size + 1, baseZ + wallY, rebuildPos)) return true;
            }
        }
        return false;
    }

    private static boolean checkForCubeOfSize(int size, World world, int baseX, int baseY, int baseZ, BlockPos rebuildPos) {
        List<TileEntityPressureChamberValve> valveList = new ArrayList<>();
        BlockPos.Mutable mPos = new BlockPos.Mutable();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    mPos = mPos.set(x + baseX, y + baseY, z + baseZ);
                    BlockState state = world.getBlockState(mPos);
                    if (x != 0 && x != size - 1 && y != 0 && y != size - 1 && z != 0 && z != size - 1) {
                        if (!world.isEmptyBlock(mPos)) {
                            return false;
                        }
                    } else if (!(state.getBlock() instanceof IBlockPressureChamber)) {
                        return false;
                    } else if (state.getBlock() instanceof BlockPressureChamberValve) {
                        // this a valve; ensure it faces the right way for the face it's in
                        boolean xMid = x != 0 && x != size - 1;
                        boolean yMid = y != 0 && y != size - 1;
                        boolean zMid = z != 0 && z != size - 1;
                        Direction facing = state.getValue(BlockStateProperties.FACING);
                        if (xMid && yMid && facing.getAxis() == Direction.Axis.Z
                                || xMid && zMid && facing.getAxis() == Direction.Axis.Y
                                || yMid && zMid && facing.getAxis() == Direction.Axis.X) {
                            TileEntity te = world.getBlockEntity(mPos);
                            if (te instanceof TileEntityPressureChamberValve) {
                                valveList.add((TileEntityPressureChamberValve) te);
                            }
                        } else {
                            return false;
                        }
                    } else {
                        // this is a wall or interface; ensure it doesn't belong to another pressure chamber
                        TileEntity te = world.getBlockEntity(mPos);
                        if (te instanceof TileEntityPressureChamberWall) {
                            TileEntity teV = ((TileEntityPressureChamberWall) te).getPrimaryValve();
                            if (teV != null && (rebuildPos == null || !rebuildPos.equals(teV.getBlockPos()))) return false;
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
        //  also preserve the pressure after multiblock formation, since base volume can change here
        float pressure = primaryValve.getPressure();
        primaryValve.setupMultiBlock(size, baseX, baseY, baseZ);
        if (primaryValve.savedMultiblockSize == primaryValve.multiBlockSize) {
            primaryValve.airHandler.setPressure(pressure);
        }
        primaryValve.savedMultiblockSize = 0;

        // note the core valve in every wall & interface, so that right-clicking & block break work as expected
        primaryValve.hasGlass = false;
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    TileEntity te = world.getBlockEntity(new BlockPos(x + baseX, y + baseY, z + baseZ));
                    if (te instanceof TileEntityPressureChamberWall) {
                        TileEntityPressureChamberWall teWall = (TileEntityPressureChamberWall) te;
                        teWall.setPrimaryValve(primaryValve);  // this also forces re-rendering with the formed texture
                        if (world.getBlockState(te.getBlockPos()).getBlock() instanceof BlockPressureChamberGlass) {
                            primaryValve.hasGlass = true;
                        }
                    } else if (te instanceof TileEntityPressureChamberValve) {
                        BlockState state = world.getBlockState(te.getBlockPos());
                        world.setBlock(te.getBlockPos(), state.setValue(BlockPressureChamberValve.FORMED, ((TileEntityPressureChamberValve) te).isPrimaryValve()), 2);
                    }
                    if (te != null) {
                        double dx = x == 0 ? -0.1 : 0.1;
                        double dz = z == 0 ? -0.1 : 0.1;
                        NetworkHandler.sendToAllTracking(
                                new PacketSpawnParticle(ParticleTypes.POOF,
                                        te.getBlockPos().getX() + 0.5, te.getBlockPos().getY() + 0.5, te.getBlockPos().getZ() + 0.5,
                                        dx, 0.3, dz, 5, 0, 0, 0),
                                te);
                    }
                }
            }
        }

        // pick up any loose items into the chamber inventory
        primaryValve.captureEntityItemsInChamber();

        // force-sync primary valve details to clients for rendering purposes
        primaryValve.scheduleDescriptionPacket();

        primaryValve.setChanged();

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
        List<ItemEntity> items = getLevel().getEntitiesOfClass(ItemEntity.class, getChamberAABB(), EntityPredicates.ENTITY_STILL_ALIVE);
        for (ItemEntity item : items) {
            ItemStack stack = item.getItem();
            ItemStack excess = ItemHandlerHelper.insertItem(itemsInChamber, stack, false);
            if (excess.isEmpty()) item.remove();
            else item.setItem(excess);
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

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerPressureChamberValve(i, playerInventory, getBlockPos());
    }

    private void rebuildRenderedItems() {
        renderedItems.clear();
        for (int i = 0; i < allItems.getSlots(); i++) {
            if (!allItems.getStackInSlot(i).isEmpty()) {
                renderedItems.add(allItems.getStackInSlot(i));
            }
        }
    }

    private class ChamberStackHandler extends ItemStackHandler {
        ChamberStackHandler() {
            super(CHAMBER_INV_SIZE);
        }

        @Override
        protected void onContentsChanged(int slot) {
            recipeRecalcNeeded = true;
            if (level != null && level.isClientSide) {
                rebuildRenderedItems();
            }
            setChanged();
        }
    }

    private class OutputStackHandler extends ItemStackHandler {
        OutputStackHandler() {
            super(OUTPUT_INV_SIZE);
        }

        @Override
        protected void onContentsChanged(int slot) {
            if (level != null && level.isClientSide) {
                rebuildRenderedItems();
            }
            setChanged();
        }
    }

    private static class ApplicableRecipe {
        final PressureChamberRecipe recipe;
        final List<Integer> slots;

        ApplicableRecipe(PressureChamberRecipe recipe, Collection<Integer> slots) {
            this.recipe = recipe;
            this.slots = ImmutableList.copyOf(slots);
        }
    }
}
