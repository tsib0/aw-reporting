//Copyright 2014 Google Inc. All Rights Reserved.
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.api.ads.adwords.awreporting.server.appengine.rest.reporting;

import com.google.api.ads.adwords.awreporting.exporter.ReportExporter;
import com.google.api.ads.adwords.awreporting.model.util.DateUtil;
import com.google.api.ads.adwords.awreporting.server.appengine.RestServer;
import com.google.api.ads.adwords.awreporting.server.appengine.exporter.ReportExporterAppEngine;
import com.google.api.ads.adwords.awreporting.server.entities.Account;
import com.google.api.ads.adwords.awreporting.server.entities.HtmlTemplate;
import com.google.api.ads.adwords.awreporting.server.rest.AbstractBaseResource;
import com.google.common.collect.Sets;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Rest entry point to launch Report Export tasks
 * 
 * @author joeltoby@google.com (Joel Toby)
 * @author jtoledo@google.com (Julian Toledo)
 */
public class ExportReportsRest extends AbstractBaseResource {
  
  private static final String DATE_FORMAT_SHORT_WITHOUTDAY = "yyyyMM";
  protected static final DateTimeFormatter dfYearMonthNoDash = DateTimeFormat.forPattern(DATE_FORMAT_SHORT_WITHOUTDAY);

  /* (non-Javadoc)
   * @see com.google.api.ads.adwords.awreporting.appengine.rest.AbstractServerResource#getHandler()
   */
  @Override
  public Representation getHandler() {
    String result = null;

    try {
      RestServer.getWebAuthenticator().checkAuthentication();
      
      Long topAccountId = getParameterAsLong("topAccountId");
      Long accountId = getParameterAsLong("accountId");
      Long templateId = getParameterAsLong("templateId");
      String userId = RestServer.getWebAuthenticator().getCurrentUser();
      
      String monthStart = getParameter("monthStart");
      String monthEnd = getParameter("monthEnd");

      String dateStart;
      String dateEnd;

      if (templateId != null) {

        // Validate templateId
        Map<String, Object> propertiesMap = new HashMap<String, Object>();
        propertiesMap.put(HtmlTemplate.USER_ID, userId);
        propertiesMap.put(HtmlTemplate.ID, templateId);

        List<HtmlTemplate> htmlTemplateList = RestServer.getPersister().get(
            HtmlTemplate.class, propertiesMap);

        if (htmlTemplateList.isEmpty()) {
          // Check 'public' templates
          Map<String, Object> publicPropertiesMap = new HashMap<String, Object>();
          publicPropertiesMap.put("isPublic", true);
          publicPropertiesMap.put(HtmlTemplate.ID, templateId);
          htmlTemplateList = RestServer.getPersister().get(HtmlTemplate.class, publicPropertiesMap);

          if(htmlTemplateList.isEmpty()) {
            this.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            throw new IllegalArgumentException("Invalid templateId");
          }
        }

        // Export Reports uses YYYYMM month format and converts it to the 
        // YYYYMMDD with the first and last day of the month.
        if (monthStart == null)
          LOGGER.info("MONTH START = NULL");
        if (monthEnd == null)
          LOGGER.info("MONTH END = NULL");
        if (monthStart == null || monthEnd == null) {

          // Set date range to last month if dates are not provided
          LOGGER.info("Report date range not provided.  Defaulting to last month.");

          dateStart = DateUtil.formatYearMonthDayNoDash(DateUtil.firstDayPreviousMonth());
          dateEnd = DateUtil.formatYearMonthDayNoDash(DateUtil.lastDayPreviousMonth());

        } else {
          dateStart = DateUtil.formatYearMonthDayNoDash(
              DateUtil.firstDayMonth(dfYearMonthNoDash.parseDateTime(monthStart)));
          dateEnd = DateUtil.formatYearMonthDayNoDash(
              DateUtil.lastDayMonth(dfYearMonthNoDash.parseDateTime(monthEnd)));
        }

        Properties properties = RestServer.getProperties();

        File outputDirectory = new File("");
        boolean sumAdExtensions = false;
        ReportExporterAppEngine reportExporterAppEngine = createReportExporter();

        // Create Export Report Task for MCC
        if (topAccountId != null && accountId == null) {

          // Check that the user owns that MCC
          RestServer.getWebAuthenticator().checkAuthentication(topAccountId);

          LOGGER.info(" Export Report Task for MCC for " + topAccountId +
              " using templateId: #" + templateId + ". Report date range: " + 
              dateStart + " - " + dateEnd);
          
          // Getting accounts Ids from DB
          Set<Long> accountIds = Sets.newHashSet();
          List<Account> accounts = RestServer.getPersister().get(Account.class, Account.TOP_ACCOUNT_ID, topAccountId);
          for (Account account : accounts) {
            accountIds.add(Long.valueOf(account.getId()));
          }

          reportExporterAppEngine.exportReports(String.valueOf(topAccountId), dateStart, dateEnd,
              accountIds, properties, templateId, outputDirectory, sumAdExtensions);
          
          // Hard coded Drive URL pending Drive bug fix.
          result = "https://drive.google.com";
        }

        // Export One Report in HTML
        if (topAccountId != null && accountId != null) {

          // Check that the user owns that MCC
          RestServer.getWebAuthenticator().checkAuthentication(topAccountId);

          LOGGER.info(" Export Report Task for account for " + accountId +
              " using templateId: #" + templateId + ". Report date range: " +
              dateStart + " - " + dateEnd);

          reportExporterAppEngine.exportReports(String.valueOf(topAccountId), dateStart, dateEnd,
              Sets.newHashSet(accountId), properties, templateId, outputDirectory, sumAdExtensions);

          return createJsonResult("OK");
        }
      } else {
        throw new IllegalArgumentException("Missing templateId parameter");
      }

    } catch (Exception exception) {
      return handleException(exception);
    }
    addReadOnlyHeaders();
    return createJsonResult(result);
  }

  /**
   * Creates a {@link ReportExporter} autowiring all the dependencies.
   *
   * @return the {@code ReportExporter} with all the dependencies properly injected.
   */
  private static ReportExporterAppEngine createReportExporter() {

    return RestServer.getApplicationContext().getBean(ReportExporterAppEngine.class);
  }
}
