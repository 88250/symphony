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
<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination.ftl">
<#include "common/sub-nav.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${city} - ${symphonyLabel}">
        </@head>
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <@subNav 'city/my' ''/>
            <div class="content fn-clear">
                <#if articles?size gt 0>
                <div class="fn-clear">
                    <@list listData=articles/>
                    <@pagination url="${servePath}/city/${city?url('utf-8')}"/>
                </div>
                <#else>
                <div class="content content-reset">
                    <#if !userGeoStatus>
                    ${cityArticlesTipLabel}
                    <#else>
                    <#if !cityFound>
                    ${geoInfoPlaceholderLabel}
                    </#if>
                    </#if>
                </div>
                </#if>
            </div>
            <div class="side wrapper">
                <#include "side.ftl">
            </div>
        </div>
    </div>
    <#include "footer.ftl">
    <@listScript/>
</body>
</html>
