package excepciones;

import java.io.IOException;

public class PuertoEnUsoException extends IOException {

    public PuertoEnUsoException(String message) {
        super(message);
    }

}