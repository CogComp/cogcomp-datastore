package org.cogcomp;

import java.io.File;

public class CogCompResources {

    public static void main(String[] args) {
        try {
            Datastore ds = new Datastore();
            // brownClusterUpload(ds);
            // levinVerbClass(ds);
            // rogetThesaurus(ds);
            // verbLemDict(ds);

            // corlex(ds);
            cbcClusters(ds);

            // read a public file without credentials
//             Datastore dsNoCredentials = new Datastore("http://smaug.cs.illinois.edu:8080");
            // File f = dsNoCredentials.getFile("org.cogcomp.brown-clusters", "brown-rcv1.clean.tokenized-CoNLL03.txt-c100-freq1.txt", 1.3);
//             Datastore dsNoCredentials = new Datastore(new ResourceConfigurator().getDefaultConfig());
//             File f = dsNoCredentials.getFile("org.cogcomp.verb.lem.dict", "verbLemDict", 1.3);
//            System.out.println(f);
        } catch (DatastoreException e) {
            e.printStackTrace();
        }
    }

    public static void brownClusterUpload(Datastore ds) throws DatastoreException {
        ds.publishFile("org.cogcomp.brown-clusters", "brown-rcv1.clean.tokenized-CoNLL03.txt-c100-freq1.txt", 1.3,
                "brown-clusters-1.3/brown-clusters/brown-rcv1.clean.tokenized-CoNLL03.txt-c100-freq1.txt", false, true);

        ds.publishFile("org.cogcomp.brown-clusters", "brown-rcv1.clean.tokenized-CoNLL03.txt-c320-freq1.txt", 1.3,
                "brown-clusters-1.3/brown-clusters/brown-rcv1.clean.tokenized-CoNLL03.txt-c320-freq1.txt", false, true);

        ds.publishFile("org.cogcomp.brown-clusters", "brown-rcv1.clean.tokenized-CoNLL03.txt-c1000-freq1.txt", 1.3,
                "brown-clusters-1.3/brown-clusters/brown-rcv1.clean.tokenized-CoNLL03.txt-c1000-freq1.txt", false, true);

        ds.publishFile("org.cogcomp.brown-clusters", "brown-rcv1.clean.tokenized-CoNLL03.txt-c3200-freq1.txt", 1.3,
                "brown-clusters-1.3/brown-clusters/brown-rcv1.clean.tokenized-CoNLL03.txt-c3200-freq1.txt", false, true);

        ds.publishFile("org.cogcomp.brown-clusters", "brownBllipClusters", 1.3,
                "brown-clusters-1.3/brown-clusters/brownBllipClusters", false, true);

        ds.publishFile("org.cogcomp.brown-clusters", "brown-english-wikitext.case-intact.txt-c1000-freq10-v3", 1.3,
                "brown-clusters-1.3/brown-clusters/brown-english-wikitext.case-intact.txt-c1000-freq10-v3.txt", false, true);
    }

    public static void levinVerbClass(Datastore ds) throws DatastoreException {
        ds.publishFile("org.cogcomp.levin.verb.class", "levin-verbClass", 1.6, "levin-verbClass-1.5/levin-verbClass.txt", false, true);
    }

    public static void rogetThesaurus(Datastore ds) throws DatastoreException {
        ds.publishFile("org.cogcomp.roget.thesaurus", "rogetThesaurus", 1.3, "rogetThesaurus-1.3/rogetThesaurus/index.txt", false, true);
    }

    public static void verbLemDict(Datastore ds) throws DatastoreException {
        ds.publishFile("org.cogcomp.verb.lem.dict", "verbLemDict", 1.3, "verb-lemDict-1.3/verb-lemDict.txt", false, true);
    }

    public static void wordEmbedding(Datastore ds) throws DatastoreException {
        ds.publishFile("org.cogcomp.wordEmbedding",
                "model-2280000000.LEARNING_RATE=1e-08.EMBEDDING_LEARNING_RATE=1e-07.EMBEDDING_SIZE=50.gz", 1.3,
                "wordEmbedding-1.3/WordEmbedding/model-2280000000.LEARNING_RATE=1e-08.EMBEDDING_LEARNING_RATE=1e-07.EMBEDDING_SIZE=50.gz",
                false, true);
    }

    public static void corlex(Datastore ds) throws DatastoreException {
        ds.publishDirectory("org.cogcomp.corelex", "corelex_nouns", 1.3, "corlex-1.3/CORLEX/", false, true);
        File f = ds.getDirectory("org.cogcomp.corelex", "corelex_nouns", 1.3, false);
        System.out.println("f: " + f);
    }

    //TODO
    public static void gazetteers(Datastore ds) throws DatastoreException {
        ds.publishFile("org.cogcomp.corelex", "corelex_nouns", 1.3, "corlex-1.3/CORLEX/corelex_nouns", false, true);
        ds.publishFile("org.cogcomp.corelex", "corelex_nouns.basictypes.synset", 1.3, "corlex-1.3/CORLEX/corelex_nouns.basictypes.synset", false, true);
        ds.publishFile("org.cogcomp.corelex", "corelex_nouns.classes", 1.3, "corlex-1.3/CORLEX/corelex_nouns.classes", false, true);
    }

    public static void cbcClusters(Datastore ds) throws DatastoreException {
        ds.publishDirectory("org.cogcomp.cbc.clusters", "cbcData", 1.3, "cbc-clusters-1.3/cbcData", false, true);
        File f = ds.getDirectory("org.cogcomp.cbc.clusters", "cbcData", 1.3, false);
        System.out.println("f: " + f);

        ds.publishFile("org.cogcomp.cbc-clusters", "brown-rcv1.clean.tokenized-CoNLL03.txt-c100-freq1.txt", 1.3,
                "brown-clusters-1.3/brown-clusters/brown-rcv1.clean.tokenized-CoNLL03.txt-c100-freq1.txt", false, true);

    }

    //TODO
    public static void linCluster(Datastore ds) throws DatastoreException {
        ds.publishFile("org.cogcomp.cbc-clusters", "brown-rcv1.clean.tokenized-CoNLL03.txt-c100-freq1.txt", 1.3,
                "brown-clusters-1.3/brown-clusters/brown-rcv1.clean.tokenized-CoNLL03.txt-c100-freq1.txt", false, true);

    }

    //TODO
    public static void linSimilarity(Datastore ds) throws DatastoreException {
        ds.publishFile("org.cogcomp.cbc-clusters", "brown-rcv1.clean.tokenized-CoNLL03.txt-c100-freq1.txt", 1.3,
                "brown-clusters-1.3/brown-clusters/brown-rcv1.clean.tokenized-CoNLL03.txt-c100-freq1.txt", false, true);

    }



}
