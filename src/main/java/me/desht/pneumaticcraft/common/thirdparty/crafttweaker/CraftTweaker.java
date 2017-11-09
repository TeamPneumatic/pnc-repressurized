package me.desht.pneumaticcraft.common.thirdparty.crafttweaker;

import java.util.LinkedList;
import java.util.List;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;

public class CraftTweaker implements IThirdParty {
	
    public static final List<IAction> REMOVALS = new LinkedList<>();
    public static final List<IAction> ADDITIONS = new LinkedList<>();


    @Override
    public void preInit() {
    }

    @Override
    public void init() {
    }

    @Override
    public void postInit() {
    	REMOVALS.forEach(CraftTweakerAPI::apply);
    	ADDITIONS.forEach(CraftTweakerAPI::apply);
    }

    @Override
    public void clientSide() {
    }

    @Override
    public void clientInit() {
    }

}
