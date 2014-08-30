package pneumaticCraft.common.tileentity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;

public class TileEntityAphorismTile extends TileEntityBase{

    private List<String> textLines = new ArrayList<String>();
    public int textRotation;

    public TileEntityAphorismTile(){
        textLines.add("");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setInteger("textRotation", textRotation);
        tag.setInteger("lines", textLines.size());
        for(int i = 0; i < textLines.size(); i++) {
            tag.setString("line" + i, textLines.get(i));
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        textRotation = tag.getInteger("textRotation");
        textLines.clear();
        int lines = tag.getInteger("lines");
        for(int i = 0; i < lines; i++) {
            textLines.add(tag.getString("line" + i));
        }
    }

    public List<String> getTextLines(){
        return textLines;
    }

    public void setTextLines(List<String> textLines){
        this.textLines = textLines;
        this.sendDescriptionPacket();
    }
}
