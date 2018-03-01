package uk.gov.hmcts.reform.emclient.service;

import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.emclient.response.FileUploadResponse;

import java.util.List;

/**
 * 
 * @author nitinprabhu
 *
 */
public interface EvidenceManagementUploadService {
    List<FileUploadResponse> uploadFilesWithS2SAuthToken(List<MultipartFile> files, String authorizationToken, String requestId);

    List<FileUploadResponse> uploadFilesWithUserAuthToken(List<MultipartFile> files, String authorizationToken, String requestId);
}
