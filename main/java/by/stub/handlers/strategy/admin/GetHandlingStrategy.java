package by.stub.handlers.strategy.admin;

import by.stub.database.StubbedDataManager;
import by.stub.handlers.AdminPortalHandler;
import by.stub.utils.HandlerUtils;
import by.stub.utils.StringUtils;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

public class GetHandlingStrategy implements AdminResponseHandlingStrategy {

   @Override
   public void handle(final HttpServletRequest request, final HttpServletResponse response, final StubbedDataManager stubbedDataManager) throws IOException {

      final StringBuilder yamlAppender = new StringBuilder();
      final int contextPathLength = AdminPortalHandler.ADMIN_ROOT.length();
      final String pathInfoNoHeadingSlash = request.getRequestURI().substring(contextPathLength);

      if (StringUtils.isSet(pathInfoNoHeadingSlash)) {
         final int targetHttpStubCycleIndex = Integer.parseInt(pathInfoNoHeadingSlash);

         if (!stubbedDataManager.isStubHttpLifecycleExistsByIndex(targetHttpStubCycleIndex)) {
            final String errorMessage = String.format("Stub request index#%s does not exist, cannot display", targetHttpStubCycleIndex);
            HandlerUtils.configureErrorResponse(response, HttpStatus.NO_CONTENT_204, errorMessage);
            return;
         }

         yamlAppender.append(stubbedDataManager.getMarshalledYamlByIndex(targetHttpStubCycleIndex));
      } else {
         yamlAppender.append(stubbedDataManager.getMarshalledYaml());
      }

      response.setContentType("text/plain;charset=UTF-8");

      final OutputStream streamOut = response.getOutputStream();
      streamOut.write(StringUtils.getBytesUtf8(yamlAppender.toString()));
      streamOut.flush();
      streamOut.close();
   }
}
