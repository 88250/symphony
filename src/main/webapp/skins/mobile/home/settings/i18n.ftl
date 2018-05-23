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
<@home "i18n">
<div class="module">
    <div class="module-header">
        <h2>${i18nTipLabel}</h2>
    </div>

    <div class="module-panel form fn-clear">
        <br/>
        <label>${languageLabel}</label>
        <select id="userLanguage">
            <#list languages as language>
            <option value="${language}" <#if language == user.userLanguage>selected</#if>>${language}</option>
            </#list>
        </select>
        <br/><br/>
        <label>${timezoneLabel}</label>
        <select id="userTimezone"">
            <#list timezones as timezone>
            <option value="${timezone.id}" <#if timezone.id == user.userTimezone>selected</#if>>${timezone.name}</option>
            </#list>
        </select>

        <br/><br/>
        <div class="fn-clear"></div>
        <div id="i18nTip" class="tip"></div><br/>
        <button class="green fn-right" onclick="Settings.update('i18n', '${csrfToken}')">${saveLabel}</button>
    </div>
</div>
</@home>