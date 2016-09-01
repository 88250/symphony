<#macro subNav type curDomain>
<div class="main">
    <div class="domains fn-clear">
        <#list domains as domain>
        <a href="${servePath}/domain/${domain.domainURI}"<#if curDomain == domain.domainURI> class="selected"</#if>>${domain.domainIconPath}&nbsp;${domain.domainTitle}</a>
        </#list>
        <a href="${servePath}/perfect"<#if 'perfect' == type> class="selected"</#if>>
           <svg height="16" viewBox="3 2 11 12" width="14">${perfectIcon}</svg>&nbsp;${perfectLabel}</a>
        <a href="${servePath}/hot"<#if 'hot' == type> class="selected"</#if>>
           <svg height="16" viewBox="0 0 12 16" width="12">${hotIcon}</svg>&nbsp;${hotLabel}</a>
    </div>
</div>
</#macro>