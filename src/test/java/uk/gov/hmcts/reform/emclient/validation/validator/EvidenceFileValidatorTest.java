package uk.gov.hmcts.reform.emclient.validation.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import uk.gov.hmcts.reform.emclient.application.EvidenceManagementClientApplication;
import uk.gov.hmcts.reform.emclient.validation.constraint.EvidenceFile;

import com.google.common.collect.ImmutableList;

@SpringBootTest(classes = {EvidenceManagementClientApplication.class})
@RunWith(SpringRunner.class)
@TestPropertySource(value="classpath:application.properties")
public class EvidenceFileValidatorTest {

    @Resource
    private Validator validator;

    @Test
    public void testJpegFileSuccessValidation() {

        MockMultipartFile jpgFile = new MockMultipartFile("test.jpg", "test.jpg", "image/jpeg", "data".getBytes());
        MockMultipartFile jpegFile = new MockMultipartFile("test.jpeg", "test.jpeg", "image/jpeg", "data".getBytes());
        MockMultipartFile capsJpgFile = new MockMultipartFile("test.JPG", "test.JPG", "image/jpeg", "data".getBytes());
        MockMultipartFile capsJpegFile = new MockMultipartFile("test.JPEG", "test.JPEG", "image/jpeg", "data".getBytes());
        MockMultipartFile mixedJpgFile = new MockMultipartFile("test.JpG", "test.JpG", "image/jpeg", "data".getBytes());
        MockMultipartFile mixedJpegFile = new MockMultipartFile("test.JpEg", "test.JpEg", "image/jpeg", "data".getBytes());

        Set<ConstraintViolation<ValidationBean>> constraintViolations =
                validateFiles(ImmutableList.of(jpgFile, jpegFile, capsJpgFile, capsJpegFile, mixedJpgFile, mixedJpegFile));

        assertTrue(constraintViolations.isEmpty());
    }

    @Test
    public void testBmpFileSuccessValidation() {
        MockMultipartFile bmpFile = new MockMultipartFile("test.bmp", "test.bmp", "image/bmp", "data".getBytes());
        MockMultipartFile capsBmpFile = new MockMultipartFile("test.BMP", "test.BMP", "image/bmp", "data".getBytes());
        MockMultipartFile mixedBmpFile = new MockMultipartFile("test.BmP", "test.BmP", "image/bmp", "data".getBytes());

        Set<ConstraintViolation<ValidationBean>> constraintViolations =
                validateFiles(ImmutableList.of(bmpFile, capsBmpFile, mixedBmpFile));

        assertTrue(constraintViolations.isEmpty());
    }

    @Test
    public void testTiffFileSuccessValidation() {
        MockMultipartFile tiffFile = new MockMultipartFile("test.tiff", "test.tiff", "image/tiff", "data".getBytes());
        MockMultipartFile tifFile = new MockMultipartFile("test.tif", "test.tif", "image/tiff", "data".getBytes());
        MockMultipartFile capsTiffFile = new MockMultipartFile("test.TIFF", "test.TIFF", "image/tiff", "data".getBytes());
        MockMultipartFile capsTifFile = new MockMultipartFile("test.TIF", "test.TIF", "image/tiff", "data".getBytes());
        MockMultipartFile mixedTiffFile = new MockMultipartFile("test.TiFf", "test.TiFf", "image/tiff", "data".getBytes());
        MockMultipartFile mixedTifFile = new MockMultipartFile("test.TiF", "test.TiF", "image/tiff", "data".getBytes());

        Set<ConstraintViolation<ValidationBean>> constraintViolations =
                validateFiles(ImmutableList.of(tiffFile, capsTiffFile, mixedTiffFile, tifFile, capsTifFile, mixedTifFile));

        assertTrue(constraintViolations.isEmpty());
    }

    @Test
    public void testPngFileSuccessValidation() {
        MockMultipartFile pngFile = new MockMultipartFile("test.png", "test.png", "image/png", "data".getBytes());
        MockMultipartFile capsPngFile = new MockMultipartFile("test.PNG", "test.PNG", "image/png", "data".getBytes());
        MockMultipartFile mixedPngFile = new MockMultipartFile("test.PnG", "test.PnG", "image/png", "data".getBytes());

        Set<ConstraintViolation<ValidationBean>> constraintViolations =
                validateFiles(ImmutableList.of(pngFile, capsPngFile, mixedPngFile));

        assertTrue(constraintViolations.isEmpty());
    }
    
    @Test
    public void testPdfFileSuccessValidation() {
        MockMultipartFile pdfFile = new MockMultipartFile("test.pdf", "test.pdf", "application/pdf", "data".getBytes());
        MockMultipartFile capsPdfFile = new MockMultipartFile("test.PDF", "test.PDF", "application/pdf", "data".getBytes());
        MockMultipartFile mixedPdfFile = new MockMultipartFile("test.PdF", "test.PdF", "application/pdf", "data".getBytes());

        Set<ConstraintViolation<ValidationBean>> constraintViolations =
                validateFiles(ImmutableList.of(pdfFile, capsPdfFile, mixedPdfFile));

        assertTrue(constraintViolations.isEmpty());
    }
    @Test
    public void testRestrictedFileFailValidation() {
        MockMultipartFile exeFile = new MockMultipartFile("test.exe", "test.exe", "application/octet-stream", "data".getBytes());

        Set<ConstraintViolation<ValidationBean>> constraintViolations =
                validateFiles(ImmutableList.of(exeFile));

        assertFalse(constraintViolations.isEmpty());
        assertEquals("Attempt to upload invalid file, this service only accepts the following file types ('jpg, jpeg, bmp, tif, tiff, png, pdf)",
                constraintViolations.iterator().next().getMessage());
    }
    
    @Test
    public void testMimeTypeFailValidation() {
        MockMultipartFile jpgFile = new MockMultipartFile("test.jpg", "test.jpg", "application/octet-stream", "data".getBytes());

        Set<ConstraintViolation<ValidationBean>> constraintViolations =
                validateFiles(ImmutableList.of(jpgFile));

        assertFalse(constraintViolations.isEmpty());
        assertEquals("Attempt to upload invalid file, this service only accepts the following file types ('jpg, jpeg, bmp, tif, tiff, png, pdf)",
                constraintViolations.iterator().next().getMessage());
    }

    private Set<ConstraintViolation<ValidationBean>> validateFiles(List<MultipartFile> files) {
        ValidationBean validationBean = new ValidationBean(files);

        return validator.validate(validationBean);
    }

    @Validated
    private final class ValidationBean {
        private final List<@EvidenceFile MultipartFile> files;

        public ValidationBean(List<@EvidenceFile MultipartFile> files) {
            this.files = files;
        }
    }
}
