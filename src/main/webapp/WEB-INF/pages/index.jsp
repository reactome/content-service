<%--suppress XmlPathReference HtmlUnknownTarget --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:import url="header.jsp"/>

<script>

    <%-- Swagger Documentation - https://github.com/swagger-api/swagger-ui --%>
    var deployPath = window.location.pathname.substr(0, window.location.pathname.lastIndexOf("/"));
    <%-- IE fix: if not present manually construct window.location.origin as IE does not support it directly --%>
    if (!window.location.origin) {
        window.location.origin = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port : '');
    }

    // Pre load translate...
    if (window.SwaggerTranslator) {
        window.SwaggerTranslator.translate();
    }

    window.swaggerUi = new SwaggerUi({
        url: window.location.origin + deployPath + "/v2/api-docs", <%-- v2 is the new swagger --%>
        dom_id: "swagger-ui-container", <%-- div container where the documentation will be loaded  --%>
        onComplete: function (swaggerApi, swaggerUi) {
            if (typeof initOAuth == "function") {
                initOAuth({
                    clientId: "your-client-id",
                    clientSecret: "your-client-secret-if-required",
                    realm: "your-realms",
                    appName: "your-app-name",
                    scopeSeparator: ",",
                    additionalQueryStringParams: {}
                });
            }

            if (window.SwaggerTranslator) {
                window.SwaggerTranslator.translate();
            }

            //addApiKeyAuthorization();

            <%-- CSS tweak --%>
            <%-- swagger has <div class="footer"> which collides to wordpress footer style --%>
            $(".swagger-section .footer").attr("class", "swagger-footer");
            $('.swagger-footer').css('padding-top', '25px');

            <%-- Apply wordpress font style in wordpress header and footer --%>
            $('div.footer').css('font', '13px/1.5 "Helvetica Neue", Arial, "Liberation Sans", FreeSans, sans-serif');
            $('div.navwrapper').css('font', '13px/1.5 "Helvetica Neue", Arial, "Liberation Sans", FreeSans, sans-serif');

        },
        docExpansion: "none",
        apisSorter: "alpha",
        defaultModelRendering: 'model',
        operationsSorter: 'alpha',
        showRequestHeaders: false
    });

    /*
    function addApiKeyAuthorization() {
        var key = encodeURIComponent($('#input_apiKey')[0].value);
        if (key && key.trim() != "") {
            var apiKeyAuth = new SwaggerClient.ApiKeyAuthorization("api_key", key, "query");
            window.swaggerUi.api.clientAuthorizations.add("api_key", apiKeyAuth);
            log("added key " + key);
        }
    }

    $('#input_apiKey').change(addApiKeyAuthorization);
    //if you have an apiKey you would like to pre-populate on the page for demonstration purposes...

    var apiKey = "myApiKeyXXXX123456789";
    $('#input_apiKey').val(apiKey);
    */

    window.swaggerUi.load();

</script>

<div style="margin-top: 20px; margin-bottom: 10px">
    <div class="reset-swagger">
        <div class="swagger-section">

            <%--
            <div id="header">
                <div class="swagger-ui-wrap">
                    <a id="logo" href="http://swagger.io"><img class="logo__img" alt="swagger" height="30" width="30"
                                                               src="swagger/images/logo_small.png"/><span class="logo__title">swagger</span></a>

                    <form id='api_selector'>
                        <div class='input'><input placeholder="http://example.com/api" id="input_baseUrl" name="baseUrl"
                                                  type="text"/></div>
                        <div id='auth_container'></div>
                        <div class='input'><a id="explore" class="header__btn" href="#" data-sw-translate>Explore</a>
                        </div>
                    </form>
                </div>
            </div>
            --%>

            <div id="swagger-ui-container" class="swagger-ui-wrap"></div>
        </div>
    </div>
</div>

</div> <%--A weird thing to avoid problems--%>
<c:import url="footer.jsp"/>