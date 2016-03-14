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
                        <#list domains as navDomain>
                        <a href="/domain/${navDomain.domainURI}" <#if navDomain.domainURI == domain.domainURI>class="selected"</#if>>${navDomain.domainTitle}</a>
                        <#if 10 < navDomain?counter>
                        <#break>
                        </#if>
                        </#list>
                        <a href='/'>${hotLabel}</a>
                        <a href='/recent'>${latestLabel}</a>
                    </div>
                    <@list listData=latestArticles/>
                    <@pagination url="/${domain.domainURI}"/>
                    <br/><br/><br/>
                    <div class="module">
                        <div class="module-header">
                            <h2>${domain.domainTitle}</h2>
                            <a href="/domain/${domain.domainURI}" class="ft-gray fn-right">${domain.domainTags?size} Tags</a>
                        </div>
                        <div class="module-panel">
                            <ul class="module-list">
                                <li>
                                    <#list domain.domainTags as tag>
                                    <a class="tag" rel="nofollow" href="/tags/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a> &nbsp;
                                    </#list>
                                </li>
                            </ul>
                            <div class="fn-hr5"></div>
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
