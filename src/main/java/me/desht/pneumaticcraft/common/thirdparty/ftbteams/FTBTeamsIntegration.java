package me.desht.pneumaticcraft.common.thirdparty.ftbteams;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.TeamRank;
import dev.ftb.mods.ftbteams.api.property.TeamProperties;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.misc.IMiscHelpers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Locale;

public class FTBTeamsIntegration {
    public static void registerTeamEntityModifier() {
        IMiscHelpers helper = PneumaticRegistry.getInstance().getMiscHelpers();

        registerModifier(helper, "ftbteam", TeamRank.MEMBER, false);
        registerModifier(helper, "ftbteam_officer", TeamRank.OFFICER, false);
        registerModifier(helper, "ftbteam_owner", TeamRank.OWNER, false);
        registerModifier(helper, "ftbteam_ally", TeamRank.ALLY, false);
        registerModifier(helper, "ftbteam_enemy", TeamRank.ENEMY, true);
    }

    private static void registerModifier(IMiscHelpers helper, String name, TeamRank minRank, boolean exact) {
        helper.registerEntityFilterModifier(name,
                str -> true, "a valid FTB team display-name or UUID", (entity, str) -> matchTeam(entity, str, minRank, exact));
    }

    private static boolean matchTeam(Entity entity, String name, TeamRank minRank, boolean exact) {
        if (entity instanceof ServerPlayer p) {
            return FTBTeamsAPI.api().getManager().getTeamForPlayer(p).map(team -> {
                TeamRank rank = team.getRankForPlayer(p.getUUID());
                boolean rankOK = exact ? rank == minRank : rank.isAtLeast(minRank);
                return rankOK && (team.getTeamId().toString().equals(name)
                        || team.getProperty(TeamProperties.DISPLAY_NAME).toLowerCase(Locale.ROOT).equals(name.toLowerCase(Locale.ROOT)));
            }).orElse(false);
        }
        return false;
    }
}
