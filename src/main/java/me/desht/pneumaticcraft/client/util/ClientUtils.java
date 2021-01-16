package me.desht.pneumaticcraft.client.util;

import me.desht.pneumaticcraft.client.gui.programmer.GuiProgWidgetOptionBase;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.EntityTrackerClientHandler;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import net.minecraft.block.BlockState;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.FirstPersonRenderer;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.IParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Miscellaneous client-side utilities
 */
public class ClientUtils {
    /**
     * Emit particles from just above the given blockpos, which is generally a machine or similar.
     * Only call this clientside.
     *
     * @param world the world
     * @param pos the block pos
     * @param particle the particle type
     */
    public static void emitParticles(World world, BlockPos pos, IParticleData particle) {
        float xOff = world.rand.nextFloat() * 0.6F + 0.2F;
        float zOff = world.rand.nextFloat() * 0.6F + 0.2F;
        getClientWorld().addParticle(particle,
                pos.getX() + xOff, pos.getY() + 1.2, pos.getZ() + zOff,
                0, 0, 0);
    }

    @Nonnull
    public static ItemStack getWornArmor(EquipmentSlotType slot) {
        return Minecraft.getInstance().player.getItemStackFromSlot(slot);
    }

    public static void addDroneToHudHandler(EntityDrone drone, BlockPos pos) {
        HUDHandler.getInstance().getSpecificRenderer(EntityTrackerClientHandler.class).getTargetsStream()
                .filter(target -> target.entity == drone)
                .forEach(target -> target.getDroneAIRenderer().addBlackListEntry(drone.world, pos));
    }

    public static boolean isKeyDown(int keyCode) {
        return InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), keyCode);
    }

    /**
     * Open a container-based GUI client-side. This is a cheeky hack, but appears to work. However, it is important
     * to call {@link ClientUtils#closeContainerGui(Screen)} from the opened GUI's {@code onClose()} method to restore
     * the player's openContainer to the correct container. Therefore the GUI being opened should remember the previous
     * open GUI, and call {@link ClientUtils#closeContainerGui(Screen)} with that GUI as an argument.
     *
     * @param type the container type to open
     * @param displayString container's display name
     */
    public static void openContainerGui(ContainerType<? extends Container> type, ITextComponent displayString) {
        ScreenManager.openScreen(type, Minecraft.getInstance(), -1, displayString);
    }

    /**
     * Close a container-based GUI, and restore the player's openContainer. See {@link ClientUtils#openContainerGui(ContainerType, ITextComponent)}
     *
     * @param parentScreen the previous-opened GUI, which will be re-opened
     */
    public static void closeContainerGui(Screen parentScreen) {
        Minecraft mc = Minecraft.getInstance();
        mc.displayGuiScreen(parentScreen);
        if (parentScreen instanceof ContainerScreen) {
            mc.player.openContainer = ((ContainerScreen<?>) parentScreen).getContainer();
        } else if (parentScreen instanceof GuiProgWidgetOptionBase) {
            mc.player.openContainer = ((GuiProgWidgetOptionBase<?>) parentScreen).getProgrammerContainer();
        }
    }

    /**
     * For use where we can't reference Minecraft directly, e.g. packet handling code.
     * @return the client world
     */
    public static World getClientWorld() {
        return Minecraft.getInstance().world;
    }

    public static PlayerEntity getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    public static boolean hasShiftDown() {
        return Screen.hasShiftDown();
    }

    /**
     * Get a TE client-side.  Convenience method for packet handling code, primarily.
     * @return a tile entity or null
     */
    public static TileEntity getClientTE(BlockPos pos) {
        return Minecraft.getInstance().world.getTileEntity(pos);
    }

    /**
     * See AWT Rectangle's intersects() method
     *
     * @param rect
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     */
    public static boolean intersects(Rectangle2d rect, double x, double y, double w, double h) {
        if (rect.getWidth() <= 0 || rect.getHeight() <= 0 || w <= 0 || h <= 0) {
            return false;
        }
        double x0 = rect.getX();
        double y0 = rect.getY();
        return (x + w > x0 &&
                y + h > y0 &&
                x < x0 + rect.getWidth() &&
                y < y0 + rect.getHeight());
    }

    public static void suppressItemEquipAnimation() {
        FirstPersonRenderer renderer = Minecraft.getInstance().getFirstPersonRenderer();
        renderer.equippedProgressMainHand = 1;
        renderer.prevEquippedProgressMainHand = 1;
    }

    /**
     * For the programmer GUI
     *
     * @return true if the screen res > 700x512
     */
    public static boolean isScreenHiRes() {
        MainWindow mw = Minecraft.getInstance().getMainWindow();
        return mw.getScaledWidth() > 700 && mw.getScaledHeight() > 512;
    }

    public static float getBrightnessAtWorldHeight() {
        PlayerEntity player = getClientPlayer();
        // TODO world.getMaxHeight() ?
        BlockPos pos = new BlockPos.Mutable(player.getPosX(), 255, player.getPosZ());
        if (player.world.isBlockLoaded(pos)) {
            return player.world.getDimensionType().getAmbientLight(player.world.getLight(pos));
        } else {
            return 0.0F;
        }
    }

    public static int getLightAt(BlockPos pos) {
        return WorldRenderer.getCombinedLight(Minecraft.getInstance().world, pos);
    }

    public static int getStringWidth(String line) {
        return Minecraft.getInstance().getRenderManager().getFontRenderer().getStringWidth(line);
    }

    public static boolean isGuiOpen() {
        return Minecraft.getInstance().currentScreen != null;
    }

    public static float[] getTextureUV(BlockState state, Direction face) {
        if (state == null) return null;
        IBakedModel model = Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(state);
        List<BakedQuad> quads = model.getQuads(state, face, Minecraft.getInstance().world.rand, EmptyModelData.INSTANCE);
        if (!quads.isEmpty()) {
            TextureAtlasSprite sprite = quads.get(0).getSprite();
            return new float[] { sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV() };
        } else {
            return null;
        }
    }

    public static void spawnEntityClientside(Entity e) {
        ((ClientWorld) getClientWorld()).addEntity(e.getEntityId(), e);
    }

    public static String translateDirection(Direction d) {
        return I18n.format("pneumaticcraft.gui.tooltip.direction." + d.toString());
    }

    public static ITextComponent translateDirectionComponent(Direction d) {
        return new TranslationTextComponent("pneumaticcraft.gui.tooltip.direction." + d.toString());
    }

    /**
     * Because keyBinding.getTranslationKey() doesn't work that well...
     *
     * @param keyBinding the keybinding
     * @return a human-friendly string representation
     */
    public static ITextComponent translateKeyBind(KeyBinding keyBinding) {
        return keyBinding.getKeyModifier().getCombinedName(keyBinding.getKey(), () -> {
            ITextComponent s = keyBinding.getKey().func_237520_d_();
            // small kludge to clearly distinguish keypad from non-keypad keys
            if (keyBinding.getKey().getType() == InputMappings.Type.KEYSYM
                    && keyBinding.getKey().getKeyCode() >= GLFW.GLFW_KEY_KP_0
                    && keyBinding.getKey().getKeyCode() <= GLFW.GLFW_KEY_KP_EQUAL) {
                return new StringTextComponent("KP_").append(s);
            } else {
                return s;
            }
        }).deepCopy().mergeStyle(TextFormatting.YELLOW);
    }

    /**
     * Add some context-sensitive info to an item's tooltip, based on the currently-open GUI.
     *
     * @param stack the item stack
     * @param tooltip tooltip to add data to
     */
    public static void addGuiContextSensitiveTooltip(ItemStack stack, List<ITextComponent> tooltip) {
        Screen screen = Minecraft.getInstance().currentScreen;

        if (screen != null) {
            String subKey = screen.getClass().getSimpleName().toLowerCase(Locale.ROOT);
            Item item = stack.getItem();
            String base = item instanceof BlockItem ? "gui.tooltip.block" : "gui.tooltip.item";
            String k = String.join(".", base, item.getRegistryName().getNamespace(), item.getRegistryName().getPath(), subKey);
            if (I18n.hasKey(k)) {
                tooltip.addAll(GuiUtils.xlateAndSplit(k).stream().map(s -> s.deepCopy().mergeStyle(TextFormatting.GRAY)).collect(Collectors.toList()));
            }
        }
    }
}
