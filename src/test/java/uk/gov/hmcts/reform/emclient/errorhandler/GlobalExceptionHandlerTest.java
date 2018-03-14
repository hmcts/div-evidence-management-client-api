package uk.gov.hmcts.reform.emclient.errorhandler;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@RunWith(MockitoJUnitRunner.class)
public class GlobalExceptionHandlerTest {

    @Mock
    private RequestAttributes mockRequestAttributes;

    private HttpServletRequest mockHttpServletRequest;
    private HttpServletResponse mockHttpServletResponse;

    private GlobalExceptionHandler underTest;


    @Before
    public void setUp() {
        RequestContextHolder.setRequestAttributes(mockRequestAttributes);

        underTest = new GlobalExceptionHandler();
        mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletResponse = new MockHttpServletResponse();
    }

    @Test
    public void handleClientExceptionShouldGetTheStatusCodeFromTheExceptionAndSetTheCorrectBody() {
        HttpClientErrorException mockException = mock(HttpClientErrorException.class);

        given(mockException.getStatusCode()).willReturn(HttpStatus.NOT_FOUND);

        ResponseEntity<Object> responseEntity =
                underTest.handleClientException(mockException, mockHttpServletRequest, mockHttpServletResponse);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("Http Client Exception. Please check service input parameters and also verify the status of service token generator", responseEntity.getBody());
    }

    @Test
    public void handleMaxUploadExceptionShouldReturn500() {
        ResourceAccessException mockException = mock(ResourceAccessException.class);

        ResponseEntity<Object> responseEntity =
                underTest.handleMaxUploadException(mockException, mockHttpServletRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("Some server side exception occurred. Please check logs for details", responseEntity.getBody());
    }

    @Test
    public void handleValidationExceptionShouldSetTheStatusCodeTo200() {
        ConstraintViolationException mockException = mock(ConstraintViolationException.class);

        ResponseEntity<Map<String, Object>> responseEntity =
                underTest.handleValidationException(mockException, mockHttpServletRequest);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void handleValidationExceptionShouldReturnErrorAttributes() {
        ConstraintViolationException mockException = mock(ConstraintViolationException.class);

        given(mockRequestAttributes.getAttribute("javax.servlet.error.status_code", 0))
                .willReturn(HttpStatus.BAD_REQUEST.value());
        given(mockRequestAttributes.getAttribute("javax.servlet.error.error_code", 0))
                .willReturn("invalidFileType");
        given(mockRequestAttributes.getAttribute("javax.servlet.error.request_uri", 0))
                .willReturn("/path/to/resource");

        ResponseEntity<Map<String, Object>> responseEntity =
                underTest.handleValidationException(mockException, mockHttpServletRequest);

        Map<String, Object> body = responseEntity.getBody();
        assertEquals(400, body.get("status"));
        assertEquals("invalidFileType", body.get("errorCode"));
        assertEquals("/path/to/resource", body.get("path"));
    }

    @Test
    public void handleValidationExceptionShouldReturnMessageFromConstraintViolation() {
        ConstraintViolation<?> mockConstraintViolation = mock(ConstraintViolation.class);
        given(mockConstraintViolation.getMessage()).willReturn("Value is invalid");

        ConstraintViolationException mockException = mock(ConstraintViolationException.class);
        given(mockException.getConstraintViolations())
                .willReturn(Collections.singleton(mockConstraintViolation));

        ResponseEntity<Map<String, Object>> responseEntity =
                underTest.handleValidationException(mockException, mockHttpServletRequest);

        Map<String, Object> body = responseEntity.getBody();
        assertEquals("Value is invalid", body.get("message"));
    }
}
