// Copyright 2013 Google Inc. All Rights Reserved.
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

package com.google.api.ads.adwords.jaxws.extensions.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.api.ads.adwords.jaxws.extensions.authentication.Authenticator;
import com.google.api.ads.common.lib.exception.OAuthException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;

@Component
/**
 * Provides an authenticated Google {@link Drive} service instance configured for AW Reports to DB.
 *   
 * @author joeltoby
 *
 */
public class GoogleDriveService { //extends Drive {
  private Authenticator authenticator;
  private Drive service;

  //  @Autowired
  public GoogleDriveService() throws OAuthException {
    // TODO (joeltoby) Fix this.
    // Inject authenticator into constructor
    //    super(
    //        new NetHttpTransport(),
    //        new JacksonFactory(),
    //        authenticator.getOAuth2Credential());

    service =  new Drive.Builder(
        new NetHttpTransport(),
        new JacksonFactory(),
        authenticator.getOAuth2Credential())
    .setApplicationName("AW Reports to DB").build();
  }

  /**
   * Temporary method to get an instance of {@link Drive} configured for AW Reports.
   * TODO (joeltoby) to be removed once this class extends {@link Drive}
   * @return
   */
  public Drive getDriveService() {
    return service;
  }

  /**
   * @param authentication
   *            the helper class for Auth
   */
  @Autowired
  public void setAuthentication(Authenticator authenticator) {
    this.authenticator = authenticator;
  }
}