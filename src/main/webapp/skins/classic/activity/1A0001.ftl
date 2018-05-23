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
        <@head title="${activity1A0001Label} - ${activityLabel} - ${symphonyLabel}">
        </@head>
        <link rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}" />
        <link rel="canonical" href="${servePath}/activity/1A0001">
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content">
                    <div class="module">
                        <h2 class="sub-head">
                            <div class="avatar-small tooltipped tooltipped-ne"
                                 aria-label="${activity1A0001Label}" style="background-image:url('${staticServePath}/images/activities/1A0001.png')"></div>
                            ${activity1A0001TitleLabel}
                        </h2>
                        <div class="content-reset fn-content">
                            ${activity1A0001GuideLabel}

                            <#if !closed && !closed1A0001 && !end && !collected && !participated>
                            <div id="betDiv">
                                <div>
                                    ${activity1A0001BetSelectLabel}
                                    <label><input name="smallOrLarge" type="radio" value="1" /> ${activity1A0001BetLargeLabel}</label>
                                    <label><input name="smallOrLarge" type="radio" value="0" checked="checked" /> ${activity1A0001BetSmallLabel}</label>
                                </div>

                                <div>
                                    ${activity1A0001BetAmountLabel}
                                    <label><input name="amount" type="radio" value="200" checked="checked" /> 200</label>
                                    <label><input name="amount" type="radio" value="300" /> 300</label>
                                    <label><input name="amount" type="radio" value="400" /> 400</label>
                                    <label><input name="amount" type="radio" value="500" /> 500</label>
                                </div>
                            </div>
                            </#if>

                            <#if participated || closed || closed1A0001 || collected || end>
                            <div id="tip" class="tip succ"><ul><li>${msg}</li></ul></div><br/>
                            <#if participated && hour?? && hour gt 15>
                            <div class="fn-clear">
                                <button id="collectBtn" class="red fn-right" onclick="Activity.collect1A0001()">${activityCollectLabel}</button>
                            </div>
                            </#if>
                            <#else>
                            <br/>
                            <div id="tip" class="tip"></div><br/>
                            <div class="fn-clear">
                                <button id="betBtn" class="red fn-right" onclick="Activity.bet1A0001('${csrfToken}')">${activityBetLabel}</button>
                            </div>
                            </#if>
                        </div>
                    </div>
                </div>
                <div class="side">
                    <#include "../side.ftl">
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
        <script src="${staticServePath}/js/activity${miniPostfix}.js?${staticResourceVersion}"></script>
    </body>
</html>