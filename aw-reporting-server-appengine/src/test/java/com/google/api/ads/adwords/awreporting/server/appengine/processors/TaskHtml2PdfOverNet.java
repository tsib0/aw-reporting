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

package com.google.api.ads.adwords.awreporting.server.appengine.processors;

import com.google.api.ads.adwords.awreporting.exporter.reportwriter.MemoryReportWriter;
import com.google.api.ads.adwords.awreporting.server.appengine.exporter.ReportExporterAppEngine;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


/**
 * Test case for Html2PdfOverNet.
 *
 * @author jtoledo@google.com (Julian Toledo)
 */
public class TaskHtml2PdfOverNet {
  
  @Test
  public void test_run() throws IOException {
    
    String html = "<html>HOLA</html>";
    
    InputStream htmlSource = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
    
    MemoryReportWriter mrwHtml = MemoryReportWriter.newMemoryReportWriter();
    
    ReportExporterAppEngine.html2PdfOverNet(htmlSource, mrwHtml);
    
    IOUtils.toString(mrwHtml.getAsSource(), "UTF-8");

  }
}
