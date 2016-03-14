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
                    <div class='domains-count'>
                        Domains: <b>${domainCnt}</b><br/>
                        Tags: <b>${tagCnt}</b>
                    </div>
                    <div class="module">
                        <div class="module-header">
                            <h2>
                                ${domainLabel}
                            </h2>
                        </div>
                        <div class="module-panel">
                            <ul class="tags fn-clear">
                                <li>
                                    <a class="btn small" rel="nofollow" href="/tags/B3log">B3log</a>
                                </li>
                                <li>
                                    <a class="btn small" rel="nofollow" href="/tags/Java">Java</a>
                                </li>
                                <li>
                                    <a class="btn small" rel="nofollow" href="/tags/Q%26A">Q&amp;A</a>
                                </li>
                                <li>
                                    <a class="btn small" rel="nofollow" href="/tags/B3log%E5%85%AC%E5%91%8A">B3log公告</a>
                                </li>
                                <li>
                                    <a class="btn small" rel="nofollow" href="/tags/%E7%B3%BB%E7%BB%9F%E5%85%AC%E5%91%8A">系统公告</a>
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>
                <div class="side">
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
