package uk.gov.hmcts.reform.emclient.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.httpclient.DefaultApacheHttpClientFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.emclient.idam.services.UserService;
import uk.gov.hmcts.reform.emclient.response.FileUploadResponse;
import uk.gov.hmcts.reform.emclient.service.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.emclient.service.EvidenceManagementDownloadService;
import uk.gov.hmcts.reform.emclient.service.EvidenceManagementSecureDocStoreService;
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

    @Autowired
    private EvidenceManagementSecureDocStoreService evidenceManagementSecureDocStoreService;

    @Autowired
    private UserService userService;

    @Value("${feature.secure-doc-store:false}")
    boolean secureDocStoreEnabled;

    @PostMapping(value = "/version/1/upload", produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation(value = "Upload a file")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "File successfully uploaded",
            response = FileUploadResponse.class),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @ResponseBody
    public List<FileUploadResponse> upload(
        @RequestHeader(value = "Authorization", required = false) String authorizationToken,
        @RequestHeader(value = "requestId", required = false) String requestId,
        @RequestParam("file") @ApiParam("Files to upload") List<@EvidenceFile MultipartFile> files) {
        if (secureDocStoreEnabled) {
            return evidenceManagementSecureDocStoreService.upload(files, userService.getIdamTokens(authorizationToken));
        } else {
            return emUploadService.upload(files, authorizationToken, requestId);
        }
    }

    @DeleteMapping(value = "/version/1/deleteFile", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete a file")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "File successfully deleted",
            response = FileUploadResponse.class),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @ResponseBody
    public ResponseEntity<?> deleteFile(@RequestHeader(value = "Authorization") String authorizationToken,
                                        @RequestHeader(value = "requestId", required = false) String requestId,
                                        @RequestParam("fileUrl") @ApiParam("File url to delete") String fileUrl) {
        return emDeleteService.deleteFile(fileUrl, authorizationToken, requestId);
    }

    @GetMapping(value = "/version/1/download/{fileId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ApiOperation(value = "Download a file")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "File successfully downloaded",
            response = FileUploadResponse.class),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @ResponseBody
    public ResponseEntity<byte[]> getFile(
        @RequestHeader(value = "Authorization") String authorizationToken,
        @PathVariable("fileId") @ApiParam("File ID to download") String fileId) {

        return emDownloadService.downloadFile(fileId, authorizationToken);
    }
}