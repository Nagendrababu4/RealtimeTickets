package com.adobe.aem.capstone.core.service;

import com.adobe.aem.capstone.core.models.GenericResponse;

public interface CustomerPortalApiService {

    String getNotificationLoggedInUser(boolean retry, String currentUser);

    String getNotificationGuestUser(boolean retry);

    String getUserActivities(boolean retry, String currentUser, String startTime, String endTime);

    public GenericResponse getOrientationChecklist(boolean retry, String currentUser);

    public GenericResponse getCaseManagementSection(boolean retry, String currentUser);
}
