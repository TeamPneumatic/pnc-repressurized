package me.desht.pneumaticcraft.client.pneumatic_armor.entity_tracker;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IEntityTrackEntry;
import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IHackableEntity;
import me.desht.pneumaticcraft.client.pneumatic_armor.ClientArmorRegistry;
import me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler.EntityTrackerClientHandler;
import me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler.HackClientHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderEntityTarget;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.hacking.HackManager;
import me.desht.pneumaticcraft.common.hacking.HackTickTracker;
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
        IHackableEntity<?> hackable = HackManager.getHackableForEntity(entity, player);
        if (hackable != null && hackable.getHackableClass().isAssignableFrom(entity.getClass())) {
            int hackTime = ClientArmorRegistry.getInstance()
                    .getClientHandler(CommonUpgradeHandlers.entityTrackerHandler, EntityTrackerClientHandler.class)
                    .getTargetsStream()
                    .filter(target -> target.entity == entity)
                    .findFirst()
                    .map(RenderEntityTarget::getHackTime)
                    .orElse(0);
            boolean entityTracked = HackTickTracker.getInstance(entity.level()).isEntityTracked(entity);
            if (hackTime == 0) {
                if (isLookingAtTarget && !entityTracked) {
                    hackable._addHackInfo(entity, curInfo, player);
                    HackClientHandler.addKeybindTooltip(curInfo);
                }
            } else {
                int requiredHackTime = hackable._getHackTime(entity, player);
                if (requiredHackTime > 0) {
                    int percentageComplete = hackTime * 100 / requiredHackTime;
                    if (percentageComplete < 100) {
                        curInfo.add(xlate("pneumaticcraft.armor.hacking.hacking", percentageComplete));
                    } else if (hackTime < requiredHackTime + 20 || entityTracked) {
                        hackable._addPostHackInfo(entity, curInfo, player);
                    } else if (isLookingAtTarget) {
                        hackable._addHackInfo(entity, curInfo, player);
                        HackClientHandler.addKeybindTooltip(curInfo);
                    }
                }
            }
        }
    }
}
