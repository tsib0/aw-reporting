// Copyright 2013 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.api.ads.adwords.awreporting.server;

import com.google.api.ads.adwords.awreporting.model.persistence.EntityPersister;
import com.google.api.ads.adwords.awreporting.server.reports.ReportAccountRest;
import com.google.api.ads.adwords.awreporting.server.reports.ReportAdExtensionRest;
import com.google.api.ads.adwords.awreporting.server.reports.ReportAdGroupRest;
import com.google.api.ads.adwords.awreporting.server.reports.ReportAdRest;
import com.google.api.ads.adwords.awreporting.server.reports.ReportCampaignNegativeKeywordRest;
import com.google.api.ads.adwords.awreporting.server.reports.ReportCampaignRest;
import com.google.api.ads.adwords.awreporting.server.reports.ReportKeywordRest;
import com.google.api.ads.adwords.awreporting.server.util.StorageHelper;
import com.google.api.ads.adwords.awreporting.util.DynamicPropertyPlaceholderConfigurer;
import com.google.api.client.util.Lists;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.resource.Directory;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.List;

/**
 * RestServer, add the Routing for the REST entry points and
 * initiates the ApplicationContext with the Spring beans
 * 
 * @author jtoledo@google.com (Julian Toledo)
 */
public class RestServer extends Application {

  private static ApplicationContext appCtx;

  private static EntityPersister persister;

  private static StorageHelper storageHelper;

  private static String propertiesFilePath;

  public static ApplicationContext getApplicationContext() {
    if (appCtx == null) {
      synchronized (RestServer.class) {
        if (appCtx == null) {
          initApplicationContextAndProperties();
        }
      }
    }
    return appCtx;
  }

  public static EntityPersister getPersister() {
    if (persister == null || appCtx == null) {
      synchronized (RestServer.class) {
        if (persister == null || appCtx == null) {
          initApplicationContextAndProperties();
        }
      }
    }
    return persister;
  }

  public static StorageHelper getStorageHelper() {
    if (storageHelper == null || appCtx == null) {
      synchronized (RestServer.class) {
        if (storageHelper == null || appCtx == null) {
          initApplicationContextAndProperties();
        }
      }
    }
    return storageHelper;
  }

  public static void createRestServer(String propertiesPath, int port) throws Exception {

    propertiesFilePath = propertiesPath;

    // Create a component
    Component component = new Component();
    component.getServers().add(Protocol.HTTP, port);
    component.getClients().add(Protocol.FILE);

    Context context = component.getContext().createChildContext();
    RestServer application = new RestServer(context);
    
    application.getContext().getParameters().add("useForwardedForHeader", "true");

    // Attach the application to the component and start it
    component.getDefaultHost().attach(application);
    component.start();
  }

  private RestServer(Context context) {
    super(context);
  }

  public synchronized Restlet createInboundRoot() {
    Router router = new Router(getContext());

    // Accounts
    // ?dateStart=yyyyMMdd&dateEnd=yyyyMMdd
    // dateRangeType=DAY or dateRangeType=MONTH
    router.attach("/mcc/{topAccountId}/reportaccount", ReportAccountRest.class); //LIST All
    router.attach("/mcc/{topAccountId}/reportaccount/{accountId}", ReportAccountRest.class); //LIST Account level
    
    // Campaigns
    // ?dateStart=yyyyMMdd&dateEnd=yyyyMMdd
    // dateRangeType=DAY or dateRangeType=MONTH
    router.attach("/mcc/{topAccountId}/reportcampaign", ReportCampaignRest.class); //LIST All
    router.attach("/mcc/{topAccountId}/reportcampaign/{accountId}", ReportCampaignRest.class); //LIST Account level
    router.attach("/mcc/{topAccountId}/reportcampaign/campaign/{campaignId}", ReportCampaignRest.class); //LIST Campaign level

    // AdGroups
    // ?dateStart=yyyyMMdd&dateEnd=yyyyMMdd
    // dateRangeType=DAY or dateRangeType=MONTH
    router.attach("/mcc/{topAccountId}/reportadgroup", ReportAdGroupRest.class); //LIST All
    router.attach("/mcc/{topAccountId}/reportadgroup/{accountId}", ReportAdGroupRest.class); //LIST Account level
    router.attach("/mcc/{topAccountId}/reportadgroup/campaign/{campaignId}", ReportAdGroupRest.class); //LIST Campaign level
    router.attach("/mcc/{topAccountId}/reportadgroup/adgroup/{adGroupId}", ReportAdGroupRest.class); //LIST AdGroup level

    // Ads
    // ?dateStart=yyyyMMdd&dateEnd=yyyyMMdd
    // dateRangeType=DAY or dateRangeType=MONTH
    router.attach("/mcc/{topAccountId}/reportad", ReportAdRest.class); //LIST All
    router.attach("/mcc/{topAccountId}/reportad/{accountId}", ReportAdRest.class); //LIST Account level
    router.attach("/mcc/{topAccountId}/reportad/campaign/{campaignId}", ReportAdRest.class); //LIST Campaign level
    router.attach("/mcc/{topAccountId}/reportad/adgroup/{adGroupId}", ReportAdRest.class); //LIST AdGroup level
    router.attach("/mcc/{topAccountId}/reportad/ad/{adId}", ReportAdRest.class); //LIST Ad level

    // Keywords
    // ?dateStart=yyyyMMdd&dateEnd=yyyyMMdd
    // dateRangeType=DAY or dateRangeType=MONTH
    router.attach("/mcc/{topAccountId}/reportkeyword", ReportKeywordRest.class); //LIST All
    router.attach("/mcc/{topAccountId}/reportkeyword/{accountId}", ReportKeywordRest.class); //LIST Account level
    router.attach("/mcc/{topAccountId}/reportkeyword/campaign/{campaignId}", ReportKeywordRest.class); //LIST Campaign level
    router.attach("/mcc/{topAccountId}/reportkeyword/adgroup/{adGroupId}", ReportKeywordRest.class); //LIST AdGroup level
    router.attach("/mcc/{topAccountId}/reportkeyword/keyword/{criterionId}", ReportKeywordRest.class); //LIST Keyword level

    // ReportCampaignNegativeKeyword
    // This one does not support dateStart and dateEnd
    router.attach("/mcc/{topAccountId}/reportcampaignnegativekeyword", ReportCampaignNegativeKeywordRest.class); //LIST All
    router.attach("/mcc/{topAccountId}/reportcampaignnegativekeyword/{accountId}", ReportCampaignNegativeKeywordRest.class); //LIST Account level
    router.attach("/mcc/{topAccountId}/reportcampaignnegativekeyword/campaign/{campaignId}", ReportCampaignNegativeKeywordRest.class); //LIST Campaign level
    router.attach("/mcc/{topAccountId}/reportcampaignnegativekeyword/keyword/{criterionId}", ReportCampaignNegativeKeywordRest.class); //LIST Keyword level

    // ReportAdExtension
    // ?dateStart=yyyyMMdd&dateEnd=yyyyMMdd
    // dateRangeType=DAY or dateRangeType=MONTH
    router.attach("/mcc/{topAccountId}/reportadextension", ReportAdExtensionRest.class); //LIST All
    router.attach("/mcc/{topAccountId}/reportadextension/{accountId}", ReportAdExtensionRest.class); //LIST Account level
    router.attach("/mcc/{topAccountId}/reportadextension/campaign/{campaignId}", ReportAdExtensionRest.class); //LIST Campaign level
    router.attach("/mcc/{topAccountId}/reportadextension/adextension/{adExtensionId}", ReportAdExtensionRest.class); //LIST Keyword level

    // *** Static files *** 
    // USING FILE
    String target = "index.html";
    Redirector redirector = new Redirector(getContext(), target, Redirector.MODE_CLIENT_FOUND);
    router.attach("/", redirector);
    File currentPath = new File(RestServer.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    String htmlPath = "file:///" + currentPath.getParent() + "/html/";
    router.attach("/", redirector);
    router.attach("", new Directory(getContext(), htmlPath));

    return router;
  }

  /**
   * Initialize the application context, adding the properties configuration file depending on the
   * specified path.
   */
  protected synchronized static void initApplicationContextAndProperties() {

    Resource resource = new ClassPathResource(propertiesFilePath);
    if (!resource.exists()) {
      resource = new FileSystemResource(propertiesFilePath);
    }
    DynamicPropertyPlaceholderConfigurer.setDynamicResource(resource);

    // Selecting the XMLs to choose the Spring Beans to load.
    List<String> listOfClassPathXml = Lists.newArrayList();
    listOfClassPathXml.add("classpath:storage-helper-beans.xml");
    listOfClassPathXml.add("classpath:aw-report-sql-beans.xml");
    appCtx = new ClassPathXmlApplicationContext(listOfClassPathXml.toArray(new String[listOfClassPathXml.size()]));

    persister = appCtx.getBean(EntityPersister.class);

    storageHelper = getApplicationContext().getBean(StorageHelper.class);
  }
}
