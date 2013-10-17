package by.stub.builder.yaml;

import by.stub.utils.FileUtils;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;


/**
 * @author Alexander Zagniotov
 * @since 4/13/13, 12:50 AM
 */
public class YamlBuilderTest {

   @Test
   public void shouldBuildStubbedResponseWithSequenceResponses() throws Exception {
      final String expectedYaml =
         "-  request:" + FileUtils.LINE_SEPARATOR +
            "      method: [PUT]" + FileUtils.LINE_SEPARATOR +
            "      url: /invoice" + FileUtils.LINE_SEPARATOR +
            "" + FileUtils.LINE_SEPARATOR +
            "   response:" + FileUtils.LINE_SEPARATOR +
            "      -  status: 200" + FileUtils.LINE_SEPARATOR +
            "         headers: " + FileUtils.LINE_SEPARATOR +
            "            content-type: application/json" + FileUtils.LINE_SEPARATOR +
            "         body: OK" + FileUtils.LINE_SEPARATOR +
            "" + FileUtils.LINE_SEPARATOR +
            "      -  status: 200" + FileUtils.LINE_SEPARATOR +
            "         headers: " + FileUtils.LINE_SEPARATOR +
            "            content-type: application/json" + FileUtils.LINE_SEPARATOR +
            "         body: Still going strong!" + FileUtils.LINE_SEPARATOR +
            "" + FileUtils.LINE_SEPARATOR +
            "      -  status: 500" + FileUtils.LINE_SEPARATOR +
            "         headers: " + FileUtils.LINE_SEPARATOR +
            "            content-type: application/json" + FileUtils.LINE_SEPARATOR +
            "         body: OMFG!!!" + FileUtils.LINE_SEPARATOR +
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
         "-  request:" + FileUtils.LINE_SEPARATOR +
            "      method: [PUT]" + FileUtils.LINE_SEPARATOR +
            "      url: /invoice" + FileUtils.LINE_SEPARATOR +
            "" + FileUtils.LINE_SEPARATOR +
            "   response:" + FileUtils.LINE_SEPARATOR +
            "      -  status: 200" + FileUtils.LINE_SEPARATOR +
            "         headers: " + FileUtils.LINE_SEPARATOR +
            "            content-type: application/json" + FileUtils.LINE_SEPARATOR +
            "         body: OK" + FileUtils.LINE_SEPARATOR +
            "" + FileUtils.LINE_SEPARATOR +
            "      -  status: 200" + FileUtils.LINE_SEPARATOR +
            "         headers: " + FileUtils.LINE_SEPARATOR +
            "            content-type: application/json" + FileUtils.LINE_SEPARATOR +
            "         body: >" + FileUtils.LINE_SEPARATOR +
            "            {\"status\", \"200\"}" + FileUtils.LINE_SEPARATOR +
            "" + FileUtils.LINE_SEPARATOR +
            "      -  status: 500" + FileUtils.LINE_SEPARATOR +
            "         headers: " + FileUtils.LINE_SEPARATOR +
            "            content-type: application/json" + FileUtils.LINE_SEPARATOR +
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
         "-  request:" + FileUtils.LINE_SEPARATOR +
            "      method: [HEAD, GET, PUT]" + FileUtils.LINE_SEPARATOR +
            "      url: /invoice" + FileUtils.LINE_SEPARATOR +
            "" + FileUtils.LINE_SEPARATOR +
            "   response:" + FileUtils.LINE_SEPARATOR +
            "      status: 200" + FileUtils.LINE_SEPARATOR +
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
         "-  request:" + FileUtils.LINE_SEPARATOR +
            "      query:" + FileUtils.LINE_SEPARATOR +
            "         status: active" + FileUtils.LINE_SEPARATOR +
            "         type: full" + FileUtils.LINE_SEPARATOR +
            "      method: [GET]" + FileUtils.LINE_SEPARATOR +
            "      url: /invoice" + FileUtils.LINE_SEPARATOR +
            "" + FileUtils.LINE_SEPARATOR +
            "   response:" + FileUtils.LINE_SEPARATOR +
            "      headers:" + FileUtils.LINE_SEPARATOR +
            "         content-type: application/json" + FileUtils.LINE_SEPARATOR +
            "         pragma: no-cache" + FileUtils.LINE_SEPARATOR +
            "      status: 200" + FileUtils.LINE_SEPARATOR +
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
         "-  request:" + FileUtils.LINE_SEPARATOR +
            "      method: [PUT]" + FileUtils.LINE_SEPARATOR +
            "      url: /invoice/123" + FileUtils.LINE_SEPARATOR +
            "      headers:" + FileUtils.LINE_SEPARATOR +
            "         content-type: application/json" + FileUtils.LINE_SEPARATOR +
            "      post: >" + FileUtils.LINE_SEPARATOR +
            "         {\"name\": \"milk\", \"description\": \"full\", \"department\": \"savoury\"}" + FileUtils.LINE_SEPARATOR +
            "" + FileUtils.LINE_SEPARATOR +
            "   response:" + FileUtils.LINE_SEPARATOR +
            "      headers:" + FileUtils.LINE_SEPARATOR +
            "         content-type: application/json" + FileUtils.LINE_SEPARATOR +
            "         pragma: no-cache" + FileUtils.LINE_SEPARATOR +
            "      status: 200" + FileUtils.LINE_SEPARATOR +
            "      body: >" + FileUtils.LINE_SEPARATOR +
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
         "-  request:" + FileUtils.LINE_SEPARATOR +
            "      headers:" + FileUtils.LINE_SEPARATOR +
            "         content-type: application/json" + FileUtils.LINE_SEPARATOR +
            "         content-language: US-en" + FileUtils.LINE_SEPARATOR +
            "         content-length: 30" + FileUtils.LINE_SEPARATOR +
            "         authorization: bob:secret" + FileUtils.LINE_SEPARATOR +
            "      method: [GET]" + FileUtils.LINE_SEPARATOR +
            "      url: /invoice" + FileUtils.LINE_SEPARATOR +
            "" + FileUtils.LINE_SEPARATOR +
            "   response:" + FileUtils.LINE_SEPARATOR +
            "      headers:" + FileUtils.LINE_SEPARATOR +
            "         content-type: application/json" + FileUtils.LINE_SEPARATOR +
            "         content-language: US-en" + FileUtils.LINE_SEPARATOR +
            "         content-length: 30" + FileUtils.LINE_SEPARATOR +
            "         pragma: no-cache" + FileUtils.LINE_SEPARATOR +
            "         location: /invoice/exit";

      final YamlBuilder yamlBuilder = new YamlBuilder();
      final String actualYaml = yamlBuilder.
         newStubbedRequest().
         withHeaderContentType("application/json").
         withHeaderContentLanguage("US-en").
         withHeaderContentLength("30").
         withHeaderAuthorization("bob:secret").
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