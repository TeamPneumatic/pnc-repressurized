package me.desht.pneumaticcraft.common.util.drama;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.lib.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adapted from the Ruby code at https://github.com/elifoster/drama-rb
 */
public class DramaGenerator {
    private static final Pattern KEYWORD = Pattern.compile("^%\\{(\\w+)}");
    private static final Map<String,Shuffler> shufflers = new HashMap<>();

    public static String generateDrama() {
        Random r = ThreadLocalRandom.current();

        shufflers.clear();

        ImmutableList.Builder<String> builder = ImmutableList.builder();
        String[] words = pick(r, DramaConstants.SENTENCES).split(" ");
        for (String word : words) {
            Matcher m = KEYWORD.matcher(word);
            if (m.find()) {
                String grp = m.group(1);
                if (DramaConstants.PARTS.containsKey(grp)) {
                    String[] strings = DramaConstants.PARTS.get(grp);
                    String s = shufflers.computeIfAbsent(grp, k -> new Shuffler(r, strings)).nextEntry();
                    builder.add(s);
                } else {
                    Log.warning("unknown sentence part type: %s", word);
                }
                DramaConstants.PARTS.get(m.group(1));
            } else {
                builder.add(word);
            }
        }
        return String.join(" ", builder.build());
    }

    private static String pick(Random random, String[] a) {
        return a[random.nextInt(a.length)];
    }

    private static class Shuffler {
        private final Random random;
        private final String[] strings;
        private int nextIdx = 0;

        public Shuffler(Random random, String[] strings) {
            this.random = random;
            this.strings = strings;
            shuffleArray(strings);
        }

        public String nextEntry() {
            String res = strings[nextIdx];
            if (++nextIdx >= strings.length) nextIdx = 0;
            return res;
        }

        private void shuffleArray(String[] array) {
            int index;
            String temp;
            for (int i = array.length - 1; i > 0; i--) {
                index = random.nextInt(i + 1);
                temp = array[index];
                array[index] = array[i];
                array[i] = temp;
            }
        }
    }
}
