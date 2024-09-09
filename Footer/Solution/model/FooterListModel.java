package com.adobe.aem.capstone.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(adaptables = Resource.class)
public class FooterListModel {

    @ValueMapValue(name = "linkLabel")
    private String linkLabel;

    @ValueMapValue(name = "navigationUrl")
    private String navigationUrl;

    public String getLinkLabel() {
        return linkLabel;
    }

    public String getNavigationUrl() {
        return navigationUrl;
    }
}