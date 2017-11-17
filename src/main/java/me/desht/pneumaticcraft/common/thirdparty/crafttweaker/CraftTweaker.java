package me.desht.pneumaticcraft.common.thirdparty.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;

import java.util.LinkedList;
import java.util.List;

public class CraftTweaker implements IThirdParty {
	
    public static final List<IAction> REMOVALS = new LinkedList<>();
    public static final List<IAction> ADDITIONS = new LinkedList<>();

    @Override
    public void postInit() {
    	REMOVALS.forEach(CraftTweakerAPI::apply);
    	ADDITIONS.forEach(CraftTweakerAPI::apply);
    }

}
