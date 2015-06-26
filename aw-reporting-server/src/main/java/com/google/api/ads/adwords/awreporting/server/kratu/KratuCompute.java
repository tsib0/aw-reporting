//Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.api.ads.adwords.awreporting.server.kratu;

import com.google.api.ads.adwords.awreporting.model.entities.ReportAccount;
import com.google.api.ads.adwords.awreporting.model.entities.ReportAd;
import com.google.api.ads.adwords.awreporting.model.entities.ReportAdGroup;
import com.google.api.ads.adwords.awreporting.model.entities.ReportCampaign;
import com.google.api.ads.adwords.awreporting.model.entities.ReportCampaignNegativeKeyword;
import com.google.api.ads.adwords.awreporting.model.entities.ReportKeyword;
import com.google.api.ads.adwords.awreporting.model.entities.ReportPlaceholderFeedItem;
import com.google.api.ads.adwords.awreporting.server.entities.Account;
import com.google.api.ads.adwords.awreporting.server.entities.Kratu;
import com.google.api.ads.adwords.awreporting.server.util.StorageHelper;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * KratuCompute
 * 
 * @author jtoledo@google.com (Julian Toledo)
 */
public class KratuCompute {

  private static final String DISPLAY_NETWORK = "Display Network";
  private static final String SEARCH_NETWORK = "Search Network";
  private static final String ACTIVE = "active";
  private static final String ENABLED = "enabled";
  private static final String BROAD = "Broad";
  private static final String PHRASE = "Phrase";
  private static final String EXACT = "Exact";
  private static final String DISAPPROVED = "disapproved";
  private static final int SITE_LINKS_EXTENSION = 1;
  private static final int CALL_EXTENSION = 2;
  private static final int LOCATION_EXTENSION = 7;

  public static final BigDecimal BIGDECIMAL_100 = new BigDecimal(100);

  private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
  private static final int SCALE = 2;

  /*
   * Creates a new Kratu, summarizing Kratus within the date range and for a single Account
   * Each item on the list is a Kratu for a day for the same account 
   * 
   * @param dailyKratus list of Kratus for the same Account
   * @param startDate first date on the range
   * @param endDate last date on the range
   * @returns Kratu, the summary resulting Kratu for the date range
   */
  public static Kratu createKratuSummary(List<Kratu> dailyKratus, Date startDate, Date endDate) {

    Kratu summarizedKratu = new Kratu();

    BigDecimal daysInRange = new BigDecimal(Days.daysBetween(new DateTime(startDate.getTime()), new DateTime(endDate.getTime())).getDays()).add(BigDecimal.ONE);

    // We use the first Kratu to set the Base Account info (They all should have the same Account info).
    if (dailyKratus != null && dailyKratus.size() > 0) {
      summarizedKratu.setTopAccountId(dailyKratus.get(0).getTopAccountId());
      summarizedKratu.setAccountName(dailyKratus.get(0).getAccountName());
      summarizedKratu.setExternalCustomerId(dailyKratus.get(0).getExternalCustomerId());
      summarizedKratu.setCurrencyCode(dailyKratus.get(0).getCurrencyCode());
      summarizedKratu.setDateTimeZone(dailyKratus.get(0).getDateTimeZone());
      summarizedKratu.setAccountSuspended(dailyKratus.get(0).getAccountSuspended());
    }

    for (Kratu dailyKratu : dailyKratus) {
      // General
      summarizedKratu.addSpend(dailyKratu.getSpend());
      summarizedKratu.setSpend(summarizedKratu.getSpend().setScale(SCALE, ROUNDING));
      summarizedKratu.addSumBudget(dailyKratu.getSumBudget());
      summarizedKratu.addConversions(dailyKratu.getConversions());
      
      // Search Info
      summarizedKratu.addTotalClicksSearch(dailyKratu.getTotalClicksSearch());
      summarizedKratu.addImpressionsSearch(dailyKratu.getImpressionsSearch());      

      summarizedKratu.addElegibleImpressionsSearch(dailyKratu.getElegibleImpressionsSearch());
      summarizedKratu.addLostImpressionsDueToBudgetSearch(dailyKratu.getLostImpressionsDueToBudgetSearch());
      summarizedKratu.addLostImpressionsDueToBidAdRankSearch(dailyKratu.getLostImpressionsDueToBidAdRankSearch());

      summarizedKratu.addCtrSearch(dailyKratu.getCtrSearch().divide(daysInRange, SCALE, ROUNDING));
      summarizedKratu.addAverageCpcSearch(dailyKratu.getAverageCpcSearch().divide(daysInRange, SCALE, ROUNDING));
      summarizedKratu.addAverageCpmSearch(dailyKratu.getAverageCpmSearch().divide(daysInRange, SCALE, ROUNDING));
      summarizedKratu.addAveragePositionSearch(dailyKratu.getAveragePositionSearch().divide(daysInRange, SCALE, ROUNDING));

      // Display Info
      summarizedKratu.addTotalClicksDisplay(dailyKratu.getTotalClicksDisplay());
      summarizedKratu.addImpressionsDisplay(dailyKratu.getImpressionsDisplay());

      summarizedKratu.addElegibleImpressionsDisplay(dailyKratu.getElegibleImpressionsDisplay());
      summarizedKratu.addLostImpressionsDueToBudgetDisplay(dailyKratu.getLostImpressionsDueToBudgetDisplay());
      summarizedKratu.addLostImpressionsDueToBidAdRankDisplay(dailyKratu.getLostImpressionsDueToBidAdRankDisplay());

      summarizedKratu.addCtrDisplay(dailyKratu.getCtrDisplay().divide(daysInRange, SCALE, ROUNDING));
      summarizedKratu.addAverageCpcDisplay(dailyKratu.getAverageCpcDisplay().divide(daysInRange, SCALE, ROUNDING));
      summarizedKratu.addAverageCpmDisplay(dailyKratu.getAverageCpmDisplay().divide(daysInRange, SCALE, ROUNDING));
      summarizedKratu.addAveragePositionDisplay(dailyKratu.getAveragePositionDisplay().divide(daysInRange, SCALE, ROUNDING));

      // Structural Info
      summarizedKratu.addNumberOfActiveCampaigns(dailyKratu.getNumberOfActiveCampaigns().divide(daysInRange, SCALE, ROUNDING));
      summarizedKratu.addNumberOfActiveAdGroups(dailyKratu.getNumberOfActiveAdGroups().divide(daysInRange, SCALE, ROUNDING));
      summarizedKratu.addNumberOfActiveAds(dailyKratu.getNumberOfActiveAds().divide(daysInRange, SCALE, ROUNDING));
      summarizedKratu.addNumberOfPositiveActiveKeywords(dailyKratu.getNumberOfPositiveActiveKeywords().divide(daysInRange, SCALE, ROUNDING));

      summarizedKratu.addNumberOfActiveBroadMatchingKeywords(dailyKratu.getNumberOfActiveBroadMatchingKeywords().divide(daysInRange, SCALE, ROUNDING));
      summarizedKratu.addNumberOfActivePhraseMatchingKeywords(dailyKratu.getNumberOfActivePhraseMatchingKeywords().divide(daysInRange, SCALE, ROUNDING));
      summarizedKratu.addNumberOfActiveExactMatchingKeywords(dailyKratu.getNumberOfActiveExactMatchingKeywords().divide(daysInRange, SCALE, ROUNDING));

      summarizedKratu.addNumberOfNegativeActiveKeywords(dailyKratu.getNumberOfNegativeActiveKeywords().divide(daysInRange, SCALE, ROUNDING));
      summarizedKratu.addNumberOfCampaignNegativeActiveKeywords(dailyKratu.getNumberOfCampaignNegativeActiveKeywords().divide(daysInRange, SCALE, ROUNDING));
      summarizedKratu.addNumberOfAdGroupNegativeActiveKeywords(dailyKratu.getNumberOfAdGroupNegativeActiveKeywords().divide(daysInRange, SCALE, ROUNDING));
      summarizedKratu.addNumberOfActiveGoodQualityScoreKeywords(dailyKratu.getNumberOfActiveGoodQualityScoreKeywords().divide(daysInRange, SCALE, ROUNDING));
      summarizedKratu.addNumberOfActiveAverageQualityScoreKeywords(dailyKratu.getNumberOfActiveAverageQualityScoreKeywords().divide(daysInRange, SCALE, ROUNDING));
      summarizedKratu.addNumberOfActivePoorQualityScoreKeywords(dailyKratu.getNumberOfActivePoorQualityScoreKeywords().divide(daysInRange, SCALE, ROUNDING));

      summarizedKratu.addNumberOfCampaignsWithCallExtensionEnabled(dailyKratu.getNumberOfCampaignsWithCallExtensionEnabled().divide(daysInRange, SCALE, ROUNDING));
      summarizedKratu.addNumberOfCampaignsWithLocationExtensionEnabled(dailyKratu.getNumberOfCampaignsWithLocationExtensionEnabled().divide(daysInRange, SCALE, ROUNDING));
      summarizedKratu.addNumberOfCampaignsWithSiteLinksEnabled(dailyKratu.getNumberOfCampaignsWithSiteLinksEnabled().divide(daysInRange, SCALE, ROUNDING));

      summarizedKratu.addNumberOfAdgroupsWithoneActiveAd(dailyKratu.getNumberOfAdgroupsWithoneActiveAd().divide(daysInRange, SCALE, ROUNDING));
      summarizedKratu.addNumberOfAdgroupsWithTwoActiveAds(dailyKratu.getNumberOfAdgroupsWithTwoActiveAds().divide(daysInRange, SCALE, ROUNDING));
      summarizedKratu.addNumberOfDisapprovedAds(dailyKratu.getNumberOfDisapprovedAds().divide(daysInRange, SCALE, ROUNDING));

      summarizedKratu.addWeightedAverageKeywordPosition(dailyKratu.getWeightedAverageKeywordPosition().divide(daysInRange, SCALE, ROUNDING));
      summarizedKratu.addWeightedAverageQualityScore(dailyKratu.getWeightedAverageQualityScore().divide(daysInRange, SCALE, ROUNDING));

      System.out.print(".");
    }

    if (summarizedKratu.getImpressionsDisplay() + summarizedKratu.getImpressionsSearch() > 0) {
      summarizedKratu.setAccountActive("Yes"); 
    }

    return summarizedKratu;
  }


  /*
   * Creates a Kratu for a Single Day from 7 AdWords Reports 
   * 
   * @param storageHelper the data layer object
   * @param topAccountId The top MCC for the account
   * @param account the AdWords Account
   * @param day the Day to create the Kratu for
   * @returns Kratu the kratu for the day
   */
  public static Kratu createDailyKratuFromDB(
      StorageHelper storageHelper,
      Long topAccountId,
      Account account,
      Date day) {
    
    Calendar dayStart = Calendar.getInstance();
    dayStart.setTime(day);
    dayStart.set(Calendar.HOUR_OF_DAY, 0);
    dayStart.set(Calendar.MINUTE, 0);
    dayStart.set(Calendar.SECOND, 0);
    
    Calendar dayEnd = Calendar.getInstance();
    dayEnd.setTime(day);
    dayEnd.set(Calendar.HOUR_OF_DAY, 23);
    dayEnd.set(Calendar.MINUTE, 59);
    dayEnd.set(Calendar.SECOND, 59);

    Long accountId = account.getExternalCustomerId();

    // Get all ReportAccount rows for the day
    List<ReportAccount> reportAccountList = storageHelper.getReportByAccountId(ReportAccount.class, accountId, dayStart.getTime(), dayEnd.getTime());

    // We only process a Kratu if they have ReportAccount data
    if (reportAccountList.size() == 0) {
      return null;
    } else {

      // Creating a new Kratu with the base Account Info
      Kratu kratu = new Kratu(topAccountId, account, dayStart.getTime());

      // Process ReportAccount Info
      for (ReportAccount reportAccount : reportAccountList) {

        kratu.addSpend(reportAccount.getCost());
        kratu.addConversions(reportAccount.getConvertedClicks());

        // SEARCH_NETWORK Info
        if (reportAccount.getAdNetwork() != null
            && reportAccount.getAdNetwork().equals(SEARCH_NETWORK)) {

          kratu.setTotalClicksSearch(reportAccount.getClicks());
          kratu.setImpressionsSearch(reportAccount.getImpressions());

          if (reportAccount.getSearchImpressionShareBigDecimal() != null  &&              
              reportAccount.getSearchImpressionShareBigDecimal().compareTo(BigDecimal.ZERO) == 1) {

            kratu.setElegibleImpressionsSearch(new BigDecimal(kratu.getImpressionsSearch()).divide(
                reportAccount.getSearchImpressionShareBigDecimal().divide(BIGDECIMAL_100), SCALE, ROUNDING));

            kratu.setLostImpressionsDueToBudgetSearch(reportAccount.getSearchLostISBudgetBigDecimal().divide(
                BIGDECIMAL_100).multiply(kratu.getElegibleImpressionsSearch()));

            kratu.setLostImpressionsDueToBidAdRankSearch(reportAccount.getSearchLostISRankBigDecimal().divide(
                BIGDECIMAL_100).multiply(kratu.getElegibleImpressionsSearch()));
          }

          kratu.setCtrSearch(reportAccount.getCtrBigDecimal());
          kratu.setAverageCpcSearch(reportAccount.getAvgCpc());
          kratu.setAverageCpmSearch(reportAccount.getAvgCpm());
          kratu.setAveragePositionSearch(reportAccount.getAvgPositionBigDecimal());
        }

        // DISPLAY_NETWORK Info
        if (reportAccount.getAdNetwork() != null
            && reportAccount.getAdNetwork().equals(DISPLAY_NETWORK)) {

          kratu.setTotalClicksDisplay(reportAccount.getClicks());
          kratu.setImpressionsDisplay(reportAccount.getImpressions());

          if (reportAccount.getContentImpressionShareBigDecimal() != null  &&
              reportAccount.getContentImpressionShareBigDecimal().compareTo(BigDecimal.ZERO) == 1)  {

            kratu.setElegibleImpressionsDisplay(new BigDecimal(kratu.getImpressionsDisplay()).divide(
                reportAccount.getContentImpressionShareBigDecimal().divide(BIGDECIMAL_100), SCALE, ROUNDING));

            kratu.setLostImpressionsDueToBudgetDisplay(reportAccount.getContentLostISBudgetBigDecimal().divide(
                BIGDECIMAL_100).multiply(kratu.getElegibleImpressionsDisplay()));

            kratu.setLostImpressionsDueToBidAdRankDisplay(reportAccount.getContentLostISRankBigDecimal().divide(
                BIGDECIMAL_100).multiply(kratu.getElegibleImpressionsDisplay()));
          }

          kratu.setCtrDisplay(reportAccount.getCtrBigDecimal());
          kratu.setAverageCpcDisplay(reportAccount.getAvgCpc());
          kratu.setAverageCpmDisplay(reportAccount.getAvgCpm());
          kratu.setAveragePositionDisplay(reportAccount.getAvgPositionBigDecimal());
        }
      }

      if (kratu.getImpressionsDisplay() + kratu.getImpressionsSearch() > 0) {
        kratu.setAccountActive("Yes");
      }


      // Process ReportCampaign Info
      List<ReportCampaign> reportCampaignList = storageHelper.getReportByAccountId(ReportCampaign.class, accountId, dayStart.getTime(), dayEnd.getTime());
      for (ReportCampaign reportCampaign : reportCampaignList) {
        if (reportCampaign.getCampaignStatus().equals(ENABLED)) {
          kratu.addNumberOfActiveCampaigns(BigDecimal.ONE);
        }

        if (reportCampaign.getBudget() != null) {
          kratu.addSumBudget(reportCampaign.getBudget());
        }
      }


      // Process ReportAdGroup Info
      List<ReportAdGroup> reportAdGroupList = storageHelper.getReportByAccountId(ReportAdGroup.class, accountId, dayStart.getTime(), dayEnd.getTime());
      for (ReportAdGroup reportAdGroup : reportAdGroupList) {
        if (reportAdGroup.getAdGroupStatus().equals(ENABLED)) {
          kratu.addNumberOfActiveAdGroups(BigDecimal.ONE);
        }
      }


      // Process ReportAd Info
      List<ReportAd> reportAdList = storageHelper.getReportByAccountId(ReportAd.class, accountId, dayStart.getTime(), dayEnd.getTime());
      Map<Long, Integer> activeAdsPerAdGroup = new HashMap<Long, Integer>();

      for (ReportAd reportAd : reportAdList) {
        if (reportAd.getAdState().equals(ENABLED)) {
          kratu.addNumberOfActiveAds(BigDecimal.ONE);
          // Counting the activeAdsPerAdGroup
          if (activeAdsPerAdGroup.containsKey(reportAd.getAdGroupId())) {
            activeAdsPerAdGroup.put(reportAd.getAdGroupId(),
                activeAdsPerAdGroup.get(reportAd.getAdGroupId()) + 1);
          } else {
            activeAdsPerAdGroup.put(reportAd.getAdGroupId(), 1);
          }
        }
        if (reportAd.getCreativeApprovalStatus().equals(DISAPPROVED)) {
          kratu.addNumberOfDisapprovedAds(BigDecimal.ONE);
        }
      }

      // Process activeAdsPerAdGroup
      for (Integer activeAds : activeAdsPerAdGroup.values()) {
        if (activeAds == 1) {
          kratu.addNumberOfAdgroupsWithoneActiveAd(BigDecimal.ONE);
        }
        if (activeAds == 2) {
          kratu.addNumberOfAdgroupsWithTwoActiveAds(BigDecimal.ONE);
        }
      }


      // Process ReportKeyword Info
      List<ReportKeyword> reportKeywordList = storageHelper.getReportByAccountId(ReportKeyword.class, accountId, dayStart.getTime(), dayEnd.getTime());
      Long sumImpressions = 0l;
      BigDecimal totalPositions = BigDecimal.ZERO;
      BigDecimal totalWeight = BigDecimal.ZERO;
      for (ReportKeyword reportKeyword : reportKeywordList) {
        if (reportKeyword.getStatus().equals(ENABLED)) {
          if (!reportKeyword.isNegative()) {
            kratu.addNumberOfPositiveActiveKeywords(BigDecimal.ONE);

            // numberOfActiveGoodQualityScoreKeywords: QS > 7
            if (reportKeyword.getQualityScoreAsBigDecimal().compareTo(BigDecimal.valueOf(7)) == 1) {
              kratu.addNumberOfActiveGoodQualityScoreKeywords(BigDecimal.ONE);
            }
            // numberOfActiveAverageQualityScoreKeywords: QS>4 && <=7
            if (reportKeyword.getQualityScoreAsBigDecimal().compareTo(BigDecimal.valueOf(4)) == 1
                && reportKeyword.getQualityScoreAsBigDecimal().compareTo(BigDecimal.valueOf(7)) < 1) {
              kratu.addNumberOfActiveAverageQualityScoreKeywords(BigDecimal.ONE);
            }
            // numberOfActivePoorQualityScoreKeywords: QS <= 4
            if (reportKeyword.getQualityScoreAsBigDecimal().compareTo(BigDecimal.valueOf(4)) < 1) {
              kratu.addNumberOfActivePoorQualityScoreKeywords(BigDecimal.ONE);
            }

            if (reportKeyword.getKeywordMatchType().equals(BROAD)) {
              kratu.addNumberOfActiveBroadMatchingKeywords(BigDecimal.ONE);
            }
            if (reportKeyword.getKeywordMatchType().equals(PHRASE)) {
              kratu.addNumberOfActivePhraseMatchingKeywords(BigDecimal.ONE);
            }
            if (reportKeyword.getKeywordMatchType().equals(EXACT)) {
              kratu.addNumberOfActiveExactMatchingKeywords(BigDecimal.ONE);
            }

            sumImpressions += reportKeyword.getImpressions();
            totalPositions = totalPositions.add(reportKeyword.getAvgPositionBigDecimal().multiply(
                BigDecimal.valueOf(reportKeyword.getImpressions())));
            totalWeight = totalWeight.add(reportKeyword.getQualityScoreAsBigDecimal().multiply(
                BigDecimal.valueOf(reportKeyword.getImpressions())));

          } else {
            kratu.addNumberOfNegativeActiveKeywords(BigDecimal.ONE);
            kratu.addNumberOfAdGroupNegativeActiveKeywords(BigDecimal.ONE);
          }
        }
      }
      if (sumImpressions > 0) {
        kratu.setWeightedAverageKeywordPosition(totalPositions.divide(BigDecimal.valueOf(sumImpressions), SCALE, ROUNDING));
        kratu.setWeightedAverageQualityScore(totalWeight.divide(BigDecimal.valueOf(sumImpressions), SCALE, ROUNDING));
      }


      // Process ReportPlacementFeedItem Info
      List<ReportPlaceholderFeedItem> reportPlaceholderFeedItemList = storageHelper.getReportByAccountId(ReportPlaceholderFeedItem.class, accountId, dayStart.getTime(), dayEnd.getTime());
      for (ReportPlaceholderFeedItem reportPlaceholderFeedItem : reportPlaceholderFeedItemList) {
        if (reportPlaceholderFeedItem.getStatus().equals(ENABLED)) {
          switch (reportPlaceholderFeedItem.getFeedPlaceholderType()) {
          case SITE_LINKS_EXTENSION:
            kratu.addNumberOfCampaignsWithSiteLinksEnabled(BigDecimal.ONE);
            break;
          case CALL_EXTENSION:
            kratu.addNumberOfCampaignsWithCallExtensionEnabled(BigDecimal.ONE);
            break;
          case LOCATION_EXTENSION:
            kratu.addNumberOfCampaignsWithLocationExtensionEnabled(BigDecimal.ONE);
            break;
          }
        }
      }

      // Process ReportCampaignNegativeKeyword Info
      List<ReportCampaignNegativeKeyword> reportCampaignNegativeKeywordList =
          storageHelper.getReportCampaignNegativeKeywordByAccountAndEndDateInRange(accountId, dayStart.getTime(), dayEnd.getTime());
      kratu.addNumberOfNegativeActiveKeywords(new BigDecimal(reportCampaignNegativeKeywordList.size()));
      kratu.addNumberOfCampaignNegativeActiveKeywords(new BigDecimal(reportCampaignNegativeKeywordList.size())); 

      kratu.setSpend(kratu.getSpend().setScale(SCALE, ROUNDING));

      return kratu;
    }
  } 
}
