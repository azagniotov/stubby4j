package by.stub.builder.yaml;

import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.util.Scanner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Alexander Zagniotov
 * @since 4/13/13, 12:50 AM
 */
public class YamlBuilderTest {

   @Test
   public void shouldBuildStubbedRequestWithStubbedResponse() throws Exception {
      final String expectedYaml =
         "request:\n" +
            "  query:\n" +
            "    status: active\n" +
            "    type: full\n" +
            "  method: GET\n" +
            "  url: /invoice\n" +
            "response:\n" +
            "  headers:\n" +
            "    content-type: application/json\n" +
            "    pragma: no-cache\n" +
            "  status: 200\n" +
            "  file: ../json/systemtest-body-response-as-file.json";

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

      assertThat(actualYaml, is(equalTo(expectedYaml)));

   }

   @Test
   public void shouldBuildStubbedRequestWithMultilineStubbedResponse() throws Exception {

      final String expectedYaml =
         "request:\n" +
            "  method: PUT\n" +
            "  url: /invoice/123\n" +
            "  headers:\n" +
            "    content-type: application/json\n" +
            "  post: >\n" +
            "    {\"name\": \"milk\", \"description\": \"full\", \"department\": \"savoury\"}\n" +
            "response:\n" +
            "  headers:\n" +
            "    content-type: application/json\n" +
            "    pragma: no-cache\n" +
            "  status: 200\n" +
            "  body: >\n" +
            "    {\"id\": \"123\", \"status\": \"updated\"}";

      final YamlBuilder yamlBuilder = new YamlBuilder();
      final String actualYaml = yamlBuilder.
         newStubbedRequest().
         withMethod("PUT").
         withUrl("/invoice/123").
         withHeaders("content-type", "application/json").
         withPost("{\"name\": \"milk\", \"description\": \"full\", \"department\": \"savoury\"}").
         newStubbedResponse().
         withHeaders("content-type", "application/json").
         withHeaders("pragma", "no-cache").
         withStatus("200").
         withBody("{\"id\": \"123\", \"status\": \"updated\"}").build();

      assertThat(actualYaml, is(equalTo(expectedYaml)));

   }

   @Test
   public void shouldBuildExactYamlThatMatchesLoadedFile() throws Exception {

      final URL jsonContentUrl = YamlBuilderTest.class.getResource("/stubbed.request.response.yaml");
      Assert.assertNotNull(jsonContentUrl);

      final String expectedYaml = new Scanner(jsonContentUrl.openStream(), "UTF-8").useDelimiter("\\A").next().trim();

      final YamlBuilder yamlBuilder = new YamlBuilder();
      final String actualYaml = yamlBuilder.
         newStubbedRequest().
         withMethod("PUT").
         withUrl("/invoice/123").
         withHeaders("content-type", "application/json").
         withPost("{\"name\": \"milk\", \"description\": \"full\", \"department\": \"savoury\"}").
         newStubbedResponse().
         withHeaders("content-type", "application/json").
         withHeaders("pragma", "no-cache").
         withStatus("200").
         withBody("{\"id\": \"123\", \"status\": \"updated\"}").build();

      assertThat(actualYaml, is(equalTo(expectedYaml)));

   }
}
