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
import me.desht.pneumaticcraft.common.block.BlockElectrostaticCompressor;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerElectrostaticCompressor;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.tileentity.RedstoneController.EmittingRedstoneMode;
import me.desht.pneumaticcraft.common.tileentity.RedstoneController.RedstoneMode;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;
import java.util.*;

public class TileEntityElectrostaticCompressor extends TileEntityPneumaticBase
        implements IRedstoneControl<TileEntityElectrostaticCompressor>, INamedContainerProvider {

    @SuppressWarnings("FieldMayBeFinal")
    @ObjectHolder("chisel:ironpane")
    private static Block CHISELED_BARS = null;

    private static final List<RedstoneMode<TileEntityElectrostaticCompressor>> REDSTONE_MODES = ImmutableList.of(
            new EmittingRedstoneMode<>("standard.never", new ItemStack(Items.GUNPOWDER), te -> false),
            new EmittingRedstoneMode<>("electrostaticCompressor.struckByLightning", Textures.JEI_EXPLOSION, te -> te.struckByLightningCooldown > 0)
    );
    private static final int MAX_ELECTROSTATIC_GRID_SIZE = 500;
    private static final int MAX_BARS_ABOVE = 10;

    @GuiSynced
    public final RedstoneController<TileEntityElectrostaticCompressor> rsController = new RedstoneController<>(this, REDSTONE_MODES);

    private boolean lastRedstoneState;
    public int ironBarsBeneath = 0;
    public int ironBarsAbove = 0;
    private int struckByLightningCooldown; // for redstone emission purposes

    public TileEntityElectrostaticCompressor() {
        super(ModTileEntities.ELECTROSTATIC_COMPRESSOR.get(), PneumaticValues.DANGER_PRESSURE_ELECTROSTATIC_COMPRESSOR, PneumaticValues.MAX_PRESSURE_ELECTROSTATIC_COMPRESSOR, PneumaticValues.VOLUME_ELECTROSTATIC_COMPRESSOR, 4);
    }

    @Override
    public void tick() {
        super.tick();

        if ((getLevel().getGameTime() & 0x1f) == 0) {  // every 32 ticks
            int max = PneumaticValues.PRODUCTION_ELECTROSTATIC_COMPRESSOR / PneumaticValues.MAX_REDIRECTION_PER_IRON_BAR;
            for (ironBarsBeneath = 0; ironBarsBeneath < max; ironBarsBeneath++) {
                if (!isValidGridBlock(getLevel().getBlockState(getBlockPos().below(ironBarsBeneath + 1)).getBlock())) {
                    break;
                }
            }
            for (ironBarsAbove = 0; ironBarsAbove < MAX_BARS_ABOVE; ironBarsAbove++) {
                if (!isValidGridBlock(getLevel().getBlockState(getBlockPos().above(ironBarsAbove + 1)).getBlock())) {
                    break;
                }
            }
        }


        if (!getLevel().isClientSide) {
            maybeLightningStrike();

            if (lastRedstoneState != rsController.shouldEmit()) {
                lastRedstoneState = !lastRedstoneState;
                updateNeighbours();
            }

            struckByLightningCooldown--;
        }
    }

    public int getStrikeChance() {
        int strikeChance = ConfigHelper.common().machines.electrostaticLightningChance.get();
        if (getLevel().isRaining()) strikeChance *= 0.5;  // slightly more likely if raining
        if (getLevel().isThundering()) strikeChance *= 0.2; // much more likely if thundering
        strikeChance *= (1f - (0.02f * ironBarsAbove));
        return strikeChance;
    }

    private void maybeLightningStrike() {
        Random rnd = getLevel().random;
        if (rnd.nextInt(getStrikeChance()) == 0) {
            int dist = rnd.nextInt(6);
            float angle = rnd.nextFloat() * (float)Math.PI * 2;
            int x = (int)(getBlockPos().getX() + dist * MathHelper.sin(angle));
            int z = (int)(getBlockPos().getZ() + dist * MathHelper.cos(angle));
            for (int y = getBlockPos().getY() + 5; y > getBlockPos().getY() - 5; y--) {
                BlockPos hitPos = new BlockPos(x, y, z);
                BlockState state = getLevel().getBlockState(hitPos);
                if (state.getBlock() instanceof BlockElectrostaticCompressor || state.getBlock() == Blocks.IRON_BARS) {
                    Set<BlockPos> gridSet = new HashSet<>();
                    Set<TileEntityElectrostaticCompressor> compressorSet = new HashSet<>();
                    getElectrostaticGrid(gridSet, compressorSet, hitPos);
                    LightningBoltEntity bolt = new LightningBoltEntity(EntityType.LIGHTNING_BOLT, getLevel());
                    bolt.setPos(x, y, z);
                    getLevel().addFreshEntity(bolt);
                    for (TileEntityElectrostaticCompressor compressor : compressorSet) {
                        compressor.addAir(PneumaticValues.PRODUCTION_ELECTROSTATIC_COMPRESSOR / compressorSet.size());
                        compressor.onStruckByLightning();
                    }
                    AxisAlignedBB box = new AxisAlignedBB(getBlockPos()).inflate(16, 16, 16);
                    for (LivingEntity entity : getLevel().getEntitiesOfClass(LivingEntity.class, box, EntityPredicates.ENTITY_STILL_ALIVE)) {
                        BlockPos pos = entity.blockPosition();
                        if (gridSet.contains(pos) || gridSet.contains(pos.below())) {
                            if (!net.minecraftforge.event.ForgeEventFactory.onEntityStruckByLightning(entity, bolt)) {
                                entity.thunderHit((ServerWorld) getLevel(), bolt);
                            }
                        }
                    }

                    break;
                }
            }
        }
    }

    @Override
    public boolean canConnectPneumatic(Direction dir) {
        return dir != Direction.UP;
    }

    public void onStruckByLightning() {
        struckByLightningCooldown = 10;
        if (getPressure() > PneumaticValues.DANGER_PRESSURE_ELECTROSTATIC_COMPRESSOR) {
            int maxRedirection = PneumaticValues.MAX_REDIRECTION_PER_IRON_BAR * ironBarsBeneath;
            int tooMuchAir = (int) ((getPressure() - PneumaticValues.DANGER_PRESSURE_ELECTROSTATIC_COMPRESSOR) * airHandler.getVolume());
            addAir(-Math.min(maxRedirection, tooMuchAir));
        }
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayerEntity player) {
        rsController.parseRedstoneMode(tag);
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return null;
    }

    /**
     * Scan surrounding blocks, adding all connected iron bars and electrostatic compressors to the grid
     *
     * @param grid set of all block positions (compressors and grid)
     * @param compressors set of all compressor positions
     * @param pos the position to start searching from
     */
    public void getElectrostaticGrid(Set<BlockPos> grid, Set<TileEntityElectrostaticCompressor> compressors, BlockPos pos) {
        Deque<BlockPos> pendingPos = new ArrayDeque<>(Collections.singleton(pos));
        grid.add(pos);
        PneumaticCraftUtils.getTileEntityAt(level, pos, TileEntityElectrostaticCompressor.class).ifPresent(compressors::add);

        while (!pendingPos.isEmpty()) {
            BlockPos checkingPos = pendingPos.pop();
            for (Direction d : DirectionUtil.VALUES) {
                BlockPos newPos = checkingPos.relative(d);
                Block block = level.getBlockState(newPos).getBlock();
                if ((isValidGridBlock(block) || block == ModBlocks.ELECTROSTATIC_COMPRESSOR.get())
                        && grid.size() < MAX_ELECTROSTATIC_GRID_SIZE && grid.add(newPos)) {
                    if (block == ModBlocks.ELECTROSTATIC_COMPRESSOR.get()) {
                        PneumaticCraftUtils.getTileEntityAt(level, newPos, TileEntityElectrostaticCompressor.class).ifPresent(compressors::add);
                    }
                    pendingPos.push(newPos);
                }
            }
        }
    }

    private static boolean isValidGridBlock(Block block) {
        return block == Blocks.IRON_BARS || block == CHISELED_BARS;
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerElectrostaticCompressor(i, playerInventory, getBlockPos());
    }

    @Override
    public RedstoneController<TileEntityElectrostaticCompressor> getRedstoneController() {
        return rsController;
    }
}
