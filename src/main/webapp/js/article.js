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
 * @version 1.0.0.2, Oct 2, 2012
 */

/**
 * @description Add article function.
 * @static
 */
var AddArticle = {
    _validateData: [{
        "id": "articleTitle",
        "type": 256,
        "msg": Label.articleTitleErrorLabel
    }, {
        "id": "articleContent",
        "type": 1048576,
        "msg": Label.articleContentErrorLabel
    }, {
        "id": "articleTags",
        "type": "tags",
        "msg": Label.articleTagsErrorLabel
    }],

    /**
     * @description 发布文章
     */
    add: function () {
        if (Validate.goValidate(this._validateData)) {
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
                        $("#tip").addClass("tip-error").text(result.msg);
                    }
                }
            });
        }
    },
    
    /**
     * @description 初识化发文页面
     */
    init: function () {
        // init validate
        Validate.initValidate(this._validateData);
    }
};

AddArticle.init();