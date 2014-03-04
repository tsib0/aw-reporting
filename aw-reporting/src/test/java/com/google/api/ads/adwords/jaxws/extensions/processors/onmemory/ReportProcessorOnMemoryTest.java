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

import static org.mockito.Mockito.doAnswer;

import com.google.api.ads.adwords.jaxws.extensions.authentication.Authenticator;
import com.google.api.ads.adwords.jaxws.extensions.downloader.MultipleClientReportDownloader;
import com.google.api.ads.adwords.jaxws.extensions.report.model.entities.Report;
import com.google.api.ads.adwords.jaxws.extensions.report.model.persistence.AuthTokenPersister;
import com.google.api.ads.adwords.jaxws.extensions.report.model.persistence.EntityPersister;
import com.google.api.ads.adwords.jaxws.extensions.util.DynamicPropertyPlaceholderConfigurer;
import com.google.api.ads.adwords.lib.jaxb.v201309.ReportDefinitionDateRangeType;
import com.google.api.ads.common.lib.exception.OAuthException;
import com.google.common.collect.ImmutableSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Test case for the {@code ReportProcessorOnMemory} class.
 *
 * @author jtoledo@google.com (Julian Toledo)
 */
public class ReportProcessorOnMemoryTest {

  @Mock
  private AuthTokenPersister mockedAuthTokenPersister;

  @Mock
  private EntityPersister mockedReportEntitiesPersister;

  @Mock
  private MultipleClientReportDownloader mockedMultipleClientReportDownloader;

  @Spy
  private ReportProcessorOnMemory reportProcessorOnMemory;
  
  @Mock
  private Authenticator authenticator;

  private Properties properties;

  private static final Set<Long> CIDS = ImmutableSet.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);

  @Captor
  ArgumentCaptor<List<? extends Report>> reportEntitiesCaptor;

  @Before
  public void setUp() throws InterruptedException, IOException, OAuthException {
    Resource resource = new FileSystemResource("src/test/resources/aw-report-sample.properties");
    DynamicPropertyPlaceholderConfigurer.setDynamicResource(resource);
    properties = PropertiesLoaderUtils.loadProperties(resource);

    reportProcessorOnMemory = new ReportProcessorOnMemory("1", 10, 2);

    MockitoAnnotations.initMocks(this);

    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(reportProcessorOnMemory);
    
  }

  @Test
  public void testGenerateReportsForMCC() throws Exception {

    reportProcessorOnMemory.generateReportsForMCC(
        ReportDefinitionDateRangeType.CUSTOM_DATE, "20130101", "20130131", CIDS, properties);
  }
}
