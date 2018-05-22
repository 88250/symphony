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