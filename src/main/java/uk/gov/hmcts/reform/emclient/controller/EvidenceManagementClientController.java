package uk.gov.hmcts.reform.emclient.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.emclient.response.FileUploadResponse;
import uk.gov.hmcts.reform.emclient.service.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.emclient.service.EvidenceManagementDownloadService;
import uk.gov.hmcts.reform.emclient.service.EvidenceManagementUploadService;
import uk.gov.hmcts.reform.emclient.validation.constraint.EvidenceFile;

import java.io.IOException;
import java.util.List;

/**
 * @author nitinprabhu
 */
@RestController
@RequestMapping(path = "/emclientapi")
@Validated
public class EvidenceManagementClientController {

    @Autowired
    private EvidenceManagementUploadService emUploadService;

    @Autowired
    private EvidenceManagementDownloadService emDownloadService;

    @Autowired
    private EvidenceManagementDeleteService emDeleteService;

    @PostMapping(value = "/version/1/uploadFilesWithS2SAuthToken", produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public List<FileUploadResponse> handleFileUploadWithS2SToken(
            @RequestHeader(value = "authorizationToken") String authorizationToken,
            @RequestHeader(value = "requestId", required = false) String requestId,
            @RequestParam("file") List<@EvidenceFile MultipartFile> files) {

        return emUploadService.uploadFilesWithS2SAuthToken(files, authorizationToken, requestId);
    }

    @PostMapping(value = "/version/1/uploadFiles", produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public List<FileUploadResponse> handleFileUpload(
            @RequestHeader(value = "authorizationToken") String authorizationToken,
            @RequestHeader(value = "requestId", required = false) String requestId,
            @RequestParam("file") List<@EvidenceFile MultipartFile> files) {

        return emUploadService.uploadFilesWithUserAuthToken(files, authorizationToken, requestId);
    }

    @GetMapping(value = "/version/1/downloadFile", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ResponseEntity<InputStreamResource> downloadFile(
            @RequestHeader(value = "authorizationToken") String authorizationToken,
            @RequestHeader(value = "requestId", required = false) String requestId,
            @RequestParam("fileUrl") String selfFileUrl) throws IOException {
        return emDownloadService.downloadFile(selfFileUrl, authorizationToken, requestId);
    }

    @DeleteMapping(value = "/version/1/deleteFile", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<?> deleteFile(@RequestHeader(value = "authorizationToken")          String authorizationToken,
                                        @RequestHeader(value = "requestId", required = false) String requestId,
                                        @RequestParam("fileUrl")                              String fileUrl) {
        return emDeleteService.deleteFile(fileUrl, authorizationToken, requestId);
    }
}