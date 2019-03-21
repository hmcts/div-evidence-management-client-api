package uk.gov.hmcts.reform.emclient.errorhandler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;

import java.util.Map;

import javax.xml.bind.ValidationException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.web.context.request.WebRequest;

@RunWith(MockitoJUnitRunner.class)
public class GlobalErrorAttributesTest {

    @Mock
    private WebRequest mockWebRequest;

    private GlobalErrorAttributes underTest;

    @Before
    public void setUp() {
        underTest = new GlobalErrorAttributes();
    }

    @Test
    public void getErrorAttributesShouldIncludeAllAttributesFromDefaultErrorAttributes() {
        given(mockWebRequest.getAttribute("javax.servlet.error.status_code", 0))
                .willReturn(400);
        given(mockWebRequest.getAttribute(DefaultErrorAttributes.class.getName() + ".ERROR", 0))
                .willReturn(new ValidationException("Value is invalid"));

        Map<String, Object> errorAttributes =
                underTest.getErrorAttributes(mockWebRequest, false);

        assertNotNull(errorAttributes.get("timestamp"));
        assertEquals(400, errorAttributes.get("status"));
        assertEquals("Bad Request", errorAttributes.get("error"));
        assertEquals("Value is invalid", errorAttributes.get("message"));
    }

    @Test
    public void getErrorAttributesShouldReturnErrorCodeWhenCorrectRequestAttributeIsSet() {
        given(mockWebRequest.getAttribute("javax.servlet.error.status_code", 0))
                .willReturn(400);
        given(mockWebRequest.getAttribute("javax.servlet.error.error_code", 0))
                .willReturn("validationFailure");
        given(mockWebRequest.getAttribute(DefaultErrorAttributes.class.getName() + ".ERROR", 0))
                .willReturn(new ValidationException("Value is invalid"));

        Map<String, Object> errorAttributes =
                underTest.getErrorAttributes(mockWebRequest, false);

        assertEquals("validationFailure", errorAttributes.get("errorCode"));

    }

    @Test
    public void getErrorAttributesShouldNotReturnErrorCodeWhenTheErrorCodeRequestAttributeIsMissing() {
        given(mockWebRequest.getAttribute("javax.servlet.error.status_code", 0))
                .willReturn(400);
        given(mockWebRequest.getAttribute(DefaultErrorAttributes.class.getName() + ".ERROR", 0))
                .willReturn(new ValidationException("Value is invalid"));

        Map<String, Object> errorAttributes =
                underTest.getErrorAttributes(mockWebRequest, false);

        assertNull( errorAttributes.get("errorCode"));

    }
}
