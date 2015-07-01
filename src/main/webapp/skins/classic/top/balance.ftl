<#include "macro-top.ftl">
<@top "balance">
<table class="points">
    <#list topBalanceUsers as user>
    <tr<#if user_index % 2 == 1> class="even"</#if>>
        <td>${user_index + 1}</td>
        <td class="name">${user.userName}</td>
        <td class="balance">${user.userPoint?c}</td>
    </tr>
    </#list>
</table>
</@top>