<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${domain.domainSeoTitle} - ${symphonyLabel}">
        <meta name="description" content="${recentArticleLabel}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content fn-clear">
                    <div class="domains fn-clear">
                        <a href='/domain/{domain.domainURI}'>domain.domainTitle</a>
                        <a href='/domain/{domain.domainURI}' class="selected">domain.domainTitle</a>
                        <a href='/domain/{domain.domainURI}'>domain.domainTitle</a>
                        <a href='/domain/{domain.domainURI}'>domain.domainTitle</a>
                    </div>
                    <@list listData=latestArticles/>
                    <@pagination url="/${domain.domainURI}"/>
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
