package pneumaticCraft.common.thirdparty.thaumcraft;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.thirdparty.IRegistryListener;
import pneumaticCraft.common.thirdparty.IThirdParty;
import pneumaticCraft.lib.Log;
import pneumaticCraft.lib.ModIds;
import pneumaticCraft.lib.Names;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.registry.GameRegistry;

public class Thaumcraft implements IThirdParty, IRegistryListener{
    private final List<Item> pcItems = new ArrayList<Item>();
    private final List<Block> pcBlocks = new ArrayList<Block>();

    @Override
    public void preInit(){
        for(int i = 0; i < 16; i++) {
            Block plantBlock = ItemPlasticPlants.getPlantBlockIDFromSeed(i);
            if(plantBlock != null) {
                FMLInterModComms.sendMessage(ModIds.THAUMCRAFT, "harvestStandardCrop", new ItemStack(plantBlock, 1, 6));
                FMLInterModComms.sendMessage(ModIds.THAUMCRAFT, "harvestStandardCrop", new ItemStack(plantBlock, 1, 13));
            }
        }
        PneumaticRegistry.getInstance().registerBlockTrackEntry(new BlockTrackEntryThaumcraft());
    }

    @Override
    public void init(){
        ItemStack lapis = new ItemStack(Items.dye, 1, 4);
        Item shard = GameRegistry.findItem(ModIds.THAUMCRAFT, "ItemShard");
        if(shard != null) {
            GameRegistry.addRecipe(new ItemStack(Itemss.machineUpgrade, 1, 10), "lal", "bcd", "lel", 'l', lapis, 'a', new ItemStack(shard, 1, 0), 'b', new ItemStack(shard, 1, 1), 'c', new ItemStack(shard, 1, 6), 'd', new ItemStack(shard, 1, 3), 'e', new ItemStack(shard, 1, 4));
        } else {
            Log.error("Thaumcraft shard item couldn't be found! Registry name has changed? Thaumcraft Upgrade has no recipe!");
        }
    }

    @Override
    public void postInit(){
        ThaumcraftApi.registerObjectTag(Names.INGOT_IRON_COMPRESSED, new AspectList().add(Aspect.METAL, 8).add(Aspect.AIR, 1));
        registerPlasticAspects(ItemPlasticPlants.SQUID_PLANT_DAMAGE, Aspect.DARKNESS);
        registerPlasticAspects(ItemPlasticPlants.FIRE_FLOWER_DAMAGE, Aspect.FIRE);
        registerPlasticAspects(ItemPlasticPlants.CREEPER_PLANT_DAMAGE, Aspect.ENERGY);
        registerPlasticAspects(ItemPlasticPlants.SLIME_PLANT_DAMAGE, Aspect.SLIME);
        registerPlasticAspects(ItemPlasticPlants.RAIN_PLANT_DAMAGE, Aspect.WATER);
        registerPlasticAspects(ItemPlasticPlants.ENDER_PLANT_DAMAGE, Aspect.ELDRITCH);
        registerPlasticAspects(ItemPlasticPlants.LIGHTNING_PLANT_DAMAGE, Aspect.WEATHER);
        //registerPlasticAspects(ItemPlasticPlants.ADRENALINE_PLANT_DAMAGE , Aspect.
        registerPlasticAspects(ItemPlasticPlants.POTION_PLANT_DAMAGE, Aspect.POISON);
        registerPlasticAspects(ItemPlasticPlants.REPULSION_PLANT_DAMAGE, Aspect.ENTROPY);
        registerPlasticAspects(ItemPlasticPlants.HELIUM_PLANT_DAMAGE, Aspect.LIGHT);
        registerPlasticAspects(ItemPlasticPlants.CHOPPER_PLANT_DAMAGE, Aspect.AIR);
        //registerPlasticAspects(ItemPlasticPlants.MUSIC_PLANT_DAMAGE , Aspect.
        registerPlasticAspects(ItemPlasticPlants.PROPULSION_PLANT_DAMAGE, Aspect.MOTION);
        registerPlasticAspects(ItemPlasticPlants.FLYING_FLOWER_DAMAGE, Aspect.FLIGHT);

        ThaumcraftApi.registerObjectTag(new ItemStack(Itemss.turbineBlade), new AspectList().add(Aspect.GREED, 2).add(Aspect.METAL, 3).add(Aspect.MOTION, 2).add(Aspect.ENERGY, 4));

        AspectList transAndCapAspects = new AspectList().add(Aspect.ENERGY, 2).add(Aspect.PLANT, 2).add(Aspect.METAL, 6);
        ThaumcraftApi.registerObjectTag(new ItemStack(Itemss.transistor), transAndCapAspects);
        ThaumcraftApi.registerObjectTag(new ItemStack(Itemss.capacitor), transAndCapAspects);

        AspectList pcbAspects = new AspectList().add(Aspect.ENERGY, 1).add(Aspect.PLANT, 2).add(Aspect.METAL, 6);
        ThaumcraftApi.registerObjectTag(new ItemStack(Itemss.emptyPCB), pcbAspects);
        ThaumcraftApi.registerObjectTag(new ItemStack(Itemss.unassembledPCB), pcbAspects);
        ThaumcraftApi.registerObjectTag(new ItemStack(Itemss.airCanister), new AspectList().add(Aspect.METAL, 30).add(Aspect.ENERGY, 4));

        /*  for(Item item : pcItems) {
              ThaumcraftApi.registerComplexObjectTag(new ItemStack(item, 1, OreDictionary.WILDCARD_VALUE), new AspectList());
          }
          for(Block block : pcBlocks) {
              ThaumcraftApi.registerComplexObjectTag(new ItemStack(block, 1, OreDictionary.WILDCARD_VALUE), new AspectList());
          }*/
    }

    private void registerPlasticAspects(int meta, Aspect aspect){
        registerPlasticAspects(meta, new AspectList().add(aspect, 1));
    }

    private void registerPlasticAspects(int meta, AspectList aspects){
        ThaumcraftApi.registerObjectTag(new ItemStack(Itemss.plasticPlant, 1, meta), aspects.add(Aspect.PLANT, 2));
        ThaumcraftApi.registerObjectTag(new ItemStack(Itemss.plastic, 1, meta), aspects);
    }

    @Override
    public void onItemRegistry(Item item){
        pcItems.add(item); //buffer the items, as we need to generate the aspects after TC does it.
    }

    @Override
    public void onBlockRegistry(Block block){
        pcBlocks.add(block);
    }

    @Override
    public void clientSide(){

    }

    @Override
    public void clientInit(){}

}
