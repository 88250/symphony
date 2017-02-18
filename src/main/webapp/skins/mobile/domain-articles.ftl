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
        ${domain.domainCSS}
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <@subNav '' '${domain.domainURI}'/>
            <div class="content fn-clear">
                <@list listData=latestArticles/>
                <@pagination url="${servePath}/domain/${domain.domainURI}"/>
                <div class="wrapper">
                    <div class="module">
                        <div class="module-header">
                            <h2>${domain.domainTitle}</h2>
                            <a href="${servePath}/domain/${domain.domainURI}" class="ft-gray fn-right">${domain.domainTags?size} Tags</a>
                        </div>
                        <div class="module-panel">
                            <ul class="module-list domain">
                                <li>
                                    <#list domain.domainTags as tag>
                                    <a class="tag" rel="nofollow" href="${servePath}/tag/${tag.tagURI}">${tag.tagTitle}</a>
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
        <@listScript/>
    </body>
</html>
