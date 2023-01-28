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

package me.desht.pneumaticcraft.client.util;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import me.desht.pneumaticcraft.client.gui.AbstractPneumaticCraftContainerScreen;
import me.desht.pneumaticcraft.client.gui.programmer.AbstractProgWidgetScreen;
import me.desht.pneumaticcraft.client.pneumatic_armor.ArmorUpgradeClientRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.EntityTrackerClientHandler;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import me.desht.pneumaticcraft.common.inventory.AbstractPneumaticCraftMenu;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Miscellaneous client-side utilities.  Used to wrap client-only code in methods safe to call from classes that could
 * be loaded on dedicated server (mainly packet handlers and event handlers, but could be anywhere...)
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
    public static void emitParticles(Level world, BlockPos pos, ParticleOptions particle, double yOffset) {
        float xOff = world.random.nextFloat() * 0.6F + 0.2F;
        float zOff = world.random.nextFloat() * 0.6F + 0.2F;
        getClientLevel().addParticle(particle,
                pos.getX() + xOff, pos.getY() + yOffset, pos.getZ() + zOff,
                0, 0, 0);
    }

    public static void emitParticles(Level world, BlockPos pos, ParticleOptions particle) {
        emitParticles(world, pos, particle, 1.2);
    }

    @Nonnull
    public static ItemStack getWornArmor(EquipmentSlot slot) {
        return getClientPlayer().getItemBySlot(slot);
    }

    public static void addDroneToHudHandler(DroneEntity drone, BlockPos pos) {
        ArmorUpgradeClientRegistry.getInstance()
                .getClientHandler(CommonUpgradeHandlers.entityTrackerHandler, EntityTrackerClientHandler.class)
                .getTargetsStream()
                .filter(target -> target.entity == drone)
                .forEach(target -> target.getDroneAIRenderer(drone).addBlackListEntry(drone.level, pos));
    }

    public static boolean isKeyDown(int keyCode) {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), keyCode);
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
    public static void openContainerGui(MenuType<? extends AbstractContainerMenu> type, Component displayString) {
        MenuScreens.create(type, Minecraft.getInstance(), -1, displayString);
    }

    /**
     * Close a container-based GUI, and restore the player's openContainer. See {@link ClientUtils#openContainerGui(MenuType, Component)}
     *
     * @param parentScreen the previously-opened GUI, which will be re-opened
     */
    public static void closeContainerGui(Screen parentScreen) {
        Minecraft.getInstance().setScreen(parentScreen);
        if (parentScreen instanceof AbstractContainerScreen) {
            getClientPlayer().containerMenu = ((AbstractContainerScreen<?>) parentScreen).getMenu();
        } else if (parentScreen instanceof AbstractProgWidgetScreen) {
            getClientPlayer().containerMenu = ((AbstractProgWidgetScreen<?>) parentScreen).getProgrammerContainer();
        }
    }

    /**
     * For use where we can't reference Minecraft directly, e.g. packet handling code.
     * @return the client world
     */
    @Nonnull
    public static Level getClientLevel() {
        return Objects.requireNonNull(Minecraft.getInstance().level);
    }

    public static Optional<Level> getOptionalClientLevel() {
        return Optional.ofNullable(Minecraft.getInstance().level);
    }

    @Nonnull
    public static Player getClientPlayer() {
        return Objects.requireNonNull(Minecraft.getInstance().player);
    }

    public static boolean hasShiftDown() {
        return Screen.hasShiftDown();
    }

    /**
     * Get a BE client-side.  Convenience method for packet handling code, primarily.
     * @return a block entity or null
     */
    public static BlockEntity getBlockEntity(BlockPos pos) {
        return getClientLevel().getBlockEntity(pos);
    }

    /**
     * Same as AWT Rectangle's intersects() method, but we don't have access to AWT...
     *
     * @param rect a rectangle
     * @param x x coord
     * @param y y coord
     * @param w width
     * @param h height
     * @return true if intersection, false otherwise
     */
    public static boolean intersects(Rect2i rect, double x, double y, double w, double h) {
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

    /**
     * For the programmer GUI
     *
     * @return true if the screen res > 700x512
     */
    public static boolean isScreenHiRes() {
        Window mw = Minecraft.getInstance().getWindow();
        return mw.getGuiScaledWidth() > 700 && mw.getGuiScaledHeight() > 512;
    }

    public static float getBrightnessAtWorldHeight() {
        Player player = getClientPlayer();
        BlockPos pos = new BlockPos.MutableBlockPos(player.getX(), getClientLevel().getMaxBuildHeight(), player.getZ());
        if (player.level.isLoaded(pos)) {
            return player.level.dimensionType().brightness(player.level.getMaxLocalRawBrightness(pos));
        } else {
            return 0.0F;
        }
    }

    public static int getLightAt(BlockPos pos) {
        return LevelRenderer.getLightColor(getClientLevel(), pos);
    }

    public static int getStringWidth(String line) {
        return Minecraft.getInstance().font.width(line);
    }

    public static boolean isGuiOpen(BlockEntity te) {
        if (Minecraft.getInstance().screen instanceof AbstractPneumaticCraftContainerScreen) {
            return ((AbstractPneumaticCraftContainerScreen<?,?>) Minecraft.getInstance().screen).te == te;
        } else {
            return false;
        }
    }

    public static float[] getTextureUV(BlockState state, Direction face) {
        if (state == null) return null;
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        List<BakedQuad> quads = model.getQuads(state, face, getClientLevel().random, EmptyModelData.INSTANCE);
        if (!quads.isEmpty()) {
            TextureAtlasSprite sprite = quads.get(0).getSprite();
            return new float[] { sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1() };
        } else {
            return null;
        }
    }

    public static void spawnEntityClientside(Entity e) {
        ((ClientLevel) getClientLevel()).putNonPlayerEntity(e.getId(), e);
    }

    public static String translateDirection(Direction d) {
        return I18n.get("pneumaticcraft.gui.tooltip.direction." + d.toString());
    }

    public static Component translateDirectionComponent(Direction d) {
        return new TranslatableComponent("pneumaticcraft.gui.tooltip.direction." + d.toString());
    }

    /**
     * Because keyBinding.getTranslationKey() doesn't work that well...
     *
     * @param keyBinding the keybinding
     * @return a human-friendly string representation
     */
    public static Component translateKeyBind(KeyMapping keyBinding) {
        return keyBinding.getKeyModifier().getCombinedName(keyBinding.getKey(), () -> {
            Component s = keyBinding.getKey().getDisplayName();
            // small kludge to clearly distinguish keypad from non-keypad keys
            if (keyBinding.getKey().getType() == InputConstants.Type.KEYSYM
                    && keyBinding.getKey().getValue() >= GLFW.GLFW_KEY_KP_0
                    && keyBinding.getKey().getValue() <= GLFW.GLFW_KEY_KP_EQUAL) {
                return new TextComponent("KP_").append(s);
            } else {
                return s;
            }
        }).copy().withStyle(ChatFormatting.YELLOW);
    }

    /**
     * Add some context-sensitive info to an item's tooltip, based on the currently-open GUI.
     *
     * @param stack the item stack
     * @param tooltip tooltip to add data to
     */
    public static void addGuiContextSensitiveTooltip(ItemStack stack, List<Component> tooltip) {
        Screen screen = Minecraft.getInstance().screen;

        if (screen != null) {
            String subKey = screen.getClass().getSimpleName().toLowerCase(Locale.ROOT);
            Item item = stack.getItem();
            String base = item instanceof BlockItem ? "gui.tooltip.block" : "gui.tooltip.item";
            String k = String.join(".", base, item.getRegistryName().getNamespace(), item.getRegistryName().getPath(), subKey);
            if (I18n.exists(k)) {
                tooltip.addAll(GuiUtils.xlateAndSplit(k).stream().map(s -> s.copy().withStyle(ChatFormatting.GRAY)).toList());
            }
        }
    }

    /**
     * Get the render distance based on current game settings
     *
     * @return the squared render distance, in blocks
     */
    public static int getRenderDistanceThresholdSq() {
        int d = Minecraft.getInstance().options.renderDistance * 16;
        return d * d;
    }

    public static boolean isFirstPersonCamera() {
        return Minecraft.getInstance().options.getCameraType().isFirstPerson();
    }

    /**
     * Determine how much to scale a graphic for the target position, based on player's distance and angle (dot product
     * of look vector and offset to pos)
     * @param targetPos the position being viewed
     * @return a scaling factor
     */
    public static float calculateViewScaling(Vec3 targetPos) {
        Player player = ClientUtils.getClientPlayer();
        Vec3 vec1 = targetPos.subtract(player.position());
        double angle = player.getLookAngle().dot(vec1.normalize());
        float size = 1f;
        if (angle >= 0.8) {
            double dist = Math.max(0.0001, Math.sqrt(player.distanceToSqr(targetPos)));
            double s = 0.8 - (1 / dist);
            size = size * (float)((angle - s) * dist);
        }
        return size;
    }

    public static float getStatSizeMultiplier(double dist) {
        if (dist < 4) {
            return Math.max(0.3f, (float) (dist / 4));
        } else if (dist < 10) {
            return 1f;
        } else {
            return (float) (dist / 10);
        }
    }

    /**
     * Called from PacketUpdateGui to sync data from the server-side block entity via open container
     * @param syncId index of the block entity field to be sync'd
     * @param value value to sync
     */
    public static void syncViaOpenContainerScreen(int syncId, Object value) {
        if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> screen) {
            AbstractContainerMenu container = screen.getMenu();
            if (container instanceof AbstractPneumaticCraftMenu<?> pncMenu) {
                pncMenu.updateField(syncId, value);
            }
            if (screen instanceof AbstractPneumaticCraftContainerScreen<?,?> pncScreen) {
                pncScreen.onGuiUpdate();
            }
        }
    }
}
