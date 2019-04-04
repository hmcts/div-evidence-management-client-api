package uk.gov.hmcts.reform.emclient.controller;


import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.List;

@RestController
@RequestMapping(path = "/emclientapi")
@Validated
public class EvidenceManagementClientController {

    @Autowired
    private EvidenceManagementDeleteService emDeleteService;

    @Autowired
    private EvidenceManagementUploadService emUploadService;

    @Autowired
    private EvidenceManagementDownloadService emDownloadService;

    @PostMapping(value = "/version/1/upload", produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)

    @ResponseBody
    public List<FileUploadResponse> upload(
            @RequestHeader(value = "Authorization", required = false) String authorizationToken,
            @RequestHeader(value = "requestId", required = false) String requestId,
            @RequestParam("file") List<@EvidenceFile MultipartFile> files) {

        return emUploadService.upload(files, authorizationToken, requestId);
    }

    @DeleteMapping(value = "/version/1/deleteFile", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<?> deleteFile(@RequestHeader(value = "Authorization") String authorizationToken,
                                        @RequestHeader(value = "requestId", required = false) String requestId,
                                        @RequestParam("fileUrl") String fileUrl) {
        return emDeleteService.deleteFile(fileUrl, authorizationToken, requestId);
    }

    @GetMapping(value = "/version/1/download/{fileId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> getFile(
            @RequestHeader(value = "Authorization") String authorizationToken,
            @PathVariable("fileId") String fileId) {

        return emDownloadService.downloadFile(fileId, authorizationToken);

    }
}