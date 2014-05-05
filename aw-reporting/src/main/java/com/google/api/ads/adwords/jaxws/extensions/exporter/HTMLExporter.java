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

import com.google.api.ads.adwords.jaxws.extensions.exporter.reportwriter.ReportWriter;
import com.google.api.ads.adwords.jaxws.extensions.util.MediaReplacedElementFactory;
import com.google.api.client.util.Lists;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import com.samskivert.mustache.Mustache;

import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.resource.XMLResource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Class to export reports to HTML using JMoustache, and convert HTML to PDF using Flying Saucer
 *
 * @author markbowyer@google.com (Mark R. Bowyer)
 * @author joeltoby@google.com (Joel Toby)
 * @author jtoledo@google.com (Julian Toledo)
 */
public class HTMLExporter {

  private static final ArrayList<String> fonts = getFonts();

  public HTMLExporter() {}

  /**
   * Exports an HTML file of the given report
   *
   * @param map the data from the Report
   * @param templateFile where to read out the HTML template
   * @param writer the {@link Writer} to which HTML should be written.  Usually an {@link ReportWriter}
   * @throws IOException error writing HTML file
   * @throws FileNotFoundException error reading template file
   */
  public static void exportHtml(final Map<String, Object> map,
      File templateFile, Writer writer) throws IOException {

    FileReader templateReader = new FileReader(templateFile);
    exportHtml(map, templateReader, writer);
  }

  /**
   * Exports an HTML file (InputStream) of the given report
   *
   * @param map the data from the Report
   * @param templateFile the InputStream where to read out the HTML template
   * @param writer the {@link Writer} to which HTML should be written.  Usually an {@link ReportWriter}
   * @throws IOException error writing HTML file
   * @throws FileNotFoundException error reading template file
   */
  public static void exportHtml(final Map<String, Object> map,
      InputStream templateFile, Writer writer) throws IOException {

    InputStreamReader templateReader = new InputStreamReader(templateFile);
    exportHtml(map, templateReader, writer);
  }

  /**
   * Exports an HTML file (InputStreamReader) of the given report
   *
   * @param map the data from the Report
   * @param templateInputStream where to read out the HTML template
   * @param writer the {@link Writer} to which HTML should be written.  Usually an {@link ReportWriter}
   * @throws IOException error writing HTML file
   * @throws FileNotFoundException error reading template file
   */
  public static void exportHtml(final Map<String, Object> map,
      InputStreamReader inputStreamReader, Writer writer) throws IOException {

    Mustache.compiler().compile(inputStreamReader).execute(map, writer);

    writer.flush();
    writer.close();
    inputStreamReader.close();
  }

  /**
   * Convert a given HTML file to a PDF file
   *
   * @param file The HTML file
   * @param reportWriter the ReportWriter to which HTML should be written
   * @throws DocumentException error creating PDF file
   * @throws IOException error closing file
   */
  public static void exportHtmlToPdf(File file, ReportWriter reportWriter)
      throws DocumentException, IOException {

    FileReader fileReader = new FileReader(file);
    Document document = XMLResource.load(fileReader).getDocument();
    exportHtmlToPdf(document, reportWriter);
    fileReader.close();
  }

  /**
   * Convert a given HTML source to a PDF file
   *
   * @param inputStream The HTML source
   * @param reportWriter the ReportWriter to which HTML should be written
   * @throws DocumentException error creating PDF file
   * @throws IOException error closing file
   */
  public static void exportHtmlToPdf(InputStream inputStream, ReportWriter reportWriter)
      throws DocumentException, IOException {

    Document document = XMLResource.load(inputStream).getDocument();
    exportHtmlToPdf(document, reportWriter);
    inputStream.close();
  }

  /**
   * Convert a given HTML report {@link InputStream} to a PDF file.<br>
   * 
   * This method does not close the report InputStream. It is the responsibility 
   * of the caller to do so.
   *
   * @param document The HTML Document
   * @param reportWriter the ReportWriter to which HTML should be written
   * @throws DocumentException error creating PDF file
   * @throws IOException error closing file
   */
  public static void exportHtmlToPdf(Document document, ReportWriter reportWriter)
      throws DocumentException, IOException {

    ITextRenderer renderer = new ITextRenderer();

    // Adding fonts for PDF generation
    for(String font : fonts) {
      renderer.getFontResolver().addFont(font, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
    }

    renderer.getSharedContext().setReplacedElementFactory(
        new MediaReplacedElementFactory(renderer.getSharedContext().getReplacedElementFactory()));
    renderer.setDocument(document, null);    
    renderer.layout();

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    renderer.createPDF(outputStream, true);
    ByteArrayInputStream is = new ByteArrayInputStream(outputStream.toByteArray());
    reportWriter.write(is);

    outputStream.flush();
    outputStream.close();
  }

  /**
   * Searches for Fonts in the fonts/ directory inside the Jar
   * 
   * @returns a string list of fonts
   */
  private static ArrayList<String> getFonts() {
    ArrayList<String> list = Lists.newArrayList();
    try {
      // Searh in the Jar
      CodeSource src = HTMLExporter.class.getProtectionDomain().getCodeSource();
      if (src != null) {
        URL jar = src.getLocation();
        ZipInputStream zip = new ZipInputStream(jar.openStream());
        while(true) {
          ZipEntry e = zip.getNextEntry();
          if (e == null)
            break;
          String name = e.getName();

          if (name.startsWith("fonts/") &&  name.endsWith(".ttf") ) {
            list.add(name);
            System.out.println("Added Font: " + name);
          }
        }
      }

      // Search in the folder
      if ( list.size() == 0) {
        final File dir = new File(HTMLExporter.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        File directory = new File(dir + "/fonts");
        if (directory.listFiles() != null) {
          for(File file : directory.listFiles()) {
            list.add(file.getPath());
            System.out.println("Added Font: " + file.getPath());
          }
        }
      }
    } catch (IOException e) {
      System.out.println("Not Fonts found");
    }
    return list;
  }
}
