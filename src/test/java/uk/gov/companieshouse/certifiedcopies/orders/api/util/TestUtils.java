package uk.gov.companieshouse.certifiedcopies.orders.api.util;

import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.springframework.core.env.Environment;

public class TestUtils {

    /**
     * Configures the CH Java SDKs used in WireMock based integration tests to interact with with WireMock
     * stubbed/mocked API endpoints.
     * @param environment the Spring {@link Environment} the tests are run in
     * @param variables {@link EnvironmentVariables} class rule permitting environment variable manipulation
     * @return the WireMock port value used for the tests
     */
    public static String givenSdkIsConfigured(final Environment environment, final EnvironmentVariables variables) {
        final String wireMockPort = environment.getProperty("wiremock.server.port");
        variables.set("CHS_API_KEY", "MGQ1MGNlYmFkYzkxZTM2MzlkNGVmMzg4ZjgxMmEz");
        variables.set("API_URL", "http://localhost:" + wireMockPort);
        variables.set("PAYMENTS_API_URL", "http://localhost:" + wireMockPort);
        return wireMockPort;
    }
}
