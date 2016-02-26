$(document).ready(function() {
    $.ajaxSetup({ cache: true });
    $.getScript('//connect.facebook.net/en_US/sdk.js', function () {
        FB.init({
            appId: fbAppId,
            version: 'v2.5' // or v2.0, v2.1, v2.2, v2.3
        });
    });

    var exchangeAccessToken = function exchangeAccessToken(data) {
        $.ajax({
            method: 'post',
            url: '/exchange',
            contentType: 'application/json',
            data: JSON.stringify(data),
            success: function (tokenResponse) {
                console.log(tokenResponse);

                var accessTokenParts = tokenResponse.accessToken.split('.');
                $.each(['header', 'payload', 'signature'], function (index, value) {
                    $('#access-token-' + value).html(accessTokenParts[index]);
                });
                $('#token-div').show();
            }
        });
    };

    $('#loginbutton').click(function () {
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
    });
});
