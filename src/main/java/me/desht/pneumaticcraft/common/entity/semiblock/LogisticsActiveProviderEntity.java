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

package me.desht.pneumaticcraft.common.entity.semiblock;

import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import me.desht.pneumaticcraft.common.semiblock.ISpecificProvider;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

public class LogisticsActiveProviderEntity extends AbstractLogisticsFrameEntity implements ISpecificProvider {
    public LogisticsActiveProviderEntity(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Override
    public int getColor() {
        return 0xFF93228C;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.MODEL_LOGISTICS_FRAME_ACTIVE_PROVIDER;  // TODO ridanisaurus
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    protected MenuType<?> getContainerType() {
        return ModMenuTypes.LOGISTICS_FRAME_PROVIDER.get();
    }

    @Override
    public boolean canProvide(ItemStack providingStack) {
        return passesFilter(providingStack);
    }

    @Override
    public boolean canProvide(FluidStack providingStack) {
        return passesFilter(providingStack.getFluid());
    }
}
