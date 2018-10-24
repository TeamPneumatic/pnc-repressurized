package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.common.block.BlockElectrostaticCompressor;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class TileEntityElectrostaticCompressor extends TileEntityPneumaticBase implements IRedstoneControl {

    @GameRegistry.ObjectHolder("chisel:ironpane")
    private static final Block CHISELED_BARS = null;

    private static final List<String> REDSTONE_LABELS = ImmutableList.of(
            "gui.tab.redstoneBehaviour.button.never",
            "gui.tab.redstoneBehaviour.electrostaticCompressor.button.struckByLightning"
    );
    private static final int MAX_ELECTROSTATIC_GRID_SIZE = 250;
    private static final int MAX_BARS_ABOVE = 8;

    private boolean lastRedstoneState;
    @GuiSynced
    public int redstoneMode = 0;
    public int ironBarsBeneath = 0;
    public int ironBarsAbove = 0;
    private int struckByLightningCooldown; //used by the redstone.

    public TileEntityElectrostaticCompressor() {
        super(PneumaticValues.DANGER_PRESSURE_ELECTROSTATIC_COMPRESSOR, PneumaticValues.MAX_PRESSURE_ELECTROSTATIC_COMPRESSOR, PneumaticValues.VOLUME_ELECTROSTATIC_COMPRESSOR, 4);
    }

    @Override
    public void update() {
        if ((getWorld().getTotalWorldTime() & 0x1f) == 0) {  // every 32 ticks
            int max = PneumaticValues.PRODUCTION_ELECTROSTATIC_COMPRESSOR / PneumaticValues.MAX_REDIRECTION_PER_IRON_BAR;
            for (ironBarsBeneath = 0; ironBarsBeneath < max; ironBarsBeneath++) {
                if (getWorld().getBlockState(getPos().offset(EnumFacing.DOWN, ironBarsBeneath + 1)).getBlock() != Blocks.IRON_BARS) {
                    break;
                }
            }
            for (ironBarsAbove = 0; ironBarsAbove < MAX_BARS_ABOVE; ironBarsAbove++) {
                if (getWorld().getBlockState(getPos().offset(EnumFacing.UP, ironBarsAbove + 1)).getBlock() != Blocks.IRON_BARS) {
                    break;
                }
            }
        }

        super.update();

        maybeLightningStrike();

        if (!getWorld().isRemote) {
            if (lastRedstoneState != shouldEmitRedstone()) {
                lastRedstoneState = !lastRedstoneState;
                updateNeighbours();
            }
            struckByLightningCooldown--;
        }
    }

    public int getStrikeChance() {
        int strikeChance = ConfigHandler.machineProperties.electrostaticLightningChance;
        if (getWorld().isRaining()) strikeChance *= 0.5;  // slightly more likely if raining
        if (getWorld().isThundering()) strikeChance *= 0.2; // much more likely if thundering
        strikeChance *= (1f - (0.02f * ironBarsAbove));
        return strikeChance;
    }

    private void maybeLightningStrike() {
        Random rnd = getWorld().rand;
        if (rnd.nextInt(getStrikeChance()) == 0) {
            int dist = rnd.nextInt(6);
            float angle = rnd.nextFloat() * (float)Math.PI;
            int x = (int)(getPos().getX() + dist * MathHelper.sin(angle));
            int z = (int)(getPos().getZ() + dist * MathHelper.cos(angle));
            for (int y = getPos().getY() + 5; y > getPos().getY() - 5; y--) {
                BlockPos hitPos = new BlockPos(x, y, z);
                IBlockState state = getWorld().getBlockState(hitPos);
                if (state.getBlock() instanceof BlockElectrostaticCompressor || state.getBlock() == Blocks.IRON_BARS) {
                    Set<BlockPos> posSet = new HashSet<>();
                    getElectrostaticGrid(posSet, getWorld(), hitPos, null);
                    List<TileEntityElectrostaticCompressor> compressors = posSet.stream()
                            .filter(pos -> world.getBlockState(pos).getBlock() == Blockss.ELECTROSTATIC_COMPRESSOR)
                            .map(pos -> world.getTileEntity(pos))
                            .filter(te -> te instanceof TileEntityElectrostaticCompressor)
                            .map(te -> (TileEntityElectrostaticCompressor) te)
                            .collect(Collectors.toList());
                    EntityLightningBolt bolt = new EntityLightningBolt(getWorld(), x, y, z, true);
                    getWorld().spawnEntity(bolt);
                    for (TileEntityElectrostaticCompressor compressor : compressors) {
                        compressor.addAir(PneumaticValues.PRODUCTION_ELECTROSTATIC_COMPRESSOR / compressors.size());
                        compressor.onStruckByLightning();
                    }
                    AxisAlignedBB box = new AxisAlignedBB(getPos()).grow(16, 16, 16);
                    for (EntityLivingBase entity : getWorld().getEntitiesWithinAABB(EntityLivingBase.class, box, EntitySelectors.IS_ALIVE)) {
                        if (posSet.contains(entity.getPosition()) || posSet.contains(entity.getPosition().down())) {
                            if (!net.minecraftforge.event.ForgeEventFactory.onEntityStruckByLightning(entity, bolt)) {
                                entity.onStruckByLightning(bolt);
                            }
                        }
                    }

                    break;
                }
            }
        }
    }

    @Override
    public boolean isConnectedTo(EnumFacing dir) {
        return dir != EnumFacing.UP;
    }

    private boolean shouldEmitRedstone() {
        switch (redstoneMode) {
            case 0:
                return false;
            case 1:
                return struckByLightningCooldown > 0;
        }
        return false;
    }

    public void onStruckByLightning() {
        struckByLightningCooldown = 10;
        if (getPressure() > PneumaticValues.DANGER_PRESSURE_ELECTROSTATIC_COMPRESSOR) {
            int maxRedirection = PneumaticValues.MAX_REDIRECTION_PER_IRON_BAR * ironBarsBeneath;
            int tooMuchAir = (int) ((getPressure() - PneumaticValues.DANGER_PRESSURE_ELECTROSTATIC_COMPRESSOR) * getAirHandler(null).getVolume());
            addAir(-Math.min(maxRedirection, tooMuchAir));
        }
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player) {
        if (buttonID == 0) {
            redstoneMode++;
            if (redstoneMode > 1) redstoneMode = 0;
        }
    }

    @Override
    public String getName() {
        return Blockss.ELECTROSTATIC_COMPRESSOR.getTranslationKey();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        super.readFromNBT(nbtTagCompound);
        redstoneMode = nbtTagCompound.getInteger("redstoneMode");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound) {
        super.writeToNBT(nbtTagCompound);
        nbtTagCompound.setInteger("redstoneMode", redstoneMode);
        return nbtTagCompound;
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }

    @Override
    protected List<String> getRedstoneButtonLabels() {
        return REDSTONE_LABELS;
    }

    /**
     * Scan recursively, adding all connected iron bars and electrostatic compressors to the grid
     *
     * @param set
     * @param world
     * @param pos
     */
    public void getElectrostaticGrid(Set<BlockPos> set, World world, BlockPos pos, EnumFacing dir) {
        for (EnumFacing d : EnumFacing.VALUES) {
            if (d == dir) continue;
            BlockPos newPos = pos.offset(d);
            Block block = world.getBlockState(newPos).getBlock();
            if ((block == Blocks.IRON_BARS || block == Blockss.ELECTROSTATIC_COMPRESSOR || block == CHISELED_BARS)
                    && set.size() < MAX_ELECTROSTATIC_GRID_SIZE && set.add(newPos)) {
                getElectrostaticGrid(set, world, newPos, d.getOpposite());
            }
        }
    }
}
