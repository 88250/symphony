<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="500 Internal Server Error! - ${symphonyLabel}">
        <meta name="robots" content="none" />
        </@head>
        <link rel="stylesheet" href="${staticServePath}/css/error.css?${staticResourceVersion}" />
    </head>
    <body class="error">
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="module article-module">
                    <h2 class="sub-head">500 Internal Server Error!</h2>
                    <div class="no-list">${err500Label}</div>
                </div>
            </div>
        </div>
        <#include '../footer.ftl'/>
    </body>
</html>