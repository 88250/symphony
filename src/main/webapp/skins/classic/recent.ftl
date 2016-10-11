<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${latestLabel} - ${symphonyLabel}">
        <meta name="description" content="${symDescriptionLabel}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content fn-clear">
                    <div class="module">
                        <div class="module-header fn-clear">
                            <span class="fn-right">
                                <a href="">${default1Label}</a>
                                <span class="ft-fade">/</span>
                                <a<#if !current> class="ft-gray"</#if> href="">${hotArticlesLabel}</a>
                                <span class="ft-fade">/</span>
                                <a<#if !current> class="ft-gray"</#if> href="">${goodCmtsLabel}</a>
                                <span class="ft-fade">/</span>
                                <a<#if !current> class="ft-gray"</#if> href="">${recentCommentLabel}</a>
                            </span>
                        </div>
                        <@list listData=latestArticles/>
                        <@pagination url="${servePath}/recent"/>
                    </div>
                    <#include "common/domains.ftl">
                </div>

                <div class="side">
                    <#include "side.ftl">
                </div>
            </div>
        </div>
        <#include "footer.ftl">
        <@listScript/>
    </body>
</html>
