package by.stub.handlers.strategy.admin;

import by.stub.database.StubbedDataManager;
import by.stub.handlers.AdminHandler;
import by.stub.javax.servlet.http.HttpServletResponseWithGetStatus;
import by.stub.utils.FileUtils;
import by.stub.utils.HandlerUtils;
import by.stub.utils.StringUtils;
import by.stub.yaml.YamlParser;
import by.stub.yaml.stubs.StubHttpLifecycle;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author: Alexander Zagniotov
 * Created: 4/25/13 11:30 PM
 */
public class PutHandlingStrategy implements AdminResponseHandlingStrategy {
   @Override
   public void handle(final HttpServletRequest request, final HttpServletResponseWithGetStatus wrapper, final StubbedDataManager stubbedDataManager) throws Exception {

      if (request.getRequestURI().equals(AdminHandler.ADMIN_ROOT)) {
         wrapper.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
         wrapper.getWriter().println("Method PUT is not allowed on URI " + request.getRequestURI());
         return;
      }

      final int contextPathLength = AdminHandler.ADMIN_ROOT.length();
      final String pathInfoNoHeadingSlash = request.getRequestURI().substring(contextPathLength);
      final int stubIndexToUpdate = Integer.parseInt(pathInfoNoHeadingSlash);

      if (!stubbedDataManager.isStubHttpLifecycleExistsByIndex(stubIndexToUpdate)) {
         final String errorMessage = String.format("Stub request index#%s does not exist, cannot update", stubIndexToUpdate);
         HandlerUtils.configureErrorResponse(wrapper, HttpStatus.NO_CONTENT_204, errorMessage);
         return;
      }

      final String put = HandlerUtils.extractPostRequestBody(request, AdminHandler.NAME);
      if (!StringUtils.isSet(put)) {
         final String errorMessage = String.format("%s request on URI %s was empty", request.getMethod(), request.getRequestURI());
         HandlerUtils.configureErrorResponse(wrapper, HttpStatus.NO_CONTENT_204, errorMessage);
         return;
      }

      final String updatedCycleUrl = stubbedDataManager.refreshStubbedData(new YamlParser(), put, stubIndexToUpdate);

      wrapper.setStatus(HttpStatus.CREATED_201);
      wrapper.addHeader(HttpHeaders.LOCATION, updatedCycleUrl);
      final String successfulMessage = String.format("Stub request index#%s updated successfully", stubIndexToUpdate);
      wrapper.getWriter().println(successfulMessage);
   }
}
