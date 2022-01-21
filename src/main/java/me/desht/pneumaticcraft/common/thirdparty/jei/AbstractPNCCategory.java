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

package me.desht.pneumaticcraft.common.thirdparty.jei;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public abstract class AbstractPNCCategory<T> implements IRecipeCategory<T> {
    private final Component localizedName;
    private final IDrawable background;
    private final IDrawable icon;
    private final ResourceLocation id;
    private final Class<? extends T> cls;

    protected AbstractPNCCategory(ResourceLocation id, Class<? extends T> cls, Component localizedName, IDrawable background, IDrawable icon) {
        this.id = id;
        this.cls = cls;
        this.localizedName = localizedName;
        this.background = background;
        this.icon = icon;
    }

    @Override
    public Component getTitle() {
        return localizedName;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public ResourceLocation getUid() {
        return id;
    }

    @Override
    public Class<? extends T> getRecipeClass() {
        return cls;
    }

    static IGuiHelper guiHelper() {
        return JEIPlugin.jeiHelpers.getGuiHelper();
    }
}
