/*
 * This file is part of Blue Power. Blue Power is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. Blue Power is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along
 * with Blue Power. If not, see <http://www.gnu.org/licenses/>
 */
package pneumaticCraft.common.util;

import java.util.Random;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketDebugBlock;

/**
 * Class aimed for debugging purposes only
 * 
 * @author MineMaarten
 */
public class Debugger{

    private static Random rand = new Random();

    public static void indicateBlock(TileEntity te){

        indicateBlock(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord);
    }

    public static void indicateBlock(World world, ChunkPosition pos){
        indicateBlock(world, pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
    }

    public static void indicateBlock(World world, int x, int y, int z){

        if(world != null) {
            if(world.isRemote) {
                for(int i = 0; i < 5; i++) {
                    double dx = x + 0.5;
                    double dy = y + 0.5;
                    double dz = z + 0.5;
                    world.spawnParticle("reddust", dx, dy, dz, 0, 0, 0);
                }
            } else {
                NetworkHandler.sendToAllAround(new PacketDebugBlock(x, y, z), world);
            }
        }
    }
}
