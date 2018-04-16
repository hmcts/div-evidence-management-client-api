package uk.gov.hmcts.reform.emclient.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import uk.gov.hmcts.reform.emclient.response.FileUploadResponse;
import uk.gov.hmcts.reform.emclient.service.EvidenceManagementDownloadService;
import uk.gov.hmcts.reform.emclient.service.EvidenceManagementUploadService;
import uk.gov.hmcts.reform.emclient.validation.constraint.EvidenceFile;

@RestController
@RequestMapping(path = "/emclientapi")
@Validated
public class EvidenceManagementClientController {

    @Autowired
    private EvidenceManagementUploadService emUploadService;

    @Autowired
    private EvidenceManagementDownloadService emDownloadService;

    @PostMapping(value = "/version/1/uploadFilesWithS2SAuthToken", produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public List<FileUploadResponse> handleFileUploadWithS2SToken(
            @RequestHeader(value = "Authorization") String authorizationToken,
            @RequestHeader(value = "requestId", required = false) String requestId,
            @RequestParam("file") List<@EvidenceFile MultipartFile> files) {

        return emUploadService.upload(files, authorizationToken, requestId);
    }

    @PostMapping(value = "/version/1/uploadFiles", produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public List<FileUploadResponse> handleFileUpload(
            @RequestHeader(value = "Authorization") String authorizationToken,
            @RequestHeader(value = "requestId", required = false) String requestId,
            @RequestParam("file") List<@EvidenceFile MultipartFile> files) {

        return emUploadService.upload(files, authorizationToken, requestId);
    }

    @PostMapping(value = "/version/1/upload", produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public List<FileUploadResponse> upload(
            @RequestHeader(value = "Authorization", required = false) String authorizationToken,
            @RequestHeader(value = "requestId", required = false) String requestId,
            @RequestParam("file") List<@EvidenceFile MultipartFile> files) {

        return emUploadService.upload(files, authorizationToken, requestId);
    }


    @GetMapping(value = "/version/1/downloadFile", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ResponseEntity<InputStreamResource> downloadFile(
            @RequestHeader(value = "Authorization") String authorizationToken,
            @RequestHeader(value = "requestId", required = false) String requestId,
            @RequestParam("fileUrl") String selfFileUrl) throws IOException {
        return emDownloadService.downloadFile(selfFileUrl, authorizationToken, requestId);
    }
}