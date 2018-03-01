package uk.gov.hmcts.reform.emclient.exception;

/**
 * Thrown in case of access errors (if the temporary store fails)
 * 
 * @author nitinprabhu
 *
 */
public class TemporaryStoreFailureException extends RuntimeException {
    private static final long serialVersionUID = 2782677098999272722L;

    public TemporaryStoreFailureException(Throwable cause) {
        super(cause);
    }
}
