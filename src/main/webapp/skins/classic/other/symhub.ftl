<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-2018, b3log.org & hacpai.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

-->
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

                </div>

                <div class="side">
                    <#include "../side.ftl">
                </div>
            </div>
        </div>
        <#include "../common/domains.ftl">
        <#include "../footer.ftl">
        <@listScript/>
    </body>
</html>
