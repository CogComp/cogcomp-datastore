package org.cogcomp;

import java.io.File;

public class CogCompResources {

    public static void main(String[] args) {
        try {
            Datastore ds = new Datastore();
            brownClusterUpload(ds);
            levinVerbClass(ds);
            rogetThesaurus(ds);
            verbLemDict(ds);
            corlex(ds);
            cbcClusters(ds);
            gazetteers(ds);
            wordEmbedding(ds);
            commaSRL(ds);
            mateTools(ds);
            pathLSTM(ds);
            word2vec(ds);
            verbSenseModels(ds);
            senseList(ds);
            wordEmbeddingFile(ds);
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

        ds.publishFile("org.cogcomp.brown-clusters", "brown-english-wikitext.case-intact.txt-c1000-freq10-v3.txt", 1.3,
                "brown-clusters-1.3/brown-clusters/brown-english-wikitext.case-intact.txt-c1000-freq10-v3.txt", false, true);


        ds.getFile("org.cogcomp.brown-clusters", "brown-rcv1.clean.tokenized-CoNLL03.txt-c100-freq1.txt", 1.3);
        ds.getFile("org.cogcomp.brown-clusters", "brown-rcv1.clean.tokenized-CoNLL03.txt-c320-freq1.txt", 1.3);
        ds.getFile("org.cogcomp.brown-clusters", "brown-rcv1.clean.tokenized-CoNLL03.txt-c1000-freq1.txt", 1.3);
        File f = ds.getFile("org.cogcomp.brown-clusters", "brown-rcv1.clean.tokenized-CoNLL03.txt-c3200-freq1.txt", 1.3);
        System.out.println(f);


        // v1.5
        ds.publishDirectory("org.cogcomp.brown-clusters", "brown-clusters", 1.5,
                "brown-clusters-1.5/brown-clusters", false, true);
        File f1 = ds.getDirectory("org.cogcomp.brown-clusters", "brown-clusters", 1.5, false);
        System.out.println("f: " + f1);
    }

    public static void commaSRL(Datastore ds) throws DatastoreException {
        ds.publishDirectory("org.cogcomp.comma-srl", "comma-srl-models", 2.2, "/Users/daniel/ideaProjects/cogcomp-datastore/comma-srl-models", false, false);
        File f = ds.getDirectory("org.cogcomp.comma-srl", "comma-srl-models", 2.2,false);
        System.out.println("f: " + f);


        //ds.publishDirectory("org.cogcomp.comma-srl", "comma-srl-data", 2.2, "/Users/daniel/ideaProjects/cogcomp-datastore/comma-srl-data", false, false);
        File f1 = ds.getDirectory("org.cogcomp.comma-srl", "comma-srl-data", 2.2,false);
        System.out.println("f1: " + f1);
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


        String fileName = "model-2280000000-LEARNING_RATE=1e-08.EMBEDDING_LEARNING_RATE=1e-07.EMBEDDING_SIZE=50.gz";
         ds.publishFile("org.cogcomp.word-embedding",fileName, 1.5,
                  "wordEmbedding-1.5/WordEmbedding/model-2280000000.LEARNING_RATE=1e-08.EMBEDDING_LEARNING_RATE=1e-07.EMBEDDING_SIZE=50.gz", false, true);
        File f = ds.getFile("org.cogcomp.word-embedding", fileName, 1.5, false);
        System.out.println("f: " + f);
    }

    public static void word2vec(Datastore ds) throws DatastoreException {
        ds.publishFile("org.cogcomp.embeddings",
                "GoogleNews-vectors-negative300-length=200000.bin", 1.0,
                "/Users/daniel/ideaProjects/convertvec/GoogleNews-vectors-negative300-length=200000.bin",
                false, false);

        File f = ds.getFile("org.cogcomp.embeddings", "GoogleNews-vectors-negative300-length=200000.bin", 1.0, false);
        System.out.println("f: " + f);


        ds.publishFile("org.cogcomp.embeddings",
                "GoogleNews-vectors-negative300.bin", 1.0,
                "/Users/daniel/ideaProjects/convertvec/GoogleNews-vectors-negative300.bin",
                false, false);

        File f2 = ds.getFile("org.cogcomp.embeddings", "GoogleNews-vectors-negative300.bin", 1.0, false);
        System.out.println("f: " + f2);
    }

    public static void corlex(Datastore ds) throws DatastoreException {
        ds.publishDirectory("org.cogcomp.corelex", "corelex_nouns", 1.3, "corlex-1.3/CORLEX/", false, true);
        File f = ds.getDirectory("org.cogcomp.corelex", "corelex_nouns", 1.3, false);
        System.out.println("f: " + f);
    }

    public static void cbcClusters(Datastore ds) throws DatastoreException {
        ds.publishDirectory("org.cogcomp.cbc.clusters", "cbcData", 1.3, "cbc-clusters-1.3/cbcData", false, true);
        File f = ds.getDirectory("org.cogcomp.cbc.clusters", "cbcData", 1.3, false);
        System.out.println("f: " + f);
    }

    public static void gazetteers(Datastore ds) throws DatastoreException {
        ds.publishDirectory("org.cogcomp.gazetteers", "gazetteers", 1.3, "gazetteers-1.3/gazetteers", false, true);
        File f = ds.getDirectory("org.cogcomp.gazetteers", "gazetteers", 1.3, false);
        System.out.println("f: " + f);

        ds.publishDirectory("org.cogcomp.gazetteers", "gazetteers", 1.5, "gazetteers-1.5/gazetteers", false, true);
        File f1 = ds.getDirectory("org.cogcomp.gazetteers", "gazetteers", 1.5, false);
        System.out.println("f: " + f1);
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

    public static void mateTools(Datastore ds) throws DatastoreException {
        ds.publishFile("org.cogcomp.mate-tools", "CoNLL2009-ST-English-ALL.anna.lemmatizer.model", 3.3,
                "/Users/daniel/Desktop/CoNLL2009-ST-English-ALL.anna-3.3.lemmatizer.model",
                false, true);
        ds.publishFile("org.cogcomp.mate-tools", "CoNLL2009-ST-English-ALL.anna.parser.model", 3.3,
                "/Users/daniel/Desktop/CoNLL2009-ST-English-ALL.anna-3.3.parser.model",
                false, true);
        ds.publishFile("org.cogcomp.mate-tools", "CoNLL2009-ST-English-ALL.anna.postagger.model", 3.3,
                "/Users/daniel/Desktop/CoNLL2009-ST-English-ALL.anna-3.3.postagger.model",
                false, true);
        File f = ds.getFile("org.cogcomp.mate-tools", "CoNLL2009-ST-English-ALL.anna.lemmatizer.model", 3.3, false);
        File f2 = ds.getFile("org.cogcomp.mate-tools", "CoNLL2009-ST-English-ALL.anna.parser.model", 3.3, false);
        File f3 = ds.getFile("org.cogcomp.mate-tools", "CoNLL2009-ST-English-ALL.anna.postagger.model", 3.3, false);
    }

    public static void pathLSTM(Datastore ds) throws DatastoreException {
        ds.publishFile("uk.ac.ed.inf", "pathLSTM.model", 1.0,
                "/Users/daniel/ideaProjects/illinois-cogcomp-nlp/models/srl-ACL2016-eng.model",
                false, true);
        File f = ds.getFile("uk.ac.ed.inf", "pathLSTM.model", 1.0, false);
    }

    public static void verbSenseModels(Datastore ds) throws DatastoreException {
        ds.publishDirectory("edu.illinois.cs.cogcomp.verbsense", "verbsense-models", 1.0, "/Users/daniel/ideaProjects/illinois-verbsense/models", false, true);
        File f = ds.getDirectory("edu.illinois.cs.cogcomp.verbsense", "verbsense-models", 1.0, false);
        System.out.println("f: " + f);
    }

    public static void senseList(Datastore ds) throws DatastoreException {
        ds.publishFile("edu.illinois.cs.cogcomp.verbsense", "sense-list.txt", 1.0,
                "/Users/daniel/ideaProjects/illinois-verbsense/data2/sense-list.txt",
                false, true);
        File f = ds.getFile("edu.illinois.cs.cogcomp.verbsense", "sense-list.txt", 1.0, false);
    }

    public static void wordEmbeddingFile(Datastore ds) throws DatastoreException {
        ds.publishFile("org.cogcomp.wordembedding", "phrase2vec.txt", 1.5,
                "/Users/daniel/Desktop/phrase2vec.txt", false, true);
        File f = ds.getFile("org.cogcomp.wordembedding", "phrase2vec.txt", 1.5);
    }
}
