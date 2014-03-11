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

package com.google.api.ads.adwords.jaxws.extensions.authentication;

/**
 * Provides the scope used by AwReports for all OAuth2 authenticated services.
 *  
 * @author joeltoby@google.com (joeltoby)
 *
 */
public class OAuthScope {
  
  private static String SCOPE_ADWORDS = "https://adwords.google.com/api/adwords";

  private static String SCOPE_DRIVE = "https://www.googleapis.com/auth/drive.file";
  
  /**
   * @return the scope used by AwReports for the AdWords API
   */
  public static String getAdWordsScope() {
    return SCOPE_ADWORDS;
  }
  
  /**
   * @return the scope used by AwReports for the Google Drive API
   */
  public static String getGoogleDriveScope() {
    return SCOPE_DRIVE;
  }
}
