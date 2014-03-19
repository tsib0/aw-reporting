// Copyright 2011 Google Inc. All Rights Reserved.
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

package com.google.api.ads.adwords.jaxws.extensions.report.model.entities;

import com.google.api.ads.adwords.jaxws.extensions.report.model.csv.annotation.CsvField;
import com.google.api.ads.adwords.jaxws.extensions.report.model.csv.annotation.CsvReport;
import com.google.api.ads.adwords.jaxws.extensions.report.model.util.BigDecimalUtil;
import com.google.api.ads.adwords.lib.jaxb.v201402.ReportDefinitionReportType;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Specific report class for ReportCampaign
 *
 * @author jtoledo@google.com (Julian Toledo)
 * @author gustavomoreira@google.com (Gustavo Moreira)
 */
@Entity
@com.googlecode.objectify.annotation.Entity
@Table(name = "AW_ReportCampaign")
@CsvReport(value = ReportDefinitionReportType.CAMPAIGN_PERFORMANCE_REPORT)
public class ReportCampaign extends ReportBase {

  @Column(name = "CAMPAIGN_ID")
  @CsvField(value = "Campaign ID", reportField = "CampaignId")
  private Long campaignId;

  @Column(name = "CAMPAIGN_NAME", length = 255)
  @CsvField(value = "Campaign", reportField = "CampaignName")
  private String campaignName;

  @Column(name = "STATUS", length = 32)
  @CsvField(value = "Campaign state", reportField = "Status")
  private String status;

  @Column(name = "BUDGET")
  @CsvField(value = "Budget", reportField = "Amount")
  private BigDecimal budget;

  @Column(name = "CONVERSIONRATESIGNIFICANCE")
  @CsvField(value = "Click conversion rate ACE indicator", reportField = "ConversionRateSignificance")
  protected BigDecimal conversionRateSignificance;

  @Column(name = "CONVERSIONRATEMANYPERCLICKSIGNIFICANCE")
  @CsvField(value = "Conversion rate ACE indicator", reportField = "ConversionRateManyPerClickSignificance")
  protected BigDecimal conversionRateManyPerClickSignificance;
  
  @Column(name = "CONVERSIONMANYPERCLICKSIGNIFICANCE")
  @CsvField(value = "Conversion ACE indicator", reportField = "ConversionManyPerClickSignificance")
  protected BigDecimal conversionManyPerClickSignificance;

  @Column(name = "COSTPERCONVERSIONMANYPERCLICKSIGNIFICANCE")
  @CsvField(value = "Cost/conversion ACE indicator", reportField = "CostPerConversionManyPerClickSignificance")
  protected BigDecimal costPerConversionManyPerClickSignificance;
  
  @Column(name = "CONVERSIONSIGNIFICANCE")
  @CsvField(value = "Converted clicks ACE indicator", reportField = "ConversionSignificance")
  protected BigDecimal conversionSignificance;

  @Column(name = "COSTPERCONVERSIONSIGNIFICANCE")
  @CsvField(value = "Cost/converted click ACE indicator", reportField = "CostPerConversionSignificance")
  protected BigDecimal costPerConversionSignificance;

  /**
   * Hibernate needs an empty constructor
   */
  public ReportCampaign() {}

  public ReportCampaign(Long topAccountId, Long accountId) {
    this.topAccountId = topAccountId;
    this.accountId = accountId;
  }

  @Override
  public void setId() {
    // Generating unique id after having accountId, campaignId and date
    if (this.getAccountId() != null && this.getCampaignId() != null) {
      this.id = this.getAccountId() + "-" + this.getCampaignId();
    }

    this.id += setIdDates();

    // Adding extra fields for unique ID
    if (this.getAdNetwork() != null && this.getAdNetwork().length() > 0) {
      this.id += "-" + this.getAdNetwork();
    }
    if (this.getAdNetworkPartners() != null && this.getAdNetworkPartners().length() > 0) {
      this.id += "-" + this.getAdNetworkPartners();
    }
    if (this.getDevice() != null && this.getDevice().length() > 0) {
      this.id += "-" + this.getDevice();
    }
    if (this.getClickType() != null && this.getClickType().length() > 0) {
      this.id += "-" + this.getClickType();
    }
  }

  public Long getCampaignId() {
    return campaignId;
  }

  public void setCampaignId(Long campaignId) {
    this.campaignId = campaignId;
  }

  public String getCampaignName() {
    return campaignName;
  }

  public void setCampaignName(String campaignName) {
    this.campaignName = campaignName;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getBudget() {
    return BigDecimalUtil.formatAsReadable(budget);
  }

  public BigDecimal getBudgetBigDecimal() {
    return budget;
  }

  public void setBudget(String budget) {
    this.budget = BigDecimalUtil.parseFromNumberString(budget);
  }  

  public String getConversionRateSignificance() {
    return BigDecimalUtil.formatAsReadable(conversionRateSignificance);
  }
  
  public BigDecimal getConversionRateSignificanceBigDecimal() {
    return conversionRateSignificance;
  }

  public void setConversionRateSignificance(String conversionRateSignificance) {
    this.conversionRateSignificance = BigDecimalUtil.parseFromNumberString(conversionRateSignificance);
  }

  public String getConversionRateManyPerClickSignificance() {
    return BigDecimalUtil.formatAsReadable(conversionRateManyPerClickSignificance);
  }
  
  public BigDecimal getConversionRateManyPerClickSignificanceBigDecimal() {
    return conversionRateManyPerClickSignificance;
  }

  public void setConversionRateManyPerClickSignificance(
      String conversionRateManyPerClickSignificance) {
    this.conversionRateManyPerClickSignificance = BigDecimalUtil.parseFromNumberString(conversionRateManyPerClickSignificance);
  }
  
  public String getConversionManyPerClickSignificance() {
    return BigDecimalUtil.formatAsReadable(conversionManyPerClickSignificance);
  }
  
  public BigDecimal getConversionManyPerClickSignificanceBigDecimal() {
    return conversionManyPerClickSignificance;
  }

  public void setConversionManyPerClickSignificance(String conversionManyPerClickSignificance) {
    this.conversionManyPerClickSignificance = BigDecimalUtil.parseFromNumberString(conversionManyPerClickSignificance);
  }
  
  public String getCostPerConversionManyPerClickSignificance() {
    return BigDecimalUtil.formatAsReadable(costPerConversionManyPerClickSignificance);
  }
  
  public BigDecimal getCostPerConversionManyPerClickSignificanceBigDecimal() {
    return costPerConversionManyPerClickSignificance;
  }

  public void setCostPerConversionManyPerClickSignificance(
      BigDecimal costPerConversionManyPerClickSignificance) {
    this.costPerConversionManyPerClickSignificance = costPerConversionManyPerClickSignificance;
  }

  public String getConversionSignificance() {
    return BigDecimalUtil.formatAsReadable(conversionSignificance);
  }
  
  public BigDecimal getConversionSignificanceBigDecimal() {
    return conversionSignificance;
  }

  public void setConversionSignificance(String conversionSignificance) {
    this.conversionSignificance = BigDecimalUtil.parseFromNumberString(conversionSignificance);
  }
  
  public String getCostPerConversionSignificance() {
    return BigDecimalUtil.formatAsReadable(costPerConversionSignificance);
  }
  
  public BigDecimal getCostPerConversionSignificanceBigDecimal() {
    return costPerConversionSignificance;
  }

  public void setCostPerConversionSignificance(String costPerConversionSignificance) {
    this.costPerConversionSignificance = BigDecimalUtil.parseFromNumberString(costPerConversionSignificance);
  }
}
