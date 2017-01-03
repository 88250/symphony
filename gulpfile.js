/*
 * Symphony - A modern community (forum/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2017,  b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * @file frontend tool.
 * 
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.4.3.2, Dec 21, 2016
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

gulp.task('clean', ['sass'], function () {
    // remove min js
    return gulp.src('./src/main/webapp/js/*.min.js', {read: false})
            .pipe(clean());
});


gulp.task('build', ['sass', 'clean'], function () {
    // min css
    gulp.src('./src/main/webapp/js/lib/editor/codemirror.css')
            .pipe(cleanCSS())
            .pipe(concat('codemirror.min.css'))
            .pipe(gulp.dest('./src/main/webapp/js/lib/editor/'));
    
     // min js
    gulp.src('./src/main/webapp/js/*.js')
            .pipe(uglify())
            .pipe(rename({suffix: '.min'}))
            .pipe(gulp.dest('./src/main/webapp/js/'));

    // concat js
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

gulp.task('default', ['sass', 'clean', 'build']);