import io.minio.MinioClient;
import io.minio.errors.*;
import org.xmlpull.v1.XmlPullParserException;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

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
 *
 */

// "http://silverbridge.cs.illinois.edu:8081", "1I9SK88HMRAB9UWQ7URC", "IsN/zQ99128acQyZF88ZdtRy+pO1eGEpubd6EhV0"
public class Datastore {
    private MinioClient minioClient = null;
    private static final String CONFIG_FILE = "datastore-config.properties";

    // this is where we keep the files locally
    private String dataStoreDirectory = System.getProperty("user.home") + File.separator + ".illinois-datastore";

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
            System.out.println(accessKey);
            System.out.println(secretKey);
            minioClient = new MinioClient(endpoint, accessKey, secretKey);
        }
        else
            minioClient = new MinioClient(endpoint);
    }

    public Datastore(String endpoint) throws InvalidPortException, InvalidEndpointException {
        // Creates Minio client object with given endpoint using anonymous access.
        minioClient = new MinioClient(endpoint);
    }

    public Datastore(String endpoint, String accessKey, String secretKey) throws InvalidPortException, InvalidEndpointException {
        System.out.println("endpoint " + endpoint);
        System.out.println("accessKey " + accessKey);
        System.out.println("secretKey " + secretKey);
        // Create a minioClient with the Minio Server name, Port, Access key and Secret key.
        minioClient = new MinioClient(endpoint, accessKey, secretKey);
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
        String fileName = dataStoreDirectory + File.separator +
                groupId + File.separator + artifactId + Double.toString(version);
        minioClient.getObject(groupId, artifactId + Double.toString(version), fileName);
        return new File(fileName);
    }

    public void publishFile(String groupId, String artifactId, Double version, String fileName) throws IOException, InvalidKeyException, NoSuchAlgorithmException, InsufficientDataException, InvalidArgumentException, InternalException, NoResponseException, InvalidBucketNameException, XmlPullParserException, ErrorResponseException {
        publishFile(groupId, artifactId, version, fileName, false);
    }

    public void publishFile(String groupId, String artifactId, Double version, String fileName, Boolean override) throws IOException, InvalidKeyException, NoSuchAlgorithmException, InsufficientDataException, InvalidArgumentException, InternalException, NoResponseException, InvalidBucketNameException, XmlPullParserException, ErrorResponseException {// Check if the bucket already exists.
        boolean isExist = minioClient.bucketExists(groupId);
        if(isExist) {
            System.out.println("GroupId already exists.");
        } else {
            // Make a new bucket called asiatrip to hold a zip file of photos.
            minioClient.makeBucket(groupId);
        }

        String[] tokens = artifactId.split("\\.(?=[^\\.]+$)");
        String versionedFileName = tokens[0] + "-" + Double.toString(version) + "." + tokens[1];

        if( override && minioClient.listObjects(groupId, versionedFileName).iterator().hasNext()) {
            System.out.println("File already exists! Cannot replace it, unless you set the override to be true. ");
        }

        System.out.println("groupId = " + groupId);
        System.out.println("artifactId + Double.toString(version) = " + versionedFileName);
        System.out.println("fileName = " + fileName);
        minioClient.putObject(groupId, versionedFileName, fileName);
    }
}
