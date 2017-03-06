package org.cogcomp;

import edu.illinois.cs.cogcomp.core.io.IOUtils;
import io.minio.MinioClient;
import io.minio.errors.*;
import io.minio.policy.PolicyType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.xmlpull.v1.XmlPullParserException;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * A thin wrapper over Minio that stores and retrieves immutable data on our machines and supports versioning.
 * Endpoint is an URL, domain name, IPv4 or IPv6 address.<pre>
 *              Valid endpoints:
 *              * https://s3.amazonaws.com
 *              * https://s3.amazonaws.com/
 *              * https://play.minio.io:9000
 *              * http://play.minio.io:9010/
 *              * localhost
 *              * localhost.localdomain
 *              * play.minio.io
 *              * 127.0.0.1
 *              * 192.168.1.60
 */

public class Datastore {
    private MinioClient minioClient = null;
    private static final String CONFIG_FILE = "datastore-config.properties";

    // this is where we keep the files locally
    private String dataStoreDirectory = System.getProperty("user.home") + File.separator + ".cogcomp-datastore";


    public Datastore() throws InvalidPortException, InvalidEndpointException {
        // Create a minioClient with the information read from configuration file
        ResourceManager rm = null;
        try {
            rm = new ResourceManager(CONFIG_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String endpoint = rm.getString("ENDPOINT");
        System.out.println(endpoint);
        if(rm.containsKey("ACCESS-KEY") && rm.containsKey("SECRET-KEY")) {
            String accessKey = rm.getString("ACCESS-KEY");
            String secretKey = rm.getString("SECRET-KEY");
            System.out.println("Reading config informtion from file . . . \n");
            System.out.println("\t\tEndpoint: " + endpoint);
            System.out.println("\t\tAccessKey: " + accessKey);
            System.out.println("\t\tSecretKey: " + secretKey);
            minioClient = new MinioClient(endpoint, accessKey, secretKey);
        }
        else
            minioClient = new MinioClient(endpoint);
        IOUtils.mkdir(dataStoreDirectory);
    }

    public Datastore(String endpoint) throws InvalidPortException, InvalidEndpointException {
        // Creates Minio client object with given endpoint using anonymous access.
        this.minioClient = new MinioClient(endpoint);
    }

    public Datastore(String endpoint, String accessKey, String secretKey) throws InvalidPortException, InvalidEndpointException {
        System.out.println("Setting the connection details directly with the constructor . . . ");
        System.out.println("\t\tEndpoint: " + endpoint);
        System.out.println("\t\tAccessKey: " + accessKey);
        System.out.println("\t\tSecretKey: " + secretKey);
        // Create a minioClient with the Minio Server name, Port, Access key and Secret key.
        minioClient = new MinioClient(endpoint, accessKey, secretKey);
        IOUtils.mkdir(dataStoreDirectory);
    }

//    public InputStream getFileAsStream(String groupId, String artifactId, Double version) throws IOException, InvalidKeyException, NoSuchAlgorithmException, InsufficientDataException, InvalidArgumentException, InternalException, NoResponseException, InvalidBucketNameException, XmlPullParserException, ErrorResponseException {
//        return minioClient.getObject(groupId, artifactId + Double.toString(version) );
//    }

//    public File getFile(String groupId, String artifactId, Double version) throws IOException, InvalidKeyException, NoSuchAlgorithmException, InsufficientDataException, InvalidArgumentException, InternalException, NoResponseException, InvalidBucketNameException, XmlPullParserException, ErrorResponseException {
//        InputStream fileStream = getFileAsStream(groupId, artifactId, version);
//        byte[] buffer = new byte[fileStream.available()];
//        fileStream.read(buffer);
//        File targetFile = new File(dataStoreDirectory + File.separator +
//                groupId + File.separator + artifactId + Double.toString(version) );
//        OutputStream outStream = new FileOutputStream(targetFile);
//        outStream.write(buffer);
//        return targetFile;
//    }

    public File getFile(String groupId, String artifactId, Double version) throws IOException, InvalidKeyException, NoSuchAlgorithmException, InsufficientDataException, InvalidArgumentException, InternalException, NoResponseException, InvalidBucketNameException, XmlPullParserException, ErrorResponseException {
        return getFile(groupId, artifactId, version, false);
    }

    public File getFile(String groupId, String artifactId, Double version, Boolean isPrivate) throws IOException, InvalidKeyException, NoSuchAlgorithmException, InsufficientDataException, InvalidArgumentException, InternalException, NoResponseException, InvalidBucketNameException, XmlPullParserException, ErrorResponseException {
        String versionedFileName = getNormalizedArtifactId(artifactId, version);
        String augmentedGroupId = (isPrivate? "private.": "readonly.") + groupId;
        System.out.println("Downloading the file from datastore . . . ");
        System.out.println("\t\tGroupId: " + augmentedGroupId);
        System.out.println("\t\tArtifactId: " + versionedFileName);
        String fileFolder = dataStoreDirectory + File.separator + augmentedGroupId;
        IOUtils.mkdir(fileFolder);
        if(versionedFileName.contains("/")) {
            int idx = versionedFileName.lastIndexOf("/");
            FileUtils.forceMkdir(new File(fileFolder + File.separator + versionedFileName.substring(0, idx)));
        }
        minioClient.getObject(augmentedGroupId, versionedFileName, fileFolder + File.separator + versionedFileName);
        return new File(fileFolder + File.separator + versionedFileName);
    }

    /**
     * To publish a file into the datastore, given a groupId, artifactId, and version number. If the file already exist,
     * with the given details, it will give error.
     */
    public void publishFile(String groupId, String artifactId, Double version, String fileName) throws IOException, InvalidKeyException, NoSuchAlgorithmException, InsufficientDataException, InvalidArgumentException, InternalException, NoResponseException, InvalidBucketNameException, XmlPullParserException, ErrorResponseException {
        publishFile(groupId, artifactId, version, fileName, false, false);
    }

    public void publishFile(String groupId, String artifactId, Double version, String fileName, Boolean privateBucket,
                            Boolean overwrite) throws IOException, InvalidKeyException, NoSuchAlgorithmException,
            InsufficientDataException, InvalidArgumentException, InternalException, NoResponseException, InvalidBucketNameException, XmlPullParserException, ErrorResponseException {// Check if the bucket already exists.
        String augmentedGroupId = (privateBucket? "private.": "readonly.") + groupId;
        boolean isExist = minioClient.bucketExists(augmentedGroupId);
        if(isExist) {
            System.out.println("GroupId already exists.");
        } else {
            // Make a new bucket called asiatrip to hold a zip file of photos.
            System.out.println("Making new bucket: " + augmentedGroupId);
            minioClient.makeBucket(augmentedGroupId);
                try {
                    if(privateBucket) {
                        // do nothing: the default behavior is private
                        // ref: http://stackoverflow.com/a/42616613/1164246
                    }
                    else {
                        // anonymous users should be able to read the file, if it is not private
                        minioClient.setBucketPolicy(augmentedGroupId, "", PolicyType.READ_ONLY);
                    }
                } catch (InvalidObjectPrefixException e) {
                    e.printStackTrace();
                }
        }

        String versionedFileName = getNormalizedArtifactId(artifactId, version);

        if(minioClient.listObjects(augmentedGroupId, versionedFileName).iterator().hasNext()) {
            if (!overwrite) {
                System.out.println("File already exists! Cannot replace it, unless you set the overwrite parameter to be true. ");
            }
            else {
                System.out.println("File already exists! Overwriting the old file. ");
            }
        }


        System.out.println("Publishing file: ");
        System.out.println("\t\t GroupId: " + augmentedGroupId);
        System.out.println("\t\t ArtifactId " + versionedFileName);
        System.out.println("\t\t FileName: " + fileName);
        minioClient.putObject(augmentedGroupId, versionedFileName, fileName);
    }

    private String getNormalizedArtifactId(String artifactId, Double version) {
        String extension = FilenameUtils.getExtension(artifactId);
        String fileNameWithoutExtension = artifactId.replace(extension, "");
        return fileNameWithoutExtension + "-" + Double.toString(version) + (extension.equals("")? "": "." + extension);
    }

    public static void main(String[] args) {
        try {
            Datastore ds = new Datastore();

            try {
                // publish a public file
                // ds.publishFile("edu.cogcomp", "pom", 1.0, "pom.xml");

                // publish a private file
                // ds.publishFile("edu.cogcomp", "pom", 1.0, "pom.xml");

                // publish a file and overwrite the old file
                // ds.publishFile("edu.cogcomp", "pom", 1.0, "pom.xml", true);

                // publish a file and observe overwrite error
                // ds.publishFile("edu.cogcomp", "pom", 1.0, "pom.xml");

                // read a public file
                // File f = ds.getFile("edu.cogcomp", "pom", 1.0);

                // read a public file without credentials
                Datastore dsNoCredentials = new Datastore("http://smaug.cs.illinois.edu:8080");
                File f = ds.getFile("edu.cogcomp", "pom", 1.0);

                // read a private file

                // publish a public folder

                // publish a private folder

                // read a public folder

                // read a private folder

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
}
