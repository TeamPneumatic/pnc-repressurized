package me.desht.pneumaticcraft.common.thirdparty.ftbteams;

import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;

public class FTBTeams implements IThirdParty {
    @Override
    public void init() {
        FTBTeamsIntegration.registerTeamEntityModifier();
    }
}
