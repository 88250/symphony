<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-present, b3log.org

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
<#macro pagination url, pjaxTitle="">
<#if paginationPageCount?? && paginationPageCount!=0 && paginationPageCount!=1>
    <div class="pagination">
        <#if paginationFirstPageNum!=1>
        <a pjax-title="${pjaxTitle}" rel="prev" href="${url}"><<1</a>
        </#if>
        <#list paginationPageNums as nums>
        <#if nums=paginationCurrentPageNum>
        <span class="current">${nums?c}</span>
        <#else>
        <a pjax-title="${pjaxTitle}" href="${url}?p=${nums?c}">${nums?c}</a>
        </#if>
        </#list>
        <#if paginationLastPageNum!=paginationPageCount>
        <a pjax-title="${pjaxTitle}" rel="next" href="${url}?p=${paginationPageCount?c}">${paginationPageCount?c}>></a>
        </#if>
    </div>
</#if>
</#macro>