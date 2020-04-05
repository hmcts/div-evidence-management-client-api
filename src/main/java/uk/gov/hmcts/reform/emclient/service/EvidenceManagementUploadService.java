package uk.gov.hmcts.reform.emclient.service;

import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.emclient.response.FileUploadResponse;

import java.util.List;

public interface EvidenceManagementUploadService {

    List<FileUploadResponse> upload(List<MultipartFile> files, String authorizationToken, String requestId);

}
