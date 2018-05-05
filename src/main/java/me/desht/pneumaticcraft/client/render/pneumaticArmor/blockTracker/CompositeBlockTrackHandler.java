package me.desht.pneumaticcraft.client.render.pneumaticArmor.blockTracker;

import io.netty.buffer.ByteBuf;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IBlockTrackEntry;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IBlockTrackProvider.IBlockTrackHandler;

public class CompositeBlockTrackHandler implements IBlockTrackHandler{

    private final Collection<IBlockTrackHandler> handlers;
    
    public CompositeBlockTrackHandler(Collection<IBlockTrackHandler> handlers){
        this.handlers = handlers;
    }
    
    @Override
    public void toBytes(ByteBuf buf){
        handlers.forEach(handler -> handler.toBytes(buf));
    }

    @Override
    public void fromBytes(ByteBuf buf){
        handlers.forEach(handler -> handler.fromBytes(buf));
    }

    @Override
    public void addInformation(List<String> infoList, Set<IBlockTrackEntry> blockTrackers){
        handlers.forEach(handler -> handler.addInformation(infoList, null));
    }
    
}
