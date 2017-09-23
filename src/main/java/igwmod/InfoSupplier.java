package igwmod;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import igwmod.api.IRecipeIntegrator;
import igwmod.api.ITextInterpreter;
import igwmod.api.WikiRegistry;
import igwmod.gui.IReservedSpace;
import igwmod.gui.IWidget;
import igwmod.gui.LocatedStack;
import igwmod.gui.LocatedString;
import igwmod.lib.IGWLog;
import igwmod.lib.Paths;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class InfoSupplier{
    private static HashMap<String, ResourceLocation> infoMap = new HashMap<String, ResourceLocation>();
    private static final int MAX_TEXT_X = 475;
    private static int currentTextColor;
    private static String curPrefix = "";
    private static String curLink = "";

    /**
     * Returns a wikipage for an object name.
     * @param objectName
     * @return
     */
    public static List<String> getInfo(String modid, String objectName, boolean returnNullIfUnavailable){
        String language = FMLClientHandler.instance().getCurrentLanguage();

        //First try to look up the page where it should be, in the assets folder of the owning mod, local language.
        List<String> info = getInfo(modid, objectName, language);
        if(info != null) return info;

        //If we failed, we might have a backup page in english lying around.
        if(!language.equals("en_US")) {
            info = getInfo(modid, objectName, "en_US");
            if(info != null) return info;
        }

        //Let's see if we can find the page where it used to be by default, in the igwmod folder.
        if(!modid.equals("igwmod")) {
            info = getInfo("igwmod", objectName, language);
            if(info != null) {
                if(ConfigHandler.debugMode) IGWLog.warning("IGW-Mod had to look in the igwmod/assets/wiki/ folder to find the " + objectName + " page. This is deprecated. now you should use " + modid + "/assets/wiki/ instead!");
                return info;
            }

            if(!language.equals("en_US")) {
                info = getInfo("igwmod", objectName, "en_US");
                if(info != null) {
                    if(ConfigHandler.debugMode) IGWLog.warning("IGW-Mod had to look in the igwmod/assets/wiki/ folder to find the " + objectName + " page. This is deprecated. now you should use " + modid + "/assets/wiki/ instead!");
                    return info;
                }
            }
        }

        if(returnNullIfUnavailable) return null;
        objectName = "/assets/" + modid + "/wiki/" + language + "/" + objectName.replace(":", "/") + ".txt";
        if(objectName.length() > 50) {
            objectName = objectName.substring(0, objectName.length() / 2) + " " + objectName.substring(objectName.length() / 2, objectName.length());
        }
        return Arrays.asList("No info available about this topic. IGW-Mod is currently looking for " + objectName + ".");
    }

    public static List<String> getInfo(String modid, String objectName, String language){
        String oldObjectName = objectName;
        objectName = modid + Paths.WIKI_PATH + language + "/" + objectName.replace(":", "/") + ".txt";
        if(!infoMap.containsKey(objectName)) {
            infoMap.put(objectName, new ResourceLocation(objectName));
        }
        try {
            InputStream stream;
            if(oldObjectName.startsWith("server/")) {
                String s = IGWMod.proxy.getSaveLocation() + "\\igwmod\\" + oldObjectName.substring(7) + ".txt";
                stream = new FileInputStream(new File(s));
            } else {
                IResourceManager manager = FMLClientHandler.instance().getClient().getResourceManager();
                ResourceLocation location = infoMap.get(objectName);
                IResource resource = manager.getResource(location);
                stream = resource.getInputStream();
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            List<String> textList = new ArrayList<String>();
            String line = br.readLine();
            while(line != null) {
                textList.add(line);
                line = br.readLine();
            }
            br.close();
            return textList;
        } catch(Exception e) {
            return null;
        }
    }

    @SideOnly(Side.CLIENT)
    public static void analyseInfo(FontRenderer fontRenderer, List<String> fileInfo, List<IReservedSpace> reservedSpaces, List<LocatedString> locatedStrings, List<LocatedStack> locatedStacks, List<IWidget> locatedTextures){
        for(ITextInterpreter ti : WikiRegistry.textInterpreters) {
            if(ti.interpret(fontRenderer, fileInfo, reservedSpaces, locatedStrings, locatedStacks, locatedTextures)) return;
        }

        currentTextColor = 0xFF000000;
        curPrefix = "";
        curLink = "";
        locatedStacks.clear();
        locatedStrings.clear();
        locatedTextures.clear();
        for(int k = 0; k < fileInfo.size(); k++) {
            String line = fileInfo.get(k);
            for(int i = 0; i < line.length(); i++) {
                if(line.charAt(i) == '[') {
                    for(int j = i; j < line.length(); j++) {
                        if(line.charAt(j) == ']') {
                            try {
                                String code = line.substring(i + 1, j);
                                if(decomposeTemplate(code, reservedSpaces, locatedStrings, locatedStacks, locatedTextures)) {
                                    String cutString = line.substring(0, i) + line.substring(j + 1, line.length());
                                    if(cutString.equals("")) fileInfo.remove(k--);
                                    else fileInfo.set(k, cutString);
                                } else {
                                    if(code.startsWith("variable{")) {
                                        String cutString = line.substring(0, i) + VariableHandler.getVariable(code.substring("variable{".length(), code.length() - 1)) + line.substring(j + 1, line.length());
                                        fileInfo.set(k, cutString);
                                    }
                                }
                            } catch(IllegalArgumentException e) {
                                fileInfo.add(TextFormatting.RED + "Problem when parsing \"" + line.substring(i + 1, j) + "\":");
                                fileInfo.add(TextFormatting.RED + e.getMessage());
                                IGWLog.warning(e.getMessage());
                            }
                            break;
                        }
                    }
                }
            }
        }

        int currentY = 20;
        for(int k = 0; k < fileInfo.size(); k++) {
            String line = " " + fileInfo.get(k);

            List<String> sentenceWordList = new ArrayList<String>(Arrays.asList(line.split(" ")));
            for(int i = 0; i < sentenceWordList.size(); i++) {
                String word = sentenceWordList.get(i);
                int index = word.indexOf("[");
                int otherIndex = word.indexOf("]");
                if(otherIndex > 0) {
                    otherIndex++;
                }
                if(index == -1) {
                    index = otherIndex;
                } else if(otherIndex != -1) {
                    index = Math.min(index, otherIndex);
                }
                if(index > 0 && index < word.length() - 1) {
                    sentenceWordList.set(i, word.substring(0, index));
                    sentenceWordList.add(i + 1, word.substring(index));
                }
            }
            String[] sentenceWords = sentenceWordList.toArray(new String[sentenceWordList.size()]);

            int currentWord = 0;
            int currentX = 0;
            String textPart = "";
            int newX = 0;
            while(currentWord < sentenceWords.length || sentenceWords.length == 0) {
                int curTextColor = currentTextColor;
                String prefix = curPrefix;
                String link = curLink;
                boolean newLine = false;
                while(true) {
                    String potentialString = currentWord >= sentenceWords.length ? "" : textPart + (textPart.equals("") ? "" : " ") + sentenceWords[currentWord];
                    if(currentWord >= sentenceWords.length || fontRenderer.getStringWidth(prefix + potentialString) + currentX > MAX_TEXT_X && fontRenderer.getStringWidth(prefix + sentenceWords[currentWord]) <= MAX_TEXT_X - 200) {
                        newLine = true;
                        newX = 0;
                        break;
                    }
                    newX = getNewXFromIntersection(new Rectangle(currentX, currentY, fontRenderer.getStringWidth(prefix + potentialString), fontRenderer.FONT_HEIGHT), reservedSpaces, locatedStacks, locatedTextures);
                    if(textPart.equals("") && fontRenderer.getStringWidth(prefix + potentialString) + newX <= MAX_TEXT_X) {
                        currentX = newX;
                    } else if(newX != currentX) {
                        break;
                    }
                    currentWord++;
                    textPart = potentialString;
                    boolean foundCode = false;
                    if(currentWord < sentenceWords.length) {
                        String potentialCode = sentenceWords[currentWord];
                        int i = potentialCode.indexOf('[');
                        int j = potentialCode.indexOf(']');
                        while(i != -1 && j != -1) {
                            try {
                                sentenceWords[currentWord] = potentialCode.substring(0, i) + potentialCode.substring(j + 1, potentialCode.length());
                                decomposeInLineTemplate(potentialCode.substring(i + 1, j));
                                newX += fontRenderer.getStringWidth(textPart + " ");
                                foundCode = true;
                            } catch(IllegalArgumentException e) {
                                fileInfo.add(TextFormatting.RED + e.getMessage());
                                IGWLog.warning(e.getMessage());
                            }
                            potentialCode = sentenceWords[currentWord];
                            i = potentialCode.indexOf('[');
                            j = potentialCode.indexOf(']');
                        }
                        if(foundCode) break;
                    }
                }
                locatedStrings.add(link.equals("") ? new LocatedString(prefix + textPart, currentX, currentY, curTextColor, false) : new LocatedString(prefix + textPart, currentX, currentY, false, link));
                if(newLine) currentY += fontRenderer.FONT_HEIGHT + 1;
                currentX = newX;
                textPart = "";
                if(sentenceWords.length == 0) break;
            }
            // currentY += fontRenderer.FONT_HEIGHT + 1;
        }
    }

    private static int getNewXFromIntersection(Rectangle rect, List<IReservedSpace> reservedSpaces, List<LocatedStack> locatedStacks, List<IWidget> locatedTextures){
        int oldX = rect.x;
        boolean modified = false;
        for(IReservedSpace reservedSpace : reservedSpaces) {
            Rectangle space = reservedSpace.getReservedSpace();
            if(space.x + space.width > rect.x && space.intersects(rect)) {
                rect = new Rectangle(space.x + space.width, rect.y, rect.width, rect.height);
                modified = true;
            }
        }
        for(LocatedStack locatedStack : locatedStacks) {
            Rectangle space = locatedStack.getReservedSpace();
            if(space.x + space.width > rect.x && space.intersects(rect)) {
                //rect = new Rectangle(rect.x, rect.y, space.x + space.width - rect.x, rect.height);
                rect = new Rectangle(space.x + space.width, rect.y, rect.width, rect.height);
                modified = true;
            }
        }
        for(IWidget locatedTexture : locatedTextures) {
            if(locatedTexture instanceof IReservedSpace) {
                Rectangle space = ((IReservedSpace)locatedTexture).getReservedSpace();
                if(space.x + space.width > rect.x && space.intersects(rect)) {
                    // rect = new Rectangle(rect.x, rect.y, space.x + space.width - rect.x, rect.height);
                    rect = new Rectangle(space.x + space.width, rect.y, rect.width, rect.height);
                    modified = true;
                }
            }
        }
        return modified ? rect.x : oldX;
    }

    private static boolean decomposeTemplate(String code, List<IReservedSpace> reservedSpaces, List<LocatedString> locatedStrings, List<LocatedStack> locatedStacks, List<IWidget> locatedTextures) throws IllegalArgumentException{
        for(IRecipeIntegrator integrator : WikiRegistry.recipeIntegrators) {
            if(code.startsWith(integrator.getCommandKey() + "{")) {
                String[] args = code.substring(integrator.getCommandKey().length() + 1, code.length() - 1).split(",");
                for(int i = 0; i < args.length; i++) {
                    args[i] = args[i].trim();
                }
                integrator.onCommandInvoke(args, reservedSpaces, locatedStrings, locatedStacks, locatedTextures);
                return true;
            }
        }
        return false;
    }

    private static void decomposeInLineTemplate(String code) throws IllegalArgumentException{
        if(!code.endsWith("}")) throw new IllegalArgumentException("Code misses a '}' at the end! Full code: " + code);
        if(code.startsWith("color{")) {
            colorCommand(code);
        } else if(code.startsWith("prefix{")) {
            prefixCommand(code);
        } else if(code.startsWith("link{")) {
            curLink = code.substring(5, code.length() - 1);
        }
    }

    private static void colorCommand(String code) throws IllegalArgumentException{

        String colorCode = code.substring(6, code.length() - 1);
        if(colorCode.startsWith("0x")) colorCode = colorCode.substring(2);
        try {
            currentTextColor = 0xFF000000 | Integer.parseInt(colorCode, 16);
        } catch(NumberFormatException e) {
            throw new IllegalArgumentException("Using an invalid color parameter. Only use hexadecimal (0123456789ABCDEF) numbers! Also only use 6 digits (no alpha digits). Full code: " + code + ", color code: " + colorCode);
        }
    }

    private static void prefixCommand(String code) throws IllegalArgumentException{
        String prefixCode = code.substring(7, code.length() - 1);
        curPrefix = "";
        for(int i = 0; i < prefixCode.length(); i++) {
            if(prefixCode.charAt(i) != 'r') curPrefix += "\u00a7" + prefixCode.charAt(i);
        }
    }
}
