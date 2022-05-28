package me.desht.pneumaticcraft.client.render.pneumatic_armor.entity_tracker;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IEntityTrackEntry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import me.desht.pneumaticcraft.client.pneumatic_armor.ArmorUpgradeClientRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.EntityTrackerClientHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.HackClientHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.hacking.HackManager;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class EntityTrackEntryHackable implements IEntityTrackEntry {
    @Override
    public boolean isApplicable(Entity entity) {
        return HackClientHandler.enabledForPlayer(ClientUtils.getClientPlayer());
    }

    @Override
    public void addInfo(Entity entity, List<Component> curInfo, boolean isLookingAtTarget) {
        Player player = ClientUtils.getClientPlayer();
        IHackableEntity hackable = HackManager.getHackableForEntity(entity, player);
        if (hackable != null) {
            int hackTime = ArmorUpgradeClientRegistry.getInstance()
                    .getClientHandler(CommonUpgradeHandlers.entityTrackerHandler, EntityTrackerClientHandler.class)
                    .getTargetsStream()
                    .filter(target -> target.entity == entity)
                    .findFirst()
                    .map(RenderEntityTarget::getHackTime)
                    .orElse(0);
            if (hackTime == 0) {
                if (isLookingAtTarget) {
                    hackable.addHackInfo(entity, curInfo, player);
                    HackClientHandler.addKeybindTooltip(curInfo);
                }
            } else {
                int requiredHackTime = hackable.getHackTime(entity, player);
                int percentageComplete = hackTime * 100 / requiredHackTime;
                if (percentageComplete < 100) {
                    curInfo.add(xlate("pneumaticcraft.armor.hacking.hacking", percentageComplete));
                } else if (hackTime < requiredHackTime + 20) {
                    hackable.addPostHackInfo(entity, curInfo, player);
                } else if (isLookingAtTarget) {
                    hackable.addHackInfo(entity, curInfo, player);
                    HackClientHandler.addKeybindTooltip(curInfo);
                }
            }
        }
    }
}
