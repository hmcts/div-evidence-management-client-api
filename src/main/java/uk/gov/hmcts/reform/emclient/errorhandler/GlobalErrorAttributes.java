package uk.gov.hmcts.reform.emclient.errorhandler;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@Component
@SuppressWarnings("deprecation")
public class GlobalErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest requestAttributes, ErrorAttributeOptions options) {
        if (options == null) {
            options = ErrorAttributeOptions.defaults();
        }
        Map<String, Object> errorAttributes =
                super.getErrorAttributes(requestAttributes, options);
        String errorCode =
                (String) requestAttributes.getAttribute("javax.servlet.error.error_code", RequestAttributes.SCOPE_REQUEST);
        if (StringUtils.isNotBlank(errorCode)) {
            errorAttributes.put("errorCode", errorCode);
        }

        return errorAttributes;
    }
}
