package excepciones;

import java.io.IOException;

public class PuertoInvalidoException extends IOException {

    public PuertoInvalidoException(String message) {
        super(message);
    }

}