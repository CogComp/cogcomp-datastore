package org.cogcomp;

public class DatastoreException extends Exception {
    public DatastoreException() { super(); }
    public DatastoreException(String message) { super(message); }
    public DatastoreException(String message, Throwable cause) { super(message, cause); }
    public DatastoreException(Throwable cause) { super(cause); }
}
