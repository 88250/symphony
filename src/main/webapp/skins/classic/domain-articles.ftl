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
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content fn-clear">
                    <div class="domains fn-clear">
                        <#list domains as navDomain>
                        <a href="/domain/${navDomain.domainURI}" <#if navDomain.domainURI == domain.domainURI>class="selected"</#if>>${navDomain.domainTitle}</a>
                        <#if 10 < navDomain?counter>
                        <#break>
                        </#if>
                        </#list>
                        <a href="/">${latestLabel}</a>
                        <a href="/hot">${hotLabel}</a>
                        <#if isLoggedIn && "" != currentUser.userCity>
                        <a href="/city/my">${currentUser.userCity}</a>
                        </#if>
                        <a href="/timeline">${timelineLabel}</a>
                    </div>
                    <div class="domain-tags">
                        <#list domains as navDomain>
                        <#if 10 < navDomain?counter>
                        <#break>
                        </#if>
                        <#if navDomain.domainURI == domain.domainURI>
                            <#list navDomain.domainTags as tag>
                            <a rel="nofollow" class="ft-gray" href="/tag/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a>  
                            <#if tag?has_next><span class="ft-fade">•</span></#if>
                            </#list>
                        </#if>
                        </#list>
                    </div>
                    <@list listData=latestArticles/>
                    <@pagination url="/domain/${domain.domainURI}"/>
                    <br/><br/><br/>
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
        <#include "footer.ftl">
        <script>
            Util.initArticlePreview();
        </script>
    </body>
</html>
