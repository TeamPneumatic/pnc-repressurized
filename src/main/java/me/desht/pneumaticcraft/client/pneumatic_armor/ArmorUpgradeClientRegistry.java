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
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.apache.commons.lang3.Validate;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public enum ArmorUpgradeClientRegistry {
    INSTANCE;

    private List<List<IArmorUpgradeClientHandler<?>>> clientUpgradeHandlers = null;
    private final Map<ResourceLocation, IArmorUpgradeClientHandler<?>> id2HandlerMap = new ConcurrentHashMap<>();
    private final Map<ResourceLocation, KeyMapping> id2KeyBindMap = new ConcurrentHashMap<>();
    private final Map<String, IArmorUpgradeClientHandler<?>> triggerKeyBindMap = new ConcurrentHashMap<>();

    public static ArmorUpgradeClientRegistry getInstance() {
        return INSTANCE;
    }

    public <T extends IArmorUpgradeHandler<?>> void registerHandler(T handler, IArmorUpgradeClientHandler<T> clientHandler) {
        id2HandlerMap.put(handler.getID(), clientHandler);

        clientHandler.getInitialKeyBinding().ifPresent(k -> registerKeyBinding(handler.getID(), k));
//        clientHandler.getSubKeybinds().forEach(rl -> registerKeyBinding(rl,
//                new KeyMapping(IArmorUpgradeHandler.getStringKey(rl),
//                        KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN,
//                        clientHandler.getSubKeybindCategory())
//        ));

        clientHandler.getTriggerKeyBinding().ifPresent(k -> registerTriggerKeybinding(k, clientHandler));
    }

    private void registerKeyBinding(ResourceLocation upgradeID, KeyMapping keyBinding) {
        id2KeyBindMap.put(upgradeID, keyBinding);
        ClientRegistry.registerKeyBinding(keyBinding);
    }

    private void registerTriggerKeybinding(KeyMapping keyBinding, IArmorUpgradeClientHandler<?> clientHandler) {
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
        return Optional.ofNullable(triggerKeyBindMap.get(keyBinding.getName()));
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
        // at that point, no upgrade handlers (client or common) are yet registered
        if (clientUpgradeHandlers == null) return;

        for (EquipmentSlot slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
            for (IArmorUpgradeClientHandler<?> renderHandler : getHandlersForSlot(slot)) {
                renderHandler.initConfig();
            }
        }
    }
}
