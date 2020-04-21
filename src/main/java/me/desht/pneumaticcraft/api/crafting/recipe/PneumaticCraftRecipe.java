package me.desht.pneumaticcraft.api.crafting.recipe;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/**
 * Base class for all PneumaticCraft machine recipes, which are registered in the vanilla RecipeManager.
 */
public abstract class PneumaticCraftRecipe implements IRecipe<PneumaticCraftRecipe.DummyIInventory> {
    private final ResourceLocation id;

    protected PneumaticCraftRecipe(ResourceLocation id) {
        this.id = id;
    }

    /**
     * Writes this recipe to a PacketBuffer.
     *
     * @param buffer The buffer to write to.
     */
    public abstract void write(PacketBuffer buffer);

    @Override
    public boolean matches(DummyIInventory inv, World worldIn) {
        return true;
    }

    @Override
    public ItemStack getCraftingResult(DummyIInventory inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canFit(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    /**
     * Just to keep vanilla happy...
     */
    public static class DummyIInventory implements IInventory {
        private static final DummyIInventory INSTANCE = new DummyIInventory();

        public static DummyIInventory getInstance() {
            return INSTANCE;
        }

        @Override
        public int getSizeInventory() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public ItemStack getStackInSlot(int index) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack decrStackSize(int index, int count) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeStackFromSlot(int index) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setInventorySlotContents(int index, ItemStack stack) {
        }

        @Override
        public void markDirty() {
        }

        @Override
        public boolean isUsableByPlayer(PlayerEntity player) {
            return false;
        }

        @Override
        public void clear() {
        }
    }
}
