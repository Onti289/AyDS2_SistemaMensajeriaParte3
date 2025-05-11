package excepciones;

import java.io.IOException;

public class ErrorEnvioMensajeException extends IOException {

    public ErrorEnvioMensajeException(String message) {
        super(message);
    }

}