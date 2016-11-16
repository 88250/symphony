<#include "macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${symphonyLabel}">
        <meta name="description" content="${symDescriptionLabel}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}" />
        <link rel="canonical" href="${servePath}">
    </head>
    <body class="index">
        <#include "header.ftl">   
        <div class="main">
            <div class="wrapper">
                <div class="index-main">
                    <div class="index-tabs fn-flex" id="articles">
                        <span class="current">${latestLabel}</span>
                        <span class="tags">${followingTagsLabel}</span>
                        <span class="users">${followingUsersLabel}</span>
                    </div>
                    <div class="index-tabs-panels list article-list">
                        <ul>
                            <#list recentArticles as article>
                            <li>
                                <div class="fn-clear ft-smaller list-info">
                                    <span class="fn-left">
                                        <#list article.articleTagObjs as articleTag>
                                        <a rel="tag" href="${servePath}/tag/${articleTag.tagURI}">${articleTag.tagTitle}</a> &nbsp; 
                                        </#list>
                                    </span>
                                    <span class="fn-right ft-fade">
                                        <#if article.articleCommentCount != 0>
                                        <a class="ft-fade" href="${servePath}${article.articlePermalink}#comments"><b class="article-level<#if article.articleCommentCount lt 40>${(article.articleCommentCount/10)?int}<#else>4</#if>">${article.articleCommentCount}</b> ${cmtLabel}</a> &nbsp;•&nbsp;
                                        </#if>   

                                        <#if article.articleViewCount != 0> 
                                        <a class="ft-fade" href="${servePath}${article.articlePermalink}"><span class="article-level<#if article.articleViewCount lt 400>${(article.articleViewCount/100)?int}<#else>4</#if>">${article.articleViewCount}</span> ${viewLabel}</a> &nbsp;•&nbsp;
                                        </#if>
                                        <span class="ft-fade">${article.timeAgo} </span>
                                    </span>
                                </div>
                                <h2>
                                    <#if 1 == article.articlePerfect>
                                    <span class="tooltipped tooltipped-w" aria-label="${perfectLabel}"><svg height="20" width="14" viewBox="3 2 11 12">${perfectIcon}</svg></span>
                                    </#if>
                                    <#if 1 == article.articleType>
                                    <span class="tooltipped tooltipped-w" aria-label="${discussionLabel}"><span class="icon-locked"></span></span>
                                    <#elseif 2 == article.articleType>
                                    <span class="tooltipped tooltipped-w" aria-label="${cityBroadcastLabel}"><span class="icon-feed"></span></span>
                                    <#elseif 3 == article.articleType>
                                    <span class="tooltipped tooltipped-w" aria-label="${thoughtLabel}"><span class="icon-video"></span></span>
                                    </#if>
                                    <a data-id="${article.oId}" data-type="${article.articleType}" rel="bookmark" href="${servePath}${article.articlePermalink}">${article.articleTitleEmoj}
                                    </a>
                                </h2>
                                <div class="ft-smaller fn-clear list-info fn-flex">
                                    <span class="fn-ellipsis fn-flex-1">
                                        <#if article.articleAnonymous == 0>
                                        <a rel="nofollow" 
                                           href="${servePath}/member/${article.articleAuthorName}"></#if><div
                                                class="avatar-small"
                                                style="background-image:url('${article.articleAuthorThumbnailURL48}')"></div><#if article.articleAnonymous == 0></a></#if>&nbsp;
                                        <#if article.articleAnonymous == 0>
                                        <a rel="nofollow" class="author"
                                           href="${servePath}/member/${article.articleAuthorName}"></#if>
                                            ${article.articleAuthorName}
                                            <#if article.articleAnonymous == 0></a></#if>
                                        <#if article.articleAuthor.userIntro != ''><span class="ft-fade"> - ${article.articleAuthor.userIntro}</span></a></#if>
                                    </span>

                                    <span class="fn-right ft-fade fn-hidden">
                                        <#if "" != article.articleLatestCmterName>
                                        &nbsp; ${article.cmtTimeAgo} 
                                        <#if "" != article.articleLatestCmt.clientCommentId>
                                        <span class="author">${article.articleLatestCmterName}</span>
                                        <#else>
                                        <#if article.articleLatestCmterName != 'someone'>
                                        <a rel="nofollow" class="author" href="${servePath}/member/${article.articleLatestCmterName}"></#if><span class="author">${article.articleLatestCmterName}</span><#if article.articleLatestCmterName != 'someone'></a>
                                        </#if>
                                        </#if> 
                                        ${cmtLabel}
                                        </#if>
                                    </span>
                                </div>
                                <a class="abstract" href="${servePath}${article.articlePermalink}">
                                    ${article.articlePreviewContent}
                                </a>
                                <span class="heat tooltipped tooltipped-n" aria-label="${postActivityLabel}" style="width:${article.articleHeat*3}px"></span>
                            </li>
                            </#list>
                        </ul>
                        <ul class="fn-none">
                            <#list followingTagArticles as article>
                            <li>
                                <div class="fn-clear ft-smaller list-info">
                                    <span class="fn-left">
                                        <#list article.articleTagObjs as articleTag>
                                        <a rel="tag" href="${servePath}/tag/${articleTag.tagURI}">${articleTag.tagTitle}</a> &nbsp; 
                                        </#list>
                                    </span>
                                    <span class="fn-right ft-fade">
                                        <#if article.articleCommentCount != 0>
                                        <a class="ft-fade" href="${servePath}${article.articlePermalink}#comments"><b class="article-level<#if article.articleCommentCount lt 40>${(article.articleCommentCount/10)?int}<#else>4</#if>">${article.articleCommentCount}</b> ${cmtLabel}</a> &nbsp;•&nbsp;
                                        </#if>   

                                        <#if article.articleViewCount != 0> 
                                        <a class="ft-fade" href="${servePath}${article.articlePermalink}"><span class="article-level<#if article.articleViewCount lt 400>${(article.articleViewCount/100)?int}<#else>4</#if>">${article.articleViewCount}</span> ${viewLabel}</a> &nbsp;•&nbsp;
                                        </#if>
                                        <span class="ft-fade">${article.timeAgo} </span>
                                    </span>
                                </div>
                                <h2>
                                    <#if 1 == article.articlePerfect>
                                    <span class="tooltipped tooltipped-w" aria-label="${perfectLabel}"><svg height="20" width="14" viewBox="3 2 11 12">${perfectIcon}</svg></span>
                                    </#if>
                                    <#if 1 == article.articleType>
                                    <span class="tooltipped tooltipped-w" aria-label="${discussionLabel}"><span class="icon-locked"></span></span>
                                    <#elseif 2 == article.articleType>
                                    <span class="tooltipped tooltipped-w" aria-label="${cityBroadcastLabel}"><span class="icon-feed"></span></span>
                                    <#elseif 3 == article.articleType>
                                    <span class="tooltipped tooltipped-w" aria-label="${thoughtLabel}"><span class="icon-video"></span></span>
                                    </#if>
                                    <a data-id="${article.oId}" data-type="${article.articleType}" rel="bookmark" href="${servePath}${article.articlePermalink}">${article.articleTitleEmoj}
                                    </a>
                                </h2>
                                <div class="ft-smaller fn-clear list-info fn-flex">
                                    <span class="fn-ellipsis fn-flex-1">
                                        <#if article.articleAnonymous == 0>
                                        <a rel="nofollow" 
                                           href="${servePath}/member/${article.articleAuthorName}"></#if><div
                                                class="avatar-small"
                                                style="background-image:url('${article.articleAuthorThumbnailURL48}')"></div><#if article.articleAnonymous == 0></a></#if>&nbsp;
                                        <#if article.articleAnonymous == 0>
                                        <a rel="nofollow" class="author"
                                           href="${servePath}/member/${article.articleAuthorName}"></#if>
                                            ${article.articleAuthorName}
                                            <#if article.articleAnonymous == 0></a></#if>
                                        <#if article.articleAuthor.userIntro != ''><span class="ft-fade"> - ${article.articleAuthor.userIntro}</span></a></#if>
                                    </span>

                                    <span class="fn-right ft-fade fn-hidden">
                                        <#if "" != article.articleLatestCmterName>
                                        &nbsp; ${article.cmtTimeAgo} 
                                        <#if "" != article.articleLatestCmt.clientCommentId>
                                        <span class="author">${article.articleLatestCmterName}</span>
                                        <#else>
                                        <#if article.articleLatestCmterName != 'someone'>
                                        <a rel="nofollow" class="author" href="${servePath}/member/${article.articleLatestCmterName}"></#if><span class="author">${article.articleLatestCmterName}</span><#if article.articleLatestCmterName != 'someone'></a>
                                        </#if>
                                        </#if> 
                                        ${cmtLabel}
                                        </#if>
                                    </span>
                                </div>
                                <a class="abstract" href="${servePath}${article.articlePermalink}">
                                    ${article.articlePreviewContent}
                                </a>
                                <span class="heat tooltipped tooltipped-n" aria-label="${postActivityLabel}" style="width:${article.articleHeat*3}px"></span>
                            </li>
                            </#list>
                        </ul>
                        <ul class="fn-none">
                            <#list followingUserArticles as article>
                            <li>
                                <div class="fn-clear ft-smaller list-info">
                                    <span class="fn-left">
                                        <#list article.articleTagObjs as articleTag>
                                        <a rel="tag" href="${servePath}/tag/${articleTag.tagURI}">${articleTag.tagTitle}</a> &nbsp; 
                                        </#list>
                                    </span>
                                    <span class="fn-right ft-fade">
                                        <#if article.articleCommentCount != 0>
                                        <a class="ft-fade" href="${servePath}${article.articlePermalink}#comments"><b class="article-level<#if article.articleCommentCount lt 40>${(article.articleCommentCount/10)?int}<#else>4</#if>">${article.articleCommentCount}</b> ${cmtLabel}</a> &nbsp;•&nbsp;
                                        </#if>   

                                        <#if article.articleViewCount != 0> 
                                        <a class="ft-fade" href="${servePath}${article.articlePermalink}"><span class="article-level<#if article.articleViewCount lt 400>${(article.articleViewCount/100)?int}<#else>4</#if>">${article.articleViewCount}</span> ${viewLabel}</a> &nbsp;•&nbsp;
                                        </#if>
                                        <span class="ft-fade">${article.timeAgo} </span>
                                    </span>
                                </div>
                                <h2>
                                    <#if 1 == article.articlePerfect>
                                    <span class="tooltipped tooltipped-w" aria-label="${perfectLabel}"><svg height="20" width="14" viewBox="3 2 11 12">${perfectIcon}</svg></span>
                                    </#if>
                                    <#if 1 == article.articleType>
                                    <span class="tooltipped tooltipped-w" aria-label="${discussionLabel}"><span class="icon-locked"></span></span>
                                    <#elseif 2 == article.articleType>
                                    <span class="tooltipped tooltipped-w" aria-label="${cityBroadcastLabel}"><span class="icon-feed"></span></span>
                                    <#elseif 3 == article.articleType>
                                    <span class="tooltipped tooltipped-w" aria-label="${thoughtLabel}"><span class="icon-video"></span></span>
                                    </#if>
                                    <a data-id="${article.oId}" data-type="${article.articleType}" rel="bookmark" href="${servePath}${article.articlePermalink}">${article.articleTitleEmoj}
                                    </a>
                                </h2>
                                <div class="ft-smaller fn-clear list-info fn-flex">
                                    <span class="fn-ellipsis fn-flex-1">
                                        <#if article.articleAnonymous == 0>
                                        <a rel="nofollow" 
                                           href="${servePath}/member/${article.articleAuthorName}"></#if><div
                                                class="avatar-small"
                                                style="background-image:url('${article.articleAuthorThumbnailURL48}')"></div><#if article.articleAnonymous == 0></a></#if>&nbsp;
                                        <#if article.articleAnonymous == 0>
                                        <a rel="nofollow" class="author"
                                           href="${servePath}/member/${article.articleAuthorName}"></#if>
                                            ${article.articleAuthorName}
                                            <#if article.articleAnonymous == 0></a></#if>
                                        <#if article.articleAuthor.userIntro != ''><span class="ft-fade"> - ${article.articleAuthor.userIntro}</span></a></#if>
                                    </span>

                                    <span class="fn-right ft-fade fn-hidden">
                                        <#if "" != article.articleLatestCmterName>
                                        &nbsp; ${article.cmtTimeAgo} 
                                        <#if "" != article.articleLatestCmt.clientCommentId>
                                        <span class="author">${article.articleLatestCmterName}</span>
                                        <#else>
                                        <#if article.articleLatestCmterName != 'someone'>
                                        <a rel="nofollow" class="author" href="${servePath}/member/${article.articleLatestCmterName}"></#if><span class="author">${article.articleLatestCmterName}</span><#if article.articleLatestCmterName != 'someone'></a>
                                        </#if>
                                        </#if> 
                                        ${cmtLabel}
                                        </#if>
                                    </span>
                                </div>
                                <a class="abstract" href="${servePath}${article.articlePermalink}">
                                    ${article.articlePreviewContent}
                                </a>
                                <span class="heat tooltipped tooltipped-n" aria-label="${postActivityLabel}" style="width:${article.articleHeat*3}px"></span>
                            </li>
                            </#list>
                        </ul>
                    </div>
                </div>
                <div class="index-side">
                    <div class="index-tabs fn-flex">
                        <span class="perfect current">
                            <svg height="16" viewBox="3 2 11 12" width="14">${perfectIcon}</svg>
                            ${perfectLabel}
                        </span>
                        <span class="check">
                            <#if isLoggedIn && !isDailyCheckin>
                            <a href="<#if useCaptchaCheckin>${servePath}/activity/checkin<#else>${servePath}/activity/daily-checkin</#if>">${dailyCheckinLabel}</a>
                            <#else>
                            <a href="${servePath}/activities">
                                ${activityLabel}
                            </a>
                            </#if>
                        </span>
                        <span class="post"><a href="${servePath}/pre-post">${postArticleLabel}</a></span>
                    </div>
                    <div class="perfect-panel list">
                        <ul>
                            <#list perfectArticles as article>
                            <li>
                                <a rel="nofollow" href="${servePath}/member/${article.articleAuthorName}">
                                    <span class="avatar-small tooltipped tooltipped-se" aria-label="wulalala" style="background-image:url('${article.articleAuthorThumbnailURL48}')"></span>
                                </a>
                                <a rel="nofollow" class="fn-ellipsis" href="${servePath}${article.articlePermalink}">${article.articleTitleEmoj}</a>
                                <a class="fn-right count ft-gray ft-smaller" href="${servePath}${article.articlePermalink}">${article.articleViewCount}</a>
                            </li>
                            </#list>
                        </ul>
                    </div>
                </div>
            </div>
            <div>
                <br/><br/>
            </div>
            <div class="wrapper">
                <div class="index-main">
                    <div class="metro-line fn-flex">
                        <div class="metro-item">
                            <a class="preview" href="${servePath}/tag/${tag0.tagURI}">
                                <img src="${staticServePath}/images/tags/${tag0.tagIconPath}" alt="${tag0.tagTitle}">
                                <b>${tag0.tagTitle}</b>
                            </a>
                        </div>
                        <div class="metro-item mid">
                            <a class="preview" href="${servePath}/tag/${tag1.tagURI}">
                                <img src="${staticServePath}/images/tags/${tag1.tagIconPath}" alt="${tag1.tagTitle}">
                                <b>${tag1.tagTitle}</b>
                            </a>
                        </div>
                        <div class="metro-item">
                            <a class="preview" href="${servePath}/tag/${tag2.tagURI}">
                                <img src="${staticServePath}/images/tags/${tag2.tagIconPath}" alt="${tag2.tagTitle}">
                                <b>${tag2.tagTitle}</b>
                            </a>
                        </div>
                    </div>
                    <div class="metro-line fn-flex">
                        <div class="metro-item">
                            <a class="preview" href="${servePath}/tag/${tag3.tagURI}">
                                <img src="${staticServePath}/images/tags/${tag3.tagIconPath}" alt="${tag3.tagTitle}">
                                <b>${tag3.tagTitle}</b>
                            </a>
                        </div>
                        <div class="metro-item mid">
                            <a class="preview" href="${servePath}/tag/${tag4.tagURI}">
                                <img src="${staticServePath}/images/tags/${tag4.tagIconPath}" alt="${tag4.tagTitle}">
                                <b>${tag4.tagTitle}</b>
                            </a>
                        </div>
                        <div class="metro-item">
                            <a class="preview" href="${servePath}/tag/${tag5.tagURI}">
                                <img src="${staticServePath}/images/tags/${tag5.tagIconPath}" alt="${tag5.tagTitle}">
                                <b>${tag5.tagTitle}</b>
                            </a>
                        </div>
                    </div>
                    <div class="metro-line fn-flex">
                        <div class="metro-item">
                            <a class="preview" href="${servePath}/tag/${tag6.tagURI}">
                                <img src="${staticServePath}/images/tags/${tag6.tagIconPath}" alt="${tag6.tagTitle}">
                                <b>${tag6.tagTitle}</b>
                            </a>
                        </div>
                        <div class="metro-item mid">
                            <a class="preview" href="${servePath}/tag/${tag7.tagURI}">
                                <img src="${staticServePath}/images/tags/${tag7.tagIconPath}" alt="${tag7.tagTitle}">
                                <b>${tag7.tagTitle}</b>
                            </a>
                        </div>
                        <div class="metro-item">
                            <a class="preview" href="${servePath}/tag/${tag8.tagURI}">
                                <img src="${staticServePath}/images/tags/${tag8.tagIconPath}" alt="${tag8.tagTitle}">
                                <b>${tag8.tagTitle}</b>
                            </a>
                        </div>
                    </div>
                    <div class="metro-border fn-flex">
                        <div></div>
                        <div class="red"></div>
                        <div class="green"></div>
                    </div>
                </div>
                <div class="index-side down">
                    <div class="list timeline">
                        <#if timelines?size <= 0>
                        <div id="emptyTimeline">${emptyTimelineLabel}</div>
                        </#if>
                        <ul>
                            <#list timelines as article>
                            <#if article_index < 15>
                            <li class="fn-ellipsis">
                                ${article.content}
                            </li>
                            </#if>
                        </#list>
                    </ul>
                </div>
                <div class="metro-line fn-flex">
                    <div class="metro-item"> ${ADLabel}</div>
                    <div class="metro-item last">
                        <a class="preview" href="https://hacpai.com/article/1460083956075">
                            <img width="44px" src="${staticServePath}/emoji/graphics/emojis/heart.png" alt="${sponsorLabel}">
                            <b>${wantPutOnLabel}</b>
                        </a>
                    </div>
                </div>
                <div class="metro-border fn-flex">
                    <div></div>
                    <div class="yellow"></div>
                </div>
            </div>
        </div>
    </div>
    <#include "footer.ftl">     
    <script type="text/javascript">
        $('.metro-item').height($('.metro-item').width());
        $('.timeline').outerHeight($('.metro-item').width() * 2 + 2);

        $('#articles span').click(function () {
            var $it = $(this);
            $('#articles span').removeClass('current');
            $it.addClass('current');

            $(".index-tabs-panels.article-list ul").hide();
            if ($it.hasClass('tags')) {
                $(".index-tabs-panels.article-list ul:eq(1)").show();
            } else if ($it.hasClass('users')) {
                $(".index-tabs-panels.article-list ul:eq(2)").show();
            } else {
                $(".index-tabs-panels.article-list ul:eq(0)").show();
            }
        });

        var $perfectList = $('.perfect-panel'),
                pTop = $perfectList.offset().top + $perfectList.outerHeight();
        $(window).scroll(function () {
            if ($(window).scrollTop() > pTop) {
                $perfectList.parent().hide();
                $(".index-tabs-panels.article-list").parent().css('width', '100%');
            }
            if ($(window).scrollTop() < 200) {
                $perfectList.parent().show();
                $(".index-tabs-panels.article-list").parent().css('width', '60%');
            }
        });
    </script>
</body>
</html>
