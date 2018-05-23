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
<#include "macro-settings.ftl">
<@home "help">

<div class="module">
    <div class="module-header">
        <h2>${userGuideLabel}</h2>
    </div>
    <div class="module-panel">
        <ul class="module-list">
            <li>
                <a href="${servePath}/about">${getStartLabel}</a>
                <span class="ft-gray">${getStartTipLabel}</span>
            </li>
            <li>
                <a href="${servePath}/tag/user_guide">${basicLabel}</a>
                <span class="ft-gray">${basicTipLabel}</span>
            </li>
            <li>
                <a href="https://hacpai.com/article/1474030007391">${hotKeyLabel}</a>
                <span class="ft-gray">${hotKeyTipLabel}</span>
            </li>
            <li>
                <a href="${servePath}/guide/markdown">Markdown ${tutorialLabel}</a>
                <span class="ft-gray">${markdownTutorialTipLabel}</span>
            </li>
            <li>
            ${pipeIntroLabel}
            </li>
        </ul>
    </div>
</div>
</@home>