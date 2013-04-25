package by.stub.handlers;

import by.stub.database.DataStore;
import by.stub.javax.servlet.http.HttpServletResponseWithGetStatus;
import by.stub.utils.ConsoleUtils;
import by.stub.utils.FileUtils;
import by.stub.utils.HandlerUtils;
import by.stub.utils.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author: Alexander Zagniotov
 * Created: 4/24/13 5:45 PM
 */
public class YamlDumpHandler extends AbstractHandler {

   private static final String NAME = "admin";

   private final DataStore dataStore;

   public YamlDumpHandler(final DataStore dataStore) {
      this.dataStore = dataStore;
   }

   @Override
   public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
      ConsoleUtils.logIncomingRequest(request, NAME);

      baseRequest.setHandled(true);
      final HttpServletResponseWithGetStatus wrapper = new HttpServletResponseWithGetStatus(response);

      try {


         //TODO Reply with 204 if no stubs are saved

         HandlerUtils.setResponseMainHeaders(wrapper);
         wrapper.setContentType("text/plain");
         wrapper.setCharacterEncoding(StringUtils.UTF_8);

         final OutputStream streamOut = wrapper.getOutputStream();
         streamOut.write(FileUtils.asciiFileToUtf8Bytes(dataStore.getDataYaml()));
         streamOut.flush();
         streamOut.close();

      } catch (final Exception ex) {
         HandlerUtils.configureErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
      }

      ConsoleUtils.logOutgoingResponse(request.getRequestURI(), wrapper, NAME);
   }
}