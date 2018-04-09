package uk.gov.hmcts.reform.emclient.response;

import lombok.Builder;
import lombok.Value;
import org.springframework.http.HttpStatus;

@Value
@Builder
public class FileUploadResponse {

   private String fileUrl;

   private String fileName;

   private String mimeType;

   private String createdBy;

   private String lastModifiedBy;

   private String createdOn;

   private String modifiedOn;

   private HttpStatus status;

}
