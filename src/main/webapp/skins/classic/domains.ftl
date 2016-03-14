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
                    <#list [1, 2, 3] as item>
                    <div class="module">
                        <div class="module-header">
                            <h2>
                                <a href="">{domainLabel}</a>
                            </h2>
                        </div>
                        <div class="module-panel">
                            <ul class="tags fn-clear">
                                <#list [1, 2, 3] as item>
                                <li>
                                    <a class="btn small" rel="nofollow" href="/tags/tag">tag</a>
                                </li>
                                </#list>
                            </ul>
                        </div>
                    </div>
                    </#list>
                </div>
                <div class="side">
                    <div class='domains-count'>
                        Domains: <b>${domainCnt}</b><br/>
                        Tags: <b>${tagCnt}</b>
                    </div>
                    <#include "common/person-info.ftl">
                </div>
            </div>
        </div>
        <#include "footer.ftl">
        <script>
            Util.initArticlePreview();
        </script>
    </body>
</html>
