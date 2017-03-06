package org.cogcomp;

import java.io.File;

public class CogCompResources {

    public static void main(String[] args) {
        try {
            Datastore ds = new Datastore();
            brownClusterUpload(ds);
            // read a public file without credentials
            Datastore dsNoCredentials = new Datastore("http://smaug.cs.illinois.edu:8080");
            File f = dsNoCredentials.getFile("edu.cogcomp.brown-clusters", "brown-rcv1.clean.tokenized-CoNLL03.txt-c100-freq1.txt", 1.3);
        } catch (DatastoreException e) {
            e.printStackTrace();
        }
    }

    public static void brownClusterUpload(Datastore ds) throws DatastoreException {
        ds.publishFile("edu.cogcomp.brown-clusters", "brown-rcv1.clean.tokenized-CoNLL03.txt-c100-freq1.txt", 1.3,
                "brown-clusters-1.3/brown-clusters/brown-rcv1.clean.tokenized-CoNLL03.txt-c100-freq1.txt", false, true);

        ds.publishFile("edu.cogcomp.brown-clusters", "brown-rcv1.clean.tokenized-CoNLL03.txt-c320-freq1.txt", 1.3,
                "brown-clusters-1.3/brown-clusters/brown-rcv1.clean.tokenized-CoNLL03.txt-c320-freq1.txt", false, true);

        ds.publishFile("edu.cogcomp.brown-clusters", "brown-rcv1.clean.tokenized-CoNLL03.txt-c1000-freq1.txt", 1.3,
                "brown-clusters-1.3/brown-clusters/brown-rcv1.clean.tokenized-CoNLL03.txt-c1000-freq1.txt", false, true);

        ds.publishFile("edu.cogcomp.brown-clusters", "brown-rcv1.clean.tokenized-CoNLL03.txt-c3200-freq1.txt", 1.3,
                "brown-clusters-1.3/brown-clusters/brown-rcv1.clean.tokenized-CoNLL03.txt-c3200-freq1.txt", false, true);

        ds.publishFile("edu.cogcomp.brown-clusters", "brownBllipClusters", 1.3,
                "brown-clusters-1.3/brown-clusters/brownBllipClusters", false, true);

        ds.publishFile("edu.cogcomp.brown-clusters", "brown-english-wikitext.case-intact.txt-c1000-freq10-v3", 1.3,
                "brown-clusters-1.3/brown-clusters/brown-english-wikitext.case-intact.txt-c1000-freq10-v3.txt", false, true);
    }

}
