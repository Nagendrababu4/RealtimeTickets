package com.adobe.aem.capstone.core.servlets;

import com.adobe.aem.capstone.core.constants.ConfigurationConstants;
import com.adobe.aem.capstone.core.models.GenericResponse;
import com.adobe.aem.capstone.core.service.CustomerPortalApiService;
import com.day.cq.dam.api.Asset;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.Servlet;
import javax.json.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

@Component(service = { Servlet.class }, property = {
        "sling.servlet.paths=/bin/OrientationChecklist",
        "sling.servlet.methods=GET"
}, immediate = true)

public class OrientationChecklistServlet extends SlingAllMethodsServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrientationChecklistServlet.class);

    @Reference
    private CustomerPortalApiService customerPortalApiService;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        String currentUser = request.getResourceResolver().getUserID();
        response.setContentType(ConfigurationConstants.JSON_CONTENT_TYPE);
        if (StringUtils.isEmpty(currentUser)) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Current user ID is empty.");
            return;
        }
        try {
            handleApiResponse(response, currentUser, request);
        } catch (Exception e) {
            LOGGER.error("Error occurred while fetching orientation checklist: {}", e.getMessage());
            handleFallbackResponse(response, request);
        }
    }

    private void handleApiResponse(SlingHttpServletResponse response, String currentUser,
        SlingHttpServletRequest request) throws IOException {
        GenericResponse apiResponse = customerPortalApiService.getOrientationChecklist(true, currentUser);
        if (apiResponse.getStatusCode() == 200) {
            String checklistResponse = getApiResponse(apiResponse);
            if (StringUtils.isNotEmpty(checklistResponse)) {
                try (JsonReader checklistResponseObj = Json.createReader(new StringReader(checklistResponse))) {
                    JsonObject jsonObject = checklistResponseObj.readObject();
                    response.getWriter().write(jsonObject.toString());
                }
            } else {
                LOGGER.error("API response is empty.");
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "API response is empty.");
            }
        } else {
            handleFallbackResponse(response, request);
        }
    }


    private void handleFallbackResponse(SlingHttpServletResponse response, SlingHttpServletRequest request)
            throws IOException {
        String localJsonData = readLocalJsonData(request, response);
        if (StringUtils.isNotEmpty(localJsonData)) {
            response.getWriter().write(localJsonData);
        } else {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to read local JSON data.");
        }
    }

    private String readLocalJsonData(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        ResourceResolver resolver = request.getResourceResolver();
        Resource jsonResource = resolver.getResource("/content/dam/dcxp/Checklist.json");
        if (jsonResource == null) {
            LOGGER.error("Resource '/content/dam/dcxp/Checklist.json' not found.");
            return null;
        }
        Asset asset = jsonResource.adaptTo(Asset.class);
        if (asset == null) {
            LOGGER.error("Failed to adapt resource to Asset.");
            return null;
        }
        try (InputStream inputStream = asset.getOriginal().getStream();
                JsonReader jsonReader = Json.createReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            JsonObject orientationChecklist = jsonReader.readObject();
            return orientationChecklist.toString();
        } catch (IOException e) {
            LOGGER.error("Error reading local JSON data: {}", e.getMessage());
            return null;
        }
    }

    private String getApiResponse(GenericResponse response) {
        if (response != null && response.getStatusCode() == 200) {
            return response.getRessponse();
        }
        return StringUtils.EMPTY;
    }

    private void sendErrorResponse(SlingHttpServletResponse response, int statusCode, String message)
            throws IOException {
        response.setStatus(statusCode);
        response.getWriter().write(message);
    }
}
