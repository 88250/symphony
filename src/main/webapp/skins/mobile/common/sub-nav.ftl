<#macro subNav type curDomain>
<div class="main">
    <div class="domains fn-clear fn-flex">
        <#list domains as domain>
        <a href="${servePath}/domain/${domain.domainURI}"<#if curDomain == domain.domainURI> class="selected"</#if>>${domain.domainTitle}</a>
        </#list>
        <a href="${servePath}/recent"<#if 'recent' == type> class="selected"</#if>>${latestLabel}</a>
        <#if isLoggedIn && "" != currentUser.userCity>
        <a href="${servePath}/city/my"<#if 'city' == type> class="selected"</#if>>${currentUser.userCity}</a>
        </#if>
        <a href="${servePath}/timeline"<#if 'timeline' == type> class="selected"</#if>>${timelineLabel}</a>
    </div>
</div>
</#macro>