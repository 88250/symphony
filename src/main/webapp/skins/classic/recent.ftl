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
                        <div class="article-list list">
                            <ul class="stick">
                                <#list latestArticles as article>
                                TODO
                                </#list>
                            </ul>
                        </div>
                    </div>
                    <div class="module">
                        <div class="module-header fn-clear">
                            <span class="fn-right ft-fade">
                                <a class="<#if "" == current>ft-fade<#else>ft-gray</#if>" href="${servePath}/recent">${defaultLabel}</a>
                                /
                                <a class="<#if "/hot" == current>ft-fade<#else>ft-gray</#if>" href="${servePath}/recent/hot">${hotArticlesLabel}</a>
                                /
                                <a class="<#if "/good" == current>ft-fade<#else>ft-gray</#if>" href="${servePath}/recent/good">${goodCmtsLabel}</a>
                                /
                                <a class="<#if "/reply" == current>ft-fade<#else>ft-gray</#if>" href="${servePath}/recent/reply">${recentCommentLabel}</a>
                            </span>
                        </div>
                        <@list listData=latestArticles/>
                        <@pagination url="${servePath}/recent${current}"/>
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
