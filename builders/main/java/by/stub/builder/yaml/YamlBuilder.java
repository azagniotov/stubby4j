package by.stub.builder.yaml;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Please refer to the accompanied unit tests for usage examples.
 *
 * @author Alexander Zagniotov
 * @since 4/13/13, 12:13 AM
 */
@SuppressWarnings("unchecked")
public final class YamlBuilder {

   private static final Yaml SNAKE_YAML;

   static {
      final DumperOptions dumperOptions = new DumperOptions();
      dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
      dumperOptions.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
      dumperOptions.setPrettyFlow(true);

      SNAKE_YAML = new Yaml(new Constructor(), new FoldedStyleRepresenter(), dumperOptions, new CustomResolver());

   }

   final Map<String, Object> httpcycles = new TreeMap<String, Object>();

   private final static String REQUEST = "request";
   private final static String RESPONSE = "response";
   private final static String HEADERS = "headers";
   private final static String BODY = "body";
   private final static String QUERY = "query";

   public YamlBuilder() {

   }

   private static final class CustomResolver extends Resolver {

      /*
       * do not resolve float and timestamp
       */
      protected void addImplicitResolvers() {
         // addImplicitResolver(Tag.BOOL, BOOL, "yYnNtTfFoO");
         // addImplicitResolver(Tags.FLOAT, FLOAT, "-+0123456789.");
         // addImplicitResolver(Tag.INT, INT, "-+0123456789");
         // addImplicitResolver(Tag.MERGE, MERGE, "<");
         // addImplicitResolver(Tag.NULL, NULL, "~nN\0");
         // addImplicitResolver(Tag.NULL, EMPTY, null);
         // addImplicitResolver(Tags.TIMESTAMP, TIMESTAMP, "0123456789");
         // addImplicitResolver(Tag.VALUE, VALUE, "=");
      }
   }


   private static final class FoldedStyleRepresenter extends Representer {

      public FoldedStyleRepresenter() {
         this.representers.put(String.class, new FoldedStyleRepresent());
      }


      private static final class FoldedStyleRepresent implements Represent {

         private boolean foldedStyleRequired = false;

         @Override
         public Node representData(final Object data) {
            final String dataValue = data.toString();

            if (foldedStyleRequired) {
               foldedStyleRequired = false;
               return new ScalarNode(Tag.STR, dataValue, null, null, DumperOptions.ScalarStyle.FOLDED.getChar());
            }

            if (dataValue.equals("body") || dataValue.equals("post")) {
               foldedStyleRequired = true;
            }

            return new ScalarNode(Tag.STR, dataValue, null, null, DumperOptions.ScalarStyle.PLAIN.getChar());
         }
      }
   }

   public Request newStubbedRequest() {
      return new Request();
   }


   public final class Request {

      Request() {
         if (!httpcycles.containsKey(REQUEST)) {
            httpcycles.put(REQUEST, new LinkedHashMap<String, Object>());
         }
      }

      public Request withMethod(final String value) {
         final Map<String, Object> keyValues = (Map<String, Object>) httpcycles.get(REQUEST);
         keyValues.put("method", value);

         return this;
      }

      public Request withUrl(final String value) {
         final Map<String, Object> keyValues = (Map<String, Object>) httpcycles.get(REQUEST);
         keyValues.put("url", value);

         return this;
      }

      public Request withHeaders(final String key, final String value) {
         final Map<String, Object> keyValues = (Map<String, Object>) httpcycles.get(REQUEST);

         if (!keyValues.containsKey(HEADERS)) {
            keyValues.put(HEADERS, new HashMap<String, Object>());
         }
         final Map<String, Object> headerKeyValues = (Map<String, Object>) keyValues.get(HEADERS);
         headerKeyValues.put(key, value);

         return this;
      }

      public Request withPost(final String post) {
         final Map<String, Object> keyValues = (Map<String, Object>) httpcycles.get(REQUEST);
         keyValues.put("post", post);

         return this;
      }

      public Request withQuery(final String key, final String value) {
         final Map<String, Object> keyValues = (Map<String, Object>) httpcycles.get(REQUEST);

         if (!keyValues.containsKey(QUERY)) {
            keyValues.put(QUERY, new HashMap<String, Object>());
         }
         final Map<String, Object> headerKeyValues = (Map<String, Object>) keyValues.get(QUERY);
         headerKeyValues.put(key, value);

         return this;
      }

      public Response newStubbedResponse() {
         return new Response();
      }
   }

   public final class Response {

      Response() {
         if (!httpcycles.containsKey(RESPONSE)) {
            httpcycles.put(RESPONSE, new LinkedHashMap<String, Object>());
         }
      }


      public Response withStatus(final String value) {
         final Map<String, Object> keyValues = (Map<String, Object>) httpcycles.get(RESPONSE);
         keyValues.put("status", value);

         return this;
      }

      public Response withFile(final String value) {
         final Map<String, Object> keyValues = (Map<String, Object>) httpcycles.get(RESPONSE);
         keyValues.put("file", value);

         return this;
      }

      public Response withBody(final String body) {
         final Map<String, Object> keyValues = (Map<String, Object>) httpcycles.get(RESPONSE);
         keyValues.put("body", body);

         return this;
      }

      public Response withHeaders(final String key, final String value) {
         final Map<String, Object> keyValues = (Map<String, Object>) httpcycles.get(RESPONSE);

         if (!keyValues.containsKey(HEADERS)) {
            keyValues.put(HEADERS, new HashMap<String, Object>());
         }
         final Map<String, Object> headerKeyValues = (Map<String, Object>) keyValues.get(HEADERS);
         headerKeyValues.put(key, value);

         return this;
      }

      public String build() {
         return SNAKE_YAML.dump(httpcycles).replaceAll(">-", ">").trim();
      }
   }
}
