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

package com.google.api.ads.adwords.awreporting.server.appengine.rest;

import com.google.api.ads.adwords.awreporting.server.appengine.RestServer;
import com.google.api.ads.adwords.awreporting.server.appengine.model.UserToken;
import com.google.api.ads.adwords.awreporting.server.rest.AbstractServerResource;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.collect.Maps;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Main Class for Rest entry points
 * 
 * @author jtoledo@google.com (Julian Toledo)
 */
public abstract class GaeAbstractServerResource extends AbstractServerResource {
  
  protected static final Logger LOGGER = Logger.getLogger(GaeAbstractServerResource.class.getName());

  private static final String DATE_FORMAT_SHORT_WITHOUTDAY = "yyyyMM";
  protected static final DateTimeFormatter dfYearMonthNoDash = DateTimeFormat.forPattern(DATE_FORMAT_SHORT_WITHOUTDAY);

  // HTTP Parameters
  protected boolean task = false;
  protected String month = null;
  protected String monthStart = null;
  protected String monthEnd = null;
  protected String authToken = null;
  protected String developerToken = null;
  protected String reportDefinition = null;
  protected String reportClassName = null;
  protected String state = null;
  protected String code = null;
  protected String userId = null;
  protected String other = null;
  protected boolean includeZeroImpressions = false;

  protected void getParameters() {

    super.getParameters();

    try {
      // Params from URL
      other = (String) getRequestAttributes().get("other");

      String taskString = this.getReference().getQueryAsForm().getFirstValue("task");
      String includeZeroImpressionsString = this.getReference().getQueryAsForm().getFirstValue("includeZeroImpressions");
      
      month = this.getReference().getQueryAsForm().getFirstValue("month");
      monthStart = this.getReference().getQueryAsForm().getFirstValue("monthStart");
      monthEnd = this.getReference().getQueryAsForm().getFirstValue("monthEnd");
      authToken = this.getReference().getQueryAsForm().getFirstValue("authToken");
      developerToken = this.getReference().getQueryAsForm().getFirstValue("developerToken");
      reportDefinition = this.getReference().getQueryAsForm().getFirstValue("reportDefinition");
      reportClassName = this.getReference().getQueryAsForm().getFirstValue("reportClassName");
      
      task = (taskString != null && taskString.equals("true"));
      includeZeroImpressions = (includeZeroImpressionsString != null && includeZeroImpressionsString.equals("true"));

      state = this.getReference().getQueryAsForm().getFirstValue("state");
      code = this.getReference().getQueryAsForm().getFirstValue("code");

    } catch(Exception exception) {
      throw new IllegalArgumentException(exception);
    }

    userId = getCurrentUser();
  }

  protected String getCurrentUser() {
    UserService userService = UserServiceFactory.getUserService();
    return userService.getCurrentUser().getUserId();
  }

  protected boolean checkAuthentication() throws Exception {
    Map<String,Object> map = Maps.newHashMap();
    map.put("userId", getCurrentUser());
    List<UserToken> userTokenList = RestServer.getPersister().get(UserToken.class, map);
    if (userTokenList != null && userTokenList.size() > 0) {
      return true;
    }
    throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, "Page FORBIDDEN for User");
  }

  protected boolean checkAuthentication(Long topAccountId) throws Exception {
    Map<String,Object> map = Maps.newHashMap();
    map.put(UserToken.USER_ID, getCurrentUser());
    map.put(UserToken.TOP_ACCOUNT_ID, topAccountId);
    List<UserToken> userTokenList = RestServer.getPersister().get(UserToken.class, map);
    if (userTokenList != null && userTokenList.size() > 0) {
      return true;
    }
    throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, "Page FORBIDDEN for User");
  }
}
