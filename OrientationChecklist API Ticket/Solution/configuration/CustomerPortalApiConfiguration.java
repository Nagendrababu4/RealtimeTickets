package com.adobe.aem.capstone.core.service.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Customer Portal API Configuration", description = "Configuration for Customer Portal API")
public @interface CustomerPortalApiConfiguration {

    @AttributeDefinition(name = "Token API", description = "Endpoint for token API")
    String tokenApi();

    @AttributeDefinition(name = "Client Id", description = "Client ID for authentication")
    String clientId();

    @AttributeDefinition(name = "Client Token Secret Key", description = "Secret key for client token")
    String secretKey();

    @AttributeDefinition(name = "Grant Type", description = "Grant type for authentication")
    String grantType();

    @AttributeDefinition(name = "Scope", description = "Scope for authentication")
    String scope();

    @AttributeDefinition(name = "Username", description = "Username for authentication")
    String username();

    @AttributeDefinition(name = "Password", description = "Password for authentication")
    String password();

    @AttributeDefinition(name = "Auth Method", description = "Authentication Method (client_credentials/password)")
    String authMethod();

    @AttributeDefinition(name = "Profile Image API", description = "Endpoint for profile image API")
    String profileImageApi();

    @AttributeDefinition(name = "Notification Guest User API", description = "Endpoint for notification guest user API")
    String notificationGuestuserApi();

    @AttributeDefinition(name = "Notification LoggedIn User API", description = "Endpoint for notification logged-in user API")
    String notificationLoggedInUserApi();

    @AttributeDefinition(name = "Activities end point", description = "Endpoint for notification logged-in user API")
    String activitiesEndPoint();

    @AttributeDefinition(name = "Orientation Checklist", description = "Endpoint for Orientation Checklist")
    String orientationChecklistEndPoint();
}