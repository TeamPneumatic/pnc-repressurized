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
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.client.gui.AbstractPneumaticCraftContainerScreen;
import me.desht.pneumaticcraft.client.gui.programmer.AbstractProgWidgetScreen;
import me.desht.pneumaticcraft.client.pneumatic_armor.ClientArmorRegistry;
import me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler.EntityTrackerClientHandler;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import me.desht.pneumaticcraft.common.fluid.FuelRegistry;
import me.desht.pneumaticcraft.common.inventory.AbstractPneumaticCraftMenu;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.thirdparty.ModNameCache;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

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
        ClientArmorRegistry.getInstance()
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

    public static Optional<Player> getOptionalClientPlayer() {
        return Optional.ofNullable(Minecraft.getInstance().player);
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

//    public static float getBrightnessAtWorldHeight() {
//        Player player = getClientPlayer();
//        BlockPos pos = new BlockPos.MutableBlockPos(player.getX(), getClientLevel().getMaxBuildHeight(), player.getZ());
//        if (player.level.isLoaded(pos)) {
//            return player.level.dimensionType().brightness(player.level.getMaxLocalRawBrightness(pos));
//        } else {
//            return 0.0F;
//        }
//    }

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
        List<BakedQuad> quads = model.getQuads(state, face, getClientLevel().random, ModelData.EMPTY, null);
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
        return Component.translatable("pneumaticcraft.gui.tooltip.direction." + d.toString());
    }

    /**
     * Because keyBinding.getTranslationKey() doesn't work that well...
     *
     * @param keyBinding the keybinding
     * @return a human-friendly string representation
     */
    public static Component translateKeyBind(KeyMapping keyBinding) {
        return keyBinding.getKeyModifier().getCombinedName(keyBinding.getKey(), () -> keyBinding.getKey().getDisplayName())
                .copy().withStyle(ChatFormatting.YELLOW);
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
            ResourceLocation regName = PneumaticCraftUtils.getRegistryName(item).orElseThrow();
            String k = String.join(".", base, regName.getNamespace(), regName.getPath(), subKey);
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
        int d = Minecraft.getInstance().options.renderDistance().get() * 16;
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

    /**
     * Format a nice list of all the known fuels and a quality factor, line-wrapped sensibly for the given width.
     *
     * @param header header line to add at start
     * @param suggestedWidth suggested maximum width, for line-wrapping purposes (returned width may be smaller, but never larger)
     * @param fluidFunc a function to map a fuel fluid to some quality property of the fluid (e.g. burn time, lighting time...)
     * @param includeBurnRate true to append burn rate multiple info
     * @return an actual display with, and a formatted list of fuels, suitable for client display
     */
    public static Pair<Integer,List<Component>> formatFuelList(Component header, int suggestedWidth, ToIntFunction<Fluid> fluidFunc, boolean includeBurnRate) {
        Font font = Minecraft.getInstance().font;

        List<Component> text = new ArrayList<>();
        text.add(header.copy().withStyle(ChatFormatting.UNDERLINE, ChatFormatting.AQUA));
        int maxWidth = font.width(header);

        FuelRegistry fuelRegistry = FuelRegistry.getInstance();

        // kludge to get rid of negatively cached values (too-early init via JEI perhaps?)
        // not a big deal to clear this cache client-side since the fuel manager is only really used here on the client
        fuelRegistry.clearCachedFuelFluids();

        Level world = getClientLevel();
        List<Fluid> fluids = new ArrayList<>(fuelRegistry.registeredFuels(world));
        Object2IntMap<Fluid> valueMap = new Object2IntOpenHashMap<>();
        fluids.forEach(f -> valueMap.put(f, fluidFunc.applyAsInt(f)));
        fluids.sort((f1, f2) -> Integer.compare(valueMap.getInt(f2), valueMap.getInt(f1)));

        Map<String, Integer> counted = fluids.stream()
                .collect(Collectors.toMap(fluid -> new FluidStack(fluid, 1).getDisplayName().getString(), fluid -> 1, Integer::sum));

        int dotWidth = font.width(".");
        Component prevLine = Component.empty();
        for (Fluid fluid : fluids) {
            int val = valueMap.getInt(fluid);
            if (val <= 0) {
                continue;
            }
            String valStr = String.format("%4d", valueMap.getInt(fluid));
            int nSpc = (32 - font.width(valStr)) / dotWidth;
            valStr = valStr + StringUtils.repeat('.', nSpc);
            String fluidName = new FluidStack(fluid, 1).getDisplayName().getString();
            float mul = fuelRegistry.getBurnRateMultiplier(world, fluid);
            Component line = mul == 1 || !includeBurnRate ?
                    Component.literal(valStr + "| " + StringUtils.abbreviate(fluidName, 25)) :
                    Component.literal(valStr + "| " + StringUtils.abbreviate(fluidName, 20)
                            + " (x" + PneumaticCraftUtils.roundNumberTo(mul, 2) + ")");
            if (!line.equals(prevLine)) {
                maxWidth = Math.max(maxWidth, font.width(line));
                text.add(line);
            }
            prevLine = line;
            if (counted.getOrDefault(fluidName, 0) > 1) {
                Component line2 = Component.literal("  " + Symbols.TRIANGLE_UP + " " + ModNameCache.getModName(fluid)).withStyle(ChatFormatting.GOLD);
                text.add(line2);
                maxWidth = Math.max(maxWidth, font.width(line2));
            }
        }

        return Pair.of(Math.min(maxWidth, suggestedWidth), text);
    }
}
