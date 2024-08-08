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

    // OrientationChecklist

    @Override
    public GenericResponse getOrientationChecklist(boolean retry, String currentUser) {
        if (currentUser == null || currentUser.isEmpty()) {
            return new GenericResponse(400, StringUtils.EMPTY, retry);
        }

        String user_id = currentUser;
        String userActivitiesApiUrl = configs.orientationChecklistEndPoint() + ConfigurationConstants.USER_ID_1
                + user_id;

        if (userActivitiesApiUrl.startsWith("https://webapi")) {
            String accessToken = getAccessTokenForWebAPI();
            if (accessToken != null) {
                userActivitiesApiUrl = userActivitiesApiUrl += "?access_token=" + accessToken;
                return WebApiServiceGateway(userActivitiesApiUrl, true, retry, null, HttpConstants.METHOD_GET,
                        accessToken);
            } else {
                return new GenericResponse(500, "Failed to get access token", retry);
            }
        } else {
            return apiServiceGateway(userActivitiesApiUrl, true, retry, null, HttpConstants.METHOD_GET);
        }
        // For egwqa or other endpoints, use the existing routine
    }

    private String getAccessTokenForWebAPI() {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(30 * 1000)
                .setConnectionRequestTimeout(30 * 1000)
                .setSocketTimeout(30 * 1000)
                .build();

        String accessToken = null;
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build()) {
            String apiPath = "https://webapiqa.bmc.com/personalisation/oauth/token";
            String oauthApiUser = "authprovider";
            String oauthApiPass = "bmcT0k3n";
            String apiUser = "bmc";
            String apiPass = "d$0509v";
            StringBuilder apiUrl = new StringBuilder();
            apiUrl.append(apiPath)
                    .append("?grant_type=password&password=").append(URLEncoder.encode(apiPass, "UTF-8"))
                    .append("&username=").append(apiUser)
                    .append("&parameters=none");
            String encoding = DatatypeConverter
                    .printBase64Binary((oauthApiUser + ":" + oauthApiPass).getBytes("UTF-8"));
            HttpPost httpPost = new HttpPost(apiUrl.toString());
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
            LOGGER.info("Executing request to fetch access token " + httpPost.getRequestLine());
            HttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String jsonString = EntityUtils.toString(response.getEntity());
                com.google.gson.JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
                if (jsonObject.has("access_token")) {
                    accessToken = jsonObject.get("access_token").getAsString();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception in getting AccessToken ", e);
        }
        return accessToken;
    }

    // CaseManagement

    @Override
    public GenericResponse getCaseManagementSection(boolean retry, String currentUser) {
        if (currentUser == null && currentUser.isEmpty()) {
            return new GenericResponse(400, StringUtils.EMPTY, retry);
        }
        String user_id = currentUser;
        String userActivitiesApiUrl = configs.orientationChecklistEndPoint() + ConfigurationConstants.USER_ID_1
                + user_id;
        return apiServiceGateway(userActivitiesApiUrl, true, retry, null, HttpConstants.METHOD_GET);
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

    public GenericResponse WebApiServiceGateway(String url, boolean token, boolean retry, List<NameValuePair> data,
            String method, String accessToken) {
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
            if (accessToken != null) {
                requestBase.setHeader(ConfigurationConstants.AUTHORIZATION,
                        ConfigurationConstants.BEARER + " " + accessToken);
            }
            if (data != null) {
                ((HttpPost) requestBase).setEntity(new UrlEncodedFormEntity(data));
            }
            try (CloseableHttpResponse response = client.execute(requestBase)) {
                HttpEntity entity = response.getEntity();

                if (response.getStatusLine().getStatusCode() == 401 && retry) {
                    getAccessTokenForWebAPI();
                    return WebApiServiceGateway(url, token, false, data, method, accessToken);
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