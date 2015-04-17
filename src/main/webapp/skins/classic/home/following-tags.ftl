<#include "macro-home.ftl">
<#include "../macro-pagination.ftl">
<@home "followingTags">
<div class="follow">
    <ol class="fn-clear">
        <#list userHomeFollowingTags as followingTag>
        <li<#if followingTag_index % 2 = 1> class="even"</#if>>
            <img class="avatar" src="${followingTag.userThumbnailURL}"/>
            <div>
                <h3>
                    <a rel="nofollow" href="/tags/${followingTag.tagTitle}" >${followingTag.tagTitle}</a>
                </h3>
            </div>
        </li>
        </#list>
    </ol>
</div>
<@pagination url="/member/${user.userName}/following/tags"/>
</@home>