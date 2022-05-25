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

import me.desht.pneumaticcraft.common.core.ModItems;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemNonDespawning extends Item {
    public ItemNonDespawning() {
        super(ModItems.defaultProps());
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entityItem) {
        // Note: the /give command creates a fake item entity which despawns in one tick by setting the entity's age
        // to one tick less than the entity's lifetime; we need to check for that
        // https://github.com/TeamPneumatic/pnc-repressurized/issues/1012
        if (!entityItem.level.isClientSide && entityItem.age < entityItem.getItem().getEntityLifespan(entityItem.level) - 1) {
            entityItem.setExtendedLifetime();
        }
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> curInfo, ITooltipFlag moreInfo) {
        super.appendHoverText(stack, worldIn, curInfo, moreInfo);

        curInfo.add(xlate("pneumaticcraft.gui.tooltip.doesNotDespawn"));
    }
}
