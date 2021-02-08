package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.common.block.BlockElectrostaticCompressor;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerElectrostaticCompressor;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.tileentity.RedstoneController.EmittingRedstoneMode;
import me.desht.pneumaticcraft.common.tileentity.RedstoneController.RedstoneMode;
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
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class TileEntityElectrostaticCompressor extends TileEntityPneumaticBase
        implements IRedstoneControl<TileEntityElectrostaticCompressor>, INamedContainerProvider {

    @ObjectHolder("chisel:ironpane")
    private static Block CHISELED_BARS = null;

    private static final List<RedstoneMode<TileEntityElectrostaticCompressor>> REDSTONE_MODES = ImmutableList.of(
            new EmittingRedstoneMode<>("standard.never", new ItemStack(Items.GUNPOWDER), te -> false),
            new EmittingRedstoneMode<>("electrostaticCompressor.struckByLightning", Textures.JEI_EXPLOSION, te -> te.struckByLightningCooldown > 0)
    );
    private static final int MAX_ELECTROSTATIC_GRID_SIZE = 250;
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

        if ((getWorld().getGameTime() & 0x1f) == 0) {  // every 32 ticks
            int max = PneumaticValues.PRODUCTION_ELECTROSTATIC_COMPRESSOR / PneumaticValues.MAX_REDIRECTION_PER_IRON_BAR;
            for (ironBarsBeneath = 0; ironBarsBeneath < max; ironBarsBeneath++) {
                if (!isValidGridBlock(getWorld().getBlockState(getPos().down(ironBarsBeneath + 1)).getBlock())) {
                    break;
                }
            }
            for (ironBarsAbove = 0; ironBarsAbove < MAX_BARS_ABOVE; ironBarsAbove++) {
                if (!isValidGridBlock(getWorld().getBlockState(getPos().up(ironBarsAbove + 1)).getBlock())) {
                    break;
                }
            }
        }


        if (!getWorld().isRemote) {
            maybeLightningStrike();

            if (lastRedstoneState != rsController.shouldEmit()) {
                lastRedstoneState = !lastRedstoneState;
                updateNeighbours();
            }

            struckByLightningCooldown--;
        }
    }

    public int getStrikeChance() {
        int strikeChance = PNCConfig.Common.Machines.electrostaticLightningChance;
        if (getWorld().isRaining()) strikeChance *= 0.5;  // slightly more likely if raining
        if (getWorld().isThundering()) strikeChance *= 0.2; // much more likely if thundering
        strikeChance *= (1f - (0.02f * ironBarsAbove));
        return strikeChance;
    }

    private void maybeLightningStrike() {
        Random rnd = getWorld().rand;
        if (rnd.nextInt(getStrikeChance()) == 0) {
            int dist = rnd.nextInt(6);
            float angle = rnd.nextFloat() * (float)Math.PI * 2;
            int x = (int)(getPos().getX() + dist * MathHelper.sin(angle));
            int z = (int)(getPos().getZ() + dist * MathHelper.cos(angle));
            for (int y = getPos().getY() + 5; y > getPos().getY() - 5; y--) {
                BlockPos hitPos = new BlockPos(x, y, z);
                BlockState state = getWorld().getBlockState(hitPos);
                if (state.getBlock() instanceof BlockElectrostaticCompressor || state.getBlock() == Blocks.IRON_BARS) {
                    Set<BlockPos> posSet = new HashSet<>();
                    getElectrostaticGrid(posSet, getWorld(), hitPos, null);
                    List<TileEntityElectrostaticCompressor> compressors = posSet.stream()
                            .filter(pos -> world.getBlockState(pos).getBlock() == ModBlocks.ELECTROSTATIC_COMPRESSOR.get())
                            .map(pos -> world.getTileEntity(pos))
                            .filter(te -> te instanceof TileEntityElectrostaticCompressor)
                            .map(te -> (TileEntityElectrostaticCompressor) te)
                            .collect(Collectors.toList());
                    LightningBoltEntity bolt = new LightningBoltEntity(EntityType.LIGHTNING_BOLT, getWorld());
                    bolt.setPosition(x, y, z);
                    getWorld().addEntity(bolt);
                    for (TileEntityElectrostaticCompressor compressor : compressors) {
                        compressor.addAir(PneumaticValues.PRODUCTION_ELECTROSTATIC_COMPRESSOR / compressors.size());
                        compressor.onStruckByLightning();
                    }
                    AxisAlignedBB box = new AxisAlignedBB(getPos()).grow(16, 16, 16);
                    for (LivingEntity entity : getWorld().getEntitiesWithinAABB(LivingEntity.class, box, EntityPredicates.IS_ALIVE)) {
                        BlockPos pos = entity.getPosition();
                        if (posSet.contains(pos) || posSet.contains(pos.down())) {
                            if (!net.minecraftforge.event.ForgeEventFactory.onEntityStruckByLightning(entity, bolt)) {
                                entity.func_241841_a((ServerWorld) getWorld(), bolt);
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
     * Scan recursively, adding all connected iron bars and electrostatic compressors to the grid
     *
     * @param set
     * @param world
     * @param pos
     */
    public void getElectrostaticGrid(Set<BlockPos> set, World world, BlockPos pos, Direction dir) {
        for (Direction d : Direction.VALUES) {
            if (d == dir) continue;
            BlockPos newPos = pos.offset(d);
            Block block = world.getBlockState(newPos).getBlock();
            if ((isValidGridBlock(block) || block == ModBlocks.ELECTROSTATIC_COMPRESSOR.get())
                    && set.size() < MAX_ELECTROSTATIC_GRID_SIZE && set.add(newPos)) {
                getElectrostaticGrid(set, world, newPos, d.getOpposite());
            }
        }
    }

    private static boolean isValidGridBlock(Block block) {
        return block == Blocks.IRON_BARS || block == CHISELED_BARS;
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerElectrostaticCompressor(i, playerInventory, getPos());
    }

    @Override
    public RedstoneController<TileEntityElectrostaticCompressor> getRedstoneController() {
        return rsController;
    }
}
