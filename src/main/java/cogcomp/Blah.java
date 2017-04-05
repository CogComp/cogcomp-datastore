package cogcomp;

import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.errors.MinioException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Blah {
    public static void main(String[] args)
            throws IOException, NoSuchAlgorithmException, InvalidKeyException, XmlPullParserException {
        try {
            MinioClient minioClient = new MinioClient("http://smaug.cs.illinois.edu:8080");
            minioClient.traceOn(System.out);

            String a1 = "readonly.org.cogcomp.comma-srl";
            String a2 = "2.2/comma-srl-data.zip";
            String a3 = "/Users/daniel/.cogcomp-datastore-tmp/comma-srl-data.zip";

            // Get object stat information.
            ObjectStat objectStat = minioClient.statObject(a1, a2);
            System.out.println(objectStat);

            minioClient.getObject(a1, a2, a3);
        } catch (MinioException e) {
            System.out.println("Error occurred: " + e);
        }
    }
}
