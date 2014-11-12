//Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.api.ads.adwords.awreporting.server.appengine.model;

import com.googlecode.objectify.annotation.Index;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Model class used to store Authentication info.
 * 
 * @author jtoledo@google.com (Julian Toledo)
 */
@Entity
@com.googlecode.objectify.annotation.Entity
@Table(name = "AW_USER_TOKEN")
public class UserToken {
  
  public static final String ID = "id";
  public static final String USER_ID = "userId";
  public static final String TOP_ACCOUNT_ID = "topAccountId";

  @Id
  @com.googlecode.objectify.annotation.Id
  @Column(name = "ID")
  private String id;

  @Index
  @Column(name = "USER_ID")
  private String userId;

  @Index
  @Column(name = "TOP_ACCOUNT_ID")
  private Long topAccountId;

  @Column(name = "NAME")
  private String name;

  @Column(name = "OAUTH_TOKEN")
  private String oauthToken;

  @Column(name = "EMAIL")
  private String email;

  @Column(name = "TIMESTAMP")
  protected Date timestamp;
  
  @Column(name = "SCOPE", length = 1024)
  private String scope;

  public UserToken() {
    timestamp = new Date();
  }

  public UserToken(String userId, Long topAccountId, String name, String email, String oauthToken) {
    this.id = userId + String.valueOf(topAccountId);
    this.userId = userId;
    this.topAccountId = topAccountId;
    this.oauthToken = oauthToken;
    this.name = name;
    this.email = email;
    this.timestamp = new Date();
  }

  public String getId() {
    return id;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public Long getTopAccountId() {
    return topAccountId;
  }

  public void setTopAccountId(Long topAccountId) {
    this.topAccountId = topAccountId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getOauthToken() {
    return oauthToken;
  }

  public void setOauthToken(String oauthToken) {
    this.oauthToken = oauthToken;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}
