<#include "macro-notifications.ftl">
<@notifications "sysAnnounce">
<#if sysAnnounceNotifications?size != 0>
<ul class="notification">
    <#list sysAnnounceNotifications as notification>
    <li class="fn-flex<#if notification.hasRead> read</#if>">
        ${notification.description}
    </li>
    </#list>
</ul>
<#else>
<div class="fn-hr10"></div>
<div class="ft-center">${noMessageLabel}</div>
</#if>

<@pagination url="${servePath}/notifications/point"/></@notifications>