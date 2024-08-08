package com.adobe.aem.capstone.core.service;

public interface CustomerPortalApiService {

    String getNotificationLoggedInUser(boolean retry, String currentUser);

    String getNotificationGuestUser(boolean retry);

    String getUserActivities(boolean retry, String currentUser, String startTime, String endTime);
}
