package uk.gov.hmcts.reform.emclient.functional;

import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.netflix.ribbon.StaticServerList;
import org.springframework.context.annotation.Bean;

public class BaseFunctionalTest {

    @TestConfiguration
    public static class LocalRibbonClientConfiguration {
        @Bean
        public ServerList<Server> ribbonServerList(@Value("${auth.provider.service.client.port}") int serverPort) {
            return new StaticServerList<>(new Server("localhost", serverPort));
        }

    }

    protected String getAppBaseUrl(String serverPort){
        if(StringUtils.isNotEmpty(serverPort)){
            String url = new LocalRibbonClientConfiguration().ribbonServerList(Integer.valueOf(serverPort)).getInitialListOfServers().get(0).getId();
            return "http://" + url;
        }else {
            throw new RuntimeException("Server Port config not found in application properties");
        }

    }
}
