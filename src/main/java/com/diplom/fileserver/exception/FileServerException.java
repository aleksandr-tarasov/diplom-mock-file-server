package com.diplom.fileserver.exception;

public class FileServerException extends RuntimeException {

    public FileServerException() {
        super();
    }

    public FileServerException(String message) {
        super(message);
    }

    public FileServerException(Throwable cause) {
        super(cause);
    }

    public FileServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
