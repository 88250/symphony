<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="404 Not Found! - ${symphonyLabel}">
        <meta name="robots" content="none" />
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/error${miniPostfix}.css?${staticResourceVersion}" />
        </@head>
    </head>
    <body class="error">
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper block module">
                <h2 class="sub-head">404 Not Found!</h2>
                <div class="ft-center"></div>
            </div>
        </div>
        <#include '../footer.ftl'/>
    </body>
    <script>
        $('.main .wrapper div.ft-center').html('<img src="${staticServePath}/images/404/' + Math.round(Math.random() * 6) + '.gif">');
        Util.mouseClickEffects();
    </script>
</html>