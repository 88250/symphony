<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-2018, b3log.org & hacpai.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

-->
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
