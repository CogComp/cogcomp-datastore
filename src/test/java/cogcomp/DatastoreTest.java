package cogcomp;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

import java.io.File;

public class DatastoreTest {
    static Datastore ds = null;

    static {
        try {
            ds = new Datastore("http://smaug.cs.illinois.edu:8080");
        } catch (DatastoreException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test() throws DatastoreException {
        File f = ds.getFile("edu.cogcomp", "pom", 1.0);
        assertTrue(f.exists());

        // download a folder
        File folder = ds.getDirectory("org.cogcomp.gazetteers", "gazetteers", 1.3, false);
        assertTrue(folder.exists());
        assertTrue(folder.isDirectory());
    }
}
