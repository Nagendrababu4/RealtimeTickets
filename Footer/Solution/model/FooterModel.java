package com.adobe.aem.capstone.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import javax.annotation.PostConstruct;
import java.util.Calendar;
import java.util.List;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class FooterModel {

    @ValueMapValue
    private String logoPath;

    @ValueMapValue
    private String logoLink;

    @ValueMapValue
    private String logoAltText;

    @ChildResource
    private List<FooterListModel> footerList;

    @ValueMapValue
    private String description;

    @ValueMapValue
    private int modificationYear;

    private String copyrightText;

    @PostConstruct
    protected void init() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        if (modificationYear == 0) {
            modificationYear = currentYear;
        }
        if (currentYear == modificationYear) {
            copyrightText = "© Copyright " + modificationYear + " BMC Software, Inc.";
        } else if (currentYear < modificationYear) {
            int nextYear = modificationYear + 1;
            copyrightText = "© Copyright " + currentYear + ", " + modificationYear + " - " + nextYear
                    + " BMC Software, Inc.";
        } else {
            copyrightText = "© Copyright " + modificationYear + "-" + currentYear + " BMC Software, Inc.";
        }
    }

    public String getLogoPath() {
        return logoPath;
    }

    public String getLogoLink() {
        return logoLink;
    }

    public String getLogoAltText() {
        return logoAltText;
    }

    public List<FooterListModel> getFooterList() {
        return footerList;
    }

    public String getDescription() {
        return description;
    }

    public String getCopyrightText() {
        return copyrightText;
    }

    public int getModificationYear() {
        return modificationYear;
    }
}
