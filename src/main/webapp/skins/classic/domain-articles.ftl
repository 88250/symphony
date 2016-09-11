<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination.ftl">
<#include "common/sub-nav.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${domain.domainSeoTitle} - ${symphonyLabel}">
        <meta name="keywords" content="${domain.domainSeoKeywords}" />
        <meta name="description" content="${domain.domainSeoDesc}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
        <@subNav '' '${domain.domainURI}'/>
        <div class="main">
            <div class="wrapper">
                <div class="content">
                    <div class="tabs-sub fn-clear">
                        <#list domains as navDomain>
                        <#if navDomain.domainURI == domain.domainURI>
                        <#list navDomain.domainTags as tag>
                        <a rel="nofollow" href="${servePath}/tag/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a>  
                        </#list>
                        </#if>
                        </#list>
                    </div>
                    <@list listData=latestArticles/>
                    <@pagination url="/domain/${domain.domainURI}"/>
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
