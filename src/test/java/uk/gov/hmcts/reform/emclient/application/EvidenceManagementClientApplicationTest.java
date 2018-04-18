package uk.gov.hmcts.reform.emclient.application;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.emclient.category.SmokeTest;

@RunWith(SpringRunner.class)
@Category(SmokeTest.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class EvidenceManagementClientApplicationTest {
    @Test
    public void applicationTest() {
        EvidenceManagementClientApplication.main(new String[] {});
    }
}
