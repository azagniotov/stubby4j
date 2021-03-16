package io.github.azagniotov.stubby4j.stubs;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;


@RunWith(MockitoJUnitRunner.class)
public class StubProxyConfigBuilderTest {

    private StubProxyConfig.Builder builder;

    @Before
    public void setUp() throws Exception {
        builder = new StubProxyConfig.Builder();
    }

    @After
    public void cleanup() throws Exception {
        RegexParser.REGEX_PATTERN_CACHE.clear();
    }

    @Test
    public void stubbedProxyConfigEqualsAssertingConfig_WhenProxyNameNull() throws Exception {

        final StubProxyConfig expectedStubProxyConfig = builder.withProxyName(null).build();
        final StubProxyConfig assertingStubProxyConfig = builder.withProxyName(null).build();

        assertThat(assertingStubProxyConfig).isEqualTo(expectedStubProxyConfig);
    }

    @Test
    public void stubbedProxyConfigEqualsAssertingConfig() throws Exception {

        final StubProxyConfig expectedStubProxyConfig = builder
                .withProxyName("unique")
                .withProxyStrategy("as-is")
                .withProxyProperty("key", "value")
                .withProxyPropertyEndpoint("http://google.com")
                .build();

        final StubProxyConfig assertingStubProxyConfig = builder
                .withProxyName("unique")
                .withProxyStrategy("as-is")
                .withProxyProperty("key", "value")
                .withProxyPropertyEndpoint("http://google.com")
                .build();

        assertThat(assertingStubProxyConfig).isEqualTo(expectedStubProxyConfig);
    }

    @Test
    public void stubbedProxyConfigNotEqualsAssertingConfig() throws Exception {

        final StubProxyConfig expectedStubProxyConfig = builder
                .withProxyName("unique")
                .withProxyStrategy("as-is")
                .withProxyPropertyEndpoint("http://google.com")
                .build();

        final StubProxyConfig assertingStubProxyConfig = builder
                .withProxyName("unique")
                .withProxyStrategy("custom")
                .withProxyPropertyEndpoint("http://google.com")
                .build();

        assertThat(assertingStubProxyConfig).isNotEqualTo(expectedStubProxyConfig);
    }

    @Test
    public void stubbedProxyConfigHashCode() throws Exception {

        final StubProxyConfig stubProxyConfigOne = builder
                .withProxyName("unique")
                .withProxyStrategy("as-is")
                .withProxyPropertyEndpoint("http://google.com")
                .build();

        final StubProxyConfig stubProxyConfigTwo = builder
                .withProxyName("unique")
                .withProxyStrategy("as-is")
                .withProxyPropertyEndpoint("http://google.com")
                .build();

        Map<StubProxyConfig, StubProxyConfig> mapping = new HashMap<>();
        mapping.put(stubProxyConfigOne, stubProxyConfigOne);
        mapping.put(stubProxyConfigTwo, stubProxyConfigTwo);

        assertThat(mapping.size()).isEqualTo(1);
    }

    @Test
    public void shouldThrowWhenUnexpectedProxyStrategyPassedIn() throws Exception {

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            builder.withProxyName("unique")
                    .withProxyStrategy("this-is-a-wrong-value")
                    .withProxyPropertyEndpoint("http://google.com")
                    .build();
        });

        String expectedMessage = "this-is-a-wrong-value";
        String actualMessage = exception.getMessage();

        assertThat(actualMessage).isEqualTo(expectedMessage);
    }
}
