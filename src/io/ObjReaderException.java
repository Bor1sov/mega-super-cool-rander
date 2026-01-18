package io;

/**
 * Исключение для ошибок чтения OBJ файлов
 */
public class ObjReaderException extends Exception {
    public ObjReaderException(String message) {
        super(message);
    }

    public ObjReaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObjReaderException(String message, int lineNumber) {
        super(String.format("Error on line %d: %s", lineNumber, message));
    }
}
