package me.desht.pneumaticcraft.common.semiblock;

import java.util.ArrayList;
import java.util.List;

import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyPlatform;
import net.minecraft.util.EnumFacing;

public class SemiBlockAssembly3DPrinter extends SemiBlockBasic<TileEntityAssemblyPlatform> {
    public static final String ID = "assembly_3d_printer";
    
    private static final float MOVE_SPEED = 0.25F/16F;
    private static final float MIN_X = 3/16F;
    private static final float MIN_Y = 2/16F;
    private static final float MIN_Z = 3/16F;
    private static final float MAX_X = 9/16F;
    private static final float MAX_Y = 5/16F;
    private static final float MAX_Z = 6/16F; 
    private static final float MOVE_AMOUNT = 1/16F;
    private static final List<float[]> MOVE_SEQUENCE = new ArrayList<>();
    
    @DescSynced
    private float destX, destY, destZ;
    
    @DescSynced
    @LazySynced
    private float curX, curY, curZ;
    private float prevX, prevY, prevZ;
    private int moveIndex = -2;
    
    static{
        for(float y = MAX_Y; y >= MIN_Y; y -= MOVE_AMOUNT){
            for(float x = MIN_X; x <= MAX_X; x += MOVE_AMOUNT){
                MOVE_SEQUENCE.add(new float[]{x, y, MIN_Z});
                MOVE_SEQUENCE.add(new float[]{x, y, MAX_Z});
                x += MOVE_AMOUNT;
                MOVE_SEQUENCE.add(new float[]{x, y, MAX_Z});
                MOVE_SEQUENCE.add(new float[]{x, y, MIN_Z});
            }
            y -= MOVE_AMOUNT;
            for(float x = MAX_X; x >= MIN_X; x -= MOVE_AMOUNT){
                MOVE_SEQUENCE.add(new float[]{x, y, MIN_Z});
                MOVE_SEQUENCE.add(new float[]{x, y, MAX_Z});
                x -= MOVE_AMOUNT;
                MOVE_SEQUENCE.add(new float[]{x, y, MAX_Z});
                MOVE_SEQUENCE.add(new float[]{x, y, MIN_Z});
            }
        }
    }

    public SemiBlockAssembly3DPrinter(){
        super(TileEntityAssemblyPlatform.class);
    }
    
    public float getCurX(float partialTick){
        return prevX + (curX - prevX) * partialTick;
    }
    
    public float getCurY(float partialTick){
        return prevY + (curY - prevY) * partialTick;
    }
    
    public float getCurZ(float partialTick){
        return prevZ + (curZ - prevZ) * partialTick;
    }
    
    @Override
    public boolean canPlace(EnumFacing facing) {
        return getTileEntity() != null;
    }


    @Override
    public void update() {
        super.update();
        updatePrintPos();
        if (!getWorld().isRemote) {
            if(moveIndex == -2){
                startPrinting();
            }
        }
    }
    
    private void updatePrintPos(){
        prevX = curX;
        prevY = curY;
        prevZ = curZ;
        
        if(curX < destX){
            curX = Math.min(curX + MOVE_SPEED, destX);
        }else if(curX > destX){
            curX = Math.max(curX - MOVE_SPEED, destX);
        }
        
        if(curY < destY){
            curY = Math.min(curY + MOVE_SPEED, destY);
        }else if(curY > destY){
            curY = Math.max(curY - MOVE_SPEED, destY);
        }
        
        if(curZ < destZ){
            curZ = Math.min(curZ + MOVE_SPEED, destZ);
        }else if(curZ > destZ){
            curZ = Math.max(curZ - MOVE_SPEED, destZ);
        }
        
        if(!world.isRemote && curX == destX && curY == destY && curZ == destZ){
            nextStep();
        }
    }

    private void nextStep(){
        if(moveIndex >= -1){
            moveIndex++;
            if(moveIndex < MOVE_SEQUENCE.size()){
                float[] vec = MOVE_SEQUENCE.get(moveIndex);
                destX = vec[0];
                destY = vec[1];
                destZ = vec[2];
            }else{
                moveIndex = -2;
            }
        }
    }

    public void startPrinting(){
        moveIndex = -1;
    }
}
