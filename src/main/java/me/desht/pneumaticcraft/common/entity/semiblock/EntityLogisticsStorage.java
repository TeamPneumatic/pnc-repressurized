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
import me.desht.pneumaticcraft.common.semiblock.ISpecificRequester;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public class EntityLogisticsStorage extends EntityLogisticsFrame implements ISpecificProvider, ISpecificRequester {
    private int minItems = 1;
    private int minFluid = 1;

    public EntityLogisticsStorage(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Override
    public int getColor() {
        return 0xFFFFFF00;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.MODEL_LOGISTICS_FRAME_STORAGE;  // TODO ridanisaurus
    }

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    protected ContainerType<?> getContainerType() {
        return ModContainers.LOGISTICS_FRAME_STORAGE.get();
    }

    @Override
    public boolean canProvide(ItemStack providingStack) {
        return passesFilter(providingStack);
    }

    @Override
    public boolean canProvide(FluidStack providingStack) {
        return passesFilter(providingStack.getFluid());
    }

    @Override
    public int amountRequested(ItemStack stack) {
        return passesFilter(stack) ? stack.getMaxStackSize() : 0;
    }

    @Override
    public int amountRequested(FluidStack stack) {
        return passesFilter(stack.getFluid()) ? stack.getAmount() : 0;
    }

    @Override
    public int getMinItemOrderSize() {
        return minItems;
    }

    @Override
    public void setMinItemOrderSize(int min) {
        minItems = min;
    }

    @Override
    public int getMinFluidOrderSize() {
        return minFluid;
    }

    @Override
    public void setMinFluidOrderSize(int min) {
        minFluid = min;
    }
}
