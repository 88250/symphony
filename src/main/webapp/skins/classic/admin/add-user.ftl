<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-2019, b3log.org & hacpai.com

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
<@admin "addUser">
<div class="content">
    <div class="module">
        <div class="module-header">
            <h2>${addUserLabel}</h2>
        </div>
        <div class="module-panel form fn-clear form--admin">
            <form action="${servePath}/admin/add-user" method="POST">
                <div class="fn__flex">
                    <label>
                        <div>${userNameLabel}</div>
                        <input name="userName" type="text"/>
                    </label>
                    <label class="mid">
                        <div>${emailLabel}</div>
                        <input name="userEmail" type="text"/>
                    </label>
                    <label>
                        <div>${passwordLabel}</div>
                        <input name="userPassword" type="text"/>
                    </label>
                </div>
                <div class="fn__flex">
                    <label>
                        <div>${roleLabel}</div>
                        <select id="domainNav" name="domainNav">
                            <option value="0">${programmerLabel}</option>
                            <option value="1">${designerLabel}</option>
                        </select>
                    </label>
                    <label class="mid"></label>
                    <label></label>
                </div>
                <br/>
                <button type="submit" class="green fn-right">${submitLabel}</button>
            </form>
        </div>
    </div>
</div>
</@admin>