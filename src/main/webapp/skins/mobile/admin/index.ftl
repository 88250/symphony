<#include "macro-admin.ftl">
<@admin "index">
<div class="wrapper">
    <div class="fn-hr10"></div>
    <ul>
        <li>${onlineVisitorCountLabel} ${onlineVisitorCnt?c}</li>
        <li>${onlineMemberCountLabel} ${onlineMemberCnt?c}</li>
        <li>${maxOnlineVisitorCountLabel} ${statistic.statisticMaxOnlineVisitorCount?c}</li>
        <li>${memberLabel} ${statistic.statisticMemberCount?c}</li>
        <li>${articleLabel} ${statistic.statisticArticleCount?c}</li>
        <li>${cmtLabel} ${statistic.statisticCmtCount?c}</li>
        <li>${domainLabel} ${statistic.statisticDomainCount?c}</li>
        <li>${tagLabel} ${statistic.statisticTagCount?c}</li>
    </ul>

    <br>
    <div>
        ${currentVersionLabel} <span id="version">${version}</span>${commaLabel}
        <span id="upgrade">${checkVersionLabel}</span>
    </div>
</div>
</@admin>
<script>
    document.addEventListener("DOMContentLoaded", function (event) {
        $.ajax({
            url: "https://rhythm.b3log.org/version/symphony/latest",
            type: "GET",
            dataType: "jsonp",
            jsonp: "callback",
            success: function (data, textStatus) {
                if ($("#version").text() === data.symphonyVersion) {
                    $("#upgrade").text('${upToDateLabel}');
                } else {
                    $("#upgrade").html('${newVersionAvailableLabel}' + '${colonLabel}'
                            + "<a href='" + data.symphonyDownload
                            + "' target='_blank'>" + data.symphonyVersion + "</a>");
                }
            }
        });
    });

</script>
