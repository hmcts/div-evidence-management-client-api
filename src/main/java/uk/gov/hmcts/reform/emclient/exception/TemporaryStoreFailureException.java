package uk.gov.hmcts.reform.emclient.exception;

public class TemporaryStoreFailureException extends RuntimeException {
    private static final long serialVersionUID = 2782677098999272722L;

    public TemporaryStoreFailureException(Throwable cause) {
        super(cause);
    }
}
