//Copyright 2012 Google Inc. All Rights Reserved.
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.api.ads.adwords.awreporting.server.appengine.rest;

import com.google.api.ads.adwords.awreporting.server.appengine.RestServer;
import com.google.api.ads.adwords.awreporting.server.rest.AbstractBaseResource;
import com.google.gson.Gson;

import org.json.JSONException;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import java.util.Map;

/**
 * Rest entry point for OAuth2
 * 
 * @author jtoledo@google.com (Julian Toledo)
 */
public class OAuthRest extends AbstractBaseResource {
  
  private static final String RETURN_URL = "/index.html#/new_mcc/";
  
  private static final String RETURN_ERROR_URL = "/index.html#/error/";

  public Representation getHandler() {
    String result = null;

    try {

      Long topAccountId = getParameterAsLong("topAccountId");
      String code = getParameter("code");
      String state = getParameter("state");

      if (topAccountId != null && state == null) {
        // Redirect the user to OAuth
        String url = RestServer.getAuthenticator().getOAuth2Url(topAccountId);
        this.setStatus(Status.REDIRECTION_TEMPORARY);
        this.setLocationRef(url);
        result = "";
      }

      if (code != null && code.length() > 0 && state != null && state.length() > 0) {

        String topAccountIdFromState = "";
        String returnUrl = RETURN_URL;
        // State contains TopAccountId and Return URL without any separator
        if (state.indexOf("http") < 0) {
          topAccountIdFromState = state;
          returnUrl +=  topAccountId;
        } else {
          topAccountIdFromState = state.substring(0, state.indexOf("http"));
          returnUrl = state.substring(state.indexOf("http"));
        }

        RestServer.getAuthenticator().processOAuth2Credential(code, topAccountIdFromState);
        this.setStatus(Status.REDIRECTION_FOUND);
        this.setLocationRef(returnUrl);
        result = "";
      }

    } catch (Exception exception) {

      // Routing auth error to RETURN_ERROR_URL
      JsonRepresentation representation = (JsonRepresentation) handleException(exception);
      try {
        String error = representation.getJsonObject().get("error").toString();
        this.setStatus(Status.REDIRECTION_FOUND);
        this.setLocationRef(RETURN_ERROR_URL + error);
      } catch (JSONException e) {
        return representation;
      }
      result = "";

    }
    addReadOnlyHeaders();
    return createJsonResult(result);
  }

  public Representation postPutHandler(String json) {

    try {

      Long topAccountId = Long.valueOf((String) new Gson().fromJson(json, Map.class).get("topAccountId"));

      // Redirect the user to OAuth
      String url = RestServer.getAuthenticator().getOAuth2Url(topAccountId);
      this.setStatus(Status.REDIRECTION_TEMPORARY);
      this.setLocationRef(url);
      addReadOnlyHeaders();

    } catch (Exception exception) {
      return handleException(exception);
    }
    addHeaders();
    return createJsonResult("");
  }
}
