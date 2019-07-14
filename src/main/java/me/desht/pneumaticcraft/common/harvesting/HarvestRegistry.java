package me.desht.pneumaticcraft.common.harvesting;

import me.desht.pneumaticcraft.api.harvesting.IHarvestHandler;
import me.desht.pneumaticcraft.api.harvesting.IHarvestRegistry;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.IntegerProperty;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;


public class HarvestRegistry implements IHarvestRegistry {
    private static final HarvestRegistry INSTANCE = new HarvestRegistry();
    
    private final List<IHarvestHandler> harvestHandlers = new ArrayList<>();
    private final List<Pair<Predicate<ItemStack>, BiConsumer<ItemStack, PlayerEntity>>> hoeHandlers = new ArrayList<>();

    public static HarvestRegistry getInstance() {
        return INSTANCE;
    }

    public void init() {
        //Crops, harvest when fully grown
        ItemStack cocoaBean = new ItemStack(Items.COCOA_BEANS);
        registerHarvestHandler(new HarvestHandlerCrops());
        registerHarvestHandlerCroplike(state -> state.getBlock() == Blocks.NETHER_WART, NetherWartBlock.AGE, stack -> stack.getItem() == Items.NETHER_WART);
        registerHarvestHandlerCroplike(state -> state.getBlock() == Blocks.COCOA, CocoaBlock.AGE, stack -> stack.isItemEqual(cocoaBean));

        //Cactus like, harvest when a block below.
        registerHarvestHandlerCactuslike(state -> state.getBlock() == Blocks.CACTUS);
        registerHarvestHandlerCactuslike(state -> state.getBlock() == Blocks.SUGAR_CANE);
        
        //Melons/Pumpkins, just harvest the block when found
        registerHarvestHandler((w, c, p, state, drone) -> state.getBlock() == Blocks.PUMPKIN || state.getBlock() == Blocks.MELON);
        
        //Trees
        registerHarvestHandler(new HarvestHandlerLeaves());

        // this works for all vanilla trees, and should work for all modded trees too as long as mod authors are consistent about block names
        for (Block logBlock : BlockTags.LOGS.getAllElements()) {
            Predicate<BlockState> isLog = state -> state.getBlock() == logBlock;
            Block sapling = convertTree(logBlock, "log", "sapling");
            if (sapling != null && sapling != Blocks.AIR) {
                Predicate<ItemStack> isSapling = stack -> stack.getItem() == sapling.asItem();
                registerHarvestHandlerTreelike(isLog, isSapling, sapling.getDefaultState());
            }
        }
        
        //Register hoe
        registerHoe(item -> item.getItem() instanceof HoeItem, (stack, player) -> stack.damageItem(1, player, p -> { }));
    }

    static Block convertTree(Block in, String from, String to) {
        ResourceLocation rl = new ResourceLocation(in.getRegistryName().toString().replace("_" + from, "_" + to));
        return ForgeRegistries.BLOCKS.getValue(rl);
    }

    public List<IHarvestHandler> getHarvestHandlers(){
        return harvestHandlers;
    }
    
    /**
     * Returns a durability use function when a hoe is found.
     * @param stack the hoe
     * @return a durability use consumer which takes a hoe and a player object
     */
    public BiConsumer<ItemStack, PlayerEntity> getDamageableHoe(ItemStack stack){
        return hoeHandlers.stream().filter(handler -> handler.getLeft().test(stack))
                                   .map(Pair::getRight)
                                   .findFirst()
                                   .orElse(null);
    }
    
    @Override
    public void registerHarvestHandler(IHarvestHandler harvestHandler){
        Validate.notNull(harvestHandler);
        harvestHandlers.add(harvestHandler);
    }
    
    @Override
    public void registerHarvestHandlerCactuslike(Predicate<BlockState> blockChecker){
        Validate.notNull(blockChecker);
        registerHarvestHandler(new HarvestHandlerCactusLike(blockChecker));
    }

    @Override
    public void registerHarvestHandlerCroplike(Predicate<BlockState> blockChecker, IntegerProperty ageProperty, Predicate<ItemStack> isSeed){
        Validate.notNull(blockChecker);
        Validate.notNull(ageProperty);
        Validate.notNull(isSeed);
        registerHarvestHandler(new HarvestHandlerCropLike(blockChecker, ageProperty, isSeed));
    }
    
    @Override
    public void registerHarvestHandlerTreelike(Predicate<BlockState> blockChecker, Predicate<ItemStack> isSapling, BlockState saplingState){
        Validate.notNull(blockChecker);
        Validate.notNull(isSapling);
        Validate.notNull(saplingState);
        registerHarvestHandler(new HarvestHandlerTree(blockChecker, isSapling, saplingState));
    }

    @Override
    public void registerHoe(Predicate<ItemStack> isHoeWithDurability, BiConsumer<ItemStack, PlayerEntity> useDurability){
        Validate.notNull(isHoeWithDurability);
        Validate.notNull(useDurability);
        hoeHandlers.add(new ImmutablePair<>(isHoeWithDurability, useDurability));
    }
}
