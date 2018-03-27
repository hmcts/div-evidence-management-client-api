package uk.gov.hmcts.reform.emclient.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import uk.gov.hmcts.reform.emclient.response.FileUploadResponse;

public interface EvidenceManagementUploadService {
    List<FileUploadResponse> uploadFilesWithS2SAuthToken(List<MultipartFile> files, String authorizationToken, String requestId);

    List<FileUploadResponse> uploadFilesWithUserAuthToken(List<MultipartFile> files, String authorizationToken, String requestId);
}
