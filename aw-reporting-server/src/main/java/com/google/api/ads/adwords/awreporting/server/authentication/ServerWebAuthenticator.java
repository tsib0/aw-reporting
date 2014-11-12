// Copyright 2014 Google Inc. All Rights Reserved.
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

package com.google.api.ads.adwords.awreporting.server.authentication;

import org.springframework.stereotype.Component;

/**
 * Defines commmon methods for Web Authentication and user management
 * for the Standalone Server
 * 
 * @author jtoledo@google.com (Julian Toledo)
 */
@Component
public class ServerWebAuthenticator implements WebAuthenticator {

  //TODO: Pending implementation
  @Override
  public String getCurrentUser() {
    // TODO Auto-generated method stub
    return null;
  }

  //TODO: Pending implementation
  @Override
  public boolean checkAuthentication() throws Exception {

    return true;
  }

  //TODO: Pending implementation
  @Override
  public boolean checkAuthentication(Long topAccountId) throws Exception {
    return true;
  }
}
