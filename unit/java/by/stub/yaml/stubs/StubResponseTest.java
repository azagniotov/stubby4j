package by.stub.yaml.stubs;

import by.stub.cli.CommandLineInterpreter;
import by.stub.utils.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Alexander Zagniotov
 * @since 10/24/12, 10:49 AM
 */
public class StubResponseTest {
   @BeforeClass
   public static void Setup() throws Exception {
      CommandLineInterpreter.parseCommandLine(new String[]{});
   }

   @Test
   public void shouldReturnBody_WhenFileCannotBeFound() throws Exception {

      final StubResponse stubResponse = new StubResponse();
      stubResponse.setBody("this is some body");

      assertThat("this is some body").isEqualTo(StringUtils.newStringUtf8(stubResponse.getResponseBody()));
   }

   @Test
   public void shouldReturnBody_WhenFileIsNull() throws Exception {

      final StubResponse stubResponse = new StubResponse();
      stubResponse.setFile(null);
      stubResponse.setBody("this is some body");

      assertThat("this is some body").isEqualTo(StringUtils.newStringUtf8(stubResponse.getResponseBody()));
   }

   @Test
   public void shouldReturnBody_WhenFileIsEmpty() throws Exception {

      final StubResponse stubResponse = new StubResponse();
      stubResponse.setBody("this is some body");

      assertThat("this is some body").isEqualTo(StringUtils.newStringUtf8(stubResponse.getResponseBody()));
   }

   @Test
   public void shouldReturnEmptyBody_WhenFileAndBodyAreNull() throws Exception {

      final StubResponse stubResponse = new StubResponse();
      stubResponse.setFile(null);
      stubResponse.setBody(null);

      assertThat("").isEqualTo(StringUtils.newStringUtf8(stubResponse.getResponseBody()));
   }

   @Test
   public void shouldReturnEmptyBody_WhenBodyIsEmpty() throws Exception {

      final StubResponse stubResponse = new StubResponse();
      stubResponse.setFile(null);
      stubResponse.setBody("");

      assertThat("").isEqualTo(StringUtils.newStringUtf8(stubResponse.getResponseBody()));
   }

   @Test
   public void shouldReturnItself_WhenNoSequenceResponses() throws Exception {

      final StubResponse stubResponse = new StubResponse();
      stubResponse.setStatus("201");
      stubResponse.setBody("SELF");

      assertThat(stubResponse.getActualStubbedResponse()).isEqualTo(stubResponse);
   }

   @Test
   public void shouldReturnSequenceResponse_WhenOneSequenceResponsePresent() throws Exception {

      final StubResponse stubResponse = new StubResponse();
      stubResponse.setStatus("201");
      stubResponse.setBody("SELF");

      final String expectedStatus = "200";
      final String expectedBody = "This is a sequence response #1";

      final List<StubResponse> sequence = new LinkedList<StubResponse>() {{
         final StubResponse stubResponse = new StubResponse();
         stubResponse.setStatus(expectedStatus);
         stubResponse.setBody(expectedBody);
         add(stubResponse);
      }};

      stubResponse.setSequence(sequence);

      final StubResponse actualStubbedResponse = stubResponse.getActualStubbedResponse();
      assertThat(actualStubbedResponse).isNotEqualTo(stubResponse);
      assertThat(actualStubbedResponse.getStatus()).isEqualTo(expectedStatus);
      assertThat(actualStubbedResponse.getBody()).isEqualTo(expectedBody);
   }

   @Test
   public void shouldReturnSecondSequenceResponseAfterSecondCall_WhenTwoSequenceResponsePresent() throws Exception {

      final StubResponse stubResponse = new StubResponse();
      stubResponse.setStatus("201");
      stubResponse.setBody("SELF");

      final String expectedStatus = "500";
      final String expectedBody = "This is a sequence response #2";

      final List<StubResponse> sequence = new LinkedList<StubResponse>() {{
         final StubResponse sequenceResponseOne = new StubResponse();
         sequenceResponseOne.setStatus("200");
         sequenceResponseOne.setBody("This is a sequence response #1");
         add(sequenceResponseOne);

         final StubResponse sequenceResponseTwo = new StubResponse();
         sequenceResponseTwo.setStatus(expectedStatus);
         sequenceResponseTwo.setBody(expectedBody);
         add(sequenceResponseTwo);
      }};

      stubResponse.setSequence(sequence);

      final StubResponse irrelevantStubbedResponse = stubResponse.getActualStubbedResponse();
      final StubResponse actualStubbedResponse = stubResponse.getActualStubbedResponse();

      assertThat(actualStubbedResponse).isNotEqualTo(stubResponse);
      assertThat(actualStubbedResponse.getStatus()).isEqualTo(expectedStatus);
      assertThat(actualStubbedResponse.getBody()).isEqualTo(expectedBody);
   }
}
