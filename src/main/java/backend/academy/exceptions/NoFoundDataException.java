package backend.academy.exceptions;

public class NoFoundDataException extends RuntimeException {
    public NoFoundDataException(String message) {
        super(message);
    }
}
