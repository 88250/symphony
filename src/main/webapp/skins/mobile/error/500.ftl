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
                <div class="content">
                    <h2>500 Internal Server Error!</h2>
                    <br/>
                    ${err500Label}
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
    </body>
</html>