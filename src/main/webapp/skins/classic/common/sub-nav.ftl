<#macro subNav type curDomain>
<div class="tabs fn-clear">
    <div class="wrapper fn-clear">
        <#list domains as domain>
        <a href="${servePath}/domain/${domain.domainURI}"<#if curDomain == domain.domainURI> class="selected"</#if>>${domain.domainIconPath}&nbsp;${domain.domainTitle}</a>
        </#list>
        <a href="${servePath}/perfect"<#if 'perfect' == type> class="selected"</#if>>
            <svg height="16" viewBox="3 2 11 12" width="14">${perfectIcon}</svg>&nbsp;${perfectLabel}</a>
        <a href="${servePath}/recent"<#if 'recent' == type> class="selected"</#if>>
            <svg height="16" viewBox="0 0 14 16" width="14">${timeIcon}</svg>&nbsp;${latestLabel}</a>
        <a href="${servePath}/hot"<#if 'hot' == type> class="selected"</#if>>
            <svg height="16" viewBox="0 0 12 16" width="12">${hotIcon}</svg>&nbsp;${hotLabel}</a>
        <#if isLoggedIn && "" != currentUser.userCity>
        <a href="${servePath}/city/my"<#if 'city/my' == type> class="selected"</#if>>
            <svg height="16" viewBox="0 0 12 16" width="12">${localIcon}</svg>&nbsp;${currentUser.userCity}</a>
        </#if>
        <a href="${servePath}/timeline"<#if 'timeline' == type> class="selected"</#if>>
            <svg height="14" viewBox="0 0 16 14" width="16">${timelineIcon}</svg>&nbsp;${timelineLabel}</a>
        <a href="${servePath}/forge/link"<#if '/forge/link' == type> class="selected"</#if>>
           <svg height="14" viewBox="0 1 16 14" width="16">${baguaIcon}</svg> ${forgeLabel}</a>
    </div>
</div>
</#macro>