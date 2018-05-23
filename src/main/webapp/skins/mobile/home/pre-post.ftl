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
        <@head title="${selectAddTypeLabel} - ${symphonyLabel}">
        <meta name="robots" content="none" />
        </@head>
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="pre-post">
                <div>
                    <a href="${servePath}/post?type=0">
                        <svg><use xlink:href="#article"></use></svg> ${articleLabel}
                    </a>
                    <div class="ft-fade">${addNormalArticleTipLabel}</div>
                </div>
                <div>
                    <a href="${servePath}/post?type=1&tags=${discussionLabel}">
                        <svg><use xlink:href="#locked"></use></svg> ${discussionLabel}
                    </a>
                    <div class="ft-fade">${addDiscussionArticleTipLabel}</div>
                </div>
                <div>
                    <a href="${servePath}/post?type=2">
                        <svg><use xlink:href="#feed"></use></svg> ${cityBroadcastLabel}
                    </a>
                    <div class="ft-fade">${addCityArticleTipLabel} <i>${broadcastPoint}</i> ${pointLabel}</div>
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
    </body>
</html>
