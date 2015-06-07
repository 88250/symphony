<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${symphonyLabel} - ${visionLabel}">
        <meta name="keywords" content="${visionLabel},${symphonyLabel}"/>
        <meta name="description" content="${symphonyLabel}${b3logDescriptionLabel}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/responsive${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper fn-clear">
                <div class="content fn-clear">
                    <@list listData=latestCmtArticles/>
                    <@pagination url=""/>
                </div>
                <div class="side">
                    <#include "side.ftl">
                </div>
            </div>
        </div>
        <#include "footer.ftl">
        <script>
            Util.initArticlePreview();
        </script>
    </body>
</html>
