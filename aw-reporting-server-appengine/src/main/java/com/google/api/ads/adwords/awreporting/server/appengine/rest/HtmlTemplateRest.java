//Copyright 2014 Google Inc. All Rights Reserved.
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
import com.google.api.ads.adwords.awreporting.server.entities.HtmlTemplate;
import com.google.api.client.util.Lists;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rest entry point to get, create or update HTML templates for a user.
 * 
 * @author joeltoby@google.com
 */
public class HtmlTemplateRest extends GaeAbstractServerResource {

  /**
   * Gets a template by templateId or a complete list
   * of all the user's templates if no templateId is provided.
   * 
   * @return a JSON array of {@link HtmlTemplate}s. If a template ID is included in the request,
   * the single template will still be returned within an array.
   */
  public Representation getHandler() {
    String result = null;

    try {
      checkAuthentication();
      getParameters();

      UserService userService = UserServiceFactory.getUserService();
      if (userService != null && userService.getCurrentUser() != null) {
        LOGGER.info("User is not NULL. Getting templates");
        List<HtmlTemplate> htmlTemplateList = Lists.newArrayList();

        // Get template by ID
        if (templateId != null) {
          Map<String, Object> propertiesMap = new HashMap<String, Object>();
          propertiesMap.put(HtmlTemplate.USER_ID, userId);
          LOGGER.info("templateId: " + templateId);
          propertiesMap.put(HtmlTemplate.ID, templateId);

          htmlTemplateList = RestServer.getPersister().get(HtmlTemplate.class, propertiesMap);

          if (htmlTemplateList.size() == 0) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No template with that templateId was found");
          }

        } else if (isPublic == true) {
          LOGGER.info("Getting public templates");
          htmlTemplateList = RestServer.getPersister().get(HtmlTemplate.class, "isPublic", true);

        } else {
          // Get all user's templates
          htmlTemplateList = RestServer.getPersister().get(
              HtmlTemplate.class, HtmlTemplate.USER_ID, userId);
        }

        result = gson.toJson(htmlTemplateList);
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
      getParameters();

      UserService userService = UserServiceFactory.getUserService();
      if (userService != null && userService.getCurrentUser() != null && templateId != null) {

        // Delete template by ID
        Map<String, Object> propertiesMap = new HashMap<String, Object>();
        propertiesMap.put(HtmlTemplate.USER_ID, userId);
        LOGGER.info("Deleting template ID: " + templateId);
        propertiesMap.put(HtmlTemplate.ID, templateId);

        List<HtmlTemplate> templatesToDelete = 
            RestServer.getPersister().get(HtmlTemplate.class, propertiesMap);
        RestServer.getPersister().remove(templatesToDelete);

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

  public Representation postPutHandler(String json) {
    String result = null;
    try {
      getParameters();

      HtmlTemplate template = new Gson().fromJson(json, HtmlTemplate.class);

      // Set the userId on the template and save it
      template.setUserId(userId);
      LOGGER.info("Persisting template...");
      HtmlTemplate savedTemplate = RestServer.getPersister().save(template);

      result = gson.toJson(savedTemplate);

    } catch (Exception exception) {
      return handleException(exception);
    }
    addHeaders();    
    return createJsonResult(result);
  }
}
