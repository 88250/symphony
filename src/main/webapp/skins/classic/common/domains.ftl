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
<#if domains?size != 0>
<div class="main__down">
    <div class="wrapper">
    <div class="module domains-module">
        <div class="module-panel">
            <ul class="module-list domain">
                <#list domains as domain>
                <#if domain.domainTags?size gt 0>
                <li>
                    <a rel="nofollow" class="slogan ft-a-title" href="${servePath}/domain/${domain.domainURI}">${domain.domainTitle}</a>
                    <div class="title">
                        <#list domain.domainTags as tag>
                        <a class="ft-gray ft-13" rel="nofollow" href="${servePath}/tag/${tag.tagURI}">${tag.tagTitle}</a> &nbsp;
                        </#list>
                    </div>
                </li>
                </#if>
                </#list>
            </ul>
        </div>
    </div>
    </div>
</div>
</#if>