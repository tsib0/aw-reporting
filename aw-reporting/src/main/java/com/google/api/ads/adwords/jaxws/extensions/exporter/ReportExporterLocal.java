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
public class ReportExporterLocal extends ReportExporter {

  private static final Logger LOGGER = Logger.getLogger(ReportExporterLocal.class);
  
  public ReportExporterLocal() {
  }
  
  
  /* (non-Javadoc)
   * @see com.google.api.ads.adwords.jaxws.extensions.exporter.ReportExporter#exportReports(java.lang.String, java.lang.String, java.util.Set, java.util.Properties, java.io.File, java.io.File, boolean)
   */
  @Override
  public void exportReports(String dateStart, String dateEnd,
      Set<Long> accountIds, Properties properties,
      File htmlTemplateFile, File outputDirectory, boolean sumAdExtensions) throws IOException, OAuthException, DocumentException {

    LOGGER.info("Starting PDF Generation");
    for (Long accountId : accountIds) {
      exportReport(dateStart, dateEnd, accountId, properties, 
          htmlTemplateFile, outputDirectory, sumAdExtensions);
    }
  }
}
