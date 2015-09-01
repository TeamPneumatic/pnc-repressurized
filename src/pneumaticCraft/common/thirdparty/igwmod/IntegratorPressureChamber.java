package pneumaticCraft.common.thirdparty.igwmod;

import igwmod.TextureSupplier;
import igwmod.WikiUtils;
import igwmod.api.IRecipeIntegrator;
import igwmod.gui.GuiWiki;
import igwmod.gui.IReservedSpace;
import igwmod.gui.IWidget;
import igwmod.gui.LocatedEntity;
import igwmod.gui.LocatedStack;
import igwmod.gui.LocatedString;
import igwmod.gui.LocatedTexture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import pneumaticCraft.api.recipe.PressureChamberRecipe;
import pneumaticCraft.common.config.Config;
import pneumaticCraft.common.recipes.PneumaticRecipeRegistry;
import pneumaticCraft.lib.Textures;

public class IntegratorPressureChamber implements IRecipeIntegrator{

    @Override
    public String getCommandKey(){
        return "pressureChamber";
    }

    @Override
    public void onCommandInvoke(String[] arguments, List<IReservedSpace> reservedSpaces, List<LocatedString> locatedStrings, List<LocatedStack> locatedStacks, List<IWidget> locatedTextures) throws IllegalArgumentException{
        if(arguments.length != 3) throw new IllegalArgumentException("Code needs 3 arguments!");
        int x;
        try {
            x = Integer.parseInt(arguments[0]);
        } catch(NumberFormatException e) {
            throw new IllegalArgumentException("The first parameter (the x coordinate) contains an invalid number. Check for invalid characters!");
        }
        int y;
        try {
            y = Integer.parseInt(arguments[1]);
        } catch(NumberFormatException e) {
            throw new IllegalArgumentException("The second parameter (the y coordinate) contains an invalid number. Check for invalid characters!");
        }
        locatedTextures.add(new LocatedTexture(TextureSupplier.getTexture(Textures.ICON_LOCATION + "textures/wiki/pressureChamberRecipe.png"), x, y, 1 / GuiWiki.TEXT_SCALE));

        //Look up the recipe

        if(arguments[2].equals("disenchanting")) {
            handleDisenchanting(x, y, locatedStacks);
        } else if(arguments[2].equals("villagers")) {
            handleVillagers(x, y, locatedTextures);
        } else {

            PressureChamberRecipe foundRecipe = null;
            for(PressureChamberRecipe recipe : PressureChamberRecipe.chamberRecipes) {
                for(ItemStack output : recipe.output) {
                    if(WikiUtils.getNameFromStack(output).equals(arguments[2])) {
                        foundRecipe = recipe;
                        break;
                    }
                }
            }
            if(foundRecipe == null) throw new IllegalArgumentException("No recipe found for the key " + arguments[2]);

            locatedStrings.add(new LocatedString(I18n.format("igwmod.pressureChamber.requiredPressure") + ":", x + 180, y + 10, 0xFF000000, false));
            locatedStrings.add(new LocatedString(foundRecipe.pressure + " bar", x + 215, y + 20, 0xFF000000, false));

            for(int i = 0; i < foundRecipe.input.length; i++) {
                LocatedStack stack = new LocatedStack(PneumaticRecipeRegistry.getSingleStack(foundRecipe.input[i]), (int)((x + 36 + i % 3 * 34) * GuiWiki.TEXT_SCALE), (int)((y + 102 - i / 3 * 34) * GuiWiki.TEXT_SCALE));
                locatedStacks.add(stack);
            }

            for(int i = 0; i < foundRecipe.output.length; i++) {
                LocatedStack stack = new LocatedStack(foundRecipe.output[i], (int)((x + 180 + i % 3 * 36) * GuiWiki.TEXT_SCALE), (int)((y + 60 + i / 3 * 36) * GuiWiki.TEXT_SCALE));
                locatedStacks.add(stack);
            }
        }
    }

    private void handleVillagers(int x, int y, List<IWidget> locatedTextures){
        locatedTextures.add(new LocatedEntity(EntityVillager.class, x + 70, y + 95, 2F));

        LocatedEntity locatedEntity = new LocatedEntity(EntityVillager.class, x + 215, y + 125, 2F);
        EntityVillager villager = (EntityVillager)locatedEntity.entity;
        villager.setProfession(Config.villagerMechanicID);
        locatedTextures.add(locatedEntity);
    }

    private void handleDisenchanting(int x, int y, List<LocatedStack> locatedStacks){

        List<ItemStack> input = new ArrayList<ItemStack>();
        List<ItemStack> output = new ArrayList<ItemStack>();

        ItemStack enchantedItem = new ItemStack(Items.diamond_sword);
        EnchantmentHelper.addRandomEnchantment(new Random(), enchantedItem, 30);

        input.add(enchantedItem);
        output.add(new ItemStack(Items.diamond_sword));

        Map<Integer, Integer> enchants = EnchantmentHelper.getEnchantments(enchantedItem);

        for(Map.Entry<Integer, Integer> enchant : enchants.entrySet()) {
            ItemStack enchantedBook = new ItemStack(Items.enchanted_book);
            Map<Integer, Integer> newMap = new HashMap<Integer, Integer>();
            newMap.put(enchant.getKey(), enchant.getValue());
            EnchantmentHelper.setEnchantments(newMap, enchantedBook);
            output.add(enchantedBook);

            input.add(new ItemStack(Items.book));
        }

        for(int i = 0; i < input.size(); i++) {
            LocatedStack stack = new LocatedStack(input.get(i), (int)((x + 36 + i % 3 * 34) * GuiWiki.TEXT_SCALE), (int)((y + 102 - i / 3 * 34) * GuiWiki.TEXT_SCALE));
            locatedStacks.add(stack);
        }

        for(int i = 0; i < output.size(); i++) {
            LocatedStack stack = new LocatedStack(output.get(i), (int)((x + 180 + i % 3 * 36) * GuiWiki.TEXT_SCALE), (int)((y + 60 + i / 3 * 36) * GuiWiki.TEXT_SCALE));
            locatedStacks.add(stack);
        }
    }
}
