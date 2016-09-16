<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="500 Internal Server Error! - ${symphonyLabel}">
        <meta name="robots" content="none" />
        </@head>
         <link type="text/css" rel="stylesheet" href="${staticServePath}/css/error${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body class="error">
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper block">
                <h2 class="sub-head">500 Internal Server Error!</h2>
                ${err500Label}
            </div>
        </div>
        <#include '../footer.ftl'/>
    </body>
</html>