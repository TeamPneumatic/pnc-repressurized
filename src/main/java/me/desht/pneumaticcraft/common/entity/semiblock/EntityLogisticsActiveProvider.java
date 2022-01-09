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

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.semiblock.ISpecificProvider;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public class EntityLogisticsActiveProvider extends EntityLogisticsFrame implements ISpecificProvider {
    public EntityLogisticsActiveProvider(EntityType<?> entityTypeIn, World worldIn) {
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
    protected ContainerType<?> getContainerType() {
        return ModContainers.LOGISTICS_FRAME_PROVIDER.get();
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
