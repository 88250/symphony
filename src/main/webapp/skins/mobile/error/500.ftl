<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="500 Internal Server Error! - ${symphonyLabel}">
        <meta name="robots" content="none" />
        </@head>
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="fn-hr10"></div>
                <h2>500 Internal Server Error!</h2>
                <div class="fn-hr10"></div>
                ${err500Label}
            </div>
        </div>
        <#include '../footer.ftl'/>
    </body>
</html>