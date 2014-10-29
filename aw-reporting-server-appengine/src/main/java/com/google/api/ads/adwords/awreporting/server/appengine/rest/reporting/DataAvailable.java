//Copyright 2012 Google Inc. All Rights Reserved.
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

package com.google.api.ads.adwords.awreporting.server.appengine.rest.reporting;

import com.google.api.ads.adwords.awreporting.model.entities.ReportAccount;
import com.google.api.ads.adwords.awreporting.model.entities.ReportAdGroup;
import com.google.api.ads.adwords.awreporting.model.entities.ReportBase;
import com.google.api.ads.adwords.awreporting.model.entities.ReportCampaign;
import com.google.api.ads.adwords.awreporting.server.appengine.RestServer;
import com.google.api.ads.adwords.awreporting.server.appengine.persistence.objectify.ObjectifyEntityPersister;
import com.google.api.ads.adwords.awreporting.server.appengine.rest.GaeAbstractServerResource;
import com.google.api.ads.adwords.awreporting.server.appengine.util.MccTaskCounter;
import com.google.api.client.util.Lists;
import com.google.api.client.util.Maps;
import com.google.common.collect.ImmutableList;

import org.restlet.representation.Representation;

import java.util.List;
import java.util.Map;

/**
 * Rest entry point generic for all Report Types
 * 
 * @author jtoledo@google.com (Julian Toledo)
 */
public class DataAvailable extends GaeAbstractServerResource {
  
  private static final ImmutableList<Class<? extends ReportBase>> REPORT_TYPES = ImmutableList.of(
      ReportAccount.class, ReportCampaign.class, ReportAdGroup.class);
  
  public Representation getHandler() {
    String result = null;

    try {
      getParameters();

      if (topAccountId != null) { // LIST Top Account level

        // Check that the user owns that MCC
        checkAuthentication(topAccountId);

        ObjectifyEntityPersister persister = (ObjectifyEntityPersister)RestServer.getPersister();

        List<Map<String, Object>> reportsList = Lists.newArrayList();
        for (Class<? extends ReportBase> reportyType : REPORT_TYPES) {
          Map<String, Object> reportMap = persister.getReportDataAvailableMonth(reportyType, topAccountId);
          if ( reportMap.size() > 0){
            reportsList.add(reportMap);
          }
        }

        Map<String, Object> resultMap = Maps.newHashMap();
        resultMap.put("reports_available", reportsList);
        resultMap.put("pending_process_tasks", (Object) MccTaskCounter.getPendingProcessTaks(topAccountId));
        resultMap.put("pending_export_tasks", (Object) MccTaskCounter.getPendingExportTaks(topAccountId));
        resultMap.put("pending_refresh_accounts_tasks", (Object) MccTaskCounter.getPendingProcessAccountsTaks(topAccountId));
        result = gson.toJson(resultMap);
      } else {
        throw new IllegalArgumentException("Missing templateId parameter");
      }

    } catch (Exception exception) {
      return handleException(exception);
    }
    addReadOnlyHeaders();
    return createJsonResult(result);
  }
}
