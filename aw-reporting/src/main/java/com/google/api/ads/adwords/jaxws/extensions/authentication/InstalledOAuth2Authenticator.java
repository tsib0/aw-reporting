package com.google.api.ads.adwords.jaxws.extensions.authentication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.api.ads.adwords.jaxws.extensions.authentication.OAuthScope.SCOPE_TYPE;
import com.google.api.ads.adwords.jaxws.extensions.exporter.reportwriter.ReportWriterType;
import com.google.api.ads.adwords.jaxws.extensions.report.model.entities.AuthMcc;
import com.google.api.ads.adwords.jaxws.extensions.report.model.persistence.AuthTokenPersister;
import com.google.api.ads.adwords.lib.client.AdWordsSession;
import com.google.api.ads.common.lib.exception.OAuthException;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

/**
 * Handles OAuth2 authentication for Installed application instances
 * 
 * @author jtoledo@google.com (jtoledo@google.com)
 * @author joeltoby@google.com (joeltoby@google.com)
 *
 */
@Component
public class InstalledOAuth2Authenticator implements Authenticator {

  private static final Logger LOGGER = Logger.getLogger(InstalledOAuth2Authenticator.class);

  private static final String USER_AGENT = "AwReporting";

  private static final String CALLBACK_URL = "urn:ietf:wg:oauth:2.0:oob";

  // AdWords Authentication Properties
  private String clientId = null;
  private String clientSecret = null;
  private String developerToken = null;
  private String scope = null;
  private AuthTokenPersister authTokenPersister;
  Credential oAuth2Credential = null;

  /**
   * Constructor.
   * 
   * @param developerToken
   *            the developer token
   * @param clientId
   *            the OAuth2 authentication clientId
   * @param clientSecret
   *            the OAuth2 authentication clientSecret
   */
  @Autowired
  public InstalledOAuth2Authenticator(
      @Value("${developerToken}") String developerToken,
      @Value(value = "${clientId}") String clientId,
      @Value(value = "${clientSecret}") String clientSecret,
      @Value(value = "${aw.report.processor.reportwritertype:}") ReportWriterType reportWriterType) {
    this.developerToken = developerToken;
    this.clientId = clientId;
    this.clientSecret = clientSecret;

    /*
     * Google Drive API will be included in the scope if it is used for 
     * writing PDF files.
     */
    if (reportWriterType != null 
        && reportWriterType == ReportWriterType.GoogleDriveWriter) {
      scope = OAuthScope.getScopeCsv(SCOPE_TYPE.ADWORDS, SCOPE_TYPE.DRIVE);
    } else {
      scope = OAuthScope.getScopeCsv(SCOPE_TYPE.ADWORDS);
    }
  }


  /* (non-Javadoc)
   * @see com.google.api.ads.adwords.jaxws.extensions.authentication.Authenticator#authenticate(java.lang.String, boolean)
   */
  public AdWordsSession.Builder authenticate(String mccAccountId, boolean force)
      throws OAuthException {

    LOGGER.debug("Retrieving auth token from DB.");
    String authToken;
    try {
      authToken = this.retrieveAuthToken(mccAccountId, force);
    } catch(OAuthException e) {
      System.out.println(e.getMessage());

      if (e.getMessage().contains("Credential could not be refreshed")) {
        System.out.println("**ERROR** Credential could not be refreshed,"
            + " we need a new OAuth2 Token");

        oAuth2Credential = this.getOAuth2Credential();
        authToken = getOAuth2Credential().getRefreshToken();
        
        LOGGER.debug("Saving Refresh Token to DB...\n");
        this.saveAuthTokenToStorage(mccAccountId, authToken, scope);

      } else {
        e.printStackTrace();
        throw e;
      }
    }

    oAuth2Credential = null;

    if(authToken == null) {
      oAuth2Credential = getOAuth2Credential();
    } else {
      oAuth2Credential = this.buildOAuth2Credentials(authToken);
    }

    try {
      oAuth2Credential = this.getOAuth2Credential();

    } catch (OAuthException e) {
      System.out.println(e.getMessage());

      if (e.getMessage().contains("Credential could not be refreshed")) {
        System.out.println("**ERROR** Credential could not be refreshed,"
            + " we need a new OAuth2 Token");

        oAuth2Credential = this.getOAuth2Credential();
        authToken = getOAuth2Credential().getRefreshToken();

        System.out.println("Saving Refresh Token to DB...\n");
        this.saveAuthTokenToStorage(mccAccountId, authToken, scope);

      } else {
        e.printStackTrace();
        throw e;
      }
    }

    return new AdWordsSession.Builder()
    .withOAuth2Credential(oAuth2Credential)
    .withUserAgent(USER_AGENT)
    .withClientCustomerId(mccAccountId)
    .withDeveloperToken(this.developerToken);
  }

  /**
   * Builds the OAuth 2.0 credential for the user with a known authToken
   * 
   * @param authToken
   *            the last authentication token
   * @return the new {@link Credential}
   * @throws OAuthException
   *             error creating the credentials
   */
  private Credential buildOAuth2Credentials(String authToken) {

    Credential credential = new GoogleCredential.Builder().setClientSecrets(clientId, clientSecret)
        .setJsonFactory(new JacksonFactory()).setTransport(new NetHttpTransport()).build()
        .setRefreshToken(authToken);

    return credential;
  }


  /**
   * Obtains an OAuth {@link Credential} configured for AW Reports by doing the OAuth dance.
   * This method should be invoked for any users for which a refresh token is not known or is
   * invalid.
   * 
   * @return
   *    The OAuth2 credentials. The scope of the token generated depends on the properties file
   *    configuration.  For example<br>
   *    If writing PDF reports to Google Drive, the scope of the token will include both AdWords and Drive.
   *    
   * @throws OAuthException
   *    If an error is encountered when trying to obtain a token.
   */
  public Credential getOAuth2Credential()
      throws OAuthException {
    if(oAuth2Credential != null) {
      return oAuth2Credential;
    } else {
      GoogleAuthorizationCodeFlow authorizationFlow = getAuthorizationFlow();

      String authorizeUrl =
          authorizationFlow.newAuthorizationUrl().setRedirectUri(CALLBACK_URL).build();

      System.out.println("\n**ACTION REQUIRED** Paste this url in your browser"
          + " and authenticate using your **AdWords Admin Email**: \n\n" + authorizeUrl + '\n');

      // Wait for the authorization code.
      System.out.println("Type the code you received on the web page here: ");
      try {
        String authorizationCode = new BufferedReader(new InputStreamReader(System.in)).readLine();

        // Authorize the OAuth2 token.
        GoogleAuthorizationCodeTokenRequest tokenRequest =
            authorizationFlow.newTokenRequest(authorizationCode);
        tokenRequest.setRedirectUri(CALLBACK_URL);
        GoogleTokenResponse tokenResponse = tokenRequest.execute();

        //  Create the credential.
        Credential credential = new GoogleCredential.Builder().setClientSecrets(clientId, clientSecret)
            .setJsonFactory(new JacksonFactory()).setTransport(new NetHttpTransport()).build().setFromTokenResponse(tokenResponse); 

        // Set authorized credentials.
        credential.setFromTokenResponse(tokenResponse);

        return credential;
      } catch(IOException e) {
        throw new OAuthException("An error occured during the Google token request.",
            e.getCause());
      }
    }
  }

  private GoogleAuthorizationCodeFlow getAuthorizationFlow() {
    GoogleAuthorizationCodeFlow authorizationFlow = new GoogleAuthorizationCodeFlow.Builder(
        new NetHttpTransport(), new JacksonFactory(), clientId, clientSecret,
        Arrays.asList(scope.split("\\s*,\\s*")))
    .setAccessType("offline").setApprovalPrompt("force").build();

    return authorizationFlow;
  }


  /**
   * Retrieves the authentication token by refreshing it if necessary, and
   * persisting the updated token into the DB.
   * 
   * @param force
   *            if the actual token should be refreshed
   * @return the valid token for the moment
   * @throws IOException
   *             error connecting to authentication server
   * @throws OAuthException
   *             error on the OAuth process
   */
  private String retrieveAuthToken(String mccAccountId, boolean force) throws OAuthException {

    LOGGER.debug("Retrieving auth token from DB.");

    AuthMcc authMcc = this.getAuthTokenFromStorage(mccAccountId);
    String authToken = null;

    // Check the Scope of the Auth on DB
    if (authMcc == null || authMcc.getScope() == null
        || !authMcc.getScope().equals(scope)) {
      force = true;
    } else {
      authToken = authMcc.getAuthToken();
    }

    // Generate a new Auth token if necessary
    if ((authMcc == null || force)) {
      try {
        if (force) {
          LOGGER.debug("Token refresh FORCED. Getting a new one.");
          
        } else {
          LOGGER.debug("Token not found. Getting one.");
        }
        authToken = getOAuth2Credential().getRefreshToken();

      } catch (OAuthException e) {
        if (e.getMessage().contains("Connection reset")) {
          LOGGER.info("Connection reset when getting authToken, retrying...");
          this.authenticate(mccAccountId, true);
          
        } else {
          LOGGER.error("Error authenticating: " + e.getMessage());
          e.printStackTrace();
          throw e;
        }
      }
      LOGGER.info("Saving Refresh Token to DB...");
      this.saveAuthTokenToStorage(mccAccountId, authToken, scope);
    }
    return authToken;
  }

  /**
   * The implementation must persist the token to be retrieved later.
   * 
   * @param mccAccountId
   *            the MCC account ID.
   * @param authToken
   *            the authentication token.
   */
  private void saveAuthTokenToStorage(String mccAccountId, String authToken, String scope) {
    LOGGER.debug("Persisting refresh token...");
    AuthMcc authMcc = new AuthMcc(mccAccountId, authToken, scope);
    this.authTokenPersister.persistAuthToken(authMcc);
    LOGGER.debug("... success.");
  }

  /**
   * The implementation should retrieve the authentication token previously
   * persisted.
   * 
   * @param mccAccountId
   *            the MCC account ID.
   * @return the authentication token.
   */
  private AuthMcc getAuthTokenFromStorage(String mccAccountId) {

    AuthMcc authToken = this.authTokenPersister.getAuthToken(mccAccountId);
    if (authToken != null) {
      return authToken;
    }
    return null;
  }

  /**
   * @param authTokenPersister
   *            the authTokenPersister to set
   */
  @Autowired
  public void setAuthTokenPersister(AuthTokenPersister authTokenPersister) {
    this.authTokenPersister = authTokenPersister;
  }
}
