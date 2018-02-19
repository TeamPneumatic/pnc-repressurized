package me.desht.pneumaticcraft.common.harvesting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import me.desht.pneumaticcraft.api.harvesting.IHarvestHandler;
import me.desht.pneumaticcraft.api.harvesting.IHarvestRegistry;
import me.desht.pneumaticcraft.common.block.Blockss;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;

import org.apache.commons.lang3.Validate;


public class HarvestRegistry implements IHarvestRegistry {
    private static final HarvestRegistry INSTANCE = new HarvestRegistry();
    
    private List<IHarvestHandler> harvestHandlers = new ArrayList<IHarvestHandler>();

    public static HarvestRegistry getInstance() {
        return INSTANCE;
    }

    public void init() {
        //Crops, harvest when fully grown
        ItemStack cocoaBean = new ItemStack(Items.DYE, 1, EnumDyeColor.BROWN.getDyeDamage());
        registerHarvestHandler(new HarvestHandlerCrops());
        registerHarvestHandlerCroplike(state -> state.getBlock() == Blocks.NETHER_WART, BlockNetherWart.AGE, stack -> stack.getItem() == Items.NETHER_WART);
        registerHarvestHandlerCroplike(state -> state.getBlock() == Blocks.COCOA, BlockCocoa.AGE, stack -> stack.isItemEqual(cocoaBean));

        //Cactus like, harvest when a block below.
        registerHarvestHandlerCactuslike(state -> state.getBlock() == Blocks.CACTUS);
        registerHarvestHandlerCactuslike(state -> state.getBlock() == Blocks.REEDS);
        
        //Melons/Pumpkins, just harvest the block when found
        registerHarvestHandler((w, c, p, state) -> state.getBlock() == Blocks.PUMPKIN ||
                                                   state.getBlock() == Blocks.MELON_BLOCK);

    }
    
    public List<IHarvestHandler> getHarvestHandlers(){
        return harvestHandlers;
    }
    
    @Override
    public void registerHarvestHandler(IHarvestHandler harvestHandler){
        Validate.notNull(harvestHandler);
        harvestHandlers.add(harvestHandler);
    }
    
    @Override
    public void registerHarvestHandlerCactuslike(Predicate<IBlockState> blockChecker){
        Validate.notNull(blockChecker);
        registerHarvestHandler(new HarvestHandlerCactusLike(blockChecker));
    }

    @Override
    public void registerHarvestHandlerCroplike(Predicate<IBlockState> blockChecker, PropertyInteger ageProperty, Predicate<ItemStack> isSeed){
        Validate.notNull(blockChecker);
        Validate.notNull(ageProperty);
        Validate.notNull(isSeed);
        registerHarvestHandler(new HarvestHandlerCropLike(blockChecker, ageProperty, isSeed));
    }
}
