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
import com.google.api.ads.adwords.awreporting.server.appengine.rest.GaeAbstractServerResource;
import com.google.api.ads.adwords.awreporting.server.appengine.util.DateUtility;
import com.google.api.ads.adwords.awreporting.server.entities.HtmlTemplate;

import org.restlet.data.Status;
import org.restlet.representation.Representation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Rest entry point to launch Report Export tasks
 * 
 * @author joeltoby@google.com (Joel Toby)
 * @author jtoledo@google.com (Julian Toledo)
 */
public class PreviewReportRest extends GaeAbstractServerResource {

  /* (non-Javadoc)
   * @see com.google.api.ads.adwords.awreporting.appengine.rest.AbstractServerResource#getHandler()
   */
  @Override
  public Representation getHandler() {
    String result = null;

    try {
      checkAuthentication();
      getParameters();

      String dateStart;
      String dateEnd;

      if (templateId != null) {

        // Validate templateId
        Map<String, Object> propertiesMap = new HashMap<String, Object>();
        propertiesMap.put(HtmlTemplate.USER_ID, userId);
        propertiesMap.put(HtmlTemplate.ID, templateId);

        List<HtmlTemplate> htmlTemplateList = RestServer.getPersister().get(
            HtmlTemplate.class, propertiesMap);

        if(htmlTemplateList.isEmpty()) {
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
        if(monthStart == null)
          LOGGER.info("MONTH START = NULL");
        if(monthEnd == null)
          LOGGER.info("MONTH END = NULL");
        if(monthStart == null || monthEnd == null) {

          // Set date range to last month if dates are not provided
          LOGGER.info("Report date range not provided.  Defaulting to last month.");

          dateStart = DateUtil.formatYearMonthDayNoDash(DateUtility.firstDayPreviousMonth());
          dateEnd = DateUtil.formatYearMonthDayNoDash(DateUtility.lastDayPreviousMonth());

        } else {
          dateStart = DateUtil.formatYearMonthDayNoDash(
              DateUtility.firstDayMonth(dfYearMonthNoDash.parseDateTime(monthStart)));
          dateEnd = DateUtil.formatYearMonthDayNoDash(
              DateUtility.lastDayMonth(dfYearMonthNoDash.parseDateTime(monthEnd)));
        }

        Properties properties = RestServer.getProperties();
        ReportExporterAppEngine reportExporterAppEngine = createReportExporter();

        // Export One Report in HTML or PDF
        if (topAccountId != null && accountId != null) {
          
          // Check that the user owns that MCC
          checkAuthentication(topAccountId);

          LOGGER.info(" Export Report Task for account for " + accountId +
              " using templateId: #" + templateId + ". Report date range: " +
              dateStart + " - " + dateEnd);

          if (reportType != null && reportType.equals("pdf")) {
            // PDF
            byte[] pdfContent = reportExporterAppEngine.getReportPdf(accountId, properties, templateId, dateStart, dateEnd);
            return createPdfResult(pdfContent);

          } else {
            // HTML (default)
            result = reportExporterAppEngine.getReportHtml(accountId, properties, templateId, dateStart, dateEnd);
            return createHtmlResult(result);
          }
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
