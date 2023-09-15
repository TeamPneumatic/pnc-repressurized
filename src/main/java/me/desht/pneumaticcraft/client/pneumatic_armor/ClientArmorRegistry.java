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

package me.desht.pneumaticcraft.client.pneumatic_armor;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.InputConstants;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.*;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.ArmorStatMoveScreen;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.KeybindingButton;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetKeybindCheckBox;
import me.desht.pneumaticcraft.client.pneumatic_armor.block_tracker.BlockTrackHandler;
import me.desht.pneumaticcraft.client.pneumatic_armor.entity_tracker.EntityTrackHandler;
import me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler.BlockTrackerClientHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public enum ClientArmorRegistry implements IClientArmorRegistry {
    INSTANCE;

    // lazy-inited per-armor-slot list of client upgrade handlers
    private List<List<IArmorUpgradeClientHandler<?>>> clientUpgradeHandlers = null;
    // map upgrade ID to client upgrade handler
    private final Map<ResourceLocation, IArmorUpgradeClientHandler<?>> id2HandlerMap = new ConcurrentHashMap<>();
    // map upgrade ID to keymapping that toggles it on/off
    private final Map<ResourceLocation, KeyMapping> id2KeyBindMap = new ConcurrentHashMap<>();
    // map keymapping name to upgrade handler that it triggers (e.g. hacking, kick, item launcher...)
    private final Map<String, IArmorUpgradeClientHandler<?>> triggerKeyBindMap = new ConcurrentHashMap<>();

    public static ClientArmorRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public void registerEntityTrackEntry(Supplier<? extends IEntityTrackEntry> entry) {
        EntityTrackHandler.getInstance().register(entry);
    }

    @Override
    public void registerBlockTrackEntry(ResourceLocation id, Supplier<? extends IBlockTrackEntry> entry) {
        BlockTrackHandler.getInstance().register(id, entry);
    }

    @Override
    public void addHUDMessage(Component title, List<Component> message, int duration, int backColor) {
        HUDHandler.getInstance().addMessage(title, message, duration, backColor);
    }

    @Override
    public <T extends IArmorUpgradeHandler<?>> void registerUpgradeHandler(T handler, IArmorUpgradeClientHandler<T> clientHandler) {
        Validate.notNull(clientHandler, "Render handler can't be null!");

        id2HandlerMap.put(handler.getID(), clientHandler);

        clientHandler.getInitialKeyBinding().ifPresent(keyMapping -> registerKeyBinding(handler.getID(), keyMapping));
        clientHandler.getTriggerKeyBinding().ifPresent(keyMapping -> registerTriggerKeybinding(clientHandler, keyMapping));
    }

    @Override
    public IKeybindingButton makeKeybindingButton(int yPos, KeyMapping keyBinding) {
        return new KeybindingButton(30, yPos, 150, 20, xlate("pneumaticcraft.armor.gui.misc.setKey"), keyBinding);
    }

    @Override
    public ICheckboxWidget makeKeybindingCheckBox(ResourceLocation upgradeId, int xPos, int yPos, int color, Consumer<ICheckboxWidget> onPressed) {
        return WidgetKeybindCheckBox.getOrCreate(upgradeId, xPos, yPos, color, onPressed);
    }

    @Override
    public IGuiAnimatedStat makeHUDStatPanel(Component title, ItemStack icon, IArmorUpgradeClientHandler<?> clientHandler) {
        StatPanelLayout layout = ArmorHUDLayout.INSTANCE.getLayoutFor(clientHandler.getID(), clientHandler.getDefaultStatLayout());
        return new WidgetAnimatedStat(null, title, WidgetAnimatedStat.StatIcon.of(icon), HUDHandler.getInstance().getStatOverlayColor(), null, layout);
    }

    @Override
    public IGuiAnimatedStat makeHUDStatPanel(Component title, ResourceLocation icon, IArmorUpgradeClientHandler<?> clientHandler) {
        StatPanelLayout layout = ArmorHUDLayout.INSTANCE.getLayoutFor(clientHandler.getID(), clientHandler.getDefaultStatLayout());
        return new WidgetAnimatedStat(null, title, WidgetAnimatedStat.StatIcon.of(icon), HUDHandler.getInstance().getStatOverlayColor(), null, layout);
    }

    @Override
    public AbstractWidget makeStatMoveButton(int x, int y, IArmorUpgradeClientHandler<?> handler) {
        return new WidgetButtonExtended(x, y, 150, 20, xlate("pneumaticcraft.armor.gui.misc.moveStatScreen"),
                b -> Minecraft.getInstance().setScreen(new ArmorStatMoveScreen(handler))
        );
    }

    @Override
    public Optional<BlockTrackerFocus> getBlockTrackerFocus() {
        if (!CommonArmorHandler.getHandlerForPlayer().upgradeUsable(CommonUpgradeHandlers.blockTrackerHandler, true)) {
            return Optional.empty();
        }
        BlockTrackerClientHandler handler = ClientArmorRegistry.getInstance()
                .getClientHandler(CommonUpgradeHandlers.blockTrackerHandler, BlockTrackerClientHandler.class);
        return Optional.of(new BlockTrackerFocus(handler.getFocusedPos(), handler.getFocusedFace()));
    }

    /*------------------------------------
     * internal & non-API methods below here
     */

    private void registerKeyBinding(ResourceLocation upgradeID, KeyMapping keyBinding) {
        id2KeyBindMap.put(upgradeID, keyBinding);
    }

    private void registerTriggerKeybinding(IArmorUpgradeClientHandler<?> clientHandler, KeyMapping keyBinding) {
        triggerKeyBindMap.put(keyBinding.getName(), clientHandler);
    }

    public void registerSubKeyBinds() {
        // this is called from late init to ensure that all handlers have the necessary information to report their sub-keybinds
        // in particular the block tracker handler needs to have a full list of block track entries available to it
        id2HandlerMap.values().forEach(clientHandler -> {
            clientHandler.getSubKeybinds().forEach(rl -> registerKeyBinding(rl,
                    new KeyMapping(IArmorUpgradeHandler.getStringKey(rl),
                            KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN,
                            clientHandler.getSubKeybindCategory())
            ));
        });
    }

    public void registerKeybindsWithMinecraft() {
        // do the actual registration of keymappings with Minecraft
        // called from late init, after registerSubKeyBinds(); not ideal, but RegisterKeyMappingsEvent fires much too early to be useful here
        // i.e. before FMLClientSetupEvent is fired and any handlers are actually registered
        KeyMapping[] keys = id2KeyBindMap.values().toArray(new KeyMapping[0]);
        Minecraft.getInstance().options.keyMappings = ArrayUtils.addAll(Minecraft.getInstance().options.keyMappings, keys);
    }

    public KeyMapping getKeybindingForUpgrade(ResourceLocation upgradeID) {
        return id2KeyBindMap.get(upgradeID);
    }

    @SuppressWarnings("unused")
    public <C extends IArmorUpgradeClientHandler<U>, U extends IArmorUpgradeHandler<?>> C getClientHandler(U armorUpgradeHandler, Class<C> clientClass) {
        List<IArmorUpgradeClientHandler<?>> clientHandlers = getHandlersForSlot(armorUpgradeHandler.getEquipmentSlot());
        // common & client armor handlers should *always* directly correspond - if they don't,
        // something went wrong with registration and a ClassCastException is inevitable...
        //noinspection unchecked
        return (C) clientHandlers.get(armorUpgradeHandler.getIndex());
    }

    public IArmorUpgradeClientHandler<?> getClientHandler(ResourceLocation id) {
        return id2HandlerMap.get(id);
    }

    public Optional<IArmorUpgradeClientHandler<?>> getTriggeredHandler(KeyMapping keyBinding) {
        return keyBinding.getKeyModifier() == KeyModifier.getActiveModifier() ?
                Optional.ofNullable(triggerKeyBindMap.get(keyBinding.getName())) :
                Optional.empty();
    }

    /**
     * Get all the client handlers for the given armor slot. This is guaranteed to be in exactly the same order as
     * the common handlers for the same slot (as returned by {@link ArmorUpgradeRegistry#getHandlersForSlot(EquipmentSlot)}
     *
     * @param slot the slot to query
     * @return a list of all the client upgrade handlers registered for that slot
     */
    public List<IArmorUpgradeClientHandler<?>> getHandlersForSlot(EquipmentSlot slot) {
        if (clientUpgradeHandlers == null) {
            initHandlerLists();
        }
        return clientUpgradeHandlers.get(slot.getIndex());
    }

    private void initHandlerLists() {
        // lazy init the by-slot lists; this adds client handlers in *exactly* the same order as common handlers
        if (!ArmorUpgradeRegistry.getInstance().isFrozen()) throw new IllegalStateException("armor upgrade registry is not frozen yet!");
        if (clientUpgradeHandlers != null) throw new IllegalStateException("handler lists already inited!?");

        ImmutableList.Builder<List<IArmorUpgradeClientHandler<?>>> builder = ImmutableList.builder();
        for (EquipmentSlot ignored : ArmorUpgradeRegistry.ARMOR_SLOTS) {
            builder.add(new ArrayList<>());
        }
        clientUpgradeHandlers = builder.build();

        for (EquipmentSlot slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
            for (IArmorUpgradeHandler<?> handler : ArmorUpgradeRegistry.getInstance().getHandlersForSlot(slot)) {
                IArmorUpgradeClientHandler<?> clientHandler = id2HandlerMap.get(handler.getID());
                // sanity check - catch missed registrations early
                Validate.notNull(clientHandler, "Null client-handler for upgrade handler '"
                        + handler.getID() + "'! Did you forget to register it?");
                clientUpgradeHandlers.get(slot.getIndex()).add(clientHandler);
            }
        }

        // now that everything is registered, we can get client handlers to process any config values needed
        refreshConfig();
    }

    public void refreshConfig() {
        // we will get called really early (when client config is first loaded)
        // at that point, no upgrade handlers (client or common) are yet registered, so bail
        if (clientUpgradeHandlers == null) return;

        for (EquipmentSlot slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
            for (IArmorUpgradeClientHandler<?> renderHandler : getHandlersForSlot(slot)) {
                renderHandler.initConfig();
            }
        }
    }

}
