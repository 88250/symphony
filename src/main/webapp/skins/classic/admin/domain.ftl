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
<@admin "domains">
<div class="content">
    <div class="module">
        <div class="module-header">
            <h2>${unmodifiableLabel}</h2>
        </div>
        <div class="module-panel form fn-clear form--admin fn__flex">
            <label>
                <div>Id</div>
                <input onfocus="this.select()" type="text" id="oId" value="${domain.oId}" readonly="readonly"/>
            </label>
        </div>
    </div>

    <div class="module">
        <div class="module-header">
            <h2>${tagLabel}</h2>
        </div>
        <div class="module-panel form fn-clear form--admin">
            <#list domain.domainTags as tag>
            <a class="tag" target="_blank" href="${servePath}/tag/${tag.tagURI}">${tag.tagTitle}</a>
            </#list>

            <#if permissions["domainAddDomainTag"].permissionGrant>
            <form method="POST" action="${servePath}/admin/domain/${domain.oId}/add-tag" class="fn__flex">
                <label>
                    <div>${addTagLabel}</div>
                    <input type="text" name="tagTitle"/>
                </label>
                <div>
                    &nbsp; &nbsp;
                    <button type="submit" class="green fn-right btn--admin">${submitLabel}</button>
                </div>
            </form>
            </#if>

            <#if permissions["domainRemoveDomainTag"].permissionGrant>
            <form method="POST" action="${servePath}/admin/domain/${domain.oId}/remove-tag" class="fn__flex">
                <label>
                    <div>${removeTagLabel}</div>
                    <input type="text" name="tagTitle"/>
                </label>
                <div>
                    &nbsp; &nbsp;
                    <button type="submit" class="green fn-right btn--admin">${submitLabel}</button>
                </div>
            </form>
            </#if>
        </div>
    </div>

    <#if permissions["domainUpdateDomainBasic"].permissionGrant>
    <div class="module">
        <div class="module-header">
            <h2>${modifiableLabel}</h2>
        </div>
        <div class="module-panel form fn-clear form--admin">
            <form action="${servePath}/admin/domain/${domain.oId}" method="POST">
                <div class="fn__flex">
                    <label>
                        <div>${domainLabel}</div>
                        <input type="text" id="domainTitle" name="domainTitle" value="${domain.domainTitle}"/>
                    </label>
                    <label class="mid">
                        <div>URI</div>
                        <input type="text" id="domainURI" name="domainURI" value="${domain.domainURI}"/>
                    </label>
                    <label>
                        <div>${sortLabel}</div>
                        <input type="text" id="domainSort" name="domainSort" value="${domain.domainSort}"/>
                    </label>
                </div>
                <div class="fn__flex">
                    <label>
                        <div>${descriptionLabel}</div>
                        <textarea rows="5" id="domainDescription"
                                  name="domainDescription">${domain.domainDescription}</textarea>
                    </label>
                </div>
                <div class="fn__flex">
                    <label>
                        <div>${iconPathLabel}</div>
                        <input type="text" id="domainIconPath" name="domainIconPath" value="${domain.domainIconPath}"/>
                    </label>
                </div>
                <div class="fn__flex">
                    <label>
                        <div>${domainNavLabel}</div>
                        <select id="domainNav" name="domainNav">
                            <option value="0"<#if 0 == domain.domainNav> selected</#if>>${yesLabel}</option>
                            <option value="1"<#if 1 == domain.domainNav> selected</#if>>${noLabel}</option>
                        </select>
                    </label>
                    <label class="mid">
                        <div>${typeLabel}</div>
                        <input type="text" id="domainType" name="domainType" value="${domain.domainType}"/>
                    </label>
                    <label>
                        <div>${statusLabel}</div>
                        <select id="domainStatus" name="domainStatus">
                            <option value="0"<#if 0 == domain.domainStatus> selected</#if>>${validLabel}</option>
                            <option value="1"<#if 1 == domain.domainStatus> selected</#if>>${banLabel}</option>
                        </select>
                    </label>
                </div>
                <div class="fn__flex">
                    <label>
                        <div>${seoTitleLabel}</div>
                        <input type="text" id="domainSeoTitle" name="domainSeoTitle" value="${domain.domainSeoTitle}"/>
                    </label>
                    <label class="mid">
                        <div>${seoKeywordsLabel}</div>
                        <input type="text" id="domainSeoKeywords" name="domainSeoKeywords"
                               value="${domain.domainSeoKeywords}"/>
                    </label>
                    <label>
                        <div>${seoDescLabel}</div>
                        <input type="text" id="domainSeoDesc" name="domainSeoDesc" value="${domain.domainSeoDesc}"/>
                    </label>
                </div>
                <div class="fn__flex">
                    <label>
                        <div>CSS</div>
                        <textarea rows="6" id="domainCSS" name="domainCSS">${domain.domainCSS}</textarea>
                    </label>
                </div>
                <br/>
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
        <div class="module-panel form fn-clear form--admin">
            <form action="${servePath}/admin/remove-domain" method="POST" class="fn__flex"
                  onsubmit="return window.confirm('${confirmRemoveLabel}')">
                <label>
                    <div>Id</div>
                    <input type="text" id="domainId" name="domainId" value="${domain.oId}" readonly class="input--admin-readonly"/>
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