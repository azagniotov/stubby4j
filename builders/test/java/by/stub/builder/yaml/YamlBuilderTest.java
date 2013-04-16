package by.stub.builder.yaml;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;


/**
 * @author Alexander Zagniotov
 * @since 4/13/13, 12:50 AM
 */
public class YamlBuilderTest {

   @Test
   public void shouldBuildStubbedRequestWithStubbedResponse() throws Exception {
      final String expectedYaml =
         "-  request:\n" +
            "      query:\n" +
            "         status: active\n" +
            "         type: full\n" +
            "      method: GET\n" +
            "      url: /invoice\n" +
            "\n" +
            "   response:\n" +
            "      headers:\n" +
            "         content-type: application/json\n" +
            "         pragma: no-cache\n" +
            "      status: 200\n" +
            "      file: ../json/systemtest-body-response-as-file.json";

      final YamlBuilder yamlBuilder = new YamlBuilder();
      final String actualYaml = yamlBuilder.
         newStubbedRequest().
         withQuery("status", "active").
         withQuery("type", "full").
         withMethod("GET").
         withUrl("/invoice").
         newStubbedResponse().
         withHeaders("content-type", "application/json").
         withHeaders("pragma", "no-cache").
         withStatus("200").
         withFile("../json/systemtest-body-response-as-file.json").build();

      assertThat(expectedYaml).isEqualTo(actualYaml);

   }


   @Test
   public void shouldBuildStubbedRequestWithMultilineStubbedResponse() throws Exception {

      final String expectedYaml =
         "-  request:\n" +
            "      method: PUT\n" +
            "      url: /invoice/123\n" +
            "      headers:\n" +
            "         content-type: application/json\n" +
            "      post: >\n" +
            "         {\"name\": \"milk\", \"description\": \"full\", \"department\": \"savoury\"}\n" +
            "\n" +
            "   response:\n" +
            "      headers:\n" +
            "         content-type: application/json\n" +
            "         pragma: no-cache\n" +
            "      status: 200\n" +
            "      body: >\n" +
            "         {\"id\": \"123\", \"status\": \"updated\"}";

      final YamlBuilder yamlBuilder = new YamlBuilder();
      final String actualYaml = yamlBuilder.
         newStubbedRequest().
         withMethod("PUT").
         withUrl("/invoice/123").
         withHeaders("content-type", "application/json").
         withFoldedPost("{\"name\": \"milk\", \"description\": \"full\", \"department\": \"savoury\"}").
         newStubbedResponse().
         withHeaders("content-type", "application/json").
         withHeaders("pragma", "no-cache").
         withStatus("200").
         withFoldedBody("{\"id\": \"123\", \"status\": \"updated\"}").build();

      assertThat(expectedYaml).isEqualTo(actualYaml);

   }
}