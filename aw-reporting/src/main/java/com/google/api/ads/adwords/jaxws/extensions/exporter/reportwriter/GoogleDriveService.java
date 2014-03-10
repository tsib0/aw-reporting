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

package com.google.api.ads.adwords.jaxws.extensions.exporter.reportwriter;

import com.google.api.ads.adwords.jaxws.extensions.authentication.Authenticator;
import com.google.api.ads.common.lib.exception.OAuthException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;

/**
 * Provides an authenticated Google {@link Drive} service instance configured for AW Reports to DB.
 *   
 * @author joeltoby
 *
 */
public class GoogleDriveService {

  private Authenticator authenticator;
  private Drive service;

  public GoogleDriveService(Authenticator authenticator) {
    this.authenticator = authenticator;
  }

  /**
   * Temporary method to get an instance of {@link Drive} configured for AW Reports.
   * TODO (joeltoby) to be removed once this class extends {@link Drive}
   * @return
   * @throws OAuthException 
   */
  public Drive getDriveService() throws OAuthException {
    if (service == null ) {
      service =  new Drive.Builder(new NetHttpTransport(), new JacksonFactory(),
          authenticator.getOAuth2Credential()).setApplicationName("AW Reports to DB").build();
    }
    return service;
  }
}