package io.github.azagniotov.stubby4j.builders.stubs;


import org.eclipse.jetty.http.HttpStatus.Code;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class StubResponseBuilderTest {

    private StubResponseBuilder stubResponseBuilder;

    @Before
    public void setUp() throws Exception {
        stubResponseBuilder = new StubResponseBuilder();
    }

    @Test
    public void shouldReturnDefaultHttpStatusCode_WhenStatusFieldNull() throws Exception {
        assertThat(Code.OK).isEqualTo(stubResponseBuilder.getHttpStatusCode());
    }

    @Test
    public void shouldReturnRespectiveHttpStatusCode_WhenStatusFieldSet() throws Exception {
        assertThat(Code.CREATED).isEqualTo(stubResponseBuilder.withHttpStatusCode(Code.CREATED).getHttpStatusCode());
    }
}
