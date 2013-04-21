package by.stub.yaml.stubs;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author: Alexander Zagniotov
 * Created: 4/20/13 5:29 PM
 */
public class StubHttpLifecycleTest {

   @Test
   public void shouldReturnStubResponse_WhenNoSequenceResponses() throws Exception {

      final StubResponse stubResponse = new StubResponse();
      stubResponse.setStatus("201");
      stubResponse.setBody("SELF");

      final StubHttpLifecycle stubHttpLifecycle = new StubHttpLifecycle();
      stubHttpLifecycle.setResponse(stubResponse);

      assertThat(stubHttpLifecycle.getActualStubbedResponse()).isEqualTo(stubResponse);
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

      final StubHttpLifecycle stubHttpLifecycle = new StubHttpLifecycle();
      stubHttpLifecycle.setResponse(sequence);

      final StubResponse actualStubbedResponse = stubHttpLifecycle.getActualStubbedResponse();
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

      final StubHttpLifecycle stubHttpLifecycle = new StubHttpLifecycle();
      stubHttpLifecycle.setResponse(sequence);

      final StubResponse irrelevantStubbedResponse = stubHttpLifecycle.getActualStubbedResponse();
      final StubResponse actualStubbedResponse = stubHttpLifecycle.getActualStubbedResponse();

      assertThat(actualStubbedResponse).isNotEqualTo(stubResponse);
      assertThat(actualStubbedResponse.getStatus()).isEqualTo(expectedStatus);
      assertThat(actualStubbedResponse.getBody()).isEqualTo(expectedBody);
   }
}
