package uk.gov.hmcts.reform.emclient.errorhandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final String EXCEPTION_MESSAGE = "Request Id : {} and Exception message : {}";
    private static final String REQUEST_ID_HEADER_KEY = "requestId";
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(HttpClientErrorException.class)
    // BAD_REQUEST(400),UNAUTHORIZED(401),FORBIDDEN(403),NOT_FOUND(404),METHOD_NOT_ALLOWED(405)...
    public ResponseEntity<Object> handleClientException(
            HttpClientErrorException clientErrorException,
            WebRequest request,
            HttpServletResponse response) {

        log.error(EXCEPTION_MESSAGE, request.getHeader(REQUEST_ID_HEADER_KEY), clientErrorException.getMessage());

        return ResponseEntity.status(clientErrorException.getStatusCode()).body(
                "Http Client Exception. Please check service input parameters and also verify the status of service token generator");
    }

    @ExceptionHandler(value = {ResourceAccessException.class, HttpServerErrorException.class})
    public ResponseEntity<Object> handleMaxUploadException(RestClientException restClientException, WebRequest request) {

        log.error(EXCEPTION_MESSAGE, request.getHeader(REQUEST_ID_HEADER_KEY), restClientException.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                "Some server side exception occurred. Please check logs for details");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(ConstraintViolationException exception, WebRequest webRequest) {
        /*
          This is a temporary solution because of an nginx configuration which requires a status code 200 to be returned.
         */
        webRequest.setAttribute("javax.servlet.error.status_code", HttpStatus.BAD_REQUEST.value(), WebRequest.SCOPE_REQUEST);
        webRequest.setAttribute("javax.servlet.error.error_code", "invalidFileType", WebRequest.SCOPE_REQUEST);
        webRequest.setAttribute("javax.servlet.error.request_uri",
            ((ServletWebRequest)webRequest).getRequest().getRequestURL().toString(),
            WebRequest.SCOPE_REQUEST);
        Map<String, Object> errorAttributes = new GlobalErrorAttributes().getErrorAttributes(webRequest, false);

        if (!exception.getConstraintViolations().isEmpty()) {
            errorAttributes.put("message", exception.getConstraintViolations().iterator().next().getMessage());
        }

        return new ResponseEntity<>(errorAttributes, HttpStatus.OK);
    }
}
