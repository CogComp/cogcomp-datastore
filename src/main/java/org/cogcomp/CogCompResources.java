package org.cogcomp;

import io.minio.errors.*;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class CogCompResources {

    public static void main(String[] args) {
        try {
            Datastore ds = new Datastore();

            try {
                brownClusterUpload(ds);

                // read a public file without credentials
                 Datastore dsNoCredentials = new Datastore("http://smaug.cs.illinois.edu:8080");
                 File f = dsNoCredentials.getFile("edu.cogcomp", "brown-clusters/brown-rcv1.clean.tokenized-CoNLL03.txt-c100-freq1.txt", 1.3);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InsufficientDataException e) {
                e.printStackTrace();
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            } catch (InternalException e) {
                e.printStackTrace();
            } catch (NoResponseException e) {
                e.printStackTrace();
            } catch (InvalidBucketNameException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (ErrorResponseException e) {
                e.printStackTrace();
            }

        } catch (InvalidPortException e) {
            e.printStackTrace();
        } catch (InvalidEndpointException e) {
            e.printStackTrace();
        }
    }

    public static void brownClusterUpload(Datastore ds) throws IOException, XmlPullParserException, NoSuchAlgorithmException, InvalidKeyException, InvalidArgumentException, ErrorResponseException, NoResponseException, InvalidBucketNameException, InsufficientDataException, InternalException {
        ds.publishFile("edu.cogcomp", "brown-clusters/brown-rcv1.clean.tokenized-CoNLL03.txt-c100-freq1.txt", 1.3,
                "brown-clusters-1.3/brown-clusters/brown-rcv1.clean.tokenized-CoNLL03.txt-c100-freq1.txt", false, true);

        ds.publishFile("edu.cogcomp", "brown-clusters/brown-rcv1.clean.tokenized-CoNLL03.txt-c320-freq1.txt", 1.3,
                "brown-clusters-1.3/brown-clusters/brown-rcv1.clean.tokenized-CoNLL03.txt-c320-freq1.txt", false, true);

        ds.publishFile("edu.cogcomp", "brown-clusters/brown-rcv1.clean.tokenized-CoNLL03.txt-c1000-freq1.txt", 1.3,
                "brown-clusters-1.3/brown-clusters/brown-rcv1.clean.tokenized-CoNLL03.txt-c1000-freq1.txt", false, true);

        ds.publishFile("edu.cogcomp", "brown-clusters/brown-rcv1.clean.tokenized-CoNLL03.txt-c3200-freq1.txt", 1.3,
                "brown-clusters-1.3/brown-clusters/brown-rcv1.clean.tokenized-CoNLL03.txt-c3200-freq1.txt", false, true);

        ds.publishFile("edu.cogcomp", "brown-clusters/brownBllipClusters", 1.3,
                "brown-clusters-1.3/brown-clusters/brownBllipClusters", false, true);

        ds.publishFile("edu.cogcomp", "brown-clusters/brown-english-wikitext.case-intact.txt-c1000-freq10-v3", 1.3,
                "brown-clusters-1.3/brown-clusters/brown-english-wikitext.case-intact.txt-c1000-freq10-v3.txt", false, true);
    }

}
