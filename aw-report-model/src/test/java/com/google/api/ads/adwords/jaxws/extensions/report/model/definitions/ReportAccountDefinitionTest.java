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

package com.google.api.ads.adwords.jaxws.extensions.report.model.definitions;

import com.google.api.ads.adwords.jaxws.extensions.report.model.entities.ReportAccount;
import com.google.api.ads.adwords.lib.jaxb.v201402.ReportDefinitionReportType;

import junit.framework.Assert;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests the Account Performance report definition.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:aw-report-model-test-beans.xml")
public class ReportAccountDefinitionTest extends AbstractReportDefinitionTest<ReportAccount> {

  /**
   * C'tor
   */
  public ReportAccountDefinitionTest() {
    super(ReportAccount.class, ReportDefinitionReportType.ACCOUNT_PERFORMANCE_REPORT,
        "src/test/resources/csv/account.csv");
  }

  /**
   * @see com.google.api.ads.adwords.jaxws.extensions.report.model.definitions.
   * AbstractReportDefinitionTest#testFirstEntry(
   * com.google.api.ads.adwords.jaxws.extensions.report.model.entities.Report)
   */
  @Override
  protected void testFirstEntry(ReportAccount first) {

    Assert.assertEquals(2602198216L, first.getAccountId().longValue());
    Assert.assertEquals("2013-05-01", first.getDay());
    Assert.assertEquals("Lucotte - Vernoil- Le-fourrier", first.getAccountDescriptiveName());
    Assert.assertEquals("1.42", first.getCost());
    Assert.assertEquals(10L, first.getClicks().longValue());
    Assert.assertEquals(1978L, first.getImpressions().longValue());
    Assert.assertEquals(0L, first.getConversions().longValue());
    Assert.assertEquals("0.51", first.getCtr());
    Assert.assertEquals("0.72", first.getAvgCpm());
    Assert.assertEquals("0.14", first.getAvgCpc());
    Assert.assertEquals("2.97", first.getAvgPosition());
    Assert.assertEquals("EUR", first.getCurrencyCode());
    Assert.assertEquals("Search Network", first.getAdNetwork());
    Assert.assertEquals("17.40", first.getSearchLostISBudget());
    Assert.assertEquals("42.93", first.getSearchLostISRank());
    Assert.assertEquals("0.00", first.getContentLostISBudget());
    Assert.assertEquals("0.00", first.getContentLostISRank());

  }


  /**
   * @see com.google.api.ads.adwords.jaxws.extensions.report.model.definitions.
   * AbstractReportDefinitionTest#testLastEntry(
   * com.google.api.ads.adwords.jaxws.extensions.report.model.entities.Report)
   */
  @Override
  protected void testLastEntry(ReportAccount last) {

    Assert.assertEquals(2602198216L, last.getAccountId().longValue());
    Assert.assertEquals("2013-05-10", last.getDay());
    Assert.assertEquals("Lucotte - Vernoil- Le-fourrier", last.getAccountDescriptiveName());
    Assert.assertEquals("0.75", last.getCost());
    Assert.assertEquals(4L, last.getClicks().longValue());
    Assert.assertEquals(2793L, last.getImpressions().longValue());
    Assert.assertEquals(0L, last.getConversions().longValue());
    Assert.assertEquals("0.14", last.getCtr());
    Assert.assertEquals("0.27", last.getAvgCpm());
    Assert.assertEquals("0.19", last.getAvgCpc());
    Assert.assertEquals("3.14", last.getAvgPosition());
    Assert.assertEquals("EUR", last.getCurrencyCode());
    Assert.assertEquals("Search Network", last.getAdNetwork());
    Assert.assertEquals("2.12", last.getSearchLostISBudget());
    Assert.assertEquals("44.45", last.getSearchLostISRank());
    Assert.assertEquals("0.00", last.getContentLostISBudget());
    Assert.assertEquals("0.00", last.getContentLostISRank());

  }

  /**
   * @see com.google.api.ads.adwords.jaxws.extensions.report.model.definitions.
   * AbstractReportDefinitionTest#retrieveCsvEntries()
   */
  @Override
  protected int retrieveCsvEntries() {
    return 7;
  }

  /**
   * @return the properties for the Account Performance report.
   */
  @Override
  protected String[] retrievePropertiesToBeSelected() {

    return new String[] {
        // Report
        "ExternalCustomerId",
        // ReportBase
        "AccountDescriptiveName",
        "AccountTimeZoneId",
        "CustomerDescriptiveName",
        "PrimaryCompanyName",
        "PrimaryUserLogin",
        "AccountCurrencyCode",
        "Date",
        "DayOfWeek",
        "Week",
        "Month",
        "MonthOfYear",
        "Quarter",
        "Year",
        "Cost",
        "Clicks",
        "Impressions",
        "Ctr",
        "AverageCpm",
        "AverageCpc",
        "AveragePosition",
        "Device",
        "ClickType",
        "AdNetworkType1",
        "AdNetworkType2",
        "ConversionsManyPerClick",
        "ConversionRateManyPerClick",
        "CostPerConversionManyPerClick",
        "ValuePerConvManyPerClick",
        "ValuePerConversionManyPerClick",
        "Conversions",
        "ConversionRate",
        "CostPerConversion",
        "ValuePerConv",
        "ValuePerConversion",
        "ConversionCategoryName",
        "ConversionTypeName",
        "ConversionValue",
        "ViewThroughConversions",
        // Specific to Account Performance
        "SearchImpressionShare",
        "SearchBudgetLostImpressionShare",
        "SearchRankLostImpressionShare",
        "ContentImpressionShare",
        "ContentBudgetLostImpressionShare",
        "ContentRankLostImpressionShare"
    };
  }

}
