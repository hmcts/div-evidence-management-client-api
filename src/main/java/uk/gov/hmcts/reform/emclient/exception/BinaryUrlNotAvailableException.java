package uk.gov.hmcts.reform.emclient.exception;

// Thrown in case of access errors (if the temporary store fails)

public class BinaryUrlNotAvailableException extends RuntimeException {

    private static final long serialVersionUID = 8758617259382387538L;

    public BinaryUrlNotAvailableException(String message) {
        super(message);
    }
}
