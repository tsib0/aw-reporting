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
import com.google.api.ads.adwords.awreporting.server.rest.AbstractBaseResource;
import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;

import org.restlet.representation.Representation;

import java.util.List;
import java.util.Map;

/**
 * Rest entry point to get MCCs for the User
 * 
 * @author jtoledo@google.com (Julian Toledo)
 * @author joeltoby@google.com (Joel Toby)
 */
public class MccRest extends AbstractBaseResource {

  public Representation getHandler() {
    String result = null;

    try {

      Long topAccountId = getParameterAsLong("topAccountId");
      String userId = RestServer.getWebAuthenticator().getCurrentUser();

      if (topAccountId != null) {

        Map<String, Object> map = Maps.newHashMap();
        map.put(UserToken.USER_ID, userId);
        map.put(UserToken.TOP_ACCOUNT_ID, topAccountId);
        result = gson.toJson(RestServer.getPersister().get(UserToken.class, map).get(0));

      } else {

        List<UserToken> userTokenList = Lists.newArrayList();
        userTokenList = RestServer.getPersister().get(UserToken.class, UserToken.USER_ID, userId);
        result = gson.toJson(userTokenList);
      }

    } catch (Exception exception) {
      return handleException(exception);
    }
    addReadOnlyHeaders();
    return createJsonResult(result);
  }

  public Representation deleteHandler() {
    String result = null;

    try {
      RestServer.getWebAuthenticator().checkAuthentication();

      Long topAccountId = getParameterAsLong("topAccountId");
      String userId = RestServer.getWebAuthenticator().getCurrentUser();

      if (topAccountId != null) {

        LOGGER.info("Deleting UserToken for CID: " + topAccountId);

        Map<String, Object> map = Maps.newHashMap();
        map.put(UserToken.USER_ID, userId);
        map.put(UserToken.TOP_ACCOUNT_ID, topAccountId);
        List<UserToken> userTokensToDelete = RestServer.getPersister().get(UserToken.class, map);

        RestServer.getPersister().remove(userTokensToDelete);

        result = "OK";
      } else {
        throw new IllegalArgumentException("Missing topAccountId or problem with the authenticated user");
      }

    } catch (Exception exception) {
      return handleException(exception);
    }
    addHeaders();
    return createJsonResult(result);
  }
}
