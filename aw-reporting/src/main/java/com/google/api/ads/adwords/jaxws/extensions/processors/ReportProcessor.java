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
import com.google.api.ads.adwords.jaxws.extensions.downloader.MultipleClientReportDownloader;
import com.google.api.ads.adwords.jaxws.extensions.report.model.csv.AnnotationBasedMappingStrategy;
import com.google.api.ads.adwords.jaxws.extensions.report.model.csv.CsvReportEntitiesMapping;
import com.google.api.ads.adwords.jaxws.extensions.report.model.entities.NameImprClicks;
import com.google.api.ads.adwords.jaxws.extensions.report.model.entities.Report;
import com.google.api.ads.adwords.jaxws.extensions.report.model.entities.ReportPlaceholderFeedItem;
import com.google.api.ads.adwords.jaxws.extensions.report.model.persistence.EntityPersister;
import com.google.api.ads.adwords.jaxws.extensions.report.model.util.DateUtil;
import com.google.api.ads.adwords.jaxws.extensions.report.model.util.ModifiedCsvToBean;
import com.google.api.ads.adwords.jaxws.extensions.util.HTMLExporter;
import com.google.api.ads.adwords.jaxws.extensions.util.ManagedCustomerDelegate;
import com.google.api.ads.adwords.jaxws.v201309.mcm.ApiException;
import com.google.api.ads.adwords.lib.client.AdWordsSession;
import com.google.api.ads.adwords.lib.jaxb.v201309.DateRange;
import com.google.api.ads.adwords.lib.jaxb.v201309.DownloadFormat;
import com.google.api.ads.adwords.lib.jaxb.v201309.ReportDefinition;
import com.google.api.ads.adwords.lib.jaxb.v201309.ReportDefinitionDateRangeType;
import com.google.api.ads.adwords.lib.jaxb.v201309.ReportDefinitionReportType;
import com.google.api.ads.adwords.lib.jaxb.v201309.Selector;
import com.google.api.client.util.Maps;
import com.google.api.client.util.Sets;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.bean.MappingStrategy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Main reporting processor responsible for downloading and saving the files to
 * the file system. The persistence of the parsed beans is delegated to the
 * configured persister.
 * 
 * @author jtoledo@google.com (Julian Toledo)
 * @author gustavomoreira@google.com (Gustavo Moreira)
 */
@Component
public class ReportProcessor {

  public static final int REPORT_BUFFER_DB = 1000;

  public static final int NUMBER_OF_REPORT_PROCESSORS = 4;

  private static final Logger LOGGER = Logger
      .getLogger(ReportProcessor.class);

  private static final DateFormat TIMESTAMPFORMAT = new SimpleDateFormat(
      "yyyy-MM-dd-HH_mm");

  private static final String REPORT_PREFIX = "AwReporting-";

  private CsvReportEntitiesMapping csvReportEntitiesMapping;

  private MultipleClientReportDownloader multipleClientReportDownloader;

  private EntityPersister persister;
  
  private Authenticator authenticator;

  private String mccAccountId = null;

  private int reportRowsSetSize = REPORT_BUFFER_DB;
  private int numberOfReportProcessors = NUMBER_OF_REPORT_PROCESSORS;

  /**
   * Constructor.
   * 
   * @param mccAccountId
   *            the MCC account ID
   * @param reportRowsSetSize
   *            the size of the set parsed before send to the DB
   * @param numberOfReportProcessors
   *            the number of numberOfReportProcessors threads to be run
   */
  @Autowired
  public ReportProcessor(
      @Value("${mccAccountId}") String mccAccountId,
      @Value(value = "${aw.report.processor.rows.size:}") Integer reportRowsSetSize,
      @Value(value = "${aw.report.processor.threads:}") Integer numberOfReportProcessors) {

    this.mccAccountId = mccAccountId;

    if (reportRowsSetSize != null && reportRowsSetSize > 0) {
      this.reportRowsSetSize = reportRowsSetSize;
    }
    if (numberOfReportProcessors != null && numberOfReportProcessors > 0) {
      this.numberOfReportProcessors = numberOfReportProcessors;
    }
  }

  private <R extends Report> void processFiles(Class<R> reportBeanClass,
      Collection<File> localFiles,
      ReportDefinitionDateRangeType dateRangeType, String dateStart,
      String dateEnd) {

    final CountDownLatch latch = new CountDownLatch(localFiles.size());
    ExecutorService executorService = Executors
        .newFixedThreadPool(numberOfReportProcessors);

    // Processing Report Local Files
    LOGGER.info(" Procesing reports...");

    Stopwatch stopwatch = Stopwatch.createStarted();

    for (File file : localFiles) {
      LOGGER.trace(".");
      try {

        ModifiedCsvToBean<R> csvToBean = new ModifiedCsvToBean<R>();
        MappingStrategy<R> mappingStrategy = new AnnotationBasedMappingStrategy<R>(
            reportBeanClass);

        LOGGER.debug("Parsing file: " + file.getAbsolutePath());
        RunnableProcessor<R> runnableProcesor = new RunnableProcessor<R>(
            file, csvToBean, mappingStrategy, dateRangeType,
            dateStart, dateEnd, mccAccountId, persister,
            reportRowsSetSize);
        runnableProcesor.setLatch(latch);
        executorService.execute(runnableProcesor);

      } catch (Exception e) {
        LOGGER.error("Ignoring file (Error when processing): "
            + file.getAbsolutePath());
        e.printStackTrace();
      }
    }

    try {
      latch.await();
    } catch (InterruptedException e) {
      LOGGER.error(e.getMessage());
      e.printStackTrace();
    }
    executorService.shutdown();
    stopwatch.stop();
    LOGGER.info("*** Finished processing all reports in "
        + (stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000)
        + " seconds ***\n");
  }

  /**
   * Uses the API to retrieve the managed accounts, and extract their IDs.
   * 
   * @return the account IDs for all the managed accounts.
   * @throws Exception
   *             error reading the API.
   */
  private Set<Long> retrieveAccountIds() throws Exception {

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

    this.cacheAccountsToFile(accountIdsSet);

    return accountIdsSet;
  }

  /**
   * Caches the accounts into a temporary file.
   * 
   * @param accountIdsSet
   *            the set with all the accounts
   */
  private void cacheAccountsToFile(Set<Long> accountIdsSet) {

    DateTime now = new DateTime();
    String nowFormat = TIMESTAMPFORMAT.format(now.toDate());

    try {
      File tempFile = File.createTempFile(nowFormat + "-accounts-ids",
          ".txt");
      LOGGER.info("Cache file created for accounts: "
          + tempFile.getAbsolutePath());

      FileWriter writer = new FileWriter(tempFile);
      for (Long accountId : accountIdsSet) {
        writer.write(Long.toString(accountId) + "\n");
      }
      writer.close();
      LOGGER.info("All account IDs added to cache file.");

    } catch (IOException e) {
      LOGGER.error("Could not create temporary file with the accounts. Accounts won't be cached.");
      e.printStackTrace();
    }
  }

  /**
   * Generate all the mapped reports to the given account IDs.
   * 
   * @param dateRangeType
   *            the date range type.
   * @param dateStart
   *            the starting date.
   * @param dateEnd
   *            the ending date.
   * @param accountIdsSet
   *            the account IDs.
   * @param properties
   *            the properties file
   * @throws Exception
   *             error reaching the API.
   */
  public void generateReportsForMCC(
      ReportDefinitionDateRangeType dateRangeType, String dateStart,
      String dateEnd, Set<Long> accountIdsSet, Properties properties)
          throws Exception {

    LOGGER.info("*** Retrieving account IDs ***");

    if (accountIdsSet == null || accountIdsSet.size() == 0) {
      accountIdsSet = this.retrieveAccountIds();
    } else {
      LOGGER.info("Accounts loaded from file.");
    }

    AdWordsSession.Builder builder = authenticator.authenticate(mccAccountId, false);

    LOGGER.info("*** Generating Reports for " + accountIdsSet.size()
        + " accounts ***");

    Stopwatch stopwatch = Stopwatch.createStarted();

    Set<ReportDefinitionReportType> reports = this.csvReportEntitiesMapping
        .getDefinedReports();

    // reports
    for (ReportDefinitionReportType reportType : reports) {

      if (properties.containsKey(reportType.name())) {
        this.downloadAndProcess(builder, reportType, dateRangeType,
            dateStart, dateEnd, accountIdsSet, properties);
      }
    }

    this.multipleClientReportDownloader.finalizeExecutorService();

    stopwatch.stop();
    LOGGER.info("*** Finished processing all reports in "
        + (stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000)
        + " seconds ***\n");
  }

  /**
   * Downloads all the files from the API and process all the rows, saving the
   * data to the configured data base.
   * 
   * @param builder
   *            the session builder.
   * @param reportType
   *            the report type.
   * @param dateRangeType
   *            the date range type.
   * @param dateStart
   *            the start date.
   * @param dateEnd
   *            the ending date.
   * @param acountIdList
   *            the account IDs.
   * @param properties
   *            the properties resource.
   */
  private <R extends Report> void downloadAndProcess(
      AdWordsSession.Builder builder,
      ReportDefinitionReportType reportType,
      ReportDefinitionDateRangeType dateRangeType, String dateStart,
      String dateEnd, Set<Long> acountIdList, Properties properties) {

    // Download Reports to local files and Generate Report objects
    LOGGER.info("\n\n ** Generating: " + reportType.name() + " **");
    LOGGER.info(" Downloading reports...");
    Collection<File> localFiles = Lists.newArrayList();
    try {

      ReportDefinition reportDefinition = getReportDefinition(reportType,
          dateRangeType, dateStart, dateEnd, properties);

      localFiles = this.multipleClientReportDownloader.downloadReports(
          builder, reportDefinition, acountIdList);

    } catch (InterruptedException e) {
      LOGGER.error(e.getMessage());
      e.printStackTrace();
      return;
    }

    this.processLocalFiles(reportType, localFiles, dateStart, dateEnd,
        dateRangeType);

    this.deleteTemporaryFiles(localFiles, reportType);
  }

  /**
   * Process the local files delegating the call to the concrete
   * implementation.
   * 
   * @param reportType
   *            the report type.
   * @param localFiles
   *            the local files.
   * @param dateStart
   *            the start date.
   * @param dateEnd
   *            the end date.
   * @param dateRangeType
   *            the date range type.
   */
  private <R extends Report> void processLocalFiles(
      ReportDefinitionReportType reportType, Collection<File> localFiles,
      String dateStart, String dateEnd,
      ReportDefinitionDateRangeType dateRangeType) {

    Stopwatch stopwatch = Stopwatch.createStarted();

    @SuppressWarnings("unchecked")
    Class<R> reportBeanClass = (Class<R>) this.csvReportEntitiesMapping
    .getReportBeanClass(reportType);
    this.processFiles(reportBeanClass, localFiles, dateRangeType,
        dateStart, dateEnd);

    stopwatch.stop();
    LOGGER.info("\n* DB Process finished in "
        + (stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000)
        + " seconds ***");
  }

  /**
   * Deletes the local files used as temporary containers.
   * 
   * @param localFiles
   *            the list of local files.
   * @param reportType
   *            the report type.
   */
  private void deleteTemporaryFiles(Collection<File> localFiles,
      ReportDefinitionReportType reportType) {

    // Delete temporary report files
    LOGGER.info("\n Deleting temporary report files after Parsing...");
    for (File file : localFiles) {
      File gUnzipFile = new File(file.getAbsolutePath() + ".gunzip");
      gUnzipFile.delete();
      file.delete();
      LOGGER.trace(".");
    }
    LOGGER.info("\n ** Finished: " + reportType.name() + " **");
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
  private ReportDefinition getReportDefinition(
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
  private void adjustDateRange(
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
  private ReportDefinition instantiateReportDefinition(
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
  private List<String> getReportInclusions(
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
   * @param csvReportEntitiesMapping
   *            the csvReportEntitiesMapping to set
   */
  @Autowired
  public void setCsvReportEntitiesMapping(
      CsvReportEntitiesMapping csvReportEntitiesMapping) {
    this.csvReportEntitiesMapping = csvReportEntitiesMapping;
  }

  /**
   * @param multipleClientReportDownloader
   *            the multipleClientReportDownloader to set
   */
  @Autowired
  public void setMultipleClientReportDownloader(
      MultipleClientReportDownloader multipleClientReportDownloader) {
    this.multipleClientReportDownloader = multipleClientReportDownloader;
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
