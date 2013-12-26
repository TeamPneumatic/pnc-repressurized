package pneumaticCraft.api.client;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

/**
 * With this class you can retrieve new instances of the PneumaticCraft's IGuiAnimatedStat implementation. You can use these in Gui's as 
 * well as anywhere you like. When you use these in Gui's you need to pass a valid GuiContainer instance, if you don't you can just pass
 * null.
 */
public class GuiAnimatedStatSupplier{
    private static Class animatedStatClass;

    public static IGuiAnimatedStat getAnimatedStat(GuiContainer gui, int backgroundColor){
        return getAnimatedStat(new Class[]{GuiContainer.class, int.class}, gui, backgroundColor);
    }

    /**
     * Returns a GuiAnimatedStat which uses an itemstack as statistic icon.
     * @param gui
     * @param iconStack
     * @param backgroundColor
     * @return
     */
    public static IGuiAnimatedStat getAnimatedStat(GuiContainer gui, ItemStack iconStack, int backgroundColor){
        return getAnimatedStat(new Class[]{GuiContainer.class, int.class, ItemStack.class}, gui, backgroundColor, iconStack);
    }

    /**
     * Returns a GuiAnimatedStat which uses a texture location as statistic icon.
     * @param gui
     * @param iconTexture
     * @param backgroundColor
     * @return
     */
    public static IGuiAnimatedStat getAnimatedStat(GuiContainer gui, String iconTexture, int backgroundColor){
        return getAnimatedStat(new Class[]{GuiContainer.class, int.class, String.class}, gui, backgroundColor, iconTexture);
    }

    private static IGuiAnimatedStat getAnimatedStat(Class[] constructorClasses, Object... constructorParameters){
        try {
            if(animatedStatClass == null) animatedStatClass = Class.forName("pneumaticCraft.client.gui.GuiAnimatedStat");
            return (IGuiAnimatedStat)animatedStatClass.getConstructor(constructorClasses).newInstance(constructorParameters);
        } catch(Exception e) {
            System.err.println("Failed to retrieve an GuiAnimatedStat intance of PneumaticCraft.");
        }
        return null;
    }
}
