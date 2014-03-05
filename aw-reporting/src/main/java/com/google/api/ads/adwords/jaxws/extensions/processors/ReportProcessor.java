// Copyright 2012 Google Inc. All Rights Reserved.
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

package com.google.api.ads.adwords.jaxws.extensions.processors;

import com.google.api.ads.adwords.jaxws.extensions.authentication.Authenticator;
import com.google.api.ads.adwords.jaxws.extensions.report.model.csv.CsvReportEntitiesMapping;
import com.google.api.ads.adwords.jaxws.extensions.report.model.entities.NameImprClicks;
import com.google.api.ads.adwords.jaxws.extensions.report.model.entities.Report;
import com.google.api.ads.adwords.jaxws.extensions.report.model.entities.ReportPlaceholderFeedItem;
import com.google.api.ads.adwords.jaxws.extensions.report.model.persistence.EntityPersister;
import com.google.api.ads.adwords.jaxws.extensions.report.model.util.DateUtil;
import com.google.api.ads.adwords.jaxws.extensions.util.HTMLExporter;
import com.google.api.ads.adwords.jaxws.extensions.util.ManagedCustomerDelegate;
import com.google.api.ads.adwords.jaxws.v201309.mcm.ApiException;
import com.google.api.ads.adwords.jaxws.v201309.mcm.ManagedCustomer;
import com.google.api.ads.adwords.lib.jaxb.v201309.DateRange;
import com.google.api.ads.adwords.lib.jaxb.v201309.DownloadFormat;
import com.google.api.ads.adwords.lib.jaxb.v201309.ReportDefinition;
import com.google.api.ads.adwords.lib.jaxb.v201309.ReportDefinitionDateRangeType;
import com.google.api.ads.adwords.lib.jaxb.v201309.ReportDefinitionReportType;
import com.google.api.ads.adwords.lib.jaxb.v201309.Selector;
import com.google.api.client.util.Maps;
import com.google.api.client.util.Sets;
import com.google.common.collect.Lists;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Reporting processor, responsible for downloading and saving the files to
 * the file system. The persistence of the parsed beans is delegated to the
 * configured persister.
 * 
 * @author jtoledo@google.com (Julian Toledo)
 * @author gustavomoreira@google.com (Gustavo Moreira)
 */

public abstract class ReportProcessor {

  private static final Logger LOGGER = Logger
      .getLogger(ReportProcessor.class);

  private static final int REPORT_BUFFER_DB = 1000;
  private static final int NUMBER_OF_REPORT_PROCESSORS = 20;

  protected static final String REPORT_PREFIX = "AwReporting-";

  protected CsvReportEntitiesMapping csvReportEntitiesMapping;

  protected EntityPersister persister;

  protected Authenticator authenticator;

  protected String mccAccountId = null;

  protected int reportRowsSetSize = REPORT_BUFFER_DB;

  protected int numberOfReportProcessors = NUMBER_OF_REPORT_PROCESSORS;

  abstract protected void cacheAccounts(Set<Long> accountIdsSet);

  abstract public void generateReportsForMCC(
      ReportDefinitionDateRangeType dateRangeType, String dateStart,
      String dateEnd, Set<Long> accountIdsSet, Properties properties)
          throws Exception;

  /**
   * Uses the API to retrieve the managed accounts, and extract their IDs.
   * 
   * @return the account IDs for all the managed accounts.
   * @throws Exception
   *             error reading the API.
   */
  public Set<Long> retrieveAccountIds() throws Exception {

    Set<Long> accountIdsSet = Sets.newHashSet();
    try {

      LOGGER.info("Account IDs being recovered from the API. This may take a while...");
      accountIdsSet = new ManagedCustomerDelegate(
          authenticator.authenticate(mccAccountId, false).build()).getAccountIds();

    } catch (ApiException e) {
      if (e.getMessage().contains("AuthenticationError")) {

        // retries Auth once for expired Tokens
        LOGGER.info("AuthenticationError, Getting a new Token...");
        LOGGER.info("Account IDs being recovered from the API. This may take a while...");
        accountIdsSet = new ManagedCustomerDelegate(
            authenticator.authenticate(mccAccountId, true).build()).getAccountIds();

      } else {
        LOGGER.error("API error: " + e.getMessage());
        e.printStackTrace();
        throw e;
      }
    }

    this.cacheAccounts(accountIdsSet);

    return accountIdsSet;
  }

  /**
   * Uses the API to retrieve the managed accounts, and extract their IDs.
   * 
   * @return the account IDs for all the managed accounts.
   * @throws Exception
   *             error reading the API.
   */
  public List<ManagedCustomer> getAccounts() throws Exception {

    List<ManagedCustomer> accounts = Lists.newArrayList();
    try {
      accounts = new ManagedCustomerDelegate(
          authenticator.authenticate(mccAccountId, false).build()).getAccounts();
    } catch (ApiException e) {
      if (e.getMessage().contains("AuthenticationError")) {
        // retries Auth once for expired Tokens
        LOGGER.info("AuthenticationError, Getting a new Token...");
        accounts = new ManagedCustomerDelegate(
            authenticator.authenticate(mccAccountId, true).build()).getAccounts();
      } else {
        LOGGER.error("API error: " + e.getMessage());
        e.printStackTrace();
        throw e;
      }
    }
    return accounts;
  }

  /**
   * Generates the PDF files from the report data
   *
   * @param dateStart the start date for the reports
   * @param dateEnd the end date for the reports
   * @param properties the properties file containing all the configuration
   * @throws Exception error creating PDF
   */
  public void generatePdf(String dateStart, String dateEnd, Properties properties,
      File htmlTemplateFile, File outputDirectory, boolean sumAdExtensions) throws Exception {

    LOGGER.info("Starting PDF Generation");
    Map<String, Object> reportMap = Maps.newHashMap();

    for (Long accountId : retrieveAccountIds()) {
      LOGGER.debug("Retrieving monthly reports for account: " + accountId);

      Set<ReportDefinitionReportType> reports = this.csvReportEntitiesMapping.getDefinedReports();
      for (ReportDefinitionReportType reportType : reports) {
        if (properties.containsKey(reportType.name())) {
          // Adding each report type rows from DB to the accounts montlyeports list.

          List<Report> monthlyReports = Lists.newArrayList(persister.listMonthReports(
              csvReportEntitiesMapping.getReportBeanClass(reportType), accountId,
              DateUtil.parseDateTime(dateStart), DateUtil.parseDateTime(dateEnd)));

          if (sumAdExtensions && reportType.name() == "PLACEHOLDER_FEED_ITEM_REPORT") {
            Map<String, NameImprClicks> adExtensionsMap = new HashMap<String, NameImprClicks>();
            int sitelinks = 0;
            for (Report report : monthlyReports) {
              String clickType = ((ReportPlaceholderFeedItem) report).getClickType();
              Long impressions = ((ReportPlaceholderFeedItem) report).getImpressions();
              Long clicks = ((ReportPlaceholderFeedItem) report).getClicks();
              if (!clickType.equals("Headline")) {
                if (clickType.equals("Sitelink")) {
                  sitelinks++;
                }
                if (adExtensionsMap.containsKey(clickType)) {
                  NameImprClicks oldValues = adExtensionsMap.get(clickType);
                  oldValues.impressions += impressions;
                  oldValues.clicks += clicks;
                  adExtensionsMap.put(clickType, oldValues);
                } else {
                  NameImprClicks values = new NameImprClicks(); 
                  values.impressions = impressions;
                  values.clicks = clicks;
                  adExtensionsMap.put(clickType, values);
                }
              }
            }

            List<NameImprClicks> adExtensions = new ArrayList<NameImprClicks>();
            for (Map.Entry<String, NameImprClicks> entry : adExtensionsMap.entrySet()) { 
              NameImprClicks nic = new NameImprClicks();
              nic.clickType = entry.getKey();
              if (nic.clickType.equals("Sitelink")) {
                nic.clickType = "Sitelinks (x" + sitelinks + ")";
              }
              nic.clicks = entry.getValue().clicks;
              nic.impressions = entry.getValue().impressions;
              adExtensions.add(nic);
            }
            reportMap.put("ADEXTENSIONS", adExtensions);
          }

          reportMap.put(reportType.name(), monthlyReports);
        }
      }

      if (reportMap != null && reportMap.size() > 0) {

        File htmlFile = new File(outputDirectory,
            "Report_" + accountId + "_" + dateStart + "_" + dateEnd + ".html");
        File pdfFile = new File(outputDirectory,
            "Report_" + accountId + "_" + dateStart +  "_" + dateEnd + ".pdf");

        LOGGER.debug("Exporting monthly reports to HTML for account: " + accountId);
        HTMLExporter.exportHTML(reportMap, htmlTemplateFile, htmlFile);

        LOGGER.debug("Converting HTML to PDF for account: " + accountId);
        HTMLExporter.convertHTMLtoPDF(htmlFile, pdfFile);
      }
    }
  }

  /**
   * Creates the complete report definition with all the dates and types
   * properly set.
   * 
   * @param reportDefinitionReportType
   *            the report type.
   * @param dateRangeType
   *            the date range type.
   * @param dateStart
   *            the initial date.
   * @param dateEnd
   *            the ending date.
   * @param properties
   *            the properties resource.
   * @return the {@code ReportDefinition} instance.
   */
  protected ReportDefinition getReportDefinition(
      ReportDefinitionReportType reportDefinitionReportType,
      ReportDefinitionDateRangeType dateRangeType, String dateStart,
      String dateEnd, Properties properties) {

    // Create the Selector with all the fields defined in the Mapping
    Selector selector = new Selector();

    List<String> reportFields = this.csvReportEntitiesMapping
        .retrievePropertiesToSelect(reportDefinitionReportType);

    // Add the inclusions from the properties file
    List<String> reportInclusions = this.getReportInclusions(
        reportDefinitionReportType, properties);
    for (String reportField : reportFields) {
      if (reportInclusions.contains(reportField)) {
        selector.getFields().add(reportField);
      }
    }
    this.adjustDateRange(reportDefinitionReportType, dateRangeType,
        dateStart, dateEnd, selector);

    return this.instantiateReportDefinition(reportDefinitionReportType,
        dateRangeType, selector);
  }

  /**
   * Adjusts the date range in case of a custom date type. The adjustment do
   * not apply for the {@code CAMPAIGN_NEGATIVE_KEYWORDS_PERFORMANCE_REPORT}.
   * 
   * @param reportDefinitionReportType
   *            the report type.
   * @param dateRangeType
   *            the date range type.
   * @param dateStart
   *            the start.
   * @param dateEnd
   *            the end.
   * @param selector
   *            the selector with the properties.
   */
  protected void adjustDateRange(
      ReportDefinitionReportType reportDefinitionReportType,
      ReportDefinitionDateRangeType dateRangeType, String dateStart,
      String dateEnd, Selector selector) {

    if (dateRangeType.equals(ReportDefinitionDateRangeType.CUSTOM_DATE)
        && !reportDefinitionReportType
        .equals(ReportDefinitionReportType.CAMPAIGN_NEGATIVE_KEYWORDS_PERFORMANCE_REPORT)) {
      DateRange dateRange = new DateRange();
      dateRange.setMin(dateStart);
      dateRange.setMax(dateEnd);
      selector.setDateRange(dateRange);
    }
  }

  /**
   * Instantiates the report definition, setting all the correct formats.
   * 
   * @param reportDefinitionReportType
   *            the report definition type.
   * @param dateRangeType
   *            the date range type.
   * @param selector
   *            the selector containing the report properties.
   * @return the {@code ReportDefinition}
   */
  protected ReportDefinition instantiateReportDefinition(
      ReportDefinitionReportType reportDefinitionReportType,
      ReportDefinitionDateRangeType dateRangeType, Selector selector) {

    // Create the Report Definition
    ReportDefinition reportDefinition = new ReportDefinition();
    reportDefinition.setReportName(REPORT_PREFIX
        + reportDefinitionReportType.value() + " "
        + System.currentTimeMillis());
    reportDefinition.setDateRangeType(dateRangeType);
    reportDefinition.setReportType(reportDefinitionReportType);
    reportDefinition.setDownloadFormat(DownloadFormat.GZIPPED_CSV);
    reportDefinition.setIncludeZeroImpressions(false);
    reportDefinition.setSelector(selector);
    return reportDefinition;
  }

  /**
   * Look for properties to include in the reports.
   * 
   * @param reportType
   *            the report type.
   * @param properties
   *            the resource properties.
   * @return the list of properties that should be included in the report.
   */
  protected List<String> getReportInclusions(
      ReportDefinitionReportType reportType, Properties properties) {

    String propertyInclusions = properties.getProperty(reportType.name());

    if (propertyInclusions != null && propertyInclusions.length() > 0) {
      String[] inclusions = propertyInclusions.split(",");
      List<String> inclusionsList = Lists
          .newArrayListWithCapacity(inclusions.length);
      for (int i = 0; i < inclusions.length; i++) {
        inclusionsList.add(inclusions[i].trim());
      }
      return inclusionsList;
    }
    return Lists.newArrayListWithCapacity(0);
  }

  /**
   * @param csvReportEntitiesMapping
   *            the csvReportEntitiesMapping to set
   */
  @Autowired
  public void setCsvReportEntitiesMapping(
      CsvReportEntitiesMapping csvReportEntitiesMapping) {
    this.csvReportEntitiesMapping = csvReportEntitiesMapping;
  }

  /**
   * @param persister
   *            the persister to set
   */
  @Autowired
  public void setPersister(EntityPersister persister) {
    this.persister = persister;
  }

  /**
   * @param authentication
   *            the helper class for Auth
   */
  @Autowired
  public void setAuthentication(Authenticator authenticator) {
    this.authenticator = authenticator;
  }
}
