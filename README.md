#Stormpath is Joining Okta
We are incredibly excited to announce that [Stormpath is joining forces with Okta](https://stormpath.com/blog/stormpaths-new-path?utm_source=github&utm_medium=readme&utm-campaign=okta-announcement). Please visit [the Migration FAQs](https://stormpath.com/oktaplusstormpath?utm_source=github&utm_medium=readme&utm-campaign=okta-announcement) for a detailed look at what this means for Stormpath users.

We're available to answer all questions at [support@stormpath.com](mailto:support@stormpath.com).


## Exchange Facebook Access Token for Stormpath Access & Resfresh Tokens

## IMPORTANT NOTE: This uses a Stormpath API that will be going away soon. It is intended for demonstration purposes only. The Java SDK will soon have native support or Social Logins

### Setup

1. Create a Stormpath account [here](https://api.stormpath.com/register).
2. Create a Facebook application [here](https://developers.facebook.com/).
   * Note the app id and app secret of the Facebook app
3. Map a Facebook Directory to your Stormpath application in the Stormpath [admin console](https://api.stormpath.com).
   * You'll need to put in the Facebook app id and secret from the previous step

### Running the app

This is a Spring Boot App that uses the raw Stormpath Java SDK

To run the app, do the following:

```
mvn clean package

STORMPATH_API_KEY_ID=<your api key id> \
STORMPATH_API_KEY_SECRET=<your api key secret> \
STORMPATH_APPLICATION_HREF=<you application href> \
FACEBOOK_APP_ID=<your facebook app id> \
java -jar target/*.jar
```

### Notes and Disclaimers

* There is an expectation that the email address retrieved from facebook exists in a Stormpath Account. If this is not
the case, the app will blow up in glorious fashion

* There are no tests and there is no error checking in the app. This is for demonstrations purposes only.

* Each time you hit the button, a new pair of tokens including an access token and a refresh token is created. You could
easily add a check to see if there's an existing token pair and return that instead of creating a new set.

### What's Happening Behind the Scenes

1. When you click the big button, the Facebook javascript library is used for you to authorize the app and login to Facebook

    ```
    FB.login(function (res) {
        if (res.status === 'connected' && res.authResponse && res.authResponse.accessToken) {
            // hit endpoint
            exchangeAccessToken(res.authResponse);
        } else {
            // show error
            console.log("something went wrong");
            console.log(res);
        }
    }, { scope: 'email' });
    ```


2. Upon successful authorization and login, the Facebook access token is passed up to a Spring Boot controller

    ```
    $.ajax({
        method: 'post',
        url: '/exchange',
        contentType: 'application/json',
        data: JSON.stringify(data),
        success: function (tokenResponse) {
            // Stormpath token response is displayed
        }
    });
    ```

3. The Spring Boot Controller verifies the Facebook Access Token and hits Stormpath to return a response containing a Stormpath Access Token and Refresh Token pair

    ```
    @RequestMapping("/exchange")
    public @ResponseBody StormpathTokenResponse exchange(@RequestBody FBAuth auth) throws IOException {

        Account account = stormpathCommunicationService.getAccountFromFBToken(auth);

        return stormpathCommunicationService.getTokenResponseFromEmail(account.getEmail());
    }
    ```

See the [function.js](https://github.com/stormpath/stormpath-exchange-fb-access-token-example/blob/master/src/main/resources/static/functions.js), 
[FBCommunicationService.java](https://github.com/stormpath/stormpath-exchange-fb-access-token-example/blob/master/src/main/java/com/stormpath/example/service/FBCommunicationService.java), and
[StormpathCommunicationService.java](https://github.com/stormpath/stormpath-exchange-fb-access-token-example/blob/master/src/main/java/com/stormpath/example/service/StormpathCommunicationService.java) 
files for more information.
