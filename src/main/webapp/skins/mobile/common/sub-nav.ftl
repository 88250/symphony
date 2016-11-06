<#macro subNav type curDomain>
<div class="main">
    <div class="domains fn-clear fn-flex">
        <#list domains as domain>
        <a href="${servePath}/domain/${domain.domainURI}"<#if curDomain == domain.domainURI> class="selected"</#if>><span<#if curDomain != domain.domainURI> class="fn-none"</#if>>${domain.domainIconPath}</span>${domain.domainTitle}</a>
        </#list>
        <a href="${servePath}/perfect"<#if 'perfect' == type> class="selected"</#if>>
           <svg height="16" viewBox="3 2 11 12" width="14" <#if 'perfect' != type> class="fn-none"</#if>>${perfectIcon}</svg>${perfectLabel}</a>
        <a href="${servePath}/hot"<#if 'hot' == type> class="selected"</#if>>
           <svg height="16" viewBox="0 0 12 16" width="12" <#if 'hot' != type> class="fn-none"</#if>>${hotIcon}</svg>${hotLabel}</a>
    </div>
</div>
</#macro>