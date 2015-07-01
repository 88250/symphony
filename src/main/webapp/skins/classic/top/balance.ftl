<#include "macro-top.ftl">
<@top "balance">
<table class="points">
    <#list topBalanceUsers as user>
    <tr<#if user_index % 2 == 1> class="even"</#if>>
        <td>${user_index + 1}</td>
        <td class="name">${user.userName}</td>
        <td class="balance"><a href="/member/${user.userName}/points" title="${user.userPoint?c}"><#if 0 == user.userAppRole>0x${user.userPointHex}<#else><div class="" style="width: 30px; height: 10px; display: inline-block; background-color: #${user.userPointCC}"></div></#if></a></td>
    </tr>
    </#list>
</table>
</@top>