<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${city} - ${symphonyLabel}">
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="domains fn-clear">
            <div class="wrapper fn-clear">
                <#list domains as domain>
                <a href='/domain/${domain.domainURI}'>${domain.domainTitle}</a>
                </#list>
                <a href="/">${latestLabel}</a>
                <a href="/hot">${hotLabel}</a>
                <#if isLoggedIn && "" != currentUser.userCity>
                <a href="/city/my" class="selected">${currentUser.userCity}</a>
                </#if>
                <a href="/timeline">${timelineLabel}</a>
                <a href="/community">${communityGroupLabel}</a>
            </div>
        </div>
        <div class="main">
            <div class="wrapper">
                <div class="content fn-clear">
                    <#if articles?size gt 0>
                    <div class="fn-clear">
                        <@list listData=articles/>
                        <@pagination url="/city/${city?url('utf-8')}"/>
                    </div>
                    <#else>
                    <div class="content content-reset">
                        <#if !userGeoStatus>
                        ${cityArticlesTipLabel}
                        <#else>
                        <#if !cityFound>
                        ${geoInfoPlaceholderLabel}
                        </#if>
                        </#if>
                    </div>
                    </#if>
                    <br/>
                    <div class="module">
                        <div class="module-header">
                            <h2>${domainLabel}${navigationLabel}</h2>
                            <a href="/domains" class="ft-gray fn-right">All Domains</a>
                        </div>
                        <div class="module-panel">
                            <ul class="module-list domain">
                                <#list domains as domain>
                                <#if domain.domainTags?size gt 0>
                                <li>
                                    <a rel="nofollow" class="slogan" href="/domain/${domain.domainURI}">${domain.domainTitle}</a>
                                    <div class="title">
                                        <#list domain.domainTags as tag>
                                        <a class="tag" rel="nofollow" href="/tag/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a>
                                        </#list>
                                    </div>
                                </li>
                                </#if>
                                </#list>
                            </ul>
                        </div>
                    </div>
                </div>
                <div class="side">
                    <#include "side.ftl">
                </div>
            </div>
        </div>
    </div>
    <#include "footer.ftl">
    <script>
        Util.initArticlePreview();
    </script>
</body>
</html>
