package igwmod.gui;

import net.minecraft.util.text.TextFormatting;


/**
 * Created by K-4U on 17-7-2015.
 */
public class LocatedSectionString extends LocatedString {
    private String beforeFormat;
    /**
     * A constructor for unlinked located strings. You can specify a color.
     * @param string
     * @param x
     * @param y
     * @param color
     * @param shadow
     */
    public LocatedSectionString(String string, int x, int y, int color, boolean shadow){
        super(TextFormatting.BOLD + string, x, y, color, shadow);
        beforeFormat = string;
    }
    /**
     * A constructor for unlinked located strings.
     * @param string
     * @param x
     * @param y
     * @param shadow
     */
    public LocatedSectionString(String string, int x, int y, boolean shadow){
        super(TextFormatting.BOLD + string, x, y, 0, shadow);
        beforeFormat = string;
    }

    @Override
    public String getName(){
        return beforeFormat;
    }
}
