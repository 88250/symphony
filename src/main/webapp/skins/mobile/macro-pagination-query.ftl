<#macro pagination url query>
<#if paginationPageCount?? && paginationPageCount!=0 && paginationPageCount!=1>
<div class="fn-clear">
    <div class="pagination">
        <#if paginationFirstPageNum!=1>
        <a rel="prev" href="${url}&${query}"><<1</a>
        </#if>
        <#list paginationPageNums as nums>
        <#if 8 gt nums_index >
        <#if nums=paginationCurrentPageNum>
        <span class="current">${nums}</span>
        <#else>
        <a href="${url}?p=${nums}&${query}">${nums}</a>
        </#if>
        </#if>
        </#list>
        <#if paginationLastPageNum!=paginationPageCount>
        <a rel="next" href="${url}?p=${paginationPageCount}&${query}">${paginationPageCount}>></a>
        </#if>
    </div>
</div>
<#else>
<div class="fn-hr10"></div>
</#if>
</#macro>