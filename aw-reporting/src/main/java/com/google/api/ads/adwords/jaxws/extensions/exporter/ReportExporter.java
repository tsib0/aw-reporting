// Copyright 2014 Google Inc. All Rights Reserved.
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

package com.google.api.ads.adwords.jaxws.extensions.exporter;

import com.google.api.ads.adwords.jaxws.extensions.authentication.Authenticator;
import com.google.api.ads.adwords.jaxws.extensions.exporter.reportwriter.FileSystemReportWriter;
import com.google.api.ads.adwords.jaxws.extensions.exporter.reportwriter.GoogleDriveReportWriter;
import com.google.api.ads.adwords.jaxws.extensions.exporter.reportwriter.MemoryReportWriter;
import com.google.api.ads.adwords.jaxws.extensions.exporter.reportwriter.ReportWriter.ReportFileType;
import com.google.api.ads.adwords.jaxws.extensions.exporter.reportwriter.ReportWriterType;
import com.google.api.ads.adwords.jaxws.extensions.report.model.csv.CsvReportEntitiesMapping;
import com.google.api.ads.adwords.jaxws.extensions.report.model.entities.NameImprClicks;
import com.google.api.ads.adwords.jaxws.extensions.report.model.entities.Report;
import com.google.api.ads.adwords.jaxws.extensions.report.model.entities.ReportPlaceholderFeedItem;
import com.google.api.ads.adwords.jaxws.extensions.report.model.persistence.EntityPersister;
import com.google.api.ads.adwords.jaxws.extensions.report.model.util.DateUtil;
import com.google.api.ads.adwords.lib.jaxb.v201402.ReportDefinitionReportType;
import com.google.api.ads.common.lib.exception.OAuthException;
import com.google.api.client.util.Maps;
import com.google.common.collect.Lists;
import com.lowagie.text.DocumentException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Exports reports PDF/HTML reports to either the FileSystem or Google Drive.
 *
 * @author joeltoby@google.com (Joel Toby)
 * @author jtoledo@google.com (Julian Toledo)
 */
@Component
public abstract class ReportExporter {

  private static final Logger LOGGER = Logger.getLogger(ReportExporter.class);

  protected CsvReportEntitiesMapping csvReportEntitiesMapping;

  protected EntityPersister persister;

  protected Authenticator authenticator;

  /**
   * Export reports to PDF/HTML.
   *
   * @param dateStart the start date for the reports
   * @param dateEnd the end date for the reports
   * @param accountIds the list of accounts to generate PDFs for
   * @param properties the properties file containing all the configuration
   * @param htmlTemplateFile
   * @param outputDirectory
   * @throws IOException 
   * @throws OAuthException 
   * @throws DocumentException
   */
  public abstract void exportReports(String mccAccountId, String dateStart, String dateEnd,
      Set<Long> accountIds, Properties properties,File htmlTemplateFile, File outputDirectory,
      Boolean sumAdExtensions) throws IOException, OAuthException, DocumentException;

  /**
   * Export reports to PDF/HTML for one account.
   *
   * @param dateStart the start date for the reports
   * @param dateEnd the end date for the reports
   * @param accountId the account CID to generate PDF for
   * @param properties the properties file containing all the configuration
   * @param htmlTemplateFile the template file to generete the report
   * @param outputDirectory where to output the files
   * @param sumAdExtensions to add up all the extensions
   * @throws IOException 
   * @throws OAuthException 
   * @throws DocumentException 
   * @throws Exception error creating PDF
   */
  public void exportReport(String mccAccountId, String dateStart, String dateEnd, Long accountId, Properties properties,
      File htmlTemplateFile, File outputDirectory, Boolean sumAdExtensions)
          throws IOException, OAuthException, DocumentException {

    LOGGER.info("Starting Report Export for account " + accountId);

    Map<String, Object> reportDataMap = createReportDataMap(dateStart, dateEnd, accountId, properties, sumAdExtensions);

    if (reportDataMap.size() > 0) {

      String propertyReportWriterType = properties.getProperty("aw.report.exporter.reportwritertype");

      Boolean writeHtml = true;
      if (properties.getProperty("aw.report.exporter.writeHtml") != null) {
        writeHtml = Boolean.valueOf(properties.getProperty("aw.report.exporter.writeHtml"));
      }

      Boolean writePdf = true;
      if (properties.getProperty("aw.report.exporter.writePdf") != null) {
        writePdf = Boolean.valueOf(properties.getProperty("aw.report.exporter.writePdf"));
      }

      LOGGER.debug("Generating in Memory HTML for account: " + accountId);
      // Writing HTML to Memory
      MemoryReportWriter mrwHtml = MemoryReportWriter.newMemoryReportWriter();
      HTMLExporter.exportHtml(reportDataMap, htmlTemplateFile, mrwHtml);

      if (propertyReportWriterType != null && 
          propertyReportWriterType.equals(ReportWriterType.GoogleDriveWriter.name())) {

        // Writing HTML to GoogleDrive
        if (writeHtml) {
          LOGGER.debug("Writing (to GoogleDrive) HTML for account: " + accountId);
          GoogleDriveReportWriter gdrwHtml = new GoogleDriveReportWriter.GoogleDriveReportWriterBuilder(
              accountId, dateStart, dateEnd, mccAccountId, authenticator, ReportFileType.HTML,
              htmlTemplateFile).build();
          gdrwHtml.write(mrwHtml.getAsSource());
        }

        // Writing PDF to GoogleDrive
        if (writePdf) {
          LOGGER.debug("Writing (to GoogleDrive) PDF for account: " + accountId);
          GoogleDriveReportWriter gdrwPdf = new GoogleDriveReportWriter.GoogleDriveReportWriterBuilder(
              accountId, dateStart, dateEnd, mccAccountId, authenticator, ReportFileType.PDF,
              htmlTemplateFile).build();
          HTMLExporter.exportHtmlToPdf(mrwHtml.getAsSource(), gdrwPdf);
        }

      } else {

        // Writing HTML to Disk
        if (writeHtml) {
          LOGGER.debug("Writing (to FileSystem) HTML for account: " + accountId);
          FileSystemReportWriter fsrwHtml = FileSystemReportWriter.newFileSystemReportWriter(
              htmlTemplateFile, dateStart, dateEnd, accountId, outputDirectory, ReportFileType.HTML);
          fsrwHtml.write(mrwHtml.getAsSource());
        }

        // Writing PDF to Disk
        if (writePdf) {
          LOGGER.debug("Writing (to FileSystem) PDF for account: " + accountId);
          FileSystemReportWriter fsrwPdf = FileSystemReportWriter.newFileSystemReportWriter(
              htmlTemplateFile, dateStart, dateEnd, accountId, outputDirectory, ReportFileType.PDF);
          HTMLExporter.exportHtmlToPdf(mrwHtml.getAsSource(), fsrwPdf);
        }
      }
    }
  }

  /**
   * Generates the HashMap with all the data from reports
   *
   * @param dateStart the start date for the reports
   * @param dateEnd the end date for the reports
   * @param accountId the account CID to generate PDF for
   * @param properties the properties file containing all the configuration
   * @param sumAdExtensions to add up all the extensions
   */
  private Map<String, Object> createReportDataMap(String dateStart, String dateEnd,
      Long accountId, Properties properties, Boolean sumAdExtensions) {

    Map<String, Object> reportDataMap = Maps.newHashMap();
    Set<ReportDefinitionReportType> reports = csvReportEntitiesMapping.getDefinedReports();
    System.out.println("** Number of reports: " + reports.size() + " ***");
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
          reportDataMap.put("ADEXTENSIONS", adExtensions);
        }
        reportDataMap.put(reportType.name(), monthlyReports);
      }
    }
    return reportDataMap;
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
   * @param persister the persister to set
   */
  @Autowired
  public void setPersister(EntityPersister persister) {
    this.persister = persister;
  }

  /**
   * @param authentication the helper class for Auth
   */
  @Autowired
  public void setAuthentication(Authenticator authenticator) {
    this.authenticator = authenticator;
  }
}
