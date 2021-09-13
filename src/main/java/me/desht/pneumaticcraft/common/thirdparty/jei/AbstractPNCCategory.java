package me.desht.pneumaticcraft.common.thirdparty.jei;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public abstract class AbstractPNCCategory<T> implements IRecipeCategory<T> {
    private final ITextComponent localizedName;
    private final IDrawable background;
    private final IDrawable icon;
    private final ResourceLocation id;
    private final Class<? extends T> cls;

    protected AbstractPNCCategory(ResourceLocation id, Class<? extends T> cls, ITextComponent localizedName, IDrawable background, IDrawable icon) {
        this.id = id;
        this.cls = cls;
        this.localizedName = localizedName;
        this.background = background;
        this.icon = icon;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public ITextComponent getTitleAsTextComponent() {
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
