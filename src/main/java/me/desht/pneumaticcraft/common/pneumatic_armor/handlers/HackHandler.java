/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.pneumatic_armor.handlers;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableBlock;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.pneumatic_armor.BaseArmorUpgradeHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorExtensionData;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.event.HackTickHandler;
import me.desht.pneumaticcraft.common.hacking.HackManager;
import me.desht.pneumaticcraft.common.hacking.WorldAndCoord;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketHackingBlockFinish;
import me.desht.pneumaticcraft.common.network.PacketHackingEntityFinish;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;

import java.util.function.Supplier;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class HackHandler extends BaseArmorUpgradeHandler<HackHandler.HackData> {
    @Override
    public ResourceLocation getID() {
        return RL("hacking");
    }

    @Override
    public EnumUpgrade[] getRequiredUpgrades() {
        return new EnumUpgrade[] { EnumUpgrade.SECURITY};
    }

    @Override
    public float getIdleAirUsage(ICommonArmorHandler armorHandler) {
        return 0;
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    @Override
    public Supplier<HackData> extensionData() {
        return HackData::new;
    }

    @Override
    public void tick(ICommonArmorHandler commonArmorHandler, boolean enabled) {
        Player player = commonArmorHandler.getPlayer();
        if (!player.level.isClientSide) {
            commonArmorHandler.getExtensionData(this).tickServerSide(player);
        }
    }

    public static class HackData implements IArmorExtensionData {
        private int hackTime;
        private WorldAndCoord hackedBlockPos;
        private Entity hackedEntity;

        private void tickServerSide(Player player) {
            if (hackedBlockPos != null) {
                tickBlockHack(player);
            } else if (hackedEntity != null) {
                tickEntityHack(player);
            }
        }

        private void tickBlockHack(Player player) {
            IHackableBlock hackableBlock = HackManager.getHackableForBlock(hackedBlockPos.world, hackedBlockPos.pos, player);
            if (hackableBlock != null) {
                BlockGetter world = hackedBlockPos.world;
                if (++hackTime >= hackableBlock.getHackTime(world, hackedBlockPos.pos, player)) {
                    hackableBlock.onHackComplete(player.level, hackedBlockPos.pos, player);
                    HackTickHandler.instance().trackBlock(player.level, hackedBlockPos.pos, hackableBlock);
                    NetworkHandler.sendToAllTracking(new PacketHackingBlockFinish(hackedBlockPos), player.level, player.blockPosition());
                    setHackedBlockPos(null);
                    AdvancementTriggers.BLOCK_HACK.trigger((ServerPlayer) player);  // safe to cast, this is server-side
                }
            } else {
                setHackedBlockPos(null);
            }
        }

        private void tickEntityHack(Player player) {
            IHackableEntity hackableEntity = HackManager.getHackableForEntity(hackedEntity, player);
            if (hackableEntity != null) {
                if (++hackTime >= hackableEntity.getHackTime(hackedEntity, player)) {
                    if (hackedEntity.isAlive()) {
                        hackableEntity.onHackFinished(hackedEntity, player);
                        HackTickHandler.instance().trackEntity(hackedEntity, hackableEntity);
                        NetworkHandler.sendToAllTracking(new PacketHackingEntityFinish(hackedEntity), hackedEntity);
                        AdvancementTriggers.ENTITY_HACK.trigger((ServerPlayer) player);  // safe to cast, this is server-side
                    }
                    setHackedEntity(null);
                }
            } else {
                setHackedEntity(null);
            }
        }

        public void setHackedBlockPos(WorldAndCoord pos) {
            this.hackedBlockPos = pos;
            this.hackedEntity = null;
            this.hackTime = 0;
        }

        public void setHackedEntity(Entity entity) {
            hackedEntity = entity;
            hackedBlockPos = null;
            hackTime = 0;
        }
    }
}
