package uk.gov.hmcts.reform.emclient.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Operation(summary = "Handles file upload to Evidence Management Document Store")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Files uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping(value = "/version/1/upload", produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public List<FileUploadResponse> upload(
        @RequestHeader(value = "Authorization", required = false) String authorizationToken,
        @RequestHeader(value = "requestId", required = false) String requestId,
        @RequestParam("file") List<@EvidenceFile MultipartFile> files) {

        return emUploadService.upload(files, authorizationToken, requestId);
    }

    @DeleteMapping(value = "/version/1/deleteFile", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Delete a file")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "File successfully deleted",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileUploadResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @ResponseBody
    public ResponseEntity<?> deleteFile(@RequestHeader(value = "Authorization") String authorizationToken,
                                        @RequestHeader(value = "requestId", required = false) String requestId,
                                        @Parameter(description = "File url to delete")
                                        @RequestParam("fileUrl") String fileUrl) {
        return emDeleteService.deleteFile(fileUrl, authorizationToken, requestId);
    }

    @GetMapping(value = "/version/1/download/{fileId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(description = "Download a file")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "File successfully downloaded",
            content = @Content(mediaType = "application/octet-stream")),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @ResponseBody
    public ResponseEntity<byte[]> getFile(
        @RequestHeader(value = "Authorization") String authorizationToken,
        @PathVariable("fileId") @Parameter(description = "File ID to download") String fileId) {

        return emDownloadService.downloadFile(fileId, authorizationToken);
    }
}
