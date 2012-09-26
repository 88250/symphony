/*
 * Copyright (c) 2012, B3log Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * @fileoverview home.
 *
 * @author <a href="mailto:LLY219@gmail.com">Liyuan Li</a>
 * @version 1.0.0.3, Sep 27, 2012
 */

/**
 * @description Home function
 * @static
 */
var Home = {
    /**
     * @description 发文校验
     */
    _validateArticle: function () {
        var $title = $("#articleTitle"),
        $content = $("#articleContent"),
        $tags = $("#articleTags"),
        $tip = $("#tip");
        
        var titleVal = $title.val().replace(/(^\s*)|(\s*$)/g, ""),
        contentVal = $content.val().replace(/(^\s*)|(\s*$)/g, ""),
        tagsList = $tags.val().replace(/(^\s*)|(\s*$)/g, "").split(",");
        
        
        if (titleVal.length === 0 || titleVal.length > 255) {
            $tip.addClass("tip-error").text(Label.articleTitleErrorLabel);
            $title.focus();
            return false;
        } else if (contentVal.length < 4 || contentVal.length > 1048576) {
            $tip.addClass("tip-error").text(Label.articleContentErrorLabel);
            $content.focus();
            return false;
        } else if ($tags.val().replace(/(^\s*)|(\s*$)/g, "") === "" || tagsList.length > 7) {
            $tip.addClass("tip-error").text(Label.articleTagsErrorLabel);
            $tags.focus();
            return false;
        } else if (tagsList.length < 8) {
            for (var i = 0; i < tagsList.length; i++) {
                if (tagsList[i].replace(/(^\s*)|(\s*$)/g, "").length > 50) {
                    $tip.addClass("tip-error").text(Label.articleTagsErrorLabel);
                    $tags.focus();
                    return false;
                }
            }    
        }
        
        $tip.removeClass("tip-error").text("");
        return true;
    },
    
    /**
     * @description 发布文章
     */
    postArticle: function () {
        if (this._validateArticle()) {
            var requestJSONObject = {
                articleTitle: $("#articleTitle").val().replace(/(^\s*)|(\s*$)/g,""),
                articleContent: $("#articleContent").val(),
                articleTags: $("#articleTags").val().replace(/(^\s*)|(\s*$)/g,""),
                syncWithSymphonyClient: $("#syncWithSymphonyClient").prop("checked")
            };
            
            $.ajax({
                url: "/article",
                type: "PUT",
                cache: false,
                data: JSON.stringify(requestJSONObject),
                success: function(result, textStatus){
                    if (result.sc) {
                        window.location = "/article-list";
                    } else {
                        $tip.addClass("tip-error").text(result.msg);
                    }
                }
            });
        }
    },
     
    /**
     * @description 设置侧边栏导航状态
     */
    _initNav: function () {
        var pathname = location.pathname;
        $(".side a").each(function () {
            if (pathname === $(this).attr("href")) {
                $(this).addClass("current");
            } 
        });
    },
    
    /**
     * @description 初识化后台页面
     */
    init: function () {
    // 侧边栏导航
    //this._initNav();
    }
};


/**
 * @description Settings function
 * @static
 */
var Settings = {
    /**
     * @description Setting 页面模块收起展开。
     */
    toggle: function (it) {
        var $it = $(it);
        var $panel = $it.parents(".module").find(".module-panel");
      
        if (it.className === "slideUp") {
            $panel.slideDown();
            it.className = "slideDown";
            $it.attr("title", "收拢");
        } else {
            $panel.slideUp();
            it.className = "slideUp";
            $it.attr("title", "展开");    
        }
    },
    
    /**
     * @description 更新 settings 页面数据。
     */
    update: function () {
        if (!this._validate()) {
            return;
        }
        
        $.ajax({
            url: "/show-settings",
            type: "GET",
            cache: false,
            success: function(result, textStatus){
                if (result.sc) {
                    window.location = "/article-list";
                } else {
                    $tip.addClass("tip-error").text(result.msg);
                }
            }
        });
    },
    
    /**
     * @description settings 页面数据校验
     */
    _validate: function () {
        
    }
};

