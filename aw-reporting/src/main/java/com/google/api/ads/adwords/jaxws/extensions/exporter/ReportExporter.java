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

package com.google.api.ads.adwords.jaxws.extensions.exporter;

import com.google.api.ads.adwords.jaxws.extensions.authentication.Authenticator;
import com.google.api.ads.adwords.jaxws.extensions.exporter.reportwriter.FileSystemReportWriter;
import com.google.api.ads.adwords.jaxws.extensions.exporter.reportwriter.GoogleDriveReportWriter;
import com.google.api.ads.adwords.jaxws.extensions.exporter.reportwriter.ReportWriter.ReportFileType;
import com.google.api.ads.adwords.jaxws.extensions.exporter.reportwriter.ReportWriterType;
import com.google.api.ads.adwords.jaxws.extensions.report.model.csv.CsvReportEntitiesMapping;
import com.google.api.ads.adwords.jaxws.extensions.report.model.entities.NameImprClicks;
import com.google.api.ads.adwords.jaxws.extensions.report.model.entities.Report;
import com.google.api.ads.adwords.jaxws.extensions.report.model.entities.ReportPlaceholderFeedItem;
import com.google.api.ads.adwords.jaxws.extensions.report.model.persistence.EntityPersister;
import com.google.api.ads.adwords.jaxws.extensions.report.model.util.DateUtil;
import com.google.api.ads.adwords.lib.jaxb.v201309.ReportDefinitionReportType;
import com.google.api.ads.common.lib.exception.OAuthException;
import com.google.api.client.util.Maps;
import com.google.common.collect.Lists;

import com.lowagie.text.DocumentException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Class to Export Reports to PDF/HTML to FileSystem or Google Drive
 *
 * @author jtoledo@google.com (Julian Toledo)
 */
@Component
public class ReportExporter {

  private static final Logger LOGGER = Logger.getLogger(ReportExporter.class);  

  protected CsvReportEntitiesMapping csvReportEntitiesMapping;

  protected EntityPersister persister;

  protected Authenticator authenticator;
  
  public ReportExporter() {
  }

  /**
   * Generates the PDF files from the report data
   *
   * @param dateStart the start date for the reports
   * @param dateEnd the end date for the reports
   * @param properties the properties file containing all the configuration
   * @throws IOException 
   * @throws OAuthException 
   * @throws DocumentException 
   * @throws Exception error creating PDF
   */
  public void exportReports(String dateStart, String dateEnd,
      Set<Long> accountIds, Properties properties,
      File htmlTemplateFile, File outputDirectory, boolean sumAdExtensions) throws IOException, OAuthException, DocumentException {

    LOGGER.info("Starting PDF Generation");
    Map<String, Object> reportMap = Maps.newHashMap();

    for (Long accountId : accountIds) {
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

        String propertyReportWriterType = properties.getProperty("aw.report.processor.reportwritertype");

        if (propertyReportWriterType != null && 
            propertyReportWriterType.equals(ReportWriterType.GoogleDriveWriter.name())) {

          String propertyTopAccountCid = properties.getProperty("mccAccountId");

          LOGGER.debug("Constructing Google Drive Report Writers to write reports");

          // Get HTML report as inputstream to avoid writing to Drive
          LOGGER.debug("Exporting monthly reports to HTML for account: " + accountId);
          ByteArrayOutputStream htmlReportOutput = new ByteArrayOutputStream();
          OutputStreamWriter htmlOutputStreamWriter = new OutputStreamWriter(htmlReportOutput);
          HTMLExporter.exportHTML(reportMap, htmlTemplateFile, htmlOutputStreamWriter);
          InputStream htmlReportInput = new ByteArrayInputStream(htmlReportOutput.toByteArray());

          GoogleDriveReportWriter pdfReportWriter = new GoogleDriveReportWriter.GoogleDriveReportWriterBuilder(
              accountId, dateStart, dateEnd, propertyTopAccountCid, authenticator).build();

          LOGGER.debug("Converting HTML to PDF for account: " + accountId);
          HTMLExporter.convertHTMLtoPDF(htmlReportInput, pdfReportWriter);

          htmlOutputStreamWriter.close();
          htmlReportInput.close();
          pdfReportWriter.close();

        } else {

          LOGGER.debug("Constructing File System Reporriters to write reports");
          FileSystemReportWriter htmlReportWriter = new FileSystemReportWriter.FileSystemReportWriterBuilder(
              outputDirectory, accountId, dateStart, dateEnd, ReportFileType.HTML).build();
          FileSystemReportWriter pdfReportWriter = new FileSystemReportWriter.FileSystemReportWriterBuilder(
              outputDirectory, accountId, dateStart, dateEnd, ReportFileType.PDF).build();

          LOGGER.debug("Exporting monthly reports to HTML for account: " + accountId);
          HTMLExporter.exportHTML(reportMap, htmlTemplateFile, htmlReportWriter);

          LOGGER.debug("Converting HTML to PDF for account: " + accountId);
          HTMLExporter.convertHTMLtoPDF(htmlReportWriter.getOutputFile(), pdfReportWriter);

          htmlReportWriter.close();
          pdfReportWriter.close();
        }
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
