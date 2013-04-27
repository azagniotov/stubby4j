package by.stub.handlers.strategy.admin;

import by.stub.database.DataStore;
import by.stub.javax.servlet.http.HttpServletResponseWithGetStatus;
import by.stub.utils.FileUtils;
import by.stub.utils.HandlerUtils;
import by.stub.utils.StringUtils;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author: Alexander Zagniotov
 * Created: 4/25/13 11:29 PM
 */
public class GetHandlingStrategy implements AdminResponseHandlingStrategy {

   @Override
   public void handle(final HttpServletRequest request, final HttpServletResponseWithGetStatus wrapper, final DataStore dataStore) throws IOException {

      if (dataStore.getStubHttpLifecycles().size() == 0) {
         final String errorMessage = String.format("There were n stub request found when doing %s request on URI %s", request.getMethod(), request.getRequestURI());
         HandlerUtils.configureErrorResponse(wrapper, HttpStatus.NO_CONTENT_204, errorMessage);
      }

      HandlerUtils.setResponseMainHeaders(wrapper);
      wrapper.setContentType("text/plain");
      wrapper.setCharacterEncoding(StringUtils.UTF_8);

      final OutputStream streamOut = wrapper.getOutputStream();
      streamOut.write(FileUtils.asciiFileToUtf8Bytes(dataStore.getDataYaml()));
      streamOut.flush();
      streamOut.close();
   }
}
