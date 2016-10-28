<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="SymHub - ${symphonyLabel}">
        <meta name="description" content="${symDescriptionLabel}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content fn-clear">
                    <div class="module">
                        <div class="module-panel">
                            <ul class="module-list">
                                <li>
                                    <a rel="nofollow" href="http://localhost:8080/member/Zephyr">
                                        <span class="avatar-small slogan" style="background-image:url('https://img.hacpai.com/avatar/1465873147243_1476423401738.gif')"></span>
                                        </a>
                                    <a rel="friend" class="title" href="http://localhost:8080/article/1477383947293">从码农到园丁——论做 IT 培训班的可行性</a>
                                </li>
                            </ul>
                        </div>
                    </div>
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
