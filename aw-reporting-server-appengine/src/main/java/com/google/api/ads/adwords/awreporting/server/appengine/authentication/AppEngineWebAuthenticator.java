package com.google.api.ads.adwords.awreporting.server.appengine.authentication;

import com.google.api.ads.adwords.awreporting.server.appengine.RestServer;
import com.google.api.ads.adwords.awreporting.server.appengine.model.UserToken;
import com.google.api.ads.adwords.awreporting.server.authentication.WebAuthenticator;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.collect.Maps;

import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.util.List;
import java.util.Map;

public class AppEngineWebAuthenticator implements WebAuthenticator {

  public String getCurrentUser() {
    UserService userService = UserServiceFactory.getUserService();
    if (userService != null) {
      return userService.getCurrentUser().getUserId();
    } else {
      throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, "Page FORBIDDEN for User");
    }
  }

  public boolean checkAuthentication() throws Exception {
    Map<String,Object> map = Maps.newHashMap();
    map.put("userId", getCurrentUser());
    List<UserToken> userTokenList = RestServer.getPersister().get(UserToken.class, map);
    if (userTokenList != null && userTokenList.size() > 0) {
      return true;
    }
    throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, "Page FORBIDDEN for User");
  }

  public boolean checkAuthentication(Long topAccountId) throws Exception {
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
