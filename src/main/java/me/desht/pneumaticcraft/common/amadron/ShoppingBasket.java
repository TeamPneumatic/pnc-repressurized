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

package me.desht.pneumaticcraft.common.amadron;

import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketAmadronOrderResponse;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Iterator;
import java.util.Map;

public abstract class ShoppingBasket implements Iterable<ResourceLocation> {
    protected final Map<ResourceLocation, Integer> basket;

    public static ImmutableBasket empty() {
        return ImmutableBasket.EMPTY;
    }

    public static MutableBasket createMutable() {
        return new MutableBasket();
    }

    protected ShoppingBasket(Map<ResourceLocation,Integer> basket) {
        this.basket = basket;
    }

//    public static ShoppingBasket fromNBT(CompoundTag subTag) {
//        ShoppingBasket res = new ShoppingBasket();
//        if (subTag != null) {
//            for (String key : subTag.getAllKeys()) {
//                int count = subTag.getInt(key);
//                if (count > 0) res.setUnits(new ResourceLocation(key), count);
//            }
//        }
//        return res;
//    }
//
//    public CompoundTag toNBT() {
//        CompoundTag subTag = new CompoundTag();
//        basket.forEach((key, value) -> {
//            if (value > 0) subTag.putInt(key.toString(), value);
//        });
//        return subTag;
//    }

    public int getUnits(ResourceLocation offerId) {
        return basket.getOrDefault(offerId, 0);
    }

    @Override
    public Iterator<ResourceLocation> iterator() {
        return basket.keySet().iterator();
    }

    public void syncToPlayer(ServerPlayer player) {
        basket.forEach((offerId, units) -> NetworkHandler.sendToPlayer(new PacketAmadronOrderResponse(offerId, units), player));
    }

    public boolean isEmpty() {
        return basket.values().stream().noneMatch(amount -> amount > 0);
    }

    public abstract ImmutableBasket toImmutable();

}
