package pneumaticCraft.common.worldgen;

import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderFlat;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraftforge.fluids.FluidRegistry;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.block.pneumaticPlants.BlockPneumaticPlantBase;
import pneumaticCraft.common.config.Config;
import pneumaticCraft.common.fluid.Fluids;
import pneumaticCraft.common.item.ItemPlasticPlants;
import cpw.mods.fml.common.IWorldGenerator;

public class WorldGeneratorPneumaticCraft implements IWorldGenerator{

    public WorldGeneratorPneumaticCraft(){}

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider){
        if(!(chunkGenerator instanceof ChunkProviderFlat)) { //don't generate on flatworlds
            switch(world.provider.dimensionId){
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

    public void generateSurface(World world, Random rand, int chunkX, int chunkZ){
        if(rand.nextDouble() < Config.oilGenerationChance / 100D) {
            int y = rand.nextInt(rand.nextInt(128) + 8);
            new WorldGenLakes(FluidRegistry.getFluid(Fluids.oil.getName()).getBlock()).generate(world, rand, chunkX + 8, y, chunkZ + 8);
        }

        for(int j = 0; j < 16; j++) {
            if(rand.nextDouble() < Config.configPlantGenerationChance[j] && j != ItemPlasticPlants.HELIUM_PLANT_DAMAGE && j != ItemPlasticPlants.FIRE_FLOWER_DAMAGE) {
                int plantsInGroup = 7 + rand.nextInt(8); //beteen 7 and 14 plants per group.
                for(int i = 0; i < plantsInGroup; i++) {
                    int x = chunkX + rand.nextInt(20);//in an area of 20x20
                    int z = chunkZ + rand.nextInt(20);
                    int y = world.getHeightValue(x, z);
                    if(y > 0 && ((BlockPneumaticPlantBase)ItemPlasticPlants.getPlantBlockIDFromSeed(j)).canPlantGrowOnThisBlock(world.getBlock(x, y - 1, z), world, x, y - 1, z)) {
                        world.setBlock(x, y, z, ItemPlasticPlants.getPlantBlockIDFromSeed(j), rand.nextInt(5), 2);
                    }
                }
            }
        }
    }

    public void generateNether(World world, Random rand, int chunkX, int chunkZ){
        if(rand.nextDouble() < Config.configPlantGenerationChance[ItemPlasticPlants.HELIUM_PLANT_DAMAGE]) {
            if(rand.nextInt(10) == 0) {//Each chunks has a 1/10 chance to spawn a group of Helium plants.
                int plantsInGroup = 7 + rand.nextInt(8); //beteen 7 and 14 plants per group.
                for(int i = 0; i < plantsInGroup; i++) {
                    int x = chunkX + rand.nextInt(20);//in an area of 20x20
                    int z = chunkZ + rand.nextInt(20);
                    int y = getNetherRoof(world, x, z);
                    if(y > 0) {
                        world.setBlock(x, y, z, Blockss.heliumPlant, rand.nextInt(5), 2);
                    }
                }
            }
        }
        if(rand.nextDouble() < Config.configPlantGenerationChance[ItemPlasticPlants.FIRE_FLOWER_DAMAGE]) {
            if(rand.nextInt(10) == 0) {//Each chunks has a 1/10 chance to spawn a group of Fire Flowers.
                int plantsInGroup = 7 + rand.nextInt(8); //beteen 7 and 14 plants per group.
                for(int i = 0; i < plantsInGroup; i++) {
                    int x = chunkX + rand.nextInt(20);//in an area of 20x20
                    int z = chunkZ + rand.nextInt(20);
                    int baseY = 0;
                    for(int j = 0; j < 64; j++) {
                        int y;
                        if(baseY == 0) {
                            y = rand.nextInt(127);
                        } else {
                            y = baseY - 4 + rand.nextInt(8);
                        }
                        if(world.isAirBlock(x, y, z) && ((BlockPneumaticPlantBase)Blockss.fireFlower).canPlantGrowOnThisBlock(world.getBlock(x, y - 1, z), world, x, y - 1, z)) {
                            world.setBlock(x, y, z, Blockss.fireFlower, rand.nextInt(5), 2);
                            if(baseY == 0) baseY = y;
                            break;
                        }
                    }
                }
            }
        }
    }

    public void generateEnd(World world, Random rand, int chunkX, int chunkZ){

    }

    private int getNetherRoof(World world, int x, int z){
        int y = 127;
        boolean lastYWasNetherrack = false;
        while(y > 0) {
            y--;
            if(world.isAirBlock(x, y, z) && lastYWasNetherrack) return y;
            lastYWasNetherrack = world.getBlock(x, y, z) == Blocks.netherrack;
        }
        return 0;
    }
}
