package pneumaticCraft.common.thirdparty.bloodmagic;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.thirdparty.IThirdParty;
import WayofTime.alchemicalWizardry.api.harvest.HarvestRegistry;
import WayofTime.alchemicalWizardry.api.harvest.IHarvestHandler;

public class BloodMagic implements IThirdParty{

    @Override
    public void preInit(){
        HarvestRegistry.registerHarvestHandler(new IHarvestHandler(){
            @Override
            public boolean harvestAndPlant(World world, int xCoord, int yCoord, int zCoord, Block block, int meta){
                List<ItemStack> seeds = new ArrayList<ItemStack>();
                ((ItemPlasticPlants)Itemss.plasticPlant).addSubItems(seeds);
                for(ItemStack seed : seeds) {
                    Block plantBlock = ItemPlasticPlants.getPlantBlockIDFromSeed(seed.getItemDamage());
                    if(block == plantBlock && (meta == 6 || meta == 13)) {
                        if(!world.isRemote) {
                            List<ItemStack> drops = block.getDrops(world, xCoord, yCoord, zCoord, meta, 0);
                            if(drops.size() == 2) {
                                world.spawnEntityInWorld(new EntityItem(world, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, drops.get(1)));
                            }
                            world.func_147480_a(xCoord, yCoord, zCoord, false);
                            world.setBlock(xCoord, yCoord, zCoord, block);
                        }
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void init(){}

    @Override
    public void postInit(){}

    @Override
    public void clientSide(){}

    @Override
    public void clientInit(){}

}
