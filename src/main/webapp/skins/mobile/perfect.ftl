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
        <@head title="${latestLabel} - ${symphonyLabel}">
        <meta name="description" content="${symDescriptionLabel}"/>
        </@head>
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <@subNav 'perfect' ''/>
            <div class="content fn-clear">
                <@list listData=perfectArticles/>
                <@pagination url="${servePath}/perfect"/>
                <#if domains?size != 0>
                <div class="wrapper">
                    <div class="module">
                        <div class="module-header">
                            <h2>${domainNavLabel}</h2>
                            <a href="${servePath}/domains" class="ft-gray fn-right">All Domains</a>
                        </div>
                        <div class="module-panel">
                            <ul class="module-list domain">
                                <#list domains as domain>
                                <#if domain.domainTags?size gt 0>
                                <li>
                                    <a rel="nofollow" class="slogan" href="${servePath}/domain/${domain.domainURI}">${domain.domainTitle}</a>
                                    <div class="title">
                                        <#list domain.domainTags as tag>
                                        <a class="tag" rel="nofollow" href="${servePath}/tag/${tag.tagURI}">${tag.tagTitle}</a>
                                        </#list>
                                    </div>
                                </li>
                                </#if>
                                </#list>
                            </ul>
                        </div>
                    </div>
                </div>
                </#if>
            </div>
            <div class="side wrapper">
                <#include "side.ftl">
            </div>
        </div>
        <#include "footer.ftl">
        <@listScript/>
    </body>
</html>
