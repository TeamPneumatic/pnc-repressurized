package me.desht.pneumaticcraft.common.thirdparty;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.Pack.Position;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.AddPackFindersEvent;

public class Fusion implements IThirdParty {
    
    private void addPackFinders(AddPackFindersEvent event) {
        if (ModList.get().isLoaded("fusion"))
            event.addPackFinders(Identifier.fromNamespaceAndPath("pneumaticcraft", "fusion_integration"), PackType.CLIENT_RESOURCES, Component.literal("Fusion for PneumaticCraft"), PackSource.BUILT_IN, false, Position.TOP);
    }

    @Override
    public void clientPreInit(IEventBus modBus) {
        modBus.addListener(this::addPackFinders);
    }
}
