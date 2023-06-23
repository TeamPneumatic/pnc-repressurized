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

package me.desht.pneumaticcraft.common.block.entity;

import com.google.common.math.IntMath;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import me.desht.pneumaticcraft.api.block.PNCBlockStateProperties;
import me.desht.pneumaticcraft.api.crafting.recipe.PressureChamberRecipe;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.api.tileentity.IAirListener;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.PNCDamageSource;
import me.desht.pneumaticcraft.common.block.IBlockPressureChamber;
import me.desht.pneumaticcraft.common.block.PressureChamberGlassBlock;
import me.desht.pneumaticcraft.common.block.PressureChamberValveBlock;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModBlockEntities;
import me.desht.pneumaticcraft.common.core.ModRecipeTypes;
import me.desht.pneumaticcraft.common.inventory.PressureChamberValveMenu;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import me.desht.pneumaticcraft.common.particle.AirParticleData;
import me.desht.pneumaticcraft.common.util.CountedItemStacks;
import me.desht.pneumaticcraft.common.util.ItemStackHandlerIterable;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.SizeLimitedItemHandlerWrapper;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class PressureChamberValveBlockEntity extends AbstractAirHandlingBlockEntity
        implements IMinWorkingPressure, IAirListener, MenuProvider {
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

    public List<PressureChamberValveBlockEntity> accessoryValves;
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

    public PressureChamberValveBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PRESSURE_CHAMBER_VALVE.get(), pos, state, PressureTier.TIER_ONE, PneumaticValues.VOLUME_PRESSURE_CHAMBER_PER_EMPTY, 4);
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
            for (PressureChamberValveBlockEntity valve : accessoryValves) {
                if (valve != this) {
                    airHandlers.add(valve.airHandler);
                }
            }
        }
        return airHandlers;
    }

    @Override
    public void tickClient() {
        super.tickClient();

        if (hasGlass && isPrimaryValve() && ConfigHelper.client().general.pressureChamberParticles.get() && roundedPressure > 0.2D) {
            if (ClientUtils.getClientPlayer().distanceToSqr(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ()) < 256) {
                Level level = nonNullLevel();
                for (int i = 0; i < nParticles; i++) {
                    double posX = multiBlockX + 1D + level.random.nextDouble() * (multiBlockSize - 2D);
                    double posY = multiBlockY + 1.5D + level.random.nextDouble() * (multiBlockSize - 2.5D);
                    double posZ = multiBlockZ + 1D + level.random.nextDouble() * (multiBlockSize - 2D);
                    level.addParticle(AirParticleData.DENSE, posX, posY, posZ, 0, 0, 0);
                }
            }
        }
    }

    @Override
    public void tickServer() {
        super.tickServer();

        checkForAirLeak();

        if (multiBlockSize != 0) {
            roundedPressure = ((int) (getPressure() * 4.0f)) / 4.0f;
            checkForRecipeRecalc();
            processApplicableRecipes();
            if (getPressure() > PneumaticValues.MAX_PRESSURE_LIVING_ENTITY) {
                handleEntitiesInChamber();
            }
        }
    }

    private void checkForRecipeRecalc() {
        if (recipeRecalcNeeded) {
            isValidRecipeInChamber = false;
            isSufficientPressureInChamber = false;
            recipePressure = Float.MAX_VALUE;
            applicableRecipes.clear();
            final SizeLimitedItemHandlerWrapper h = new SizeLimitedItemHandlerWrapper(itemsInChamber);
            if (h.getSlots() > 0) {
                ModRecipeTypes.PRESSURE_CHAMBER.get().stream(level).forEach(recipe -> {
                    IntCollection slots = recipe.findIngredients(h);
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
    public void onLoad() {
        super.onLoad();

        doPostNBTSetup();
    }

    /**
     * Initialize the accessory valve list from saved NBT data.
     * This can't be done in readFromNBT() because there may be multiple valve BE's in the multiblock,
     * and all of them need to be fully initialized before this is run.
     */
    private void doPostNBTSetup() {
        if (!nbtValveList.isEmpty()) {
            BlockState state = nonNullLevel().getBlockState(getBlockPos());
            if (state.getBlock() instanceof PressureChamberValveBlock)
                nonNullLevel().setBlock(getBlockPos(), state.setValue(PNCBlockStateProperties.FORMED, isPrimaryValve()), 2);

            accessoryValves.clear();
            for (BlockPos valve : nbtValveList) {
                BlockEntity te = nonNullLevel().getBlockEntity(valve);
                if (te instanceof PressureChamberValveBlockEntity) {
                    accessoryValves.add((PressureChamberValveBlockEntity) te);
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
            case X -> {
                disconnected.set(Direction.WEST.get3DDataValue());
                disconnected.set(Direction.EAST.get3DDataValue());
            }
            case Y -> {
                disconnected.set(Direction.UP.get3DDataValue());
                disconnected.set(Direction.DOWN.get3DDataValue());
            }
            case Z -> {
                disconnected.set(Direction.NORTH.get3DDataValue());
                disconnected.set(Direction.SOUTH.get3DDataValue());
            }
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
        PressureChamberValveBlockEntity primaryValve = accessoryValves.isEmpty() ? null : accessoryValves.get(accessoryValves.size() - 1);
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
                    if (nonNullLevel().getGameTime() - lastSoundTick > 5) {
                        nonNullLevel().playSound(null, getBlockPos(), SoundEvents.CHICKEN_EGG, SoundSource.BLOCKS, 0.7f, 0.8f);
                        lastSoundTick = nonNullLevel().getGameTime();
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
        AABB bbBox = new AABB(multiBlockX + 1, multiBlockY + 1, multiBlockZ + 1, multiBlockX + multiBlockSize - 1, multiBlockY + multiBlockSize - 1, multiBlockZ + multiBlockSize - 1);
        List<LivingEntity> entities = nonNullLevel().getEntitiesOfClass(LivingEntity.class, bbBox);
        for (LivingEntity entity : entities) {
            // Note: villager conversion is no longer a thing, since due to new 1.14+ villager mechanics,
            // the converted villager will just lose his progression. Instead, just place down a
            // charging station, and an unemployed villager will claim it.
            entity.hurt(PNCDamageSource.pressure(getLevel()), (int) (getPressure() * 2D));
        }
    }

    private boolean checkForGlass() {
        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();
        for (int x = 0; x < multiBlockSize; x++) {
            for (int y = 0; y < multiBlockSize; y++) {
                for (int z = 0; z < multiBlockSize; z++) {
                    mPos = mPos.set(multiBlockX + x, multiBlockY + y, multiBlockZ + z);
                    if (nonNullLevel().getBlockState(mPos).getBlock() instanceof PressureChamberGlassBlock) {
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
    public void load(CompoundTag tag) {
        super.load(tag);

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
        ListTag accList = tag.getList("Valves", Tag.TAG_COMPOUND);
        nbtValveList.clear();
        for (int i = 0; i < accList.size(); ++i) {
            CompoundTag tagCompound = accList.getCompound(i);
            nbtValveList.add(NbtUtils.readBlockPos(tagCompound));
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("multiBlockX", multiBlockX);
        tag.putInt("multiBlockY", multiBlockY);
        tag.putInt("multiBlockZ", multiBlockZ);
        tag.putInt("multiBlockSize", multiBlockSize);
        tag.put("itemsInChamber", itemsInChamber.serializeNBT());
        tag.put("craftedItems", craftedItems.serializeNBT());

        // Write the accessory valve list to NBT
        ListTag accList = accessoryValves.stream()
                .map(valve -> NbtUtils.writeBlockPos(valve.getBlockPos()))
                .collect(Collectors.toCollection(ListTag::new));
        tag.put("Valves", accList);
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
                    BlockEntity te = nonNullLevel().getBlockEntity(new BlockPos(x + multiBlockX, y + multiBlockY, z + multiBlockZ));
                    if (te instanceof PressureChamberWallBlockEntity teWall) {
                        // Clear the base BE's, so that the walls can be used in a new MultiBlock
                        teWall.setPrimaryValve(null);
                    }
                }
            }
        }
        if (accessoryValves != null) {
            for (PressureChamberValveBlockEntity valve : accessoryValves) {
                valve.savedMultiblockSize = valve.multiBlockSize;
                // keep the valve pressure constant after multi-block break (4x4x4 or 5x5x5 will cause base volume to drop)
                float pressure = valve.getPressure();
                valve.setupMultiBlock(0, 0, 0, 0);
                valve.airHandler.setPressure(pressure);
                if (valve != this) {
                    valve.accessoryValves.clear();
                    valve.sendDescriptionPacket();
                }
                valve.setChanged();
            }
            accessoryValves.clear();
        }
        sendDescriptionPacket();
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

    public static boolean checkIfProperlyFormed(Level world, BlockPos pos) {
        return checkIfProperlyFormed(world, pos, false);
    }

    public static boolean checkIfProperlyFormed(Level world, BlockPos pos, boolean forceRebuild) {
        for (int i = 3; i < 6; i++) {
            if (checkForShiftedCubeOfSize(i, world, pos, forceRebuild)) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkForShiftedCubeOfSize(int size, Level world, BlockPos pos, boolean forceRebuild) {
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

    private static boolean checkForCubeOfSize(int size, Level world, int baseX, int baseY, int baseZ, BlockPos rebuildPos) {
        List<PressureChamberValveBlockEntity> valveList = new ArrayList<>();
        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();
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
                    } else if (state.getBlock() instanceof PressureChamberValveBlock) {
                        // this a valve; ensure it faces the right way for the face it's in
                        boolean xMid = x != 0 && x != size - 1;
                        boolean yMid = y != 0 && y != size - 1;
                        boolean zMid = z != 0 && z != size - 1;
                        Direction facing = state.getValue(BlockStateProperties.FACING);
                        if (xMid && yMid && facing.getAxis() == Direction.Axis.Z
                                || xMid && zMid && facing.getAxis() == Direction.Axis.Y
                                || yMid && zMid && facing.getAxis() == Direction.Axis.X) {
                            BlockEntity te = world.getBlockEntity(mPos);
                            if (te instanceof PressureChamberValveBlockEntity) {
                                valveList.add((PressureChamberValveBlockEntity) te);
                            }
                        } else {
                            return false;
                        }
                    } else {
                        // this is a wall or interface; ensure it doesn't belong to another pressure chamber
                        BlockEntity te = world.getBlockEntity(mPos);
                        if (te instanceof PressureChamberWallBlockEntity) {
                            BlockEntity teV = ((PressureChamberWallBlockEntity) te).getPrimaryValve();
                            if (teV != null && (rebuildPos == null || !rebuildPos.equals(teV.getBlockPos()))) return false;
                        }
                    }
                }
            }
        }

        // So the structure is valid; just check that we have at least one valid valve
        if (valveList.isEmpty()) return false;

        // primary valve is the last one scanned (which will be @ max X/Y/Z)
        PressureChamberValveBlockEntity primaryValve = valveList.get(valveList.size() - 1);

        // every valve in the structure has a list of every valve, including itself
        valveList.forEach(valve -> valve.accessoryValves = new ArrayList<>(valveList));

        // coalesce any upgrades out of non-primary valves and into the primary valve
        // this might be necessary if the pressure chamber is being rebuilt with more valves
        if (valveList.size() > 1) maybeMoveUpgrades(valveList, primaryValve);

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
                    BlockEntity be = world.getBlockEntity(new BlockPos(x + baseX, y + baseY, z + baseZ));
                    if (be instanceof PressureChamberWallBlockEntity wall) {
                        wall.setPrimaryValve(primaryValve);  // this also forces re-rendering with the formed texture
                        if (world.getBlockState(be.getBlockPos()).getBlock() instanceof PressureChamberGlassBlock) {
                            primaryValve.hasGlass = true;
                        }
                    } else if (be instanceof PressureChamberValveBlockEntity v) {
                        BlockState state = world.getBlockState(be.getBlockPos());
                        world.setBlock(be.getBlockPos(), state.setValue(PNCBlockStateProperties.FORMED, v.isPrimaryValve()), Block.UPDATE_CLIENTS);
                    }
                    if (be != null) {
                        double dx = x == 0 ? -0.1 : 0.1;
                        double dz = z == 0 ? -0.1 : 0.1;
                        NetworkHandler.sendToAllTracking(
                                new PacketSpawnParticle(ParticleTypes.POOF,
                                        be.getBlockPos().getX() + 0.5, be.getBlockPos().getY() + 0.5, be.getBlockPos().getZ() + 0.5,
                                        dx, 0.3, dz, 5, 0, 0, 0),
                                be);
                    }
                }
            }
        }

        // pick up any loose items into the chamber inventory
        primaryValve.captureEntityItemsInChamber();

        // force-sync primary valve details to clients for rendering purposes
        primaryValve.scheduleDescriptionPacket();

        valveList.forEach(AbstractPneumaticCraftBlockEntity::setChanged);

        return true;
    }

    private static void maybeMoveUpgrades(List<PressureChamberValveBlockEntity> valveList, PressureChamberValveBlockEntity primaryValve) {
        var primaryUpgradeHandler = primaryValve.getUpgradeHandler();
        for (var valve : valveList) {
            if (valve != primaryValve) {
                var upgradeHandler = valve.getUpgradeHandler();
                for (int i = 0; i < upgradeHandler.getSlots(); i++) {
                    ItemStack stack = upgradeHandler.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        ItemStack excess = ItemHandlerHelper.insertItemStacked(primaryUpgradeHandler, stack, false);
                        upgradeHandler.setStackInSlot(i, excess);
                    }
                }
            }
        }
    }

    private boolean isPrimaryValve() {
        return multiBlockSize > 0;
    }

    private AABB getChamberAABB() {
        return new AABB(multiBlockX, multiBlockY, multiBlockZ,
                multiBlockX + multiBlockSize, multiBlockY + multiBlockSize, multiBlockZ + multiBlockSize);
    }
    
    private void captureEntityItemsInChamber() {
        List<ItemEntity> items = nonNullLevel().getEntitiesOfClass(ItemEntity.class, getChamberAABB(), EntitySelector.ENTITY_STILL_ALIVE);
        for (ItemEntity item : items) {
            ItemStack stack = item.getItem();
            ItemStack excess = ItemHandlerHelper.insertItem(itemsInChamber, stack, false);
            if (excess.isEmpty()) item.discard();
            else item.setItem(excess);
        }
    }

    @Override
    public AABB getRenderBoundingBox(){
        return getChamberAABB();
    }

    @Override
    public float getMinWorkingPressure() {
        return isValidRecipeInChamber ? recipePressure : -Float.MAX_VALUE;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new PressureChamberValveMenu(i, playerInventory, getBlockPos());
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
        final IntList slots;

        ApplicableRecipe(PressureChamberRecipe recipe, IntCollection slots) {
            this.recipe = recipe;
            this.slots = new IntArrayList(slots);
        }
    }
}
