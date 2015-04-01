package by.stub.builder.yaml;

import by.stub.utils.FileUtils;
import by.stub.yaml.stubs.StubAuthorizationTypes;
import org.junit.Test;

import static by.stub.utils.FileUtils.*;
import static by.stub.yaml.stubs.StubAuthorizationTypes.*;
import static org.fest.assertions.api.Assertions.assertThat;


/**
 * @author Alexander Zagniotov
 * @since 4/13/13, 12:50 AM
 */
public class YamlBuilderTest {

   @Test
   public void shouldBuildStubbedResponseWithSequenceResponses() throws Exception {
      final String expectedYaml =
         "-  request:" + BR +
            "      method: [PUT]" + BR +
            "      url: /invoice" + BR +
            "" + BR +
            "   response:" + BR +
            "      -  status: 200" + BR +
            "         headers: " + BR +
            "            content-type: application/json" + BR +
            "         body: OK" + BR +
            "" + BR +
            "      -  status: 200" + BR +
            "         headers: " + BR +
            "            content-type: application/json" + BR +
            "         body: Still going strong!" + BR +
            "" + BR +
            "      -  status: 500" + BR +
            "         headers: " + BR +
            "            content-type: application/json" + BR +
            "         body: OMFG!!!" + BR +
            "         file: ../../response.json";

      final YamlBuilder yamlBuilder = new YamlBuilder();
      final String actualYaml = yamlBuilder
         .newStubbedRequest()
         .withMethodPut()
         .withUrl("/invoice")
         .newStubbedResponse()
         .withSequenceResponseStatus("200")
         .withSequenceResponseHeaders("content-type", "application/json")
         .withSequenceResponseLiteralBody("OK")
         .withLineBreak()
         .withSequenceResponseStatus("200")
         .withSequenceResponseHeaders("content-type", "application/json")
         .withSequenceResponseLiteralBody("Still going strong!")
         .withLineBreak()
         .withSequenceResponseStatus("500")
         .withSequenceResponseHeaders("content-type", "application/json")
         .withSequenceResponseLiteralBody("OMFG!!!")
         .withSequenceResponseFile("../../response.json")
         .withLineBreak()
         .build();

      assertThat(actualYaml).isEqualTo(expectedYaml);

   }

   @Test
   public void shouldBuildStubbedResponseWithSequenceResponsesFoldedBodyAndFile() throws Exception {
      final String expectedYaml =
         "-  request:" + BR +
            "      method: [PUT]" + BR +
            "      url: /invoice" + BR +
            "" + BR +
            "   response:" + BR +
            "      -  status: 200" + BR +
            "         headers: " + BR +
            "            content-type: application/json" + BR +
            "         body: OK" + BR +
            "" + BR +
            "      -  status: 200" + BR +
            "         headers: " + BR +
            "            content-type: application/json" + BR +
            "         body: >" + BR +
            "            {\"status\", \"200\"}" + BR +
            "" + BR +
            "      -  status: 500" + BR +
            "         headers: " + BR +
            "            content-type: application/json" + BR +
            "         file: ../path/to/error.file";

      final YamlBuilder yamlBuilder = new YamlBuilder();
      final String actualYaml = yamlBuilder
         .newStubbedRequest()
         .withMethodPut()
         .withUrl("/invoice")
         .newStubbedResponse()
         .withSequenceResponseStatus("200")
         .withSequenceResponseHeaders("content-type", "application/json")
         .withSequenceResponseLiteralBody("OK")
         .withLineBreak()
         .withSequenceResponseStatus("200")
         .withSequenceResponseHeaders("content-type", "application/json")
         .withSequenceResponseFoldedBody("{\"status\", \"200\"}")
         .withLineBreak()
         .withSequenceResponseStatus("500")
         .withSequenceResponseHeaders("content-type", "application/json")
         .withSequenceResponseFile("../path/to/error.file")
         .withLineBreak()
         .build();

      assertThat(actualYaml).isEqualTo(expectedYaml);

   }

   @Test
   public void shouldBuildStubbedRequestWithMultipleMethods() throws Exception {
      final String expectedYaml =
         "-  request:" + BR +
            "      method: [HEAD, GET, PUT]" + BR +
            "      url: /invoice" + BR +
            "" + BR +
            "   response:" + BR +
            "      status: 200" + BR +
            "      body: OK";

      final YamlBuilder yamlBuilder = new YamlBuilder();
      final String actualYaml = yamlBuilder.
         newStubbedRequest().
         withMethodHead().
         withMethodGet().
         withMethodPut().
         withUrl("/invoice").
         newStubbedResponse().
         withStatus("200").
         withLiteralBody("OK").build();

      assertThat(actualYaml).isEqualTo(expectedYaml);

   }

   @Test
   public void shouldBuildStubbedRequestWithStubbedResponse() throws Exception {
      final String expectedYaml =
         "-  request:" + BR +
            "      query:" + BR +
            "         status: active" + BR +
            "         type: full" + BR +
            "      method: [GET]" + BR +
            "      url: /invoice" + BR +
            "" + BR +
            "   response:" + BR +
            "      headers:" + BR +
            "         content-type: application/json" + BR +
            "         pragma: no-cache" + BR +
            "      status: 200" + BR +
            "      file: ../json/systemtest-body-response-as-file.json";

      final YamlBuilder yamlBuilder = new YamlBuilder();
      final String actualYaml = yamlBuilder.
         newStubbedRequest().
         withQuery("status", "active").
         withQuery("type", "full").
         withMethodGet().
         withUrl("/invoice").
         newStubbedResponse().
         withHeaders("content-type", "application/json").
         withHeaders("pragma", "no-cache").
         withStatus("200").
         withFile("../json/systemtest-body-response-as-file.json").build();

      assertThat(actualYaml).isEqualTo(expectedYaml);

   }


   @Test
   public void shouldBuildStubbedRequestWithMultilineStubbedResponse() throws Exception {

      final String expectedYaml =
         "-  request:" + BR +
            "      method: [PUT]" + BR +
            "      url: /invoice/123" + BR +
            "      headers:" + BR +
            "         content-type: application/json" + BR +
            "      post: >" + BR +
            "         {\"name\": \"milk\", \"description\": \"full\", \"department\": \"savoury\"}" + BR +
            "" + BR +
            "   response:" + BR +
            "      headers:" + BR +
            "         content-type: application/json" + BR +
            "         pragma: no-cache" + BR +
            "      status: 200" + BR +
            "      body: >" + BR +
            "         {\"id\": \"123\", \"status\": \"updated\"}";

      final YamlBuilder yamlBuilder = new YamlBuilder();
      final String actualYaml = yamlBuilder.
         newStubbedRequest().
         withMethodPut().
         withUrl("/invoice/123").
         withHeaders("content-type", "application/json").
         withFoldedPost("{\"name\": \"milk\", \"description\": \"full\", \"department\": \"savoury\"}").
         newStubbedResponse().
         withHeaders("content-type", "application/json").
         withHeaders("pragma", "no-cache").
         withStatus("200").
         withFoldedBody("{\"id\": \"123\", \"status\": \"updated\"}").build();

      assertThat(actualYaml).isEqualTo(expectedYaml);

   }


   @Test
   public void shouldBuildStubbedRequestWithStubbedResponseWhenBothHaveManyHeaders() throws Exception {
      final String expectedYaml =
         "-  request:" + BR +
            "      headers:" + BR +
            "         content-type: application/json" + BR +
            "         content-language: US-en" + BR +
            "         content-length: 30" + BR +
            "         " + BASIC.asYamlProp() + ": bob:secret" + BR +
            "         " + BEARER.asYamlProp() + ": jkRUTBUjghbjtUGT==" + BR +
            "         " + CUSTOM.asYamlProp() + ": Custom jkRUTBUjghbjtUGT==" + BR +
            "      method: [GET]" + BR +
            "      url: /invoice" + BR +
            "" + BR +
            "   response:" + BR +
            "      headers:" + BR +
            "         content-type: application/json" + BR +
            "         content-language: US-en" + BR +
            "         content-length: 30" + BR +
            "         pragma: no-cache" + BR +
            "         location: /invoice/exit";

      final YamlBuilder yamlBuilder = new YamlBuilder();
      final String actualYaml = yamlBuilder.
         newStubbedRequest().
         withHeaderContentType("application/json").
         withHeaderContentLanguage("US-en").
         withHeaderContentLength("30").
         withHeaderAuthorizationBasic("bob:secret").
         withHeaderAuthorizationBearer("jkRUTBUjghbjtUGT==").
         withHeaderAuthorizationCustom("Custom jkRUTBUjghbjtUGT==").
         withMethodGet().
         withUrl("/invoice").
         newStubbedResponse().
         withHeaderContentType("application/json").
         withHeaderContentLanguage("US-en").
         withHeaderContentLength("30").
         withHeaderPragma("no-cache").
         withHeaderLocation("/invoice/exit").build();

      assertThat(actualYaml).isEqualTo(expectedYaml);

   }
}