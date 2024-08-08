package com.adobe.aem.capstone.core.servlets;

import com.adobe.aem.capstone.core.service.CustomerPortalApiService;
import com.adobe.aem.capstone.core.constants.ConfigurationConstants;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.apache.commons.lang3.StringUtils;
import javax.servlet.ServletException;
import org.slf4j.LoggerFactory;
import javax.servlet.Servlet;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component(service = { Servlet.class }, property = {
        "sling.servlet.paths=/bin/user/recent/activities",
        "sling.servlet.methods=GET"
}, immediate = true)

public class UserActivitiesServlet extends SlingAllMethodsServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserActivitiesServlet.class);

    @Reference
    private CustomerPortalApiService customerPortalApiService;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        String currentUser = request.getResourceResolver().getUserID();

        // Check if the currentUser is null, empty or anonymous
        if (StringUtils.isEmpty(currentUser) || ConfigurationConstants.ANONYMOUS_USER.equals(currentUser)) {
            response.sendError(SlingHttpServletResponse.SC_FORBIDDEN,
                    "User ID is not available or anonymous user cannot access notifications.");
            return;
        }
        response.setContentType(ConfigurationConstants.JSON_CONTENT_TYPE);
        LocalDateTime endTime = LocalDateTime.now();
        LOGGER.debug("End time: {}", endTime);

        LocalDateTime startTime = endTime.minusMonths(6);
        LOGGER.debug("Start time: {}", startTime);

        // Format the times as ISO 8601 strings
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String startTimeStr = startTime.format(formatter);
        String endTimeStr = endTime.format(formatter); 

        // Use the currentUser directly without encoding
        String jsonResponse = customerPortalApiService.getUserActivities(true, currentUser, startTimeStr,
                endTimeStr);

        // Log the response
        LOGGER.info("Received response: {}", jsonResponse);

        // Write the JSON response to the output
        if (jsonResponse != null) {
            response.getWriter().write(jsonResponse);
        } else {
            LOGGER.error("Failed to retrieve user activities.");
            response.sendError(SlingHttpServletResponse.SC_BAD_REQUEST, "Unable to fetch user activities");
        }
    }
}
