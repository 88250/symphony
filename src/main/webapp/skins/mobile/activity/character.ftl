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
<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${characterLabel} - ${activityLabel} - ${symphonyLabel}">
        </@head>
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="fn-hr10"></div>
                <div class="content activity">
                    <div class="content-reset">
                        ${activityCharacterTitleLabel}
                        <div class="fn-clear">
                            <div class="fn-left">
                                <#if noCharacter??>
                                ${activityCharacterNotCharacterLabel}
                                <#else>
                                ${activityCharacterGuideLabel}
                                </#if>
                            </div>
                            <div class="fn-right activity-char-btns">
                                <button class="red" onclick="Activity.clearCharacter('charCanvas')">${clearLabel}</button>
                                &nbsp;
                                <button class="green" onclick="Activity.submitCharacter('charCanvas')">${submitLabel}</button>
                            </div>
                        </div>
                        <canvas id="charCanvas" width="306" height="300"></canvas>
                        <#if !noCharacter??>
                        <ul>
                            <li>${userCharacterProgressLabel}${colonLabel}${userProgress}</li>
                            <li>${totalCharacterProgressLabel}${colonLabel}${totalProgress}</li>
                        </ul>
                        </#if>
                    </div>
                </div>
                <div class="side">
                    <#include "../side.ftl">
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
        <script src="${staticServePath}/js/activity${miniPostfix}.js?${staticResourceVersion}"></script>
        <script>
                                    Activity.charInit('charCanvas');
        </script>
    </body>
</html>