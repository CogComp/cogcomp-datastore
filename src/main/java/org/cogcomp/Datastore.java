package org.cogcomp;

import com.google.common.io.ByteStreams;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.errors.*;
import io.minio.policy.PolicyType;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.commons.io.FileUtils;
import org.xmlpull.v1.XmlPullParserException;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * A thin wrapper over Minio that stores and retrieves immutable data on our machines and supports versioning.
 * Endpoint is an URL, domain name, IPv4 or IPv6 address. Here are examples of valid endpoints:
 *      https://s3.amazonaws.com
 *      https://s3.amazonaws.com/
 *      https://play.minio.io:9000
 *      http://play.minio.io:9010/
 *      localhost
 *      localhost.localdomain
 *      play.minio.io
 *      127.0.0.1
 *      192.168.1.60
 */

public class Datastore {
    private boolean traceOn = false;
    private MinioClient minioClient = null;
    private static final String CONFIG_FILE = "datastore-config.properties";

    // this is where we keep the files locally
    private String DATASTORE_FOLDER = null;

    // this is where we keep the temporary files
    private String TMP_FOLDER = null;

    // if this is not null, we will put files in this location
    private String cacheFolder = null;

    /** this is used as a semaphore to lock creation of cache dirs. */
    static private String MKDIR_LOCK = "LOCK_MK_MINIO_CACHE_DIRS";
    
    /** The process to download and cache a resource is two steps. First step is to identify
     * a synchronization lock uniquely associated with the resource by name. Second step is then 
     * to sync on that lock to load the resource. These two processes are sychronized to different
     * objects.*/
    static private final HashMap<String, String> loadLock = new HashMap<String, String>();
    
    public Datastore() throws DatastoreException {
        // Create a minioClient with the information read from configuration file
        ResourceManager rm = null;
        try {
            rm = new ResourceManager(CONFIG_FILE);
        } catch (IOException e) {
            e.printStackTrace();
            throw new DatastoreException("invalid resource keys . . . ");
        }

        String endpoint = rm.getString("ENDPOINT");
        System.out.println(endpoint);
        if(rm.containsKey("ACCESS-KEY") && rm.containsKey("SECRET-KEY")) {
            String accessKey = rm.getString("ACCESS-KEY");
            String secretKey = rm.getString("SECRET-KEY");
            this.cacheFolder = rm.containsKey("CACHE-ROOT-FOLDER")?rm.getString("CACHE-ROOT-FOLDER"):null;
            System.out.println("Reading config informtion from file . . . \n");
            System.out.println("\t\tEndpoint: " + endpoint);
            System.out.println("\t\tAccessKey: " + accessKey);
            System.out.println("\t\tSecretKey: " + secretKey);
            try {
                minioClient = new MinioClient(endpoint, accessKey, secretKey);
                if(this.traceOn) minioClient.traceOn(System.out);
            } catch (InvalidEndpointException e) {
                e.printStackTrace();
                throw new DatastoreException("Invalid end-point url . . .");
            } catch (InvalidPortException e) {
                e.printStackTrace();
                throw new DatastoreException("Invalid end-point port . . .");
            }
        }
        else {
            try {
                minioClient = new MinioClient(endpoint);
                if(this.traceOn) minioClient.traceOn(System.out);
            } catch (InvalidEndpointException e) {
                e.printStackTrace();
                throw new DatastoreException("Invalid end-point url . . .");
            } catch (InvalidPortException e) {
                e.printStackTrace();
                throw new DatastoreException("Invalid end-point port . . .");
            }
        }
        setCacheFolders();
        makeDirectories();
    }
    
    /**
     * Checks if a file is gzipped and returns the appropriate stream.
     * @param stream the input stream to the file to check.
     * @return the input stream, converted to a gzip input stream if necessary.
     * @throws IOException
     */
    private static InputStream checkGZipped(InputStream stream) throws IOException {
        // check the first two bytes, if it's a gzip signature, return a gzip stream
        PushbackInputStream pb = new PushbackInputStream(stream, 2);
        byte[] signature = new byte[2];
        pb.read(signature);
        pb.unread(signature);
        if (signature[0] == (byte) 0x1f && signature[1] == (byte) 0x8b)
            return new GZIPInputStream(pb);
        else
            return pb;
    }

    /**
     * Loads a file either from the file system, and if not there, look in the 
     * classpath, returning null if not found
     * @param file The name of the file
     * @return an input stream if the resource was found, or null if not.
     */
    @SuppressWarnings("resource")
    public static InputStream findFile(String file) {
        InputStream stream = null;
        // see if there is a file.
        try {
            stream = new FileInputStream(file);
        } catch (FileNotFoundException fnfe) {
            
            // it wasn't in file system, check the classpath.
            List<URL> list = null;
            try {
                list = IOUtils.lsResources(Datastore.class, file);
            } catch (URISyntaxException | IOException e) {
                
                // If in classpath, there is a problem that will prevent loading.;
                return null;
            }
            if (list.isEmpty()) {
                
                // also not in the classpath, return null;
                return null;
            }
            
            URL fileURL = list.get(0);
            URLConnection connection;
            try {
                connection = fileURL.openConnection();
            } catch (IOException e) {
                
                // in the class path, but could not open it.
                return null;
            }
            try {
                stream = connection.getInputStream();
            } catch (IOException e) {

                // in the class path, but could not open it.
                return null;
            }
        }
        
        // Open stream as GZipped if needed
        try {
            stream = checkGZipped(stream);
        } catch (IOException e) {

            // in the class path, but could not open it.
            return null;
        }
        return stream;
    }

    /**
     * synchronize the creation of these directories, not sure what a race condition
     * here might do, so we will just avoid it.
     */
    private void makeDirectories() {
        synchronized (MKDIR_LOCK) {
            IOUtils.mkdir(DATASTORE_FOLDER);
            IOUtils.mkdir(TMP_FOLDER);
        }
    }
    public Datastore(String endpoint) throws DatastoreException {
        this(endpoint, System.getProperty("user.home"));
    }

    public Datastore(String endpoint, String cacheFolder) throws DatastoreException {
        // Creates Minio client object with given endpoint using anonymous access.
        try {
            this.minioClient = new MinioClient(endpoint);
            if(this.traceOn) minioClient.traceOn(System.out);
        } catch (InvalidEndpointException e) {
            e.printStackTrace();
            throw new DatastoreException("Invalid end-point url . . .");
        } catch (InvalidPortException e) {
            e.printStackTrace();
            throw new DatastoreException("Invalid end-point port . . .");
        }
        this.cacheFolder = cacheFolder;
        setCacheFolders();
        makeDirectories();
    }

    public Datastore(ResourceManager rm) throws InvalidPortException, InvalidEndpointException {
        String endpoint = rm.getString("datastoreEndpoint");
        this.minioClient = new MinioClient(endpoint);
        if(this.traceOn) minioClient.traceOn(System.out);
        this.cacheFolder = (rm.containsKey("CACHE-ROOT-FOLDER"))?rm.getString("CACHE-ROOT-FOLDER"):null;
        this.setCacheFolders();
        this.makeDirectories();
    }

    public Datastore(String endpoint, String accessKey, String secretKey) throws InvalidPortException, InvalidEndpointException {
        this(endpoint, accessKey, secretKey, System.getProperty("user.home"));
    }

    public Datastore(String endpoint, String accessKey, String secretKey, String cacheFolder) throws InvalidPortException, InvalidEndpointException {
        System.out.println("Setting the connection details directly with the constructor . . . ");
        System.out.println("\t\tEndpoint: " + endpoint);
        System.out.println("\t\tAccessKey: " + accessKey);
        System.out.println("\t\tSecretKey: " + secretKey);
        // Create a minioClient with the Minio Server name, Port, Access key and Secret key.
        minioClient = new MinioClient(endpoint, accessKey, secretKey);
        this.cacheFolder = cacheFolder;
        if(this.traceOn) minioClient.traceOn(System.out);
        this.setCacheFolders();
        this.makeDirectories();

    }

    // upon running this file, we'd set the folders for caching.
    // ideally this should be run only once and at the initialization time
    private void setCacheFolders() {
        String f = (this.cacheFolder == null)? System.getProperty("user.home"):this.cacheFolder;
        this.DATASTORE_FOLDER = f + File.separator + ".cogcomp-datastore";
        this.TMP_FOLDER = this.DATASTORE_FOLDER + File.separator + "tmp";
    }

    public void setTrace(boolean traceOn) {
        this.traceOn = traceOn;
    }

    public File getFile(String groupId, String artifactId, Double version) throws DatastoreException {
        return getFile(groupId, artifactId, version, false);
    }

    public File getFile(String groupId, String artifactId, Double version, Boolean isPrivate) throws DatastoreException {
        try {
            IOUtils.cleanDir(TMP_FOLDER);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String versionedFileName = getNormalizedArtifactId(artifactId, version);
        String augmentedGroupId = (isPrivate? "private.": "readonly.") + groupId;
        System.out.println("Downloading the file from datastore . . . ");
        System.out.println("\t\tGroupId: " + augmentedGroupId);
        System.out.println("\t\tArtifactId: " + versionedFileName);
        String fileFolder = DATASTORE_FOLDER + File.separator + augmentedGroupId;
        String downloadedFileName = fileFolder + File.separator + versionedFileName;

        // first do the bookkeeping, make a synchronization lock object for loading this resource, or just fetch
        // if it already exists.
        synchronized(loadLock) {
            String loadlock = loadLock.get(downloadedFileName);
            if (loadlock != null) {
                // this must be a shared singleton style resource, or the sync lock does nothing.
                downloadedFileName = loadlock; 
            } else {
                loadLock.put(downloadedFileName, downloadedFileName);
            }
        }
        
        // now we have a single object to sync on to load the resource.
        synchronized(downloadedFileName) {
            if(new File(downloadedFileName).exists()) {
                System.out.println("File " +  downloadedFileName + " already exists. Skipping download from the datastore . . . ");
            }
            else {
                IOUtils.mkdir(fileFolder);
                if (versionedFileName.contains("/")) {
                    int idx = versionedFileName.lastIndexOf("/");
                    String location = fileFolder + File.separator + versionedFileName.substring(0, idx);
                    try {
                        FileUtils.forceMkdir(new File(location));
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new DatastoreException("Unable to create folder in your local machine " + location + " . . .");
                    }
                }
                try {
                    // if the file already exists, drop it:
                    // IOUtils.rm(fileFolder + File.separator + versionedFileName);
    
                    ObjectStat objectStat = this.minioClient.statObject(augmentedGroupId, versionedFileName);
    
                    InputStream is = new ProgressStream("Downloading .. ", ProgressBarStyle.ASCII,
                            objectStat.length(), minioClient.getObject(augmentedGroupId, versionedFileName));
    
                    Path path = Paths.get(downloadedFileName);
                    OutputStream os = Files.newOutputStream(path, StandardOpenOption.CREATE);
    
                    long bytesWritten = ByteStreams.copy(is, os);
                    is.close();
                    os.close();
                    if (bytesWritten != objectStat.length()) {
                        throw new IOException(path + ": unexpected data written.  expected = " + objectStat.length() + ", written = " + bytesWritten);
                    }
                } catch (InvalidBucketNameException e) {
                    e.printStackTrace();
                    throw new DatastoreException("Invalid bucket name . . . ");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InsufficientDataException e) {
                    e.printStackTrace();
                    throw new DatastoreException("Insufficient data . . . ");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                    throw new DatastoreException("Invalid key . . . ");
                } catch (NoResponseException e) {
                    e.printStackTrace();
                    throw new DatastoreException("No server response . . . ");
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (ErrorResponseException e) {
                    e.printStackTrace();
                } catch (InternalException e) {
                    e.printStackTrace();
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
        return new File(downloadedFileName);
    }

    /**
     * To publish a file into the datastore, given a groupId, artifactId, and version number. If the file already exist,
     * with the given details, it will give error.
     */
    public void publishFile(String groupId, String artifactId, Double version, String fileName) throws DatastoreException  {
        publishFile(groupId, artifactId, version, fileName, false, false);
    }

    public void publishFile(String groupId, String artifactId, Double version, String fileName, Boolean privateBucket,
                            Boolean overwrite) throws DatastoreException  {
        String augmentedGroupId = (privateBucket? "private.": "readonly.") + groupId;
        try {
            setBucketPolicies(augmentedGroupId, privateBucket);
            String versionedFileName = getNormalizedArtifactId(artifactId, version);

            if(minioClient.listObjects(augmentedGroupId, versionedFileName).iterator().hasNext()) {
                if (!overwrite) {
                    System.out.println("File already exists! Cannot replace it, unless you set the overwrite parameter to be true. ");
                    return;
                }
                else {
                    System.out.println("File already exists! Overwriting the old file. ");
                }
            }
            System.out.println("Publishing file: ");
            System.out.println("\t\t GroupId: " + augmentedGroupId);
            System.out.println("\t\t ArtifactId " + versionedFileName);
            System.out.println("\t\t FileName: " + fileName);

            File file = new File(fileName);
            InputStream pis = new BufferedInputStream(new ProgressStream("Uploading... ",
                    ProgressBarStyle.ASCII, new FileInputStream(file)));
            //minioClient.putObject(augmentedGroupId, versionedFileName, fileName);
            minioClient.putObject(augmentedGroupId, versionedFileName, pis, pis.available(), "application/octet-stream");
            pis.close();
        } catch (InvalidBucketNameException e) {
            e.printStackTrace();
            throw new DatastoreException("InvalidBucketName . . . ");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InsufficientDataException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            throw new DatastoreException("InvalidKeyName . . . ");
        } catch (NoResponseException e) {
            e.printStackTrace();
            throw new DatastoreException("No response from server . . . ");
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (ErrorResponseException e) {
            e.printStackTrace();
        } catch (InternalException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        } catch (InvalidObjectPrefixException e) {
            e.printStackTrace();
        }
    }

    private void setBucketPolicies(String augmentedGroupId, boolean privateBucket) throws IOException, InvalidKeyException, NoSuchAlgorithmException, InsufficientDataException, InternalException, NoResponseException, InvalidBucketNameException, XmlPullParserException, ErrorResponseException, InvalidObjectPrefixException {
        // Check if the bucket already exists.
        boolean isExist = minioClient.bucketExists(augmentedGroupId);
        if(isExist) {
            System.out.println("GroupId already exists.");
        } else {
            // Make a new bucket called asiatrip to hold a zip file of photos.
            System.out.println("Making new bucket: " + augmentedGroupId);
            minioClient.makeBucket(augmentedGroupId);
            if(privateBucket) {
                // do nothing: the default behavior is private
                // ref: http://stackoverflow.com/a/42616613/1164246
            }
            else {
                // anonymous users should be able to read the file, if it is not private
                minioClient.setBucketPolicy(augmentedGroupId, "", PolicyType.READ_ONLY);
            }
        }
    }

    public void publishDirectory(String groupId, String artifactId, Double version, String path, Boolean privateBucket,
                            Boolean overwrite) throws DatastoreException  {
        String augmentedGroupId = (privateBucket? "private.": "readonly.") + groupId;
        try {
            setBucketPolicies(augmentedGroupId, privateBucket);
            String versionedFileName = getNormalizedArtifactId(artifactId, version) + ".zip";

            // creating a zip file of the folder. Ensure the path to the temp zip file actually exists.
            String zippedFileName = TMP_FOLDER + File.separator + version + File.separator + new File(path).getName() + ".zip";
            IOUtils.mkdir(TMP_FOLDER + File.separator + version + File.separator);
            ZipHelper.zipDir(path, zippedFileName);

            if(minioClient.listObjects(augmentedGroupId, versionedFileName).iterator().hasNext()) {
                if (!overwrite) {
                    System.out.println("Directory already exists! Cannot replace it, unless you set the overwrite parameter to be true. ");
                }
                else {
                    System.out.println("Directory already exists! Overwriting the old file. ");
                }
            }

            File fileObj = new File(zippedFileName);
            String fileSizeReadable = FileUtils.byteCountToDisplaySize(fileObj.length());
            System.out.println("Size of the zipped file: " + fileSizeReadable);
            System.out.println("Publishing directory: ");
            System.out.println("\t\t GroupId: " + augmentedGroupId);
            System.out.println("\t\t ArtifactId " + versionedFileName);
            System.out.println("\t\t FolderPath: " + path);
            System.out.println("\t\t ZippedPath: " + zippedFileName);
            File file = new File(zippedFileName);
            InputStream pis = new BufferedInputStream(new ProgressStream("Uploading... ",
                    ProgressBarStyle.ASCII, new FileInputStream(file)));
            //minioClient.putObject(augmentedGroupId, versionedFileName, fileName);
            minioClient.putObject(augmentedGroupId, versionedFileName, pis, pis.available(), "application/octet-stream");
            //minioClient.putObject(augmentedGroupId, versionedFileName, zippedFileName);
            pis.close();
            IOUtils.rm(zippedFileName);
        } catch (InvalidBucketNameException e) {
            e.printStackTrace();
            throw new DatastoreException("InvalidBucketName . . . ");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InsufficientDataException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            throw new DatastoreException("InvalidKeyName . . . ");
        } catch (NoResponseException e) {
            e.printStackTrace();
            throw new DatastoreException("No response from server . . . ");
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (ErrorResponseException e) {
            e.printStackTrace();
        } catch (InternalException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        } catch (InvalidObjectPrefixException e) {
            e.printStackTrace();
        }
    }

    public File getDirectory(String groupId, String artifactId, Double version, Boolean isPrivate) throws DatastoreException {
        try {
            IOUtils.cleanDir(TMP_FOLDER);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String versionedFileName = getNormalizedArtifactId(artifactId, version) + ".zip";
        String augmentedGroupId = (isPrivate? "private.": "readonly.") + groupId;
        System.out.println("Downloading the folder from datastore . . . ");
        System.out.println("\t\tGroupId: " + augmentedGroupId);
        System.out.println("\t\tArtifactId: " + versionedFileName);
        String fileFolder = DATASTORE_FOLDER + File.separator + augmentedGroupId;
        String path = fileFolder + File.separator + version + File.separator + artifactId;

        // first do the bookkeeping, make a synchronization lock object for loading this resource, or just fetch
        // if it already exists.
        synchronized(loadLock) {
            String loadlock = loadLock.get(path);
            if (loadlock != null) {
                // this must be a shared singleton style resource, or the sync lock does nothing.
                path = loadlock; 
            } else {
                loadLock.put(path, path);
            }
        }
        
        // now we have a single object to sync on to load the resource.
        synchronized(path) {
            if(IOUtils.exists(path)) {
                System.out.println("The target " + path + " already exists. Skipping download from the datastore . . . ");
            }
            else {
                IOUtils.mkdir(fileFolder);
                if (versionedFileName.contains("/")) {
                    int idx = versionedFileName.lastIndexOf("/");
                    String location = fileFolder + File.separator + versionedFileName.substring(0, idx);
                    try {
                        FileUtils.forceMkdir(new File(location));
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new DatastoreException("Unable to create folder in your local machine " + location + " . . .");
                    }
                }
    
                // creating a zip file of the folder
                String zippedFileName = TMP_FOLDER + File.separator + version + File.separator + artifactId + ".zip";
                IOUtils.mkdir(TMP_FOLDER + File.separator + version + File.separator);
                try {
                    System.out.println("augmentedGroupId: " + augmentedGroupId);
                    System.out.println("versionedFileName: " + versionedFileName);
                    System.out.println("zippedFileName: " + zippedFileName);
                    minioClient.getObject(augmentedGroupId, versionedFileName, zippedFileName);
                } catch (InvalidBucketNameException e) {
                    e.printStackTrace();
                    throw new DatastoreException("Invalid bucket name . . . ");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InsufficientDataException e) {
                    e.printStackTrace();
                    throw new DatastoreException("Insufficient data . . . ");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                    throw new DatastoreException("Invalid key . . . ");
                } catch (NoResponseException e) {
                    e.printStackTrace();
                    throw new DatastoreException("No server response . . . ");
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (ErrorResponseException e) {
                    e.printStackTrace();
                } catch (InternalException e) {
                    e.printStackTrace();
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                }
                IOUtils.mkdir(path);
    
                // unzip the downloaded zip file
                ZipHelper.unZipIt(zippedFileName, path);
                System.out.println("zippedFileName: " + zippedFileName);
                System.out.println("path: " + path);
                System.out.println("artifactId: " + artifactId);
                try {
                    IOUtils.rm(zippedFileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return new File(path);
    }

    private String getNormalizedArtifactId(String artifactId, Double version) {
        // old way: put the version number between the file name and its extension
        /*
        String extension = FilenameUtils.getExtension(artifactId);
        String fileNameWithoutExtension = artifactId.replace(extension, "");
        fileNameWithoutExtension + "-" + Double.toString(version) + (extension.equals("")? "": "." + extension);
        */
        return version + "/" + artifactId;
    }

    public static void main(String[] args) {
        Datastore ds = null;
        try {
            ds = new Datastore();
            // publish a public file
            ds.publishFile("edu.cogcomp", "pom", 1.0, "pom.xml");

            // publish a private file
            // ds.publishFile("edu.cogcomp", "pom", 1.0, "pom.xml");

            // publish a file and overwrite the old file
            // ds.publishFile("edu.cogcomp", "pom", 1.0, "pom.xml", true);

            // publish a file and observe overwrite error
            // ds.publishFile("edu.cogcomp", "pom", 1.0, "pom.xml");

            // read a public file
            // File f = ds.getFile("edu.cogcomp", "pom", 1.0);

            // read a public file without credentials
            // Datastore dsNoCredentials = new Datastore("http://smaug.cs.illinois.edu:8080");
            // File f = ds.getFile("edu.cogcomp", "pom", 1.0);

            // read a private file

            // publish a public folder

            // publish a private folder

            // read a public folder

            // read a private folder
        } catch (DatastoreException e) {
            e.printStackTrace();
        }
    }
}
