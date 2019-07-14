package me.desht.pneumaticcraft.common.thirdparty.igwmod;

import igwmod.TextureSupplier;
import igwmod.api.IRecipeIntegrator;
import igwmod.gui.*;
import me.desht.pneumaticcraft.common.recipes.PressureChamberRecipe;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.*;

public class IntegratorPressureChamber implements IRecipeIntegrator {

    @Override
    public String getCommandKey() {
        return "pressureChamber";
    }

    @Override
    public void onCommandInvoke(String[] arguments, List<IReservedSpace> reservedSpaces, List<LocatedString> locatedStrings, List<LocatedStack> locatedStacks, List<IWidget> locatedTextures) throws IllegalArgumentException {
        if (arguments.length != 3) throw new IllegalArgumentException("Code needs 3 arguments!");
        int x;
        try {
            x = Integer.parseInt(arguments[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The first parameter (the x coordinate) contains an invalid number. Check for invalid characters!");
        }
        int y;
        try {
            y = Integer.parseInt(arguments[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The second parameter (the y coordinate) contains an invalid number. Check for invalid characters!");
        }
        locatedTextures.add(new LocatedTexture(TextureSupplier.getTexture(Textures.ICON_LOCATION + "textures/wiki/pressure_chamber_recipe.png"), x, y, 1 / GuiWiki.TEXT_SCALE));

        //Look up the recipe

        switch (arguments[2]) {
            case "disenchanting":
                handleDisenchanting(x, y, locatedStacks);
                break;
            case "villagers":
                handleVillagers(x, y, locatedTextures);
                break;
            default:
                IPressureChamberRecipe foundRecipe = null;
                for (IPressureChamberRecipe recipe : PressureChamberRecipe.recipes) {
                    for (ItemStack output : recipe.getResult()) {
                        if (IGWHandler.getNameFromStack(output).equals(arguments[2])) {
                            foundRecipe = recipe;
                            break;
                        }
                    }
                }
                if (foundRecipe == null)
                    throw new IllegalArgumentException("No recipe found for the key " + arguments[2]);

                locatedStrings.add(new LocatedString(I18n.format("igwmod.pressureChamber.requiredPressure") + ":", x + 180, y + 10, 0xFF000000, false));
                locatedStrings.add(new LocatedString(foundRecipe.getCraftingPressure() + " bar", x + 215, y + 20, 0xFF000000, false));

                for (int i = 0; i < foundRecipe.getInput().size(); i++) {
                    LocatedStack stack = new LocatedStack(foundRecipe.getInput().get(i).getSingleStack(), (int) ((x + 36 + i % 3 * 34) * GuiWiki.TEXT_SCALE), (int) ((y + 102 - i / 3 * 34) * GuiWiki.TEXT_SCALE));
                    locatedStacks.add(stack);
                }

                for (int i = 0; i < foundRecipe.getResult().size(); i++) {
                    LocatedStack stack = new LocatedStack(foundRecipe.getResult().get(i), (int) ((x + 180 + i % 3 * 36) * GuiWiki.TEXT_SCALE), (int) ((y + 60 + i / 3 * 36) * GuiWiki.TEXT_SCALE));
                    locatedStacks.add(stack);
                }
                break;
        }
    }

    private void handleVillagers(int x, int y, List<IWidget> locatedTextures) {
        locatedTextures.add(new LocatedEntity(VillagerEntity.class, x + 70, y + 95, 2F));

        LocatedEntity locatedEntity = new LocatedEntity(VillagerEntity.class, x + 215, y + 125, 2F);
        VillagerEntity villager = (VillagerEntity) locatedEntity.entity;
//        villager.setProfession(ConfigHandler.general.villagerMechanicID);
        locatedTextures.add(locatedEntity);
    }

    private void handleDisenchanting(int x, int y, List<LocatedStack> locatedStacks) {

        List<ItemStack> input = new ArrayList<>();
        List<ItemStack> output = new ArrayList<>();

        ItemStack enchantedItem = new ItemStack(Items.DIAMOND_SWORD);
        EnchantmentHelper.addRandomEnchantment(new Random(), enchantedItem, 30, true);

        input.add(enchantedItem);
        output.add(new ItemStack(Items.DIAMOND_SWORD));

        Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(enchantedItem);

        for (Map.Entry<Enchantment, Integer> enchant : enchants.entrySet()) {
            ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
            Map<Enchantment, Integer> newMap = new HashMap<>();
            newMap.put(enchant.getKey(), enchant.getValue());
            EnchantmentHelper.setEnchantments(newMap, enchantedBook);
            output.add(enchantedBook);
            input.add(new ItemStack(Items.BOOK));
        }

        for (int i = 0; i < input.size(); i++) {
            LocatedStack stack = new LocatedStack(input.get(i), (int) ((x + 36 + i % 3 * 34) * GuiWiki.TEXT_SCALE), (int) ((y + 102 - i / 3 * 34) * GuiWiki.TEXT_SCALE));
            locatedStacks.add(stack);
        }

        for (int i = 0; i < output.size(); i++) {
            LocatedStack stack = new LocatedStack(output.get(i), (int) ((x + 180 + i % 3 * 36) * GuiWiki.TEXT_SCALE), (int) ((y + 60 + i / 3 * 36) * GuiWiki.TEXT_SCALE));
            locatedStacks.add(stack);
        }
    }
}
