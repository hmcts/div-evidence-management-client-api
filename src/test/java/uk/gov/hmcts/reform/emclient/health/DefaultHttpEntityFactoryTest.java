package uk.gov.hmcts.reform.emclient.health;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;

@RunWith(MockitoJUnitRunner.class)
public class DefaultHttpEntityFactoryTest {

    @InjectMocks
    private DefaultHttpEntityFactory httpEntityFactory;

    @Test
    public void shouldReturnEntityWithJsonAcceptHeadersWhenCallingCreateEntityForHealthCheck() throws Exception {
        HttpEntity<Object> httpEntity = httpEntityFactory.createRequestEntityForHealthCheck();

        assertThat(httpEntity.getHeaders().size()).isEqualTo(1);
        assertThat(httpEntity.getHeaders().getAccept().get(0)).isEqualTo(MediaType.APPLICATION_JSON_UTF8);
        assertThat(httpEntity.getBody()).isNull();
    }
}