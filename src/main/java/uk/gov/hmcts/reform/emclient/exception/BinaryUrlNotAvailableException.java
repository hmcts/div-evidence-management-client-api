package uk.gov.hmcts.reform.emclient.exception;

public class BinaryUrlNotAvailableException extends RuntimeException {

    private static final long serialVersionUID = 8758617259382387538L;

    public BinaryUrlNotAvailableException(String message) {
        super(message);
    }
}
