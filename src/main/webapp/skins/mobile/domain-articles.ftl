<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${domain.domainSeoTitle} - ${symphonyLabel}">
        <meta name="keywords" content="${domain.domainSeoKeywords}" />
        <meta name="description" content="${domain.domainSeoDesc}"/>
        </@head>
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="content fn-clear">
                <div class="domains fn-clear">
                    <#list domains as navDomain>
                    <a href="/domain/${navDomain.domainURI}" <#if navDomain.domainURI == domain.domainURI>class="selected"</#if>>${navDomain.domainTitle}</a>
                    </#list>
                    <a href="/">${latestLabel}</a>
                    <a href="/hot">${hotLabel}</a>
                    <#if isLoggedIn && "" != currentUser.userCity>
                    <a href="/city/my">${currentUser.userCity}</a>
                    </#if>
                    <a href="/timeline">${timelineLabel}</a>
                    <a href="/community">${communityGroupLabel}</a>
                </div>
                <@list listData=latestArticles/>
                <@pagination url="/domain/${domain.domainURI}"/>
                <div class="wrapper">
                    <div class="module">
                        <div class="module-header">
                            <h2>${domain.domainTitle}</h2>
                            <a href="/domain/${domain.domainURI}" class="ft-gray fn-right">${domain.domainTags?size} Tags</a>
                        </div>
                        <div class="module-panel">
                            <ul class="module-list domain">
                                <li>
                                    <#list domain.domainTags as tag>
                                    <a class="tag" rel="nofollow" href="/tag/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a>
                                    </#list>
                                </li>
                            </ul>
                            <div class="fn-hr5"></div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="side wrapper">
                <#include "side.ftl">
            </div>
        </div>
        <#include "footer.ftl">
    </body>
</html>
