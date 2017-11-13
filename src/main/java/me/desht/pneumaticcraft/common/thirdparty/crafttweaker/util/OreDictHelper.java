package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import com.blamejared.mtlib.helpers.InputHelper;

import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.oredict.IOreDictEntry;

public class OreDictHelper {
	private OreDictHelper() {}
	
	public static Pair<String, Integer> toPair(IOreDictEntry entry) {
		return Pair.of(entry.getName(), entry.getAmount());
	}
	
	@SuppressWarnings("unchecked")
	public static Object[] toInput(IIngredient[] input) {
		List inputs = new ArrayList();
		
		for(int i = 0; i < input.length; i++) {
			if(input[i] instanceof IOreDictEntry) {
				inputs.add(toPair((IOreDictEntry)input[i]));
			} else if(input[i] instanceof IItemStack) {
				inputs.add(InputHelper.toStack((IItemStack)input[i]));
			}
		}
		
		return inputs.toArray(new Object[inputs.size()]);
	}
}
