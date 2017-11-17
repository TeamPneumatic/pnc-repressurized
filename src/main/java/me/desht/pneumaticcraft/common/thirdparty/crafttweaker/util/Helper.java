package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.oredict.IOreDictEntry;
import crafttweaker.mc1120.item.MCItemStack;
import crafttweaker.mc1120.liquid.MCLiquidStack;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Helper {
	private Helper() {}
	
	public static Pair<String, Integer> toPair(IOreDictEntry entry) {
		return Pair.of(entry.getName(), entry.getAmount());
	}
	
	@SuppressWarnings("unchecked")
	public static Object[] toInput(IIngredient[] input) {
		@SuppressWarnings("rawtypes")
		List inputs = new ArrayList();
		
		for(int i = 0; i < input.length; i++) {
			if(input[i] instanceof IOreDictEntry) {
				inputs.add(toPair((IOreDictEntry)input[i]));
			} else if(input[i] instanceof IItemStack) {
				inputs.add(toStack((IItemStack)input[i]));
			}
		}
		
		return inputs.toArray(new Object[inputs.size()]);
	}
	
    public static void logError(String message) {
        CraftTweakerAPI.logError(message);
    }
    
    public static void logError(String message, Throwable exception) {
        CraftTweakerAPI.logError(message, exception);
    }
    
    public static void logWarning(String message) {
        CraftTweakerAPI.logWarning(message);
    }
    
    public static void logInfo(String message) {
        CraftTweakerAPI.logInfo(message);
    }
    
    public static ItemStack toStack(IItemStack iStack) {
        if(iStack == null) {
            return ItemStack.EMPTY;
        } else {
            Object internal = iStack.getInternal();
            if(!(internal instanceof ItemStack)) {
                logError("Not a valid item stack: " + iStack);
            }
            
            return (ItemStack) internal;
        }
    }
    
    public static IItemStack[] toStacks(IIngredient[] iIngredient) {
    	return Stream.of(iIngredient).map(i -> i.getItems()).flatMap(List::stream).toArray(IItemStack[]::new);
    }
    
    public static ItemStack[] toStacks(IItemStack[] iStack) {
    	return Stream.of(iStack).map(Helper::toStack).toArray(ItemStack[]::new);
    }
    
    public static FluidStack toFluid(ILiquidStack iStack) {
        if(iStack == null) {
            return null;
        } else
            return FluidRegistry.getFluidStack(iStack.getName(), iStack.getAmount());
    }
    
    public static ILiquidStack toILiquidStack(FluidStack stack) {
        if(stack == null) {
            return null;
        }
        
        return new MCLiquidStack(stack);
    }
    
    /**
     * Returns a string representation of the item which can also be used in scripts
     */
    @SuppressWarnings("rawtypes")
    public static String getStackDescription(Object object) {
        if(object instanceof IIngredient) {
            return getStackDescription((IIngredient) object);
        } else if(object instanceof ItemStack) {
            return toIItemStack((ItemStack) object).toString();
        } else if(object instanceof FluidStack) {
            return getStackDescription((FluidStack) object);
        } else if(object instanceof Block) {
            return toIItemStack(new ItemStack((Block) object, 1, 0)).toString();
        } else if(object instanceof String) {
            // Check if string specifies an oredict entry
            List<ItemStack> ores = OreDictionary.getOres((String) object);
            
            if(!ores.isEmpty()) {
                return "<ore:" + (String) object + ">";
            } else {
                return "\"" + (String) object + "\"";
            }
        } else if(object instanceof List) {
            return getListDescription((List) object);
        } else if(object instanceof Object[]) {
            return getListDescription(Arrays.asList((Object[]) object));
        } else if(object != null) {
            return "\"" + object.toString() + "\"";
        } else if(object instanceof Ingredient && !((Ingredient) object).apply(ItemStack.EMPTY) && ((Ingredient) object).getMatchingStacks().length > 0) {
            return getStackDescription(((Ingredient) object).getMatchingStacks()[0]);
        } else {
            return "null";
        }
    }
    
    public static String getStackDescription(IIngredient stack) {
        Object internalObject = stack.getInternal();
        
        if(internalObject instanceof ItemStack) {
            return getStackDescription((ItemStack) internalObject);
        } else if(internalObject instanceof FluidStack) {
            return getStackDescription((FluidStack) internalObject);
        } else if(internalObject instanceof IOreDictEntry) {
            return getStackDescription(((IOreDictEntry) internalObject).getName());
        } else {
            return "null";
        }
    }
    
    public static String getStackDescription(FluidStack stack) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("<liquid:").append(stack.getFluid().getName()).append('>');
        
        if(stack.amount > 1) {
            sb.append(" * ").append(stack.amount);
        }
        
        return sb.toString();
    }
    
    public static String getListDescription(List<?> objects) {
        StringBuilder sb = new StringBuilder();
        
        if(objects.isEmpty()) {
            sb.append("[]");
        } else {
            sb.append('[');
            for(Object object : objects) {
                if(object instanceof List) {
                    sb.append(getListDescription((List) object)).append(", ");
                } else if(object instanceof Object[]) {
                    sb.append(getListDescription(Arrays.asList((Object[]) object))).append(", ");
                } else {
                    sb.append(getStackDescription(object)).append(", ");
                }
            }
            sb.setLength(sb.length() - 2);
            sb.append(']');
        }
        
        return sb.toString();
    }
    
    public static IItemStack toIItemStack(ItemStack stack) {
        if(stack.isEmpty()) {
            return null;
        }
        
        return new MCItemStack(stack);
    }
    
    public static FluidStack[] toFluids(ILiquidStack[] iStack) {
    	return Stream.of(iStack).map(Helper::toFluid).toArray(FluidStack[]::new);
    }
    
    public static boolean matches(IIngredient ingredient, IItemStack itemStack) {
        if(ingredient == null) {
            return false;
        }
        
        return ingredient.matches(itemStack);
    }


    public static boolean matches(IIngredient ingredient, IItemStack[] itemStack) {
        if(ingredient == null) {
            return false;
        }
        
        return Stream.of(itemStack).allMatch(i -> ingredient.matches(i));
    }
    
    public static boolean matches(IIngredient ingredient, ILiquidStack liquidStack) {
        if(ingredient == null) {
            return false;
        }

        // Do we have a wildcard (<*>) ?
        if(ingredient.matches(liquidStack)) {
            return true;
        }
        
        // Does ingredient reference liquids?
        if(ingredient.getLiquids() != null) {
            for (ILiquidStack liquid : ingredient.getLiquids()) {
                if(toFluid(liquid).isFluidEqual(toFluid(liquidStack))) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
	public static boolean areEqual(ItemStack stack1, ItemStack stack2) {
		if (stack1.isEmpty() || stack2.isEmpty()) {
			return false;
		}  else {
			return stack1.isItemEqual(stack2);
		}
	}
	
	public static boolean areEqual(FluidStack stack1, FluidStack stack2) {
	    if(stack1 == null || stack2 == null) {
	        return false;
	    }
	    
	    return stack1.isFluidEqual(stack2);
	}
}
