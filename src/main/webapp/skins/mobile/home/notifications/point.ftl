<#include "macro-notifications.ftl">
<@notifications "point">
<#if pointNotifications?size != 0>
<ul class="notification">
    <#list pointNotifications as notification>
    <li class="fn-flex<#if notification.hasRead> read</#if>">
        ${notification.description}
    </li>
    </#list>
</ul>
<#else>
${noMessageLabel}
</#if>

<@pagination url="/notifications/point"/></@notifications>