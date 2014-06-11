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

package com.google.api.ads.adwords.jaxws.extensions.processors.onmemory;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

import com.google.api.ads.adwords.jaxws.extensions.authentication.Authenticator;
import com.google.api.ads.adwords.jaxws.extensions.authentication.InstalledOAuth2Authenticator;
import com.google.api.ads.adwords.jaxws.extensions.exporter.reportwriter.ReportWriterType;
import com.google.api.ads.adwords.jaxws.extensions.report.model.csv.CsvReportEntitiesMapping;
import com.google.api.ads.adwords.jaxws.extensions.report.model.entities.Report;
import com.google.api.ads.adwords.jaxws.extensions.report.model.entities.ReportAccount;
import com.google.api.ads.adwords.jaxws.extensions.report.model.persistence.EntityPersister;
import com.google.api.ads.adwords.jaxws.extensions.util.DynamicPropertyPlaceholderConfigurer;
import com.google.api.ads.adwords.jaxws.extensions.util.FileUtil;
import com.google.api.ads.adwords.lib.client.AdWordsSession;
import com.google.api.ads.adwords.lib.jaxb.v201402.ReportDefinitionDateRangeType;
import com.google.api.ads.adwords.lib.utils.ReportDownloadResponseException;
import com.google.api.ads.adwords.lib.utils.ReportException;
import com.google.api.ads.common.lib.exception.OAuthException;
import com.google.api.ads.common.lib.exception.ValidationException;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.util.Lists;
import com.google.common.collect.Sets;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

/**
 * Test case for the {@code ReportProcessorOnMemory} class.
 *
 * @author jtoledo@google.com (Julian Toledo)
 */
public class ReportProcessorOnMemoryTest {

  private static final String PROPERTIES_FILE = "src/test/resources/aw-report-sample2.properties";

  private static final String CSV_FILE_PATH =
      "src/test/resources/csv/reportDownload-ACCOUNT_PERFORMANCE_REPORT-2602198216-1370030134500.report";
  
  private static final int NUMBER_OF_ACCOUNTS = 50;
  private static final int NUMBER_OF_THREADS = 50;

  private Properties properties;

  private static final Set<Long> CIDS = Sets.newHashSet();
  
  private byte[] reportData;

  private ApplicationContext appCtx;

  @Mock
  private EntityPersister mockedEntitiesPersister;

  @Spy
  private ReportProcessorOnMemory reportProcessorOnMemory;
 
  // We will Spy each RunnableProcessorOnMemory
  private List<RunnableProcessorOnMemory<ReportAccount>> runnableProcessorOnMemoryList = Lists.newArrayList();

  @Mock
  private Authenticator authenticator;

  @Captor
  ArgumentCaptor<List<? extends Report>> reportEntitiesCaptor;

  AdWordsSession adWordsSession;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() throws InterruptedException, IOException, OAuthException,
  ValidationException, ReportException, ReportDownloadResponseException {

    adWordsSession =
        new AdWordsSession.Builder().withEndpoint("http://www.google.com")
            .withDeveloperToken("DeveloperToken")
            .withClientCustomerId("123")
            .withUserAgent("UserAgent")
            .withOAuth2Credential(new GoogleCredential.Builder().build())
            .build();

    for (int i = 1; i <= NUMBER_OF_ACCOUNTS; i++) {
      CIDS.add(Long.valueOf(i));
    }

    Resource resource = new FileSystemResource(PROPERTIES_FILE);
    DynamicPropertyPlaceholderConfigurer.setDynamicResource(resource);
    properties = PropertiesLoaderUtils.loadProperties(resource);
    appCtx = new ClassPathXmlApplicationContext("classpath:aw-report-test-beans.xml");

    reportProcessorOnMemory = new ReportProcessorOnMemory(100, NUMBER_OF_THREADS);

    authenticator = new InstalledOAuth2Authenticator("DevToken", "ClientId", "ClientSecret",
        ReportWriterType.FileSystemWriter);

    MockitoAnnotations.initMocks(this);

    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(mockedEntitiesPersister)
        .persistReportEntities(Mockito.<List<? extends Report>>anyObject());

    reportProcessorOnMemory.setCsvReportEntitiesMapping(
        appCtx.getBean(CsvReportEntitiesMapping.class));
    reportProcessorOnMemory.setAuthentication(authenticator);
    reportProcessorOnMemory.setPersister(mockedEntitiesPersister);

    // Mocking the Authentication because in OAuth2 we are force to call buildOAuth2Credentials
    AdWordsSession.Builder builder =
        new AdWordsSession.Builder().withEndpoint("http://www.google.com")
            .withDeveloperToken("DeveloperToken")
            .withClientCustomerId("123")
            .withUserAgent("UserAgent")
            .withOAuth2Credential( new GoogleCredential.Builder().build());

    doReturn(builder).when(authenticator)
        .authenticate(Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean());

    reportData = getReporDatefromCsv();

    // Modifying ReportProcessorOnMemory to use the Mocked objects and avoid calling the real API 
    doAnswer(new Answer<RunnableProcessorOnMemory<ReportAccount>>() {
      @Override
      public RunnableProcessorOnMemory<ReportAccount> answer(InvocationOnMock invocation) throws Throwable {
        Object[] args = invocation.getArguments();

        RunnableProcessorOnMemory<ReportAccount> runnableProcessorOnMemory = Mockito.spy((RunnableProcessorOnMemory<ReportAccount>)args[0]);

        doAnswer(new Answer<ByteArrayInputStream>() {
          @Override
          public ByteArrayInputStream answer(InvocationOnMock invocation) throws Throwable {
            return new ByteArrayInputStream(reportData);
          }
        }).when(runnableProcessorOnMemory).getReportInputStream();

        runnableProcessorOnMemoryList.add(runnableProcessorOnMemory);
        return runnableProcessorOnMemory;
      }
    }).when(reportProcessorOnMemory).getRunnableProcessorOnMemory(Mockito.any(RunnableProcessorOnMemory.class));
  }

  @Test
  public void testGenerateReportsForMCC() throws Exception {

    reportProcessorOnMemory.generateReportsForMCC(null, "123",
        ReportDefinitionDateRangeType.CUSTOM_DATE, "20130101", "20130131", CIDS, properties);

    for(RunnableProcessorOnMemory<ReportAccount> runnable : runnableProcessorOnMemoryList) {
      Exception error = runnable.getError();
      if (error != null) {
        fail("There where errors in a RunnableProcessorOnMemory: " + error.getMessage());
      }
    }
  }

  private byte[] getReporDatefromCsv() throws IOException {
    FileInputStream fis = new FileInputStream(CSV_FILE_PATH);
    ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
    GZIPOutputStream gzipOut = new GZIPOutputStream(baos);
    FileUtil.copy(fis, gzipOut);
    gzipOut.flush();
    gzipOut.close();
    byte[] array = baos.toByteArray();
    baos.flush();
    baos.close();
    return array;
  }
}
