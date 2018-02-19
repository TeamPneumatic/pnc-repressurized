package me.desht.pneumaticcraft.common.harvesting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import me.desht.pneumaticcraft.api.harvesting.IHarvestHandler;
import me.desht.pneumaticcraft.api.harvesting.IHarvestRegistry;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;


public class HarvestRegistry implements IHarvestRegistry {
    private static final HarvestRegistry INSTANCE = new HarvestRegistry();
    
    private List<IHarvestHandler> harvestHandlers = new ArrayList<>();
    private List<Pair<Predicate<ItemStack>, BiConsumer<ItemStack, EntityPlayer>>> hoeHandlers = new ArrayList<>();

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
        registerHarvestHandler((w, c, p, state, drone) -> state.getBlock() == Blocks.PUMPKIN ||
                                                   state.getBlock() == Blocks.MELON_BLOCK);

        //Register hoe
        registerHoe(item -> item.getItem() instanceof ItemHoe, (stack, player) -> stack.damageItem(1, player));
    }
    
    public List<IHarvestHandler> getHarvestHandlers(){
        return harvestHandlers;
    }
    
    /**
     * Returns a durability use function when a hoe is found.
     * @param stack
     * @return
     */
    public BiConsumer<ItemStack, EntityPlayer> getDamageableHoe(ItemStack stack){
        return hoeHandlers.stream().filter(handler -> handler.getLeft().test(stack))
                                   .map(handler -> handler.getRight())
                                   .findFirst()
                                   .orElse(null);
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

    @Override
    public void registerHoe(Predicate<ItemStack> isHoeWithDurability, BiConsumer<ItemStack, EntityPlayer> useDurability){
        Validate.notNull(isHoeWithDurability);
        Validate.notNull(useDurability);
        hoeHandlers.add(new ImmutablePair<Predicate<ItemStack>, BiConsumer<ItemStack, EntityPlayer>>(isHoeWithDurability, useDurability));
    }
}
