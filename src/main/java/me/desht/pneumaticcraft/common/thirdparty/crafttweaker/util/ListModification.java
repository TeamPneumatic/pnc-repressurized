package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util;

import crafttweaker.IAction;

import java.util.ArrayList;
import java.util.List;

public abstract class ListModification<T> implements IAction {
	protected final List<T> recipes;
	protected final List<T> entries = new ArrayList<>();
	protected final String name;
	
	public ListModification(String name, List<T> recipes) {
		this.name = name;
		this.recipes = recipes;
	}
	
	public ListModification(String name, List<T> recipes, List<T> entries) {
		this(name, recipes);
		
        if(entries != null) {
        	this.entries.addAll(entries);
        }
	}
	
	public ListModification(String name, List<T> recipes, T entry) {
		this(name, recipes);
        
		if(entry != null) {
        	entries.add(entry);
        }
	}
}
