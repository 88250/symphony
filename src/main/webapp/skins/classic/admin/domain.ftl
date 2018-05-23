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
<@admin "domains">
<div class="content">
    <div class="module">
        <div class="module-header">
            <h2>${unmodifiableLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <label for="oId">Id</label>
            <input type="text" id="oId" value="${domain.oId}" readonly="readonly" />
        </div>
    </div>

    <div class="module">
        <div class="module-header">
            <h2>${tagLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <#list domain.domainTags as tag>
            <a class="tag" target="_blank" href="${servePath}/tag/${tag.tagURI}">${tag.tagTitle}</a>
            </#list>

            <#if permissions["domainAddDomainTag"].permissionGrant>
            <form method="POST" action="${servePath}/admin/domain/${domain.oId}/add-tag" class="fn-clear">
                <label form="addTag">${addTagLabel}</label>
                <input type="text" name="tagTitle" />

                <br/><br/>
                <button type="submit" class="green fn-right">${submitLabel}</button>
            </form>
            </#if>

            <#if permissions["domainRemoveDomainTag"].permissionGrant>
            <form method="POST" action="${servePath}/admin/domain/${domain.oId}/remove-tag">
                <label form="addTag">${removeTagLabel}</label>
                <input type="text" name="tagTitle" />

                <br/><br/>
                <button type="submit" class="green fn-right">${submitLabel}</button>
            </form>
            </#if>
        </div>
    </div>

    <#if permissions["domainUpdateDomainBasic"].permissionGrant>
    <div class="module">
        <div class="module-header">
            <h2>${modifiableLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <form action="${servePath}/admin/domain/${domain.oId}" method="POST">
                <label for="domainTitle">${domainLabel}${updateCaseOnlyLabel}</label>
                <input type="text" id="domainTitle" name="domainTitle" value="${domain.domainTitle}" />

                <label for="domainURI">URI</label>
                <input type="text" id="domainURI" name="domainURI" value="${domain.domainURI}" />

                <label for="domainDescription">${descriptionLabel}</label>
                <textarea rows="5" id="domainDescription" name="domainDescription">${domain.domainDescription}</textarea>

                <label for="domainIconPath">${iconPathLabel}</label>
                <input type="text" id="domainIconPath" name="domainIconPath" value="${domain.domainIconPath}" />

                <label for="domainSort">${sortLabel}</label>
                <input type="text" id="domainSort" name="domainSort" value="${domain.domainSort}" />

                <label>${domainNavLabel}</label>
                <select id="domainNav" name="domainNav">
                    <option value="0"<#if 0 == domain.domainNav> selected</#if>>${yesLabel}</option>
                    <option value="1"<#if 1 == domain.domainNav> selected</#if>>${noLabel}</option>
                </select>

                <label for="domainType">${typeLabel}</label>
                <input type="text" id="domainType" name="domainType" value="${domain.domainType}" />

                <label>${statusLabel}</label>
                <select id="domainStatus" name="domainStatus">
                    <option value="0"<#if 0 == domain.domainStatus> selected</#if>>${validLabel}</option>
                    <option value="1"<#if 1 == domain.domainStatus> selected</#if>>${banLabel}</option>
                </select>

                <label for="domainSeoTitle">${seoTitleLabel}</label>
                <input type="text" id="domainSeoTitle" name="domainSeoTitle" value="${domain.domainSeoTitle}" />

                <label for="domainSeoKeywords">${seoKeywordsLabel}</label>
                <input type="text" id="domainSeoKeywords" name="domainSeoKeywords" value="${domain.domainSeoKeywords}" />

                <label for="domainSeoDesc">${seoDescLabel}</label>
                <input type="text" id="domainSeoDesc" name="domainSeoDesc" value="${domain.domainSeoDesc}" />

                <label for="domainCSS">CSS</label>
                <textarea rows="20" id="domainCSS" name="domainCSS">${domain.domainCSS}</textarea>

                <br/><br/>
                <button type="submit" class="green fn-right">${submitLabel}</button>
            </form>
        </div>
    </div>
    </#if>

    <#if permissions["domainRemoveDomain"].permissionGrant>
    <div class="module">
        <div class="module-header">
            <h2 class="ft-red">${removeDataLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <form action="${servePath}/admin/remove-domain" method="POST" onsubmit="return window.confirm('${confirmRemoveLabel}')">
                <label for="domainId">Id</label>
                <input type="text" id="domainId" name="domainId" value="${domain.oId}" readonly="readonly"/>

                <br/><br/>
                <button type="submit" class="green fn-right" >${submitLabel}</button>
            </form>
        </div>
    </div>
    </#if>
</div>
</@admin>