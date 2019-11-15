package me.desht.pneumaticcraft.common.recipes;

import com.google.common.collect.ImmutableMap;
import me.desht.pneumaticcraft.api.recipe.IPressureChamberRecipe;
import me.desht.pneumaticcraft.common.util.ItemStackHandlerIterable;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class PressureChamberVacuumDisenchantHandler implements IPressureChamberRecipe {
    private static final ResourceLocation ID = RL("pressure_chamber_disenchanting");

    @Override
    public float getCraftingPressure() {
        return -0.75F;
    }

    @Override
    public boolean isValidRecipe(ItemStackHandler chamberHandler) {
        return !getDisenchantableItem(chamberHandler).isEmpty() && !getBook(chamberHandler).isEmpty();
    }
    
    private ItemStack getDisenchantableItem(ItemStackHandler inputStacks){
        return new ItemStackHandlerIterable(inputStacks)
                        .stream()
                        .filter(stack -> stack.getItem() != Items.ENCHANTED_BOOK && EnchantmentHelper.getEnchantments(stack).size() > 0)
                        .findFirst()
                        .orElse(ItemStack.EMPTY);
    }
    
    private ItemStack getBook(ItemStackHandler inputStacks){
        return new ItemStackHandlerIterable(inputStacks)
                        .stream()
                        .filter(stack -> stack.getItem() == Items.BOOK)
                        .findFirst()
                        .orElse(ItemStack.EMPTY);
    }

    @Override
    public NonNullList<ItemStack> craftRecipe(ItemStackHandler chamberHandler) {
        ItemStack enchantedStack = getDisenchantableItem(chamberHandler);
        getBook(chamberHandler).shrink(1);
        
        // take a random enchantment off the enchanted item...
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(enchantedStack);
        List<Enchantment> l = new ArrayList<>(enchantments.keySet());
        Enchantment strippedEnchantment = l.get(new Random().nextInt(l.size()));
        int level = enchantments.get(strippedEnchantment);
        enchantments.remove(strippedEnchantment);
        EnchantmentHelper.setEnchantments(enchantments, enchantedStack);

        // ...and create an enchanted book with it
        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantmentHelper.setEnchantments(ImmutableMap.of(strippedEnchantment, level), enchantedBook);

        return NonNullList.from(ItemStack.EMPTY, enchantedBook);
    }

    @Override
    public List<List<ItemStack>> getInputsForDisplay() {
        List<List<ItemStack>> res = new ArrayList<>();

        ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE);
        pick.addEnchantment(Enchantments.FORTUNE, 1);
        IPressureChamberRecipe.setTooltipKey(pick, "gui.nei.tooltip.vacuumEnchantItem");
        res.add(NonNullList.from(ItemStack.EMPTY, pick));

        ItemStack book = new ItemStack(Items.BOOK);
        res.add(NonNullList.from(ItemStack.EMPTY, book));

        return res;
    }

    @Override
    public NonNullList<ItemStack> getResultForDisplay() {
        ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE);
        IPressureChamberRecipe.setTooltipKey(pick, "gui.nei.tooltip.vacuumEnchantItemOut");
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        book.addEnchantment(Enchantments.FORTUNE, 1);
        IPressureChamberRecipe.setTooltipKey(book, "gui.nei.tooltip.vacuumEnchantBookOut");
        return NonNullList.from(ItemStack.EMPTY, pick, book);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeResourceLocation(ID);
        buf.writeFloat(getCraftingPressure());
        buf.writeVarInt(2);
        getInputsForDisplay().get(0).forEach(s -> Ingredient.fromStacks(s).write(buf));
        buf.writeVarInt(2);
        getResultForDisplay().forEach(buf::writeItemStack);
    }
}
