package me.desht.pneumaticcraft.common.util;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ItemStackHandlerIterable implements Iterable<ItemStack>{

    private final IItemHandlerModifiable itemStackHandler;
    
    public ItemStackHandlerIterable(IItemHandlerModifiable itemStackHandler){
        this.itemStackHandler = itemStackHandler;
    }
    
    @Override
    public Iterator<ItemStack> iterator(){
        return new Iterator<ItemStack>(){
            private int curIndex = 0;
            
            @Override
            public boolean hasNext(){
                return curIndex < itemStackHandler.getSlots();
            }

            @Override
            public ItemStack next(){
                if(!hasNext()) throw new NoSuchElementException();
                return itemStackHandler.getStackInSlot(curIndex++);
            }
            
            @Override
            public void remove() {
                if(curIndex == 0) throw new IllegalStateException("First call next()!");
                itemStackHandler.setStackInSlot(curIndex - 1, ItemStack.EMPTY);
            }
        };
    }
    
    public Stream<ItemStack> stream(){
        return StreamSupport.stream(spliterator(), false);
    }

}
