package uk.gov.hmcts.reform.emclient.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.hateoas.hal.HalLinkDiscoverer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.emclient.exception.UnsupportedDocumentTypeException;
import uk.gov.hmcts.reform.emclient.idam.models.IdamTokens;
import uk.gov.hmcts.reform.emclient.response.FileUploadResponse;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

@Service
@Slf4j
public class EvidenceManagementSecureDocStoreService {

    private final CaseDocumentClient caseDocumentClient;

    @Autowired
    public EvidenceManagementSecureDocStoreService(CaseDocumentClient caseDocumentClient) {
        this.caseDocumentClient = caseDocumentClient;
    }

    public List<FileUploadResponse> upload(List<MultipartFile> files, IdamTokens idamTokens) {

        UploadResponse uploadResponse;
        try {
            uploadResponse = caseDocumentClient
                .uploadDocuments(idamTokens.getIdamOauth2Token(), idamTokens.getServiceAuthorization(), "Divorce", "Divorce", files);
        } catch (HttpClientErrorException httpClientErrorException) {
            log.error("Secure Doc Store service failed to upload documents...", httpClientErrorException);
            if (null != files) {
                logFiles(files);
            }
            throw new UnsupportedDocumentTypeException(httpClientErrorException);
        }
        if (uploadResponse != null) {
            return toUploadResponse(uploadResponse);
        }
        return null;
    }

    private List<FileUploadResponse> toUploadResponse(UploadResponse uploadResponse) {
        Stream<Document> documentStream = stream(uploadResponse.getDocuments().spliterator(), false);
        return documentStream
            .map(this::createUploadResponse)
            .collect(Collectors.toList());
    }

    private FileUploadResponse createUploadResponse(Document document) {
        return FileUploadResponse.builder()
            .status(HttpStatus.OK)
            .fileUrl(new HalLinkDiscoverer().findLinkWithRel("self",
                document.links.self.href).getHref())
            .fileName(document.originalDocumentName)
            .createdBy(document.createdBy)
            .createdOn(document.createdOn.toString())
            .lastModifiedBy(document.lastModifiedBy)
            .modifiedOn(document.modifiedOn.toString())
            .build();
    }


    public byte[] download(String selfHref, IdamTokens idamTokens) {
        try {
            ResponseEntity<Resource> responseEntity = downloadResource(selfHref, idamTokens);

            ByteArrayResource resource = (ByteArrayResource) responseEntity.getBody();
            return (resource != null) ? resource.getByteArray() : new byte[0];
        } catch (HttpClientErrorException httpClientErrorException) {
            log.error("Secure Doc Store service failed to download document...", httpClientErrorException);
            throw new UnsupportedDocumentTypeException(httpClientErrorException);
        }
    }

    private ResponseEntity<Resource> downloadResource(String selfHref, IdamTokens idamTokens) {
        String documentHref = URI.create(selfHref).getPath().replaceFirst("/", "");
        return caseDocumentClient.getDocumentBinary(idamTokens.getIdamOauth2Token(),
            idamTokens.getServiceAuthorization(), documentHref);
    }

    private void logFiles(List<MultipartFile> files) {
        files.forEach(file -> {
            log.info("Name: {}", file.getName());
            log.info("OriginalName {}", file.getOriginalFilename());
        });
    }
}
