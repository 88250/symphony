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
<#macro top type>
<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <#if type == "balance">
        <@head title="${wealthRankLabel} - ${symphonyLabel}">
        <link rel="canonical" href="${servePath}/top/balance">
        </@head>
        </#if>
        <#if type == "consumption">
        <@head title="${consumptionRankLabel} - ${symphonyLabel}">
        <link rel="canonical" href="${servePath}/top/consumption">
        </@head>
        </#if>
        <#if type == "checkin">
        <@head title="${checkinTopLabel} - ${symphonyLabel}">
        <link rel="canonical" href="${servePath}/top/checkin">
        </@head>
        </#if>
        <link rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}" />
        <link rel="stylesheet" href="${staticServePath}/css/responsive.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content">
                    <div class="module">
                    <#nested>
                        <div class="top-ranking">
                            <#include "../common/ranking.ftl">
                        </div>
                    </div>
                </div>
                <div class="side">
                    <#include "../side.ftl">
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
        <script src="${staticServePath}/js/settings${miniPostfix}.js?${staticResourceVersion}"></script>
    </body>
</html>
</#macro>
