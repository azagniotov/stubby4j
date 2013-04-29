package by.stub.handlers.strategy.admin;

import by.stub.database.DataStore;
import by.stub.handlers.AdminHandler;
import by.stub.javax.servlet.http.HttpServletResponseWithGetStatus;
import by.stub.utils.HandlerUtils;
import by.stub.utils.StringUtils;
import by.stub.yaml.stubs.StubHttpLifecycle;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @author: Alexander Zagniotov
 * Created: 4/25/13 11:29 PM
 */
public class GetHandlingStrategy implements AdminResponseHandlingStrategy {

   @Override
   public void handle(final HttpServletRequest request, final HttpServletResponseWithGetStatus wrapper, final DataStore dataStore) throws IOException {

      final StringBuilder yamlAppender = new StringBuilder();
      final int contextPathLength = AdminHandler.ADMIN_ROOT.length();
      final String pathInfoNoHeadingSlash = request.getRequestURI().substring(contextPathLength);

      if (StringUtils.isSet(pathInfoNoHeadingSlash)) {
         final int targetHttpStubCycleIndex = Integer.parseInt(pathInfoNoHeadingSlash);

         if (dataStore.getStubHttpLifecycles().size() - 1 < targetHttpStubCycleIndex) {
            final String errorMessage = String.format("Stub request index#%s does not exist, cannot display", targetHttpStubCycleIndex);
            HandlerUtils.configureErrorResponse(wrapper, HttpStatus.NO_CONTENT_204, errorMessage);
            return;
         }

         yamlAppender.append(dataStore.getMarshalledYamlByIndex(targetHttpStubCycleIndex));

      } else {
         final List<StubHttpLifecycle> stubbedCycles = dataStore.getStubHttpLifecycles();
         for (final StubHttpLifecycle cycle : stubbedCycles) {
            yamlAppender.append(cycle.getMarshalledYaml()).append("\n\n");
         }
      }

      wrapper.setContentType(MimeTypes.TEXT_PLAIN_UTF_8);

      final OutputStream streamOut = wrapper.getOutputStream();
      streamOut.write(StringUtils.getBytesUtf8(yamlAppender.toString()));
      streamOut.flush();
      streamOut.close();
   }
}
