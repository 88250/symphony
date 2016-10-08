/*
 * Copyright (c) 2012-2016, b3log.org & hacpai.com & fangstar.com
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
 * @file frontend tool.
 * 
 * @author <a href="mailto:liliyuan@fangstar.net">Liyuan Li</a>
 * @version 1.3.2.0, Oct 8, 2016 
 */

'use strict';

var gulp = require("gulp");
var concat = require('gulp-concat');
var cleanCSS = require('gulp-clean-css');
var uglify = require('gulp-uglify');
var sass = require('gulp-sass');
var clean = require('gulp-clean');
var rename = require('gulp-rename');

gulp.task('sass', function () {
    return gulp.src('./src/main/webapp/scss/*.scss')
            .pipe(sass({outputStyle: 'compressed'}).on('error', sass.logError))
            .pipe(gulp.dest('./src/main/webapp/css'));
});

gulp.task('sass:watch', function () {
    gulp.watch('./src/main/webapp/scss/*.scss', ['sass']);
});


gulp.task('build', function () {
    // css
    gulp.src('./src/main/webapp/js/lib/editor/codemirror.css')
            .pipe(cleanCSS())
            .pipe(concat('codemirror.min.css'))
            .pipe(gulp.dest('./src/main/webapp/js/lib/editor/'));

    // js
    var jsJqueryUpload = ['./src/main/webapp/js/lib/jquery/file-upload-9.10.1/vendor/jquery.ui.widget.js',
        './src/main/webapp/js/lib/jquery/file-upload-9.10.1/jquery.iframe-transport.js',
        './src/main/webapp/js/lib/jquery/file-upload-9.10.1/jquery.fileupload.js',
        './src/main/webapp/js/lib/jquery/file-upload-9.10.1/jquery.fileupload-process.js',
        './src/main/webapp/js/lib/jquery/file-upload-9.10.1/jquery.fileupload-validate.js'];
    gulp.src(jsJqueryUpload)
            .pipe(uglify())
            .pipe(concat('jquery.fileupload.min.js'))
            .pipe(gulp.dest('./src/main/webapp/js/lib/jquery/file-upload-9.10.1/'));

    var jsCodemirror = ['./src/main/webapp/js/lib/editor/diff_match_patch.js',
        './src/main/webapp/js/lib/editor/codemirror.js',
        './src/main/webapp/js/lib/editor/placeholder.js',
        './src/main/webapp/js/lib/editor/merge.js',
        './src/main/webapp/js/overwrite/codemirror/addon/hint/show-hint.js',
        './src/main/webapp/js/lib/editor/editor.js',
        './src/main/webapp/js/lib/to-markdown.js'];
    gulp.src(jsCodemirror)
            .pipe(uglify())
            .pipe(concat('codemirror.min.js'))
            .pipe(gulp.dest('./src/main/webapp/js/lib/editor/'));

    var jsCommonLib = ['./src/main/webapp/js/lib/jquery/jquery-3.1.0.min.js',
        './src/main/webapp/js/lib/md5.js',
        './src/main/webapp/js/lib/reconnecting-websocket.min.js',
        './src/main/webapp/js/lib/jquery/jquery.bowknot.min.js',
        './src/main/webapp/js/lib/ua-parser.min.js',
        './src/main/webapp/js/lib/jquery/jquery.hotkeys.js'];
    gulp.src(jsCommonLib)
            .pipe(uglify())
            .pipe(concat('libs.min.js'))
            .pipe(gulp.dest('./src/main/webapp/js/lib/compress/'));

    var jsArticleLib = [
        // start codemirror.min.js
        './src/main/webapp/js/lib/editor/diff_match_patch.js',
        './src/main/webapp/js/lib/editor/codemirror.js',
        './src/main/webapp/js/lib/editor/placeholder.js',
        './src/main/webapp/js/lib/editor/merge.js',
        './src/main/webapp/js/overwrite/codemirror/addon/hint/show-hint.js',
        './src/main/webapp/js/lib/editor/editor.js',
        './src/main/webapp/js/lib/to-markdown.js',
        // end codemirror.min.js
        './src/main/webapp/js/lib/highlight.js-9.6.0/highlight.pack.js',
        // start jquery.fileupload.min.js
        './src/main/webapp/js/lib/jquery/file-upload-9.10.1/vendor/jquery.ui.widget.js',
        './src/main/webapp/js/lib/jquery/file-upload-9.10.1/jquery.iframe-transport.js',
        './src/main/webapp/js/lib/jquery/file-upload-9.10.1/jquery.fileupload.js',
        './src/main/webapp/js/lib/jquery/file-upload-9.10.1/jquery.fileupload-process.js',
        './src/main/webapp/js/lib/jquery/file-upload-9.10.1/jquery.fileupload-validate.js',
        // end jquery.fileupload.min.js
        './src/main/webapp/js/lib/sound-recorder/SoundRecorder.js',
        './src/main/webapp/js/lib/jquery/jquery.qrcode.min.js',
        './src/main/webapp/js/lib/zeroclipboard/ZeroClipboard.min.js'];
    gulp.src(jsArticleLib)
            .pipe(uglify())
            .pipe(concat('article-libs.min.js'))
            .pipe(gulp.dest('./src/main/webapp/js/lib/compress/'));

});