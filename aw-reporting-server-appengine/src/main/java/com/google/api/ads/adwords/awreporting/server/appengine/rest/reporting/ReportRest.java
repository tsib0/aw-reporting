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

import com.google.api.ads.adwords.awreporting.model.entities.Report;
import com.google.api.ads.adwords.awreporting.model.entities.ReportBase;
import com.google.api.ads.adwords.awreporting.server.appengine.RestServer;
import com.google.api.ads.adwords.awreporting.server.appengine.rest.GaeAbstractServerResource;

import org.restlet.representation.Representation;

import java.util.List;

/**
 * Rest entry point generic for all Report Types
 * 
 * @author jtoledo@google.com (Julian Toledo)
 */
public abstract class ReportRest<ReportSub extends Report> extends GaeAbstractServerResource {

  protected Class<ReportSub> classReportSub;

  public Representation getHandler() {
    String result = null;

    try {
      checkAuthentication();
      getParameters();

      if (topAccountId != null) { // LIST Top Account level
        
        // Check that the user owns that MCC
        checkAuthentication(topAccountId);
        
        List<ReportSub> listReports;

        if (dateStart != null && dateEnd != null) {

          listReports = RestServer.getPersister().get(classReportSub, Report.TOP_ACCOUNT_ID, topAccountId,
              ReportBase.MONTH, dateStart, dateEnd);

        } else {
          listReports =  RestServer.getPersister().get(classReportSub, Report.TOP_ACCOUNT_ID, topAccountId);
        }

        if (listReports != null) {
          result = gson.toJson(listReports);
        }
      }

      if (topAccountId != null && accountId != null) { // LIST Account level

        // Check that the user owns that MCC
        checkAuthentication(topAccountId);

        List<ReportSub> listReports;

        if (dateStart != null && dateEnd != null) {

          listReports = RestServer.getPersister().get(classReportSub, Report.ACCOUNT_ID, accountId,
              ReportBase.MONTH, dateStart, dateEnd);

        } else {
          listReports =  RestServer.getPersister().get(classReportSub, Report.ACCOUNT_ID, accountId);
        }

        if (listReports != null) {
          result = gson.toJson(listReports);
        }
      }

    } catch (Exception exception) {
      return handleException(exception);
    }
    addReadOnlyHeaders();
    return createJsonResult(result);
  }
}
