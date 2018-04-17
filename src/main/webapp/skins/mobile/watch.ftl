<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "common/sub-nav.ftl">
<!DOCTYPE html>
<html lang="zh-cmn-Hans">
    <head>
        <@head title="${followLabel} - ${symphonyLabel}">
        <meta name="description" content="${symDescriptionLabel}"/>
        </@head>
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="content fn-clear">
                    <div class="domains fn-flex">
                        <a class="<#if "" == current>selected</#if>" href="${servePath}/watch">${followingTagsLabel}</a>
                        <a class="<#if "/users" == current>selected</#if>" href="${servePath}/watch/users">${followingUsersLabel}</a>
                    </div>
                <@list listData=watchingArticles/>
            </div>
            <#if domains?size != 0>
                <div class="module">
                    <div class="module-header">
                        <h2>${domainNavLabel}</h2>
                        <a href="${servePath}/domains" class="ft-gray fn-right">All Domains</a>
                    </div>
                    <div class="module-panel">
                        <ul class="module-list domain">
                            <#list domains as domain>
                                <#if domain.domainTags?size gt 0>
                                    <li>
                                        <a rel="nofollow" class="slogan" href="${servePath}/domain/${domain.domainURI}">${domain.domainTitle}</a>
                                        <div class="title">
                                            <#list domain.domainTags as tag>
                                                <a class="tag" rel="nofollow" href="${servePath}/tag/${tag.tagURI}">${tag.tagTitle}</a>
                                            </#list>
                                        </div>
                                    </li>
                                </#if>
                            </#list>
                        </ul>
                    </div>
                </div>
            </#if>
            <#include "side.ftl">
        </div>
        <#include "footer.ftl">
        <@listScript/>
    </body>
</html>
