package uk.gov.hmcts.reform.emclient.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.emclient.exception.UnsupportedDocumentTypeException;
import uk.gov.hmcts.reform.emclient.idam.models.IdamTokens;
import uk.gov.hmcts.reform.emclient.response.FileUploadResponse;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EvidenceManagementSecureDocStoreServiceTest {

    private EvidenceManagementSecureDocStoreService evidenceManagementSecureDocStoreService;

    @Mock
    private CaseDocumentClient caseDocumentClient;
    @Mock
    private MultipartFile mockFile;

    @Before
    public void setUpTest() {
        evidenceManagementSecureDocStoreService = new EvidenceManagementSecureDocStoreService(caseDocumentClient);
    }

    @Test
    public void shouldUploadDocSecurely() {

        Document document = buildDocument();
        IdamTokens idamTokens = buildIdamTokens();
        List<MultipartFile> files = Arrays.asList(mockFile);
        UploadResponse uploadResponse = new UploadResponse(Arrays.asList(document));

        when(caseDocumentClient.uploadDocuments(idamTokens.getIdamOauth2Token(), idamTokens.getServiceAuthorization(), "Divorce", "Divorce", files))
            .thenReturn(uploadResponse);

        List<FileUploadResponse> result = evidenceManagementSecureDocStoreService.upload(files,
            idamTokens);
        FileUploadResponse response = result.get(0);

        assertFileUploadResponse(response);
    }

    @Test
    public void shouldReturnNullIWhenResponseIsNull() {

        IdamTokens idamTokens = buildIdamTokens();
        List<MultipartFile> files = Arrays.asList(mockFile);

        when(caseDocumentClient.uploadDocuments(idamTokens.getIdamOauth2Token(), idamTokens.getServiceAuthorization(), "Divorce", "Divorce", files))
            .thenReturn(null);

        List<FileUploadResponse> result = evidenceManagementSecureDocStoreService.upload(files,
            idamTokens);

        assertNull(result);
    }

    @Test(expected = HttpClientErrorException.class)
    public void shouldThrowHttpClientErrorExceptionOnUploadError() {

        IdamTokens idamTokens = buildIdamTokens();
        List<MultipartFile> files = Arrays.asList(mockFile);

        when(caseDocumentClient.uploadDocuments(idamTokens.getIdamOauth2Token(), idamTokens.getServiceAuthorization(), "Divorce", "Divorce", files))
            .thenThrow(HttpClientErrorException.class);

        evidenceManagementSecureDocStoreService.upload(files,
            idamTokens);
    }

    @Test
    public void shouldDownloadDoc() {

        IdamTokens idamTokens = buildIdamTokens();

        ResponseEntity<Resource> downloadResponse = new ResponseEntity<>(HttpStatus.ACCEPTED);
        String docPartUrl = "documents/f5734b18-c075-4417-81ce-c0c2e0155dbe";
        String binaryHref = "http://dm-store-aat.service.core-compute-aat.internal/" + docPartUrl;
        when(caseDocumentClient.getDocumentBinary(idamTokens.getIdamOauth2Token(), idamTokens.getServiceAuthorization(),
            docPartUrl))
            .thenReturn(downloadResponse);

        evidenceManagementSecureDocStoreService
            .download(binaryHref,
                idamTokens);

        verify(caseDocumentClient).getDocumentBinary(idamTokens.getIdamOauth2Token(), idamTokens.getServiceAuthorization(),
            docPartUrl);
    }

    @Test(expected = HttpClientErrorException.class)
    public void shouldThrowUnsupportedDocumentTypeExceptionOnDownloadError() {
        IdamTokens idamTokens = buildIdamTokens();

        String docPartUrl = "documents/f5734b18-c075-4417-81ce-c0c2e0155dbe";
        String binaryHref = "http://dm-store-aat.service.core-compute-aat.internal/" + docPartUrl;
        when(caseDocumentClient.getDocumentBinary(idamTokens.getIdamOauth2Token(), idamTokens.getServiceAuthorization(),
            docPartUrl))
            .thenThrow(HttpClientErrorException.class);

        evidenceManagementSecureDocStoreService
            .download(binaryHref,
                idamTokens);
    }

    @Test
    public void shouldDeleteDoc() {
        IdamTokens idamTokens = buildIdamTokens();

        String docPartUrl = "documents/f5734b18-c075-4417-81ce-c0c2e0155dbe";
        String binaryHref = "http://dm-store-aat.service.core-compute-aat.internal/" + docPartUrl;

        evidenceManagementSecureDocStoreService
            .delete(binaryHref,
                idamTokens);

        verify(caseDocumentClient).deleteDocument(idamTokens.getIdamOauth2Token(), idamTokens.getServiceAuthorization(),
            evidenceManagementSecureDocStoreService.getDocumentIdFromSelfHref(binaryHref),Boolean.TRUE);
    }

    private void assertFileUploadResponse(FileUploadResponse response) {
        assertThat(response.getCreatedBy(), is("someUser"));
        assertThat(response.getCreatedOn(), is("2021-11-02T12:25:30.000001234"));
        assertThat(response.getModifiedOn(), is("2021-11-02T12:25:30.000001234"));
        assertThat(response.getFileName(), is("originalDocumentName"));
        assertThat(response.getMimeType(), is("application/pdf"));
        assertThat(response.getFileUrl(), is("selfURL"));
    }

    private IdamTokens buildIdamTokens() {
        IdamTokens idamTokens = IdamTokens.builder()
            .idamOauth2Token("idamToken")
            .serviceAuthorization("serviceAuth")
            .build();
        return idamTokens;
    }

    private Document buildDocument() {
        Date dateToUse = java.sql.Timestamp.valueOf(LocalDateTime.of(2021, 11, 2, 12, 25, 30, 1234));
        Document document = Document.builder()
            .createdOn(dateToUse)
            .createdBy("someUser")
            .lastModifiedBy("someUser")
            .modifiedOn(dateToUse)
            .originalDocumentName("originalDocumentName")
            .mimeType("application/pdf")
            .links(getLinks())
            .build();
        return document;
    }

    private Document.Links getLinks() {
        Document.Links links = new Document.Links();
        links.binary = new Document.Link();
        links.self = new Document.Link();
        links.binary.href = "binaryUrl";
        links.self.href = "selfURL";
        return links;
    }

}