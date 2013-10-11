package by.stub.handlers.strategy.admin;

import by.stub.database.StubbedDataManager;
import by.stub.handlers.AdminPortalHandler;
import by.stub.javax.servlet.http.HttpServletResponseWithGetStatus;
import by.stub.utils.HandlerUtils;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author: Alexander Zagniotov
 * Created: 4/25/13 11:30 PM
 */
public class DeleteHandlingStrategy implements AdminResponseHandlingStrategy {
   @Override
   public void handle(final HttpServletRequest request, final HttpServletResponseWithGetStatus wrapper, final StubbedDataManager stubbedDataManager) throws IOException {

      if (request.getRequestURI().equals(AdminPortalHandler.ADMIN_ROOT)) {
         wrapper.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
         wrapper.getWriter().println("Method DELETE is not allowed on URI " + request.getRequestURI());
         return;
      }

      final int contextPathLength = AdminPortalHandler.ADMIN_ROOT.length();
      final String pathInfoNoHeadingSlash = request.getRequestURI().substring(contextPathLength);
      final int stubIndexToDelete = Integer.parseInt(pathInfoNoHeadingSlash);

      if (!stubbedDataManager.isStubHttpLifecycleExistsByIndex(stubIndexToDelete)) {
         final String errorMessage = String.format("Stub request index#%s does not exist, cannot delete", stubIndexToDelete);
         HandlerUtils.configureErrorResponse(wrapper, HttpStatus.NO_CONTENT_204, errorMessage);
         return;
      }

      stubbedDataManager.deleteStubHttpLifecycleByIndex(stubIndexToDelete);
      wrapper.setStatus(HttpStatus.OK_200);
      wrapper.getWriter().println(String.format("Stub request index#%s deleted successfully", stubIndexToDelete));
   }
}
