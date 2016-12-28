<#include "../macro-head.ftl">
<#include "../macro-list.ftl">
<#include "../macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="SymHub - ${symphonyLabel}">
        <meta name="description" content="${symDescriptionLabel}"/>
        </@head>
        <link rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content fn-clear">
                    <div class="module">
                        <div class="module-panel">
                            <ul class="module-list">
                                <#list syms as sym>
                                <li>
                                    <a rel="nofollow" href="${sym.symURL}" target="_blank">
                                        <span class="avatar-small slogan" style="background-image:url('${sym.symIcon}')"></span>
                                    </a>
                                    <a rel="friend" class="title"  target="_blank" href="${sym.symURL}">${sym.symTitle} - 
                                        <span class="ft-gray">${sym.symDesc}</span>
                                    </a>
                                </li>
                                </#list>
                            </ul>
                        </div>
                    </div>
                    <#include "../common/domains.ftl">
                </div>

                <div class="side">
                    <#include "../side.ftl">
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
        <@listScript/>
    </body>
</html>
