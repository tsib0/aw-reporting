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
import com.google.api.ads.adwords.awreporting.server.entities.Account;

import org.restlet.representation.Representation;

import java.util.List;

/**
 * Rest entry point to get Accounts
 * 
 * @author jtoledo@google.com (Julian Toledo)
 */
public class AccountRest extends GaeAbstractServerResource {

  public Representation getHandler() {
    String result = null;
    try {
      checkAuthentication();
      getParameters();

      if (topAccountId != null) { // LIST Top Account level
        // Check that the user owns that MCC
        checkAuthentication(topAccountId);
        result = gson.toJson(RestServer.getPersister().get(Account.class, Account.TOP_ACCOUNT_ID, topAccountId));
      }

      if (accountId != null) { // LIST Account level
        List<Account> accounts = RestServer.getPersister().get(Account.class, Account.ID, accountId);
        // Check that the user owns that MCC
        checkAuthentication(accounts.get(0).getTopAccountId());
        result = gson.toJson(accounts.get(0));
      }

    } catch (Exception exception) {
      return handleException(exception);
    }
    addReadOnlyHeaders();
    return createJsonResult(result);
  }
}
