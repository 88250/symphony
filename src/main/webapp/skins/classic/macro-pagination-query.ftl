<#macro pagination url query pjaxTitle="">
<#if paginationPageCount?? && paginationPageCount!=0 && paginationPageCount!=1>
    <div class="pagination">
        <#if paginationFirstPageNum!=1>
        <a pjax-title="${pjaxTitle}" rel="prev" href="${url}?${query}"><<1</a>
        </#if>
        <#list paginationPageNums as nums>
        <#if nums=paginationCurrentPageNum>
        <span class="current">${nums}</span>
        <#else>
        <a pjax-title="${pjaxTitle}" href="${url}?p=${nums}&${query}">${nums}</a>
        </#if>
        </#list>
        <#if paginationLastPageNum!=paginationPageCount>
        <a pjax-title="${pjaxTitle}" rel="next" href="${url}?p=${paginationPageCount}&${query}">${paginationPageCount}>></a>
        </#if>
    </div>
</#if>
</#macro>