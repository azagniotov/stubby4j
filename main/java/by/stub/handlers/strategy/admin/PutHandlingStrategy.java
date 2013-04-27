package by.stub.handlers.strategy.admin;

import by.stub.database.DataStore;
import by.stub.javax.servlet.http.HttpServletResponseWithGetStatus;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author: Alexander Zagniotov
 * Created: 4/25/13 11:30 PM
 */
public class PutHandlingStrategy implements AdminResponseHandlingStrategy {
   @Override
   public void handle(final HttpServletRequest request, final HttpServletResponseWithGetStatus wrapper, final DataStore dataStore) throws IOException {
      wrapper.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
      wrapper.getWriter().println("Method PUT is not allowed on URI " + request.getRequestURI());
   }
}
