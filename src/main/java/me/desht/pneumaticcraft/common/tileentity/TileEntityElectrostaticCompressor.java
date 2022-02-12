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
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.common.PneumaticCraftTags;
import me.desht.pneumaticcraft.common.block.BlockElectrostaticCompressor;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModBlockEntities;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.ElectrostaticCompressorMenu;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.tileentity.RedstoneController.EmittingRedstoneMode;
import me.desht.pneumaticcraft.common.tileentity.RedstoneController.RedstoneMode;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.*;

public class TileEntityElectrostaticCompressor extends TileEntityPneumaticBase
        implements IRedstoneControl<TileEntityElectrostaticCompressor>, MenuProvider
{
    private static final List<RedstoneMode<TileEntityElectrostaticCompressor>> REDSTONE_MODES = ImmutableList.of(
            new EmittingRedstoneMode<>("standard.never", new ItemStack(Items.GUNPOWDER), te -> false),
            new EmittingRedstoneMode<>("electrostaticCompressor.struckByLightning", Textures.JEI_EXPLOSION, te -> te.struckByLightningCooldown > 0)
    );
    public static final int MAX_ELECTROSTATIC_GRID_SIZE = 500;
    private static final int MAX_BARS_ABOVE = 10;

    @GuiSynced
    public final RedstoneController<TileEntityElectrostaticCompressor> rsController = new RedstoneController<>(this, REDSTONE_MODES);

    private boolean lastRedstoneState;
    public int ironBarsBeneath = 0;
    public int ironBarsAbove = 0;
    private int struckByLightningCooldown; // for redstone emission purposes

    public TileEntityElectrostaticCompressor(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTROSTATIC_COMPRESSOR.get(), pos, state, PressureTier.TIER_TWO, PneumaticValues.VOLUME_ELECTROSTATIC_COMPRESSOR, 4);
    }

    @Override
    public void tickCommonPre() {
        super.tickCommonPre();

        if ((nonNullLevel().getGameTime() & 0x1f) == 0) {  // every 32 ticks
            int max = PneumaticValues.PRODUCTION_ELECTROSTATIC_COMPRESSOR / PneumaticValues.MAX_REDIRECTION_PER_IRON_BAR;
            for (ironBarsBeneath = 0; ironBarsBeneath < max; ironBarsBeneath++) {
                if (!isValidGridBlock(nonNullLevel().getBlockState(getBlockPos().below(ironBarsBeneath + 1)).getBlock())) {
                    break;
                }
            }
            for (ironBarsAbove = 0; ironBarsAbove < MAX_BARS_ABOVE; ironBarsAbove++) {
                if (!isValidGridBlock(nonNullLevel().getBlockState(getBlockPos().above(ironBarsAbove + 1)).getBlock())) {
                    break;
                }
            }
        }
    }

    @Override
    public void tickServer() {
        super.tickServer();

        maybeLightningStrike();

        if (lastRedstoneState != rsController.shouldEmit()) {
            lastRedstoneState = !lastRedstoneState;
            updateNeighbours();
        }

        struckByLightningCooldown--;
    }

    public int getStrikeChance() {
        int strikeChance = ConfigHelper.common().machines.electrostaticLightningChance.get();
        if (nonNullLevel().isRaining()) strikeChance *= 0.5;  // slightly more likely if raining
        if (nonNullLevel().isThundering()) strikeChance *= 0.2; // much more likely if thundering
        strikeChance *= (1f - (0.02f * ironBarsAbove));
        return strikeChance;
    }

    private void maybeLightningStrike() {
        Level level = nonNullLevel();
        Random rnd = level.random;
        if (rnd.nextInt(getStrikeChance()) == 0) {
            int dist = rnd.nextInt(6);
            float angle = rnd.nextFloat() * (float)Math.PI * 2;
            int x = (int)(getBlockPos().getX() + dist * Mth.sin(angle));
            int z = (int)(getBlockPos().getZ() + dist * Mth.cos(angle));
            for (int y = getBlockPos().getY() + 5; y > getBlockPos().getY() - 5; y--) {
                BlockPos hitPos = new BlockPos(x, y, z);
                BlockState state = level.getBlockState(hitPos);
                if (state.getBlock() instanceof BlockElectrostaticCompressor || state.getBlock() == Blocks.IRON_BARS) {
                    Set<BlockPos> gridSet = new ObjectOpenHashSet<>(MAX_ELECTROSTATIC_GRID_SIZE);
                    Set<TileEntityElectrostaticCompressor> compressorSet = new ObjectOpenHashSet<>(20);
                    getElectrostaticGrid(gridSet, compressorSet, hitPos);
                    LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
                    bolt.setPos(x, y, z);
                    level.addFreshEntity(bolt);
                    for (TileEntityElectrostaticCompressor compressor : compressorSet) {
                        compressor.onStruckByLightning(compressor, compressorSet.size());
                    }
                    AABB box = new AABB(getBlockPos()).inflate(16, 16, 16);
                    for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box, EntitySelector.ENTITY_STILL_ALIVE)) {
                        BlockPos pos = entity.blockPosition();
                        if (gridSet.contains(pos) || gridSet.contains(pos.below())) {
                            if (!net.minecraftforge.event.ForgeEventFactory.onEntityStruckByLightning(entity, bolt)) {
                                entity.thunderHit((ServerLevel) level, bolt);
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

    private void onStruckByLightning(TileEntityElectrostaticCompressor compressor, int divisor) {
        compressor.addAir(PneumaticValues.PRODUCTION_ELECTROSTATIC_COMPRESSOR / divisor);
        struckByLightningCooldown = 10;
        float excessPressure = compressor.getPressure() - compressor.getDangerPressure();
        if (excessPressure > 0f) {
            int maxRedirection = PneumaticValues.MAX_REDIRECTION_PER_IRON_BAR * compressor.ironBarsBeneath;
            int excessAir = (int) (excessPressure * compressor.airHandler.getVolume());
            compressor.addAir(-Math.min(maxRedirection, excessAir));
        }
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
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
        nonNullLevel().getBlockEntity(pos, ModBlockEntities.ELECTROSTATIC_COMPRESSOR.get())
                .ifPresent(compressors::add);

        while (!pendingPos.isEmpty()) {
            BlockPos checkingPos = pendingPos.pop();
            for (Direction d : DirectionUtil.VALUES) {
                BlockPos newPos = checkingPos.relative(d);
                Block block = nonNullLevel().getBlockState(newPos).getBlock();
                if ((isValidGridBlock(block) || block == ModBlocks.ELECTROSTATIC_COMPRESSOR.get())
                        && grid.size() < MAX_ELECTROSTATIC_GRID_SIZE && grid.add(newPos)) {
                    if (block == ModBlocks.ELECTROSTATIC_COMPRESSOR.get()) {
                        nonNullLevel().getBlockEntity(newPos, ModBlockEntities.ELECTROSTATIC_COMPRESSOR.get())
                                .ifPresent(compressors::add);
                    }
                    pendingPos.push(newPos);
                }
            }
        }
    }

    private static boolean isValidGridBlock(Block block) {
        return PneumaticCraftTags.Blocks.ELECTROSTATIC_GRID.contains(block);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new ElectrostaticCompressorMenu(i, playerInventory, getBlockPos());
    }

    @Override
    public RedstoneController<TileEntityElectrostaticCompressor> getRedstoneController() {
        return rsController;
    }
}
