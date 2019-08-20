package uk.gov.hmcts.reform.divorce.emclient;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;

@Slf4j
@ContextConfiguration(classes = {ServiceContextConfiguration.class})
public class IntegrationTest {

    @Value("${http.proxy:#{null}}")
    protected String httpProxy;

    @PostConstruct
    public void init() {
        if (!Strings.isNullOrEmpty(httpProxy)) {
            try {
                URL proxy = new URL(httpProxy);
                if (!InetAddress.getByName(proxy.getHost()).isReachable(2000)) { // check proxy connectivity
                    throw new IOException("Could not reach proxy in timeout time");
                }
                System.setProperty("http.proxyHost", proxy.getHost());
                System.setProperty("http.proxyPort", Integer.toString(proxy.getPort()));
                System.setProperty("https.proxyHost", proxy.getHost());
                System.setProperty("https.proxyPort", Integer.toString(proxy.getPort()));
            } catch (IOException e) {
                log.error("Error setting up proxy - are you connected to the VPN?", e);
                throw new RuntimeException("Error setting up proxy", e);
            }
        }
    }
}
