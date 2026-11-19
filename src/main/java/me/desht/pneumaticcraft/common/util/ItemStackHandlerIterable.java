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

package me.desht.pneumaticcraft.common.util;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ItemStackHandlerIterable implements Iterable<ItemStack>{

    private final ResourceHandler<ItemResource> itemStackHandler;
    
    public ItemStackHandlerIterable(ResourceHandler<ItemResource> itemStackHandler){
        this.itemStackHandler = itemStackHandler;
    }
    
    @Override
    public Iterator<ItemStack> iterator(){
        return new Iterator<>() {
            private int curIndex = 0;

            @Override
            public boolean hasNext() {
                return curIndex < itemStackHandler.size();
            }

            @Override
            public ItemStack next() {
                if (!hasNext()) throw new NoSuchElementException();
                return IOHelper.getStack(itemStackHandler, curIndex++);
            }

            @Override
            public void remove() {
                if (curIndex == 0) throw new IllegalStateException("First call next()!");
                IOHelper.setStack(itemStackHandler, curIndex - 1, ItemStack.EMPTY);
            }
        };
    }
    
    public Stream<ItemStack> stream(){
        return StreamSupport.stream(spliterator(), false);
    }

}
