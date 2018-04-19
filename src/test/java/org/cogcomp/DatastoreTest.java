package org.cogcomp;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class DatastoreTest {
    Datastore ds = null;

    @Before
    public void setup() throws Exception {
        ds = new Datastore("http://smaug.cs.illinois.edu:8080");
    }

    @Test
    public void test() throws Exception {
        File f = ds.getFile("edu.cogcomp", "pom", 1.0);
        assertTrue(f.exists());

        // download a folder
        File folder = ds.getDirectory("org.cogcomp.gazetteers", "gazetteers", 1.3, false);
        assertTrue(folder.exists());
        assertTrue(folder.isDirectory());
    }
    
    /**
     * This test is useless unless we trash the gaz first, since there is nothing
     * to load and no reason to synchronize. So this is a by definition a BAD unit test. My bad.
     * @throws Exception
     */
    @Test
    public void testRaceCondition() throws Exception {
        class LoadGazThread extends Thread {
            public void run() {
                Datastore nds = null;
                try {
                    nds = new Datastore("http://smaug.cs.illinois.edu:8080");
                } catch (DatastoreException e) {
                    e.printStackTrace();
                }
                try {
                    File gazDirectory = nds.getDirectory("org.cogcomp.gazetteers", "gazetteers", 1.3, false);
                    InputStream stream = new FileInputStream(gazDirectory.getPath() + File.separator + "gazetteers" 
                                    + File.separator + "gazetteers-list.txt");
                    BufferedReader br = new BufferedReader(new InputStreamReader(stream));
                    String line;
                    ArrayList<String> filenames = new ArrayList<>();
                    while ((line = br.readLine()) != null);
                    stream.close();
                } catch (DatastoreException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
        final int NUM = 8;
        LoadGazThread[] threads = new LoadGazThread[NUM];
        for (int i = 0; i < NUM ; i++) {
            threads[i] = new LoadGazThread();
        }
        for (int i = 0; i < NUM ; i++) {
            threads[i].start();
        }
        for (int i = 0; i < NUM ; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ie) {}
        }
    }
}
