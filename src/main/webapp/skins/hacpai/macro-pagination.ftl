<#macro pagination url>
<#if paginationPageCount?? && paginationPageCount!=0 && paginationPageCount!=1>
<div class="pagination">
    <#if paginationFirstPageNum!=1>
    <a rel="prev" href="${url}"><<1</a>
    </#if>
    <#list paginationPageNums as nums>
    <#if nums=paginationCurrentPageNum>
    <span class="current">${nums}</span>
    <#else>
    <a href="${url}?p=${nums}">${nums}</a>
    </#if>
    </#list>
    <#if paginationLastPageNum!=paginationPageCount>
    <a rel="next" href="${url}?p=${paginationPageCount}">${paginationPageCount}>></a>
    </#if>
</div>
</#if>
</#macro>