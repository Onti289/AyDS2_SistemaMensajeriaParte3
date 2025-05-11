package excepciones;

import java.io.IOException;

public class IPInvalidoException extends IOException {

    public IPInvalidoException(String message) {
        super(message);
    }

}