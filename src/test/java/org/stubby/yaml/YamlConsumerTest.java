package org.stubby.yaml;

import org.junit.Assert;
import org.junit.Test;
import org.stubby.yaml.stubs.StubHttpLifecycle;

import java.io.File;
import java.util.List;

/**
 * @author Alexander Zagniotov
 * @since 6/11/12, 10:29 PM
 */
public class YamlConsumerTest {

   @Test
   public void testReadYamlNoHeaders() throws Exception {

      final File yaml = new File("/Users/ThoughtWorks/development/git/stubby4j/src/test/resources/httplifecycles-noheaders.yaml");
      Assert.assertNotNull(yaml);

      final List<StubHttpLifecycle> stubHttpLifecycle = YamlConsumer.readYaml(yaml);
      final String expected = "[" +
            "StubHttpLifecycle{" +
            "request=StubRequest{url='/invoice/123', method='GET', postBody='null', headers={}}, " +
            "response=StubResponse{status='200', body='This is a response for 123', headers={}}}, " +
            "StubHttpLifecycle{" +
            "request=StubRequest{url='/invoice/567', method='GET', postBody='null', headers={}}, " +
            "response=StubResponse{status='503', body='This is a response for 567', headers={}}}" +
            "]";
      Assert.assertEquals(expected, stubHttpLifecycle.toString());
   }

   @Test
   public void testReadYamlWithHeaders() throws Exception {

      final File yaml = new File("/Users/ThoughtWorks/development/git/stubby4j/src/test/resources/httplifecycles.yaml");
      Assert.assertNotNull(yaml);

      final List<StubHttpLifecycle> stubHttpLifecycle = YamlConsumer.readYaml(yaml);
      final String expected = "[" +
            "StubHttpLifecycle{" +
            "request=StubRequest{url='/invoice/123', method='GET', postBody='null', " +
            "headers={content-type=application/json}}, " +
            "response=StubResponse{status='200', body='This is a response for 123', " +
            "headers={content-type=application/text}}}, " +
            "StubHttpLifecycle{" +
            "request=StubRequest{url='/invoice/567', method='GET', postBody='null', " +
            "headers={content-type=application/alex, pragma=no-cache}}, " +
            "response=StubResponse{status='503', body='This is a response for 567', " +
            "headers={content-type=application/momo}}}]";
      Assert.assertEquals(expected, stubHttpLifecycle.toString());
   }
}
