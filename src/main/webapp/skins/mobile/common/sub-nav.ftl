<#macro subNav type curDomain>
<div class="main">
    <div class="domains fn-clear fn-flex">
        <#list domains as domain>
        <a href="${servePath}/domain/${domain.domainURI}"<#if curDomain == domain.domainURI> class="selected"</#if>>${domain.domainTitle}</a>
        </#list>
        <#if isLoggedIn && "" != currentUser.userCity>
        <a href="${servePath}/city/my"<#if 'city' == type> class="selected"</#if>>${currentUser.userCity}</a>
        </#if>
        <#if isLoggedIn>
            <a href="${servePath}/watch"<#if selected?? && 'watch' == selected> class="current"</#if>>
            ${followLabel}</a>
        </#if>
    </div>
</div>
</#macro>