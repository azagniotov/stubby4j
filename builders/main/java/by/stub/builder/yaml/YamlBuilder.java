package by.stub.builder.yaml;

import by.stub.utils.FileUtils;
import com.google.api.client.http.HttpMethods;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Please refer to the accompanied unit tests for usage examples.
 *
 * @author Alexander Zagniotov
 * @since 4/13/13, 12:13 AM
 */
@SuppressWarnings("unchecked")
public final class YamlBuilder {

   private static final String TWO_SPACE = "  ";
   private static final String THREE_SPACE = "   ";
   private static final String SIX_SPACE = String.format("%s%s", THREE_SPACE, THREE_SPACE);
   private static final String NINE_SPACE = String.format("%s%s", SIX_SPACE, THREE_SPACE);

   private final static String REQUEST = String.format("-%s%s", TWO_SPACE, "request:");
   private final static String RESPONSE = String.format("%s%s", THREE_SPACE, "response:");

   private final static String HEADERS = String.format("%s%s", SIX_SPACE, "headers:");
   private final static String QUERY = String.format("%s%s", SIX_SPACE, "query:");
   private final static String METHOD = String.format("%s%s", SIX_SPACE, "method: ");
   private final static String TEMP_METHOD_PLACEHOLDER_TOKEN = "METHOD_TOKEN";
   private final static String STATUS = String.format("%s%s", SIX_SPACE, "status: ");
   private final static String FILE = String.format("%s%s", SIX_SPACE, "file: ");
   private final static String URL = String.format("%s%s", SIX_SPACE, "url: ");

   private final static String ONELINEPOST = String.format("%s%s", SIX_SPACE, "post: ");
   private final static String MULTILINEPOST = String.format("%s%s", SIX_SPACE, "post: >\n");

   private final static String ONELINEBODY = String.format("%s%s", SIX_SPACE, "body: ");
   private final static String MULTILINEBODY = String.format("%s%s", SIX_SPACE, "body: >\n");

   private final static String NL = FileUtils.LINE_SEPARATOR;

   private final static String REQUEST_HEADERS_KEY = String.format("%s-%s", REQUEST, HEADERS);
   private final static String REQUEST_QUERY_KEY = String.format("%s-%s", REQUEST, QUERY);

   private final static String RESPONSE_HEADERS_KEY = String.format("%s-%s", RESPONSE, HEADERS);
   private final static String RESPONSE_QUERY_KEY = String.format("%s-%s", RESPONSE, QUERY);


   final Set<String> storedStubbedMethods = new LinkedHashSet<String>();

   final Set<String> unusedNodes = new HashSet<String>() {{
      add(REQUEST_HEADERS_KEY);
      add(REQUEST_QUERY_KEY);
      add(RESPONSE_HEADERS_KEY);
      add(RESPONSE_QUERY_KEY);
   }};

   private static final StringBuilder REQUEST_STRING_BUILDER = new StringBuilder();
   private static final StringBuilder RESPONSE_STRING_BUILDER = new StringBuilder();

   public YamlBuilder() {

   }

   public Request newStubbedRequest() {
      return new Request();
   }

   public final class Request {

      public Request() {
         REQUEST_STRING_BUILDER.setLength(0);
         REQUEST_STRING_BUILDER.append(REQUEST).append(NL);
      }

      public Request withMethod(final String value) {
         return appendTemporaryMethodPlaceholderStoreMethod(value);
      }

      public Request withMethodGet() {
         return appendTemporaryMethodPlaceholderStoreMethod(HttpMethods.GET);
      }

      public Request withMethodPut() {
         return appendTemporaryMethodPlaceholderStoreMethod(HttpMethods.PUT);
      }

      public Request withMethodPost() {
         return appendTemporaryMethodPlaceholderStoreMethod(HttpMethods.POST);
      }

      public Request withMethodHead() {
         return appendTemporaryMethodPlaceholderStoreMethod(HttpMethods.HEAD);
      }

      public Request withUrl(final String value) {
         REQUEST_STRING_BUILDER.append(URL).append(value).append(NL);

         return this;
      }

      private Request appendTemporaryMethodPlaceholderStoreMethod(final String methodName) {
         if (REQUEST_STRING_BUILDER.indexOf(METHOD) == -1) {
            REQUEST_STRING_BUILDER.append(METHOD).append(TEMP_METHOD_PLACEHOLDER_TOKEN).append(NL);
         }

         storedStubbedMethods.add(methodName);

         return this;
      }

      public Request withHeaders(final String key, final String value) {

         if (unusedNodes.contains(REQUEST_HEADERS_KEY)) {
            REQUEST_STRING_BUILDER.append(HEADERS).append(NL);
            unusedNodes.remove(REQUEST_HEADERS_KEY);
         }

         final String tabbedKey = String.format("%s%s: ", NINE_SPACE, key);
         REQUEST_STRING_BUILDER.append(tabbedKey).append(value).append(NL);

         return this;
      }

      public Request withLiteralPost(final String post) {
         REQUEST_STRING_BUILDER.append(ONELINEPOST).append(post).append(NL);

         return this;
      }

      public Request withFoldedPost(final String post) {
         final String tabbedPost = String.format("%s%s", NINE_SPACE, post);
         REQUEST_STRING_BUILDER.append(MULTILINEPOST).append(tabbedPost).append(NL);

         return this;
      }

      public Request withQuery(final String key, final String value) {

         if (unusedNodes.contains(REQUEST_QUERY_KEY)) {
            REQUEST_STRING_BUILDER.append(QUERY).append(NL);
            unusedNodes.remove(REQUEST_QUERY_KEY);
         }

         final String tabbedKey = String.format("%s%s: ", NINE_SPACE, key);
         REQUEST_STRING_BUILDER.append(tabbedKey).append(value).append(NL);

         return this;
      }

      public Response newStubbedResponse() {
         return new Response();
      }

      public String toString() {
         return REQUEST_STRING_BUILDER.toString();
      }
   }

   public final class Response {

      public Response() {
         RESPONSE_STRING_BUILDER.setLength(0);
         RESPONSE_STRING_BUILDER.append(RESPONSE).append(NL);
      }

      public Response withStatus(final String value) {
         RESPONSE_STRING_BUILDER.append(STATUS).append(value).append(NL);

         return this;
      }

      public Response withFile(final String value) {
         RESPONSE_STRING_BUILDER.append(FILE).append(value).append(NL);

         return this;
      }

      public Response withLiteralBody(final String body) {
         RESPONSE_STRING_BUILDER.append(ONELINEBODY).append(body).append(NL);

         return this;
      }

      public Response withFoldedBody(final String body) {
         final String tabbedBody = String.format("%s%s", NINE_SPACE, body);
         RESPONSE_STRING_BUILDER.append(MULTILINEBODY).append(tabbedBody).append(NL);

         return this;
      }

      public Response withHeaders(final String key, final String value) {

         if (unusedNodes.contains(RESPONSE_HEADERS_KEY)) {
            RESPONSE_STRING_BUILDER.append(HEADERS).append(NL);
            unusedNodes.remove(RESPONSE_HEADERS_KEY);
         }

         final String tabbedKey = String.format("%s%s: ", NINE_SPACE, key);
         RESPONSE_STRING_BUILDER.append(tabbedKey).append(value).append(NL);

         return this;
      }

      public String build() {

         final String rawRequestString = REQUEST_STRING_BUILDER.toString();
         final String cleansedRequestString = rawRequestString.replaceAll(TEMP_METHOD_PLACEHOLDER_TOKEN, storedStubbedMethods.toString());
         final String yaml = String.format("%s\n%s", cleansedRequestString, RESPONSE_STRING_BUILDER.toString()).trim();

         unusedNodes.clear();
         unusedNodes.add(REQUEST_HEADERS_KEY);
         unusedNodes.add(REQUEST_QUERY_KEY);
         unusedNodes.add(RESPONSE_HEADERS_KEY);
         unusedNodes.add(RESPONSE_QUERY_KEY);
         storedStubbedMethods.clear();

         return yaml;
      }
   }
}
