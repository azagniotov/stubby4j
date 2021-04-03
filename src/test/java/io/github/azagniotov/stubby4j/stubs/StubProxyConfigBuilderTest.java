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
    public void stubbedProxyConfigDefaultStrategyNotAdditive() throws Exception {

        final StubProxyConfig stubProxyConfig = builder.build();
        assertThat(stubProxyConfig.isAdditiveStrategy()).isFalse();
        assertThat(stubProxyConfig.getStrategy()).isEqualTo(StubProxyStrategy.AS_IS);
    }

    @Test
    public void stubbedProxyConfigStrategyAdditive() throws Exception {

        final StubProxyConfig stubProxyConfig = builder.withStrategy(StubProxyStrategy.ADDITIVE.toString()).build();
        assertThat(stubProxyConfig.isAdditiveStrategy()).isTrue();
        assertThat(stubProxyConfig.getStrategy()).isEqualTo(StubProxyStrategy.ADDITIVE);
    }

    @Test
    public void stubbedProxyConfigHasNoHeaders() throws Exception {

        final StubProxyConfig stubProxyConfig = builder.build();
        assertThat(stubProxyConfig.hasHeaders()).isFalse();
        assertThat(stubProxyConfig.getHeaders().isEmpty()).isTrue();
    }

    @Test
    public void stubbedProxyConfigHasDefaultUuid() throws Exception {

        final StubProxyConfig stubProxyConfig = builder.build();
        assertThat(stubProxyConfig.getUUID()).isEqualTo("default");
    }

    @Test
    public void stubbedProxyConfigNameResetsToDefaultUuid() throws Exception {

        final StubProxyConfig stubProxyConfig = builder.withUuid("newName").build();
        assertThat(stubProxyConfig.getUUID()).isEqualTo("newName");

        final StubProxyConfig freshStubProxyConfig = builder.build();
        assertThat(freshStubProxyConfig.getUUID()).isEqualTo("default");
    }

    @Test
    public void stubbedProxyConfigEqualsAssertingConfig_WhenProxyNameNull() throws Exception {

        final StubProxyConfig expectedStubProxyConfig = builder.withUuid(null).build();
        final StubProxyConfig assertingStubProxyConfig = builder.withUuid(null).build();

        assertThat(assertingStubProxyConfig).isEqualTo(expectedStubProxyConfig);
    }

    @Test
    public void stubbedProxyConfigEqualsAssertingConfig_WhenProxyConfigDescriptionDifferent() throws Exception {

        // proxy config description does NOT participate in equality
        final StubProxyConfig expectedStubProxyConfig = builder.withUuid("one")
                .withDescription("description")
                .build();
        final StubProxyConfig assertingStubProxyConfig = builder
                .withUuid("one")
                .build();

        assertThat(assertingStubProxyConfig).isEqualTo(expectedStubProxyConfig);
    }

    @Test
    public void stubbedProxyConfigNotEqualsAssertingConfig_WhenProxyNamesDifferent() throws Exception {

        final StubProxyConfig expectedStubProxyConfig = builder.withUuid("one").build();
        final StubProxyConfig assertingStubProxyConfig = builder.withUuid("two").build();

        assertThat(assertingStubProxyConfig).isNotEqualTo(expectedStubProxyConfig);
    }

    @Test
    public void stubbedProxyConfigNotEqualsAssertingConfig_WhenProxyPropertiesDifferent() throws Exception {

        final StubProxyConfig expectedStubProxyConfig = builder.withProperty("key", "anotherValue").build();
        final StubProxyConfig assertingStubProxyConfig = builder.withProperty("key", "value").build();

        assertThat(assertingStubProxyConfig).isNotEqualTo(expectedStubProxyConfig);
    }

    @Test
    public void stubbedProxyConfigReturnsExpectedEndpointAndDescription() throws Exception {

        final StubProxyConfig stubProxyConfig = builder
                .withDescription("This is a proxy config for Google")
                .withUuid("unique")
                .withStrategy("as-is")
                .withProperty("key", "value")
                .withPropertyEndpoint("http://google.com")
                .build();

        assertThat(stubProxyConfig.getPropertyEndpoint()).isEqualTo("http://google.com");
        assertThat(stubProxyConfig.getDescription()).isEqualTo("This is a proxy config for Google");
    }

    @Test
    public void stubbedProxyConfigEqualsAssertingConfig() throws Exception {

        final StubProxyConfig expectedStubProxyConfig = builder
                .withUuid("unique")
                .withStrategy("as-is")
                .withHeader("headerKey", "headerValue")
                .withProperty("key", "value")
                .withPropertyEndpoint("http://google.com")
                .build();

        final StubProxyConfig assertingStubProxyConfig = builder
                .withUuid("unique")
                .withStrategy("as-is")
                .withHeader("headerKey", "headerValue")
                .withProperty("key", "value")
                .withPropertyEndpoint("http://google.com")
                .build();

        assertThat(expectedStubProxyConfig.hasHeaders()).isTrue();
        assertThat(expectedStubProxyConfig.getHeaders().isEmpty()).isFalse();

        assertThat(assertingStubProxyConfig).isEqualTo(expectedStubProxyConfig);
    }

    @Test
    public void stubbedProxyConfigNotEqualsAssertingConfigWithDifferentHeader() throws Exception {

        final StubProxyConfig expectedStubProxyConfig = builder
                .withUuid("unique")
                .withStrategy("as-is")
                .withHeader("headerKey", "headerValue")
                .withProperty("key", "value")
                .withPropertyEndpoint("http://google.com")
                .build();

        final StubProxyConfig assertingStubProxyConfig = builder
                .withUuid("unique")
                .withStrategy("as-is")
                .withHeader("headerKey", "headerDifferentValue")
                .withProperty("key", "value")
                .withPropertyEndpoint("http://google.com")
                .build();

        assertThat(expectedStubProxyConfig.hasHeaders()).isTrue();
        assertThat(expectedStubProxyConfig.getHeaders().isEmpty()).isFalse();

        assertThat(assertingStubProxyConfig.hasHeaders()).isTrue();
        assertThat(assertingStubProxyConfig.getHeaders().isEmpty()).isFalse();

        assertThat(assertingStubProxyConfig).isNotEqualTo(expectedStubProxyConfig);
    }

    @Test
    public void stubbedProxyConfigNotEqualsAssertingConfig() throws Exception {

        final StubProxyConfig expectedStubProxyConfig = builder
                .withUuid("unique")
                .withStrategy("as-is")
                .withPropertyEndpoint("http://google.com")
                .build();

        final StubProxyConfig assertingStubProxyConfig = builder
                .withUuid("unique")
                .withStrategy("additive")
                .withPropertyEndpoint("http://google.com")
                .build();

        assertThat(assertingStubProxyConfig).isNotEqualTo(expectedStubProxyConfig);
    }

    @Test
    public void stubbedProxyConfigHashCode() throws Exception {

        final StubProxyConfig stubProxyConfigOne = builder
                .withUuid("unique")
                .withStrategy("as-is")
                .withPropertyEndpoint("http://google.com")
                .build();

        final StubProxyConfig stubProxyConfigTwo = builder
                .withUuid("unique")
                .withStrategy("as-is")
                .withPropertyEndpoint("http://google.com")
                .build();

        Map<StubProxyConfig, StubProxyConfig> mapping = new HashMap<>();
        mapping.put(stubProxyConfigOne, stubProxyConfigOne);
        mapping.put(stubProxyConfigTwo, stubProxyConfigTwo);

        assertThat(mapping.size()).isEqualTo(1);
    }

    @Test
    public void stubbedProxyConfigAsYaml() throws Exception {

        final StubProxyConfig stubProxyConfig = builder
                .withUuid("unique")
                .withStrategy("as-is")
                .withProperty("key", "value")
                .withPropertyEndpoint("http://google.com")
                .withProxyConfigAsYAML(
                        "- proxy-config:\n" +
                                "    proxy-strategy: as-is\n" +
                                "    proxy-properties:\n" +
                                "      endpoint: https://jsonplaceholder.typicode.com")
                .build();

        assertThat(stubProxyConfig.getProxyConfigAsYAML()).isEqualTo(
                "- proxy-config:\n" +
                        "    proxy-strategy: as-is\n" +
                        "    proxy-properties:\n" +
                        "      endpoint: https://jsonplaceholder.typicode.com");
    }

    @Test
    public void shouldThrowWhenUnexpectedProxyStrategyPassedIn() throws Exception {

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            builder.withUuid("unique")
                    .withStrategy("this-is-a-wrong-value")
                    .withPropertyEndpoint("http://google.com")
                    .build();
        });

        String expectedMessage = "this-is-a-wrong-value";
        String actualMessage = exception.getMessage();

        assertThat(actualMessage).isEqualTo(expectedMessage);
    }
}
