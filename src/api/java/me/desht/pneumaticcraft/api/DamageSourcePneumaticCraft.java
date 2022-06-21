/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class DamageSourcePneumaticCraft extends DamageSource {
    /**
     * {@code @Deprecated} Will be removed in 1.19 in favor of Vanilla Freeze Damage Source
     */
    @Deprecated(forRemoval = true)
    public static final DamageSource FREEZING = DamageSource.FREEZE;
    public static final DamageSource PRESSURE = new DamageSourcePneumaticCraft("pressure", 2).bypassArmor();
    public static final DamageSource ETCHING_ACID = new DamageSourcePneumaticCraft("acid", 2);
    public static final DamageSource SECURITY_STATION = new DamageSourcePneumaticCraft("securityStation").bypassArmor();
    public static final DamageSource PLASTIC_BLOCK = new DamageSourcePneumaticCraft("plastic_block", 2);

    public static boolean isDroneOverload(DamageSource src) {
        return src instanceof DamageSourceDroneOverload;
    }

    private final int deathMessageCount;

    private DamageSourcePneumaticCraft(String damageType, int messages) {
        super(damageType);
        deathMessageCount = messages;
    }

    DamageSourcePneumaticCraft(String damageType) {
        this(damageType, 1);
    }

    @Override
    public Component getLocalizedDeathMessage(LivingEntity dyingEntity) {
        int messageNumber = dyingEntity.getRandom().nextInt(deathMessageCount) + 1;

        LivingEntity killer = dyingEntity.getKillCredit();
        String s = PneumaticRegistry.MOD_ID + ".death.attack." + msgId + messageNumber;
        String s1 = s + ".player";
        return killer != null && I18n.exists(s1) ?
                Component.translatable(s1, dyingEntity.getDisplayName(), killer.getDisplayName()) :
                Component.translatable(s, dyingEntity.getDisplayName());
    }

    public static class DamageSourceDroneOverload extends DamageSourcePneumaticCraft {
        private final String msgKey;
        private final Object[] params;

        public DamageSourceDroneOverload(String msgKey, Object... params) {
            super("droneOverload");
            bypassArmor();
            bypassInvul();
            this.msgKey = msgKey;
            this.params = new Object[params.length];
            System.arraycopy(params, 0, this.params, 0, params.length);
        }

        @Override
        public Component getLocalizedDeathMessage(LivingEntity dyingEntity) {
            return Component.translatable("pneumaticcraft.death.drone.overload." + msgKey, params);
        }
    }
}
