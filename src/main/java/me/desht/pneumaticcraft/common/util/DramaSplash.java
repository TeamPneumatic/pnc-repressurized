package me.desht.pneumaticcraft.common.util;

import me.desht.pneumaticcraft.lib.Log;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DramaSplash {
    private static final int MIN_SIZE = 5;
    private static final int MAX_SIZE = 10;
    private static final long SLEEP_TIME = 5000;  // sleep time between getting another drama text

    private static DramaSplash INSTANCE;
    private URL splashSite;
    private final ConcurrentLinkedQueue<String> dramaFifo;
    private Thread grabberThread = null;

    private DramaSplash() {
        try {
            splashSite = new URL("http://mc-drama.herokuapp.com/raw");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            splashSite = null;
        }
        dramaFifo = new ConcurrentLinkedQueue<>();
        fetchMoreSplash();
    }

    public static DramaSplash getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DramaSplash();
        }
        return INSTANCE;
    }

    public String getSplash() {
        String res =  dramaFifo.poll();
        if (dramaFifo.size() < MIN_SIZE) {
            fetchMoreSplash();
        }
        return res == null ? "" : res;
    }

    private void fetchMoreSplash() {
        if (grabberThread == null && splashSite != null) {
            grabberThread = new Thread(new SplashGrabber());
            grabberThread.start();
            Log.info("Started splash fetcher: thread " + grabberThread.getName());
        }
    }

    private class SplashGrabber implements Runnable {
        @Override
        public void run() {
            try {
                while (dramaFifo.size() < MAX_SIZE) {
                    String s = IOUtils.toString(splashSite, StandardCharsets.UTF_8);
                    dramaFifo.offer(s);
                    if (dramaFifo.size() >= MIN_SIZE) {
                        try {
                            Thread.sleep(SLEEP_TIME);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            grabberThread = null;
            Log.info("Finished fetching splash: " + dramaFifo.size() + " texts in queue");
        }
    }
}
