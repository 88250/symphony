<#include "macro-head.ftl">
<#include "macro-list.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${symphonyLabel}">
        <meta name="description" content="${symDescriptionLabel}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content fn-clear">
                    <div class="domains fn-clear">
                        <#list domains as domain>
                        <a href='/domain/${domain.domainURI}'>${domain.domainTitle}</a>
                        <#if 10 < domain?counter>
                        <#break>
                        </#if>
                        </#list>
                    </div>
                    <@list listData=indexArticles/>
                    <div class="fn-clear">
                        <br/>
                        <a href="/domains" class="ft-gray">All Domains</a>
                        <a href="/recent" class="fn-right">${moreRecentArticleLabel}</a>
                        <br/> <br/>
                    </div>

                    <#list domains as domain>
                    <div class="module">
                        <div class="module-header">
                            <h2>
                                <a rel="nofollow" href="/domain/${domain.domainURI}">${domain.domainTitle}</a>
                            </h2>
                        </div>
                        <div class="module-panel">
                            <ul class="tags fn-clear">
                                <#list domain.domainTags as tag>
                                <li>
                                    <a class="btn small" rel="nofollow" href="/tags/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a>
                                </li>
                                </#list>
                            </ul>
                        </div>
                    </div>
                    </#list>
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
