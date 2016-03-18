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

    window.swaggerUi = new SwaggerUi({
        url: window.location.origin + deployPath + "/v2/api-docs", <%-- v2 is the new swagger --%>
        dom_id: "swagger-ui-container", <%-- div container where the documentation will be loaded  --%>
        onComplete: function (swaggerApi, swaggerUi) {

            <%-- swagger footer colides to wordpress footer --%>
            $(".swagger-section .footer").attr("class", "swagger-footer");
            $('.swagger-footer').css('padding-top', '25px');
        },
        docExpansion: "none",
        apisSorter: "alpha",
        defaultModelRendering: 'model',
        operationsSorter: 'alpha',
        showRequestHeaders: false
    });

    window.swaggerUi.load();

</script>

<div style="margin-top: 20px; margin-bottom: 10px">
    <div class="swagger-section">
        <div id="swagger-ui-container" class="swagger-ui-wrap"></div>
    </div>
</div>

</div>

<%--<link href="/wordpress/wp-content/themes/HS_OICR_2013/960_24_col.css" rel="stylesheet" type="text/css">--%>
<%--<link href="/wordpress/wp-content/themes/HS_OICR_2013/reset.css" rel="stylesheet" type="text/css">--%>
<%--<link href="/wordpress/wp-content/themes/HS_OICR_2013/text.css" rel="stylesheet" type="text/css">--%>
<%--<link rel="stylesheet" type="text/css" media="all" href="/wordpress/wp-content/themes/HS_OICR_2013/style.css">--%>
<%--<link href="/wordpress/wp-content/themes/HS_OICR_2013/buttons.css" rel="stylesheet" type="text/css">--%>
<%--<script>!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0],p=/^http:/.test(d.location)?'http':'https';if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src=p+"://platform.twitter.com/widgets.js";fjs.parentNode.insertBefore(js,fjs);}}(document,"script","twitter-wjs");</script>--%>
<%--<link rel="pingback" href="/wordpress/xmlrpc.php">--%>

<%--A weird thing to avoid problems--%>
<c:import url="footer.jsp"/>