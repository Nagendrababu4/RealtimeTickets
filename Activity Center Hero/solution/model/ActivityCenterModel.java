package com.adobe.aem.capstone.core.models;

import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ActivityCenterModel {

    @ValueMapValue
    private String heroTitle;

    @ValueMapValue
    private String heroDescription;

    @ValueMapValue
    private String heroImage;

    @ValueMapValue
    private String heroDesktopImage;

    public String getHeroTitle() {
        return heroTitle;
    }

    public String getHeroDescription() {
        return heroDescription;
    }

    public String getHeroImage() {
        return heroImage;
    }

    public String getHeroDesktopImage() {
        return heroDesktopImage;
    }
}
