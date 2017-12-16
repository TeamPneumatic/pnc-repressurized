package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util;

import java.util.List;

public class RemoveAllRecipes<T> extends ListRemoval<T> {

	public RemoveAllRecipes(String name, List<T> recipes) {
		super(name, recipes);
	}
	
	@Override
	public void apply() {
	    entries.addAll(recipes);
	    
		super.apply();
	}
	
	@Override
	public String describe() {
		return String.format("Removing all %d %s recipe(s)", recipes.size(), this.name);
	}
}
