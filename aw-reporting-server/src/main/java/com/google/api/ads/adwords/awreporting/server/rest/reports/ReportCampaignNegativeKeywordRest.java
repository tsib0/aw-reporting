//Copyright 2011 Google Inc. All Rights Reserved.
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.api.ads.adwords.awreporting.server.rest.reports;

import com.google.api.ads.adwords.awreporting.model.entities.Report;
import com.google.api.ads.adwords.awreporting.model.entities.ReportCampaignNegativeKeyword;
import com.google.api.ads.adwords.awreporting.server.rest.AbstractBaseResource;
import com.google.api.ads.adwords.awreporting.server.rest.RestServer;
import com.google.api.ads.adwords.awreporting.server.util.StorageHelper;

import org.restlet.representation.Representation;

import java.util.Date;
import java.util.List;

/**
 * ReportCampaignNegativeKeywordRest
 * 
 * CampaignNegativeKeyword do not use Segments: Date, Month, etc.
 * That is why is an specific case and does not use the AbstractReportRest
 * 
 * @author jtoledo@google.com (Julian Toledo)
 */
public class ReportCampaignNegativeKeywordRest extends AbstractBaseResource {

  private StorageHelper storageHelper = RestServer.getStorageHelper();

  public Representation getHandler() {
    String result = null;

    try {

      Long topAccountId = getParameterAsLong("topAccountId");
      Long accountId = getParameterAsLong("accountId");
      Long campaignId = getParameterAsLong("campaignId");
      Long criterionId = getParameterAsLong("criterionId");
      Date dateStart = getParameterAsDate("dateStart");
      Date dateEnd = getParameterAsDate("dateEnd");

      List<ReportCampaignNegativeKeyword> listReport = null;

      if (topAccountId != null &&
          accountId == null &&
          campaignId == null &&
          criterionId == null) {

        listReport = storageHelper.getReportCampaignNegativeKeywordByAccountAndEndDateInRange(
            Report.TOP_ACCOUNT_ID, topAccountId, dateStart, dateEnd);
      }

      if (accountId != null) {
        listReport = storageHelper.getReportCampaignNegativeKeywordByAccountAndEndDateInRange(
            Report.ACCOUNT_ID, accountId, dateStart, dateEnd);
      }

      if (campaignId != null) {
        listReport = storageHelper.getReportCampaignNegativeKeywordByAccountAndEndDateInRange(
            Report.CAMPAIGN_ID, campaignId, dateStart, dateEnd);
      }

      if (criterionId != null) {
        listReport = storageHelper.getReportCampaignNegativeKeywordByAccountAndEndDateInRange(
            Report.KEYWORD_ID, criterionId, dateStart, dateEnd);
      }

      if (listReport != null) {
          result = gson.toJson(listReport);
      }
    } catch (Exception exception) {
      return handleException(exception);
    }

    addReadOnlyHeaders();
    return createJsonResult(result);
  }
}
