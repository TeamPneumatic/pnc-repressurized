package me.desht.pneumaticcraft.api.harvesting;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Registry for registering harvest handlers. Note that any subclass of {@link net.minecraft.block.BlockCrops} is supported automatically.
 * @author MineMaarten
 *
 */
public interface IHarvestRegistry{
    /**
     * Registers a generic harvest handler.
     * @param harvestHandler
     */
    public void registerHarvestHandler(IHarvestHandler harvestHandler);
    
    /**
     * Registers a harvest handler for block states that need to be farmed like cactusses/sugar canes,
     * in that the top blocks can be harvested as long as there is a plant block left at the bottom.
     * @param blockChecker return true if the given block state is a state you target.
     */
    public void registerHarvestHandlerCactuslike(Predicate<IBlockState> blockChecker);
    
    /**
     * Registers a harvest handler for block states that need to farmed like wheat/carrots/beetroot,
     * in that the block can be harvested when 'ageProperty' gets to its max growth value. Additionally,
     * when needing to replant, this means using a dropped seed (defined by 'isSeed'), and resetting the age to the min growth value.
     * @param blockChecker return true if the given block state is of a block you target. Checking for the right age is not necessary, this is done automatically.
     * @param ageProperty the block state property that keeps track of age. When the current age is equal to the max age, the crop will be harvested.
     * When requiring to replant, the min value of this property is used. The allowed block state by 'blockChecker' should include this property otherwise the game will crash!
     * @param isSeed return true if the given stack is the seed you target. Be aware that this can be called for item stacks that are not dropped from this crop.
     */
    public void registerHarvestHandlerCroplike(Predicate<IBlockState> blockChecker, PropertyInteger ageProperty, Predicate<ItemStack> isSeed);
    
    /**
     * Registers a harvest handler for trees.
     * @param blockChecker should return for the logs of this tree.
     * @param isSapling    should return if the given item stack is a sapling item (which can be used to replant)
     * @param saplingState the state of the sapling to be planted.
     */
    public void registerHarvestHandlerTreelike(Predicate<IBlockState> blockChecker, Predicate<ItemStack> isSapling, IBlockState saplingState);
    
    /**
     * Registers a custom hoe to be used by drones, by default any subclass of ItemHoe should work already.
     * @param isHoeWithDurability return true if the given item stack is a hoe, with durability left.
     * @param useDurability Called when isHoeWithDurability returns true, durability should be used in this implementation.
     */
    public void registerHoe(Predicate<ItemStack> isHoeWithDurability, BiConsumer<ItemStack, EntityPlayer> useDurability);
}
