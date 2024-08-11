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

package me.desht.pneumaticcraft.common.drone.ai;

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.drone.progwidgets.IEntityProvider;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

public class DroneAIEntityImport extends DroneEntityBase<IEntityProvider, Entity> {

    public DroneAIEntityImport(IDrone drone, IEntityProvider progWidget) {
        super(drone, progWidget);
    }

    @Override
    protected boolean isEntityValid(Entity entity) {
        if (entity instanceof Player p && p.isSpectator()) {
            return false;
        } else if (entity instanceof LivingEntity || entity instanceof AbstractMinecart || entity instanceof Boat) {
            return drone.getCarryingEntities().isEmpty();
        } else if (ConfigHelper.common().drones.dronesCanImportXPOrbs.get() && entity instanceof ExperienceOrb) {
            return PneumaticCraftUtils.fillTankWithOrb(drone.getFluidTank(), (ExperienceOrb) entity, FluidAction.SIMULATE);
        }
        return false;
    }

    @Override
    protected boolean doAction() {
        if (ConfigHelper.common().drones.dronesCanImportXPOrbs.get() && targetedEntity instanceof ExperienceOrb orb) {
            ItemStack heldStack = drone.getInv().getStackInSlot(0);
            if (!heldStack.isEmpty() && heldStack.isDamaged() && EnchantmentHelper.has(heldStack, EnchantmentEffectComponents.REPAIR_WITH_XP)) {
                int toRepair = Math.min((int)(orb.value * heldStack.getXpRepairRatio()), heldStack.getDamageValue());
                orb.value -= toRepair / 2;  // see ExperienceOrbEntity#durabilityToXp()
                heldStack.setDamageValue(heldStack.getDamageValue() - toRepair);
            }
            if (orb.value <= 0 || PneumaticCraftUtils.fillTankWithOrb(drone.getFluidTank(), orb, FluidAction.EXECUTE)) {
                targetedEntity.discard();
            }
        } else {
            drone.setCarryingEntity(targetedEntity);
        }
        return false;
    }
}
