package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util;

import java.util.List;

import com.blamejared.mtlib.utils.BaseListRemoval;

public class RemoveAllRecipes<T> extends BaseListRemoval<T> {

	public RemoveAllRecipes(String name, List<T> list) {
		super(name, list);
	}
	
	@Override
	public void apply() {
		recipes.addAll(list);
		super.apply();
	}
	
	@Override
	protected String getRecipeInfo(T recipe) {
		return "";
	}
	
	@Override
	public String describe() {
		return String.format("Removing all %s Recipe(s)", this.name);
	}
}
