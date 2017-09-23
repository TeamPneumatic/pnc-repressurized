package me.desht.pneumaticcraft.common.worldgen;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkGeneratorFlat;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

public class WorldGeneratorPneumaticCraft implements IWorldGenerator {

    public WorldGeneratorPneumaticCraft() {
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (!(chunkGenerator instanceof ChunkGeneratorFlat)) { //don't generate on flatworlds
            switch (world.provider.getDimension()) {
                case 0:
                    generateSurface(world, random, chunkX * 16, chunkZ * 16);
                    break;
                case -1:
                    generateNether(world, random, chunkX * 16, chunkZ * 16);
                    break;
                case 1:
                    generateEnd(world, random, chunkX * 16, chunkZ * 16);
                    break;
                default:
                    generateSurface(world, random, chunkX * 16, chunkZ * 16);
            }
        }
    }

    public void generateSurface(World world, Random rand, int chunkX, int chunkZ) {
        if (rand.nextDouble() < ConfigHandler.general.oilGenerationChance / 100D) {
            PneumaticCraftRepressurized.instance.validateFluids(null);
            int y = rand.nextInt(rand.nextInt(128) + 8);
            new WorldGenLakes(FluidRegistry.getFluid(Fluids.OIL.getName()).getBlock()).generate(world, rand, new BlockPos(chunkX + 8, y, chunkZ + 8));
        }

    }

    public void generateNether(World world, Random rand, int chunkX, int chunkZ) {

    }

    public void generateEnd(World world, Random rand, int chunkX, int chunkZ) {

    }

}
