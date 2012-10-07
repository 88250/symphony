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
 * @fileoverview article page and add comment.
 *
 * @author <a href="mailto:LLY219@gmail.com">Liyuan Li</a>
 * @version 1.0.0.1, Oct 7, 2012
 */

/**
 * @description Add comment function.
 * @static
 */
var Comment = {
    _validateData: [{
        "id": "commentContent",
        "type": 256,
        "msg": Label.commentErrorLabel
    }],

    /**
     * @description 添加评论
     */
    add: function (id) {
        if (Validate.goValidate(this._validateData)) {
            var requestJSONObject = {
                articleId: id,
                commentContent: $("#commentContent").val().replace(/(^\s*)|(\s*$)/g,"")
            };
            
            $.ajax({
                url: "/comment",
                type: "PUT",
                cache: false,
                data: JSON.stringify(requestJSONObject),
                success: function(result, textStatus){
                    if (result.sc) {
                        window.location.reload();
                    } else {
                        $("#commentContent").next().addClass("tip-error").text(result.msg);
                    }
                }
            });
        }
    },
    
    /**
     * @description 初识化发文页面
     */
    init: function () {
        $("#commentContent").val("");
    }
};

Comment.init();
