package me.desht.pneumaticcraft.common.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.awt.*;

public class ItemColorHandler {
    public static void registerColorHandlers() {
        final IItemColor ammoColor = (stack, tintIndex) -> {
            switch (tintIndex) {
                case 1:
                    return getAmmoColor(stack);
                default:
                    return Color.WHITE.getRGB();
            }
        };
        final IItemColor plasticColor = (stack, tintIndex) -> {
            int plasticColour = ItemPlastic.getColour(stack);
            return plasticColour >= 0 ? plasticColour : 0xffffff;
        };

        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(ammoColor, Itemss.GUN_AMMO);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(plasticColor, Itemss.PLASTIC);
    }

    public static int getAmmoColor(@Nonnull ItemStack stack) {
        ItemStack potion = ItemGunAmmo.getPotion(stack);
        return potion.isEmpty() ? 0x00FFFF00 : Minecraft.getMinecraft().getItemColors().colorMultiplier(potion, 0);
    }
}
