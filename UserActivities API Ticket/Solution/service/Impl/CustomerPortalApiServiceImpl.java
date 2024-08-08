package com.adobe.aem.capstone.core.service.Impl;

import com.adobe.aem.capstone.core.constants.ConfigurationConstants;
import com.adobe.aem.capstone.core.service.CustomerPortalApiService;
import com.adobe.aem.capstone.core.models.GenericResponse;
import com.adobe.aem.capstone.core.service.config.CustomerPortalApiConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.http.HttpHeaders;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import com.google.gson.JsonParser;
import org.slf4j.LoggerFactory;
import javax.json.*;
import javax.xml.bind.DatatypeConverter;
import org.apache.http.HttpResponse;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component(service = CustomerPortalApiService.class)
@Designate(ocd = CustomerPortalApiConfiguration.class)
public class CustomerPortalApiServiceImpl implements CustomerPortalApiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerPortalApiServiceImpl.class);
    private static final String ACCESS_TOKEN = "access_token";
    private static final String EXPIRES_IN = "expires_in";
    private String apiToken;

    private long tokenExpirationTime;

    private CustomerPortalApiConfiguration configs;

    @Activate
    @Modified
    public void activate(CustomerPortalApiConfiguration configuration) {
        this.configs = configuration;
        getApiToken();
    }

    @Override
    public String getNotificationLoggedInUser(boolean retry, String currentUser) {
        if (currentUser == null && currentUser.isEmpty()) {
            return StringUtils.EMPTY;
        }
        String userId = encodeCurrentUser(currentUser);

        String userApiUrl = configs.notificationLoggedInUserApi() + ConfigurationConstants.USER_ID + userId
                + ConfigurationConstants.PLATFORM;
        GenericResponse response = apiServiceGateway(userApiUrl, true, retry, null, HttpConstants.METHOD_GET);
        return getApiResponse(response);
    }

    // UserActivities

    @Override
    public String getUserActivities(boolean retry, String currentUser, String startTime, String endTime) {
        if (currentUser == null && currentUser.isEmpty()) {
            return StringUtils.EMPTY;
        }
        String userId = currentUser;
        String userActivitiesApiUrl = configs.activitiesEndPoint() + ConfigurationConstants.USER_ID + userId
                + ConfigurationConstants.START_TIME + startTime +
                ConfigurationConstants.END_TIME + endTime;
        GenericResponse response = apiServiceGateway(userActivitiesApiUrl, true, retry, null, HttpConstants.METHOD_GET);
        return getApiResponse(response);
    }

    @Override
    public String getNotificationGuestUser(boolean retry) {
        GenericResponse response = apiServiceGateway(configs.notificationLoggedInUserApi(), true, retry, null,
                HttpConstants.METHOD_GET);
        return getApiResponse(response);
    }

    public void getApiToken() {

        List<NameValuePair> data = new ArrayList<>();
        data.add(new BasicNameValuePair(ConfigurationConstants.CLIENT_ID,
                configs.clientId()));
        data.add(new BasicNameValuePair(ConfigurationConstants.CLIENT_SECRET,
                configs.secretKey()));
        data.add(new BasicNameValuePair(ConfigurationConstants.GRANT_TYPE,
                configs.grantType()));
        data.add(new BasicNameValuePair(ConfigurationConstants.SCOPE,
                configs.scope()));

        GenericResponse response = apiServiceGateway(configs.tokenApi(), false,
                false, data, HttpConstants.METHOD_POST);
        if (response != null && response.getStatusCode() == 200) {
            try {
                JsonReader reader = Json.createReader(new StringReader(response.getRessponse()));
                JsonObject responseJson = reader.readObject();
                if (responseJson != null && responseJson.getString(ACCESS_TOKEN) != null) {
                    this.apiToken = responseJson.getString(ACCESS_TOKEN);
                    this.tokenExpirationTime = System.currentTimeMillis()
                            + (responseJson.getInt(EXPIRES_IN) * 60 * 1000L);
                }
            } catch (ParseException e) {
                LOGGER.error("Error while fetch token from API {}", e.getMessage());
            }
        }
    }

    public GenericResponse apiServiceGateway(String url, boolean token, boolean retry, List<NameValuePair> data,
            String method) {
        HttpRequestBase requestBase;
        if (method.equals(HttpConstants.METHOD_GET)) {
            requestBase = new HttpGet(url);
        } else if (method.equals(HttpConstants.METHOD_POST)) {
            requestBase = new HttpPost(url);
        } else {
            LOGGER.error("Unsupported HTTP method: {}", method);
            return null;
        }
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            if (token && hasTokenValid()) {
                requestBase.setHeader(ConfigurationConstants.AUTHORIZATION,
                        ConfigurationConstants.BEARER + " " + this.apiToken);
            }
            if (data != null) {
                ((HttpPost) requestBase).setEntity(new UrlEncodedFormEntity(data));
            }
            try (CloseableHttpResponse response = client.execute(requestBase)) {
                HttpEntity entity = response.getEntity();

                if (response.getStatusLine().getStatusCode() == 401 && retry) {
                    getApiToken();
                    return apiServiceGateway(url, token, false, data, method);
                }
                return new GenericResponse(response.getStatusLine().getStatusCode(), EntityUtils.toString(entity),
                        retry);
            }
        } catch (ParseException | IOException e) {
            LOGGER.error("Error while fetching data from API {}", e.getMessage());
        }
        return null;
    }

    public boolean hasTokenValid() {
        if (System.currentTimeMillis() >= tokenExpirationTime) {
            getApiToken();
        }
        return true;
    }

    private String encodeCurrentUser(String currentUser) {
        try {
            return URLEncoder.encode(currentUser, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return currentUser;
        }
    }

    private String getApiResponse(GenericResponse response) {
        if (response.getRessponse() != null && response.getStatusCode() == 200) {
            return response.getRessponse();
        }
        return StringUtils.EMPTY;
    }
}