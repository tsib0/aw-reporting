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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import org.restlet.data.Status;
import org.restlet.representation.Representation;

/**
 * Rest entry point for User info
 * 
 * @author jtoledo@google.com (Julian Toledo)
 */
public class UserRest extends GaeAbstractServerResource {

  public Representation getHandler() {
    String result = null;
    try {
      getParameters();

      if (other == null) {
        UserService userService = UserServiceFactory.getUserService();
        result = gson.toJson(userService.getCurrentUser());
      }

      if (other != null && other.equals("login")) {
        UserService userService = UserServiceFactory.getUserService();
        String url = userService.createLoginURL("/");
        this.setStatus(Status.REDIRECTION_FOUND);
        this.setLocationRef(url);
        result = "";
      }

      if (other != null && other.equals("logout")) {
        UserService userService = UserServiceFactory.getUserService();
        String url = userService.createLogoutURL("/");
        this.setStatus(Status.REDIRECTION_FOUND);
        this.setLocationRef(url);
        result = "";
      }

    } catch (Exception exception) {
      return handleException(exception);
    }
    addReadOnlyHeaders();
    return createJsonResult(result);
  }
}
