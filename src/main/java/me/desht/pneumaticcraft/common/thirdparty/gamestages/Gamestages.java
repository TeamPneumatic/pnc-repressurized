package me.desht.pneumaticcraft.common.thirdparty.gamestages;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class Gamestages implements IThirdParty {
    @Override
    public void preInit() {
        PneumaticRegistry.getInstance().registerPlayerMatcher(RL("gamestages"), new GamestagesMatcher.Factory());
    }
}
