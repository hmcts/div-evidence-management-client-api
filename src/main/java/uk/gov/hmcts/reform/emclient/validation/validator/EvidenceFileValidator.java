package uk.gov.hmcts.reform.emclient.validation.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import uk.gov.hmcts.reform.emclient.validation.constraint.EvidenceFile;

@Component
public class EvidenceFileValidator implements ConstraintValidator<EvidenceFile, MultipartFile> {

    @Value("${endpoints.fileupload.extensions}")
    private String allowedFileExtensions;

    @Value("${endpoints.fileupload.mimetypes}")
    private String allowedMimeTypes;

    @Override
    public void initialize(EvidenceFile evidenceFile) {}

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext constraintValidatorContext) {
        if (!validFile(file.getOriginalFilename()) || !validMimeType(file.getContentType())) {
            return false;
        }
        return true;
    }

    private boolean validMimeType(final String mimeType) {
        if (!StringUtils.containsIgnoreCase(allowedMimeTypes, mimeType)) {
            return false;
        }
        return true;
    }

    private boolean validFile(final String filename) {
        if (!StringUtils.containsIgnoreCase(allowedFileExtensions, FilenameUtils.getExtension(filename))) {
            return false;
        }
        return true;
    }
}
