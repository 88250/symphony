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
<@admin "breezemoons">
<div class="content">
    <div class="module">
        <div class="module-header">
            <h2>${unmodifiableLabel}</h2>
        </div>
        <div class="module-panel form fn-clear form--admin">
            <div class="fn__flex">
                <label>
                    <div>Id</div>
                    <input onfocus="this.select()" type="text" id="oId" value="${breezemoon.oId}" readonly="readonly"/>
                </label>
                <label>
                    <div>${authorIdLabel}</div>
                    <input onfocus="this.select()" type="text" id="breezemoonAuthorId" name="breezemoonAuthorId"
                           value="${breezemoon.breezemoonAuthorId}" readonly="readonly"/>
                </label>
                <label>
                    <div>IP</div>
                    <input onfocus="this.select()" type="text" id="breezemoonIP" name="breezemoonIP"
                           value="${breezemoon.breezemoonIP}"
                           readonly="readonly"/>
                </label>
                <label>
                    <div>${cityLabel}</div>
                    <input onfocus="this.select()" type="text" name="breezemoonCity" value="${breezemoon.breezemoonCity}" readonly="readonly"/>
                </label>
            </div>
            <div class="fn__flex">
                <label>
                    <div>UA</div>
                    <input onfocus="this.select()" type="text" id="breezemoonUA" name="breezemoonUA"
                           value="${breezemoon.breezemoonUA}"
                           readonly="readonly"/>
                </label>
            </div>
        </div>
    </div>

    <#if permissions["breezemoonUpdateBreezemoon"].permissionGrant>
    <div class="module">
        <div class="module-header">
            <h2>${modifiableLabel}</h2>
        </div>
        <div class="module-panel form fn-clear form--admin">
            <form action="${servePath}/admin/breezemoon/${breezemoon.oId}" method="POST">
                <div class="fn__flex">
                    <label>
                        <div>${statusLabel}</div>
                        <select id="breezemoonStatus" name="breezemoonStatus">
                            <option value="0"<#if 0 == breezemoon.breezemoonStatus>
                                    selected</#if>>${validLabel}</option>
                            <option value="1"<#if 1 == breezemoon.breezemoonStatus> selected</#if>>${banLabel}</option>
                        </select>
                    </label>
                </div>
                <div class="fn__flex">
                    <label>
                        <div>${contentLabel}</div>
                        <textarea id="breezemoonContent" name="breezemoonContent"
                                  rows="10">${breezemoon.breezemoonContent}</textarea>
                    </label>
                </div>
                <br/>
                <button type="submit" class="green fn-right">${submitLabel}</button>
            </form>
        </div>
    </div>
    </#if>

    <#if permissions["breezemoonRemoveBreezemoon"].permissionGrant>
    <div class="module">
        <div class="module-header">
            <h2 class="ft-red">${removeDataLabel}</h2>
        </div>
        <div class="module-panel form fn-clear form--admin">
            <form action="${servePath}/admin/remove-breezemoon" method="POST"
                  onsubmit="return window.confirm('${confirmRemoveLabel}')" class="fn__flex">
                <label>
                    <div>Id</div>
                    <input type="text" id="id" name="id" value="${breezemoon.oId}" readonly class="input--admin-readonly"/>
                </label>
                <div>
                    &nbsp; &nbsp;
                    <button type="submit" class="red fn-right btn--admin">${submitLabel}</button>
                </div>
            </form>
        </div>
    </div>
    </#if>
</div>
</@admin>