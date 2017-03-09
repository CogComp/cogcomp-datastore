package org.cogcomp;

import org.cogcomp.Datastore;
import org.cogcomp.DatastoreException;
import org.junit.Test;

import java.io.File;

public class DatastoreTest {
    static Datastore ds = null;

    static {
        try {
            ds = new Datastore();
        } catch (DatastoreException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test() throws DatastoreException {
        ds.publishFile("edu.cogcomp", "pom", 1.0, "pom.xml");

        File f = ds.getFile("edu.cogcomp", "pom", 1.0);
        assert f.exists();
    }
}
