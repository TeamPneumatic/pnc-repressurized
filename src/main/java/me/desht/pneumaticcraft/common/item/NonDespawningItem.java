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

package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.mixin.accessors.ItemEntityAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class NonDespawningItem extends Item {
    public NonDespawningItem() {
        super(ModItems.defaultProps());
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entityItem) {
        // Note: the /give command creates a fake item entity which despawns in one tick by setting the entity's age
        // to one tick less than the entity's lifetime; we need to check for that
        // https://github.com/TeamPneumatic/pnc-repressurized/issues/1012
        if (!entityItem.level().isClientSide && ((ItemEntityAccess)entityItem).getAge() < entityItem.getItem().getEntityLifespan(entityItem.level()) - 1) {
            entityItem.setExtendedLifetime();
        }
        return false;
    }

    /**
     * allows items to add custom lines of information to the mouseover description
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> curInfo, TooltipFlag moreInfo) {
        super.appendHoverText(stack, worldIn, curInfo, moreInfo);
        curInfo.add(xlate("pneumaticcraft.gui.tooltip.doesNotDespawn"));
    }
}
