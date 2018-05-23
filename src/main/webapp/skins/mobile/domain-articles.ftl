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
        <@head title="${domain.domainSeoTitle} - ${symphonyLabel}">
        <meta name="keywords" content="${domain.domainSeoKeywords}" />
        <meta name="description" content="${domain.domainSeoDesc}"/>
        </@head>
        ${domain.domainCSS}
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <@subNav '' '${domain.domainURI}'/>
            <div class="content fn-clear">
                <@list listData=latestArticles/>
                <@pagination url="${servePath}/domain/${domain.domainURI}"/>
                <div class="wrapper">
                    <div class="module">
                        <div class="module-header">
                            <h2>${domain.domainTitle}</h2>
                            <a href="${servePath}/domain/${domain.domainURI}" class="ft-gray fn-right">${domain.domainTags?size} Tags</a>
                        </div>
                        <div class="module-panel">
                            <ul class="module-list domain">
                                <li>
                                    <#list domain.domainTags as tag>
                                    <a class="tag" rel="nofollow" href="${servePath}/tag/${tag.tagURI}">${tag.tagTitle}</a>
                                    </#list>
                                </li>
                            </ul>
                            <div class="fn-hr5"></div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="side wrapper">
                <#include "side.ftl">
            </div>
        </div>
        <#include "footer.ftl">
        <@listScript/>
    </body>
</html>
