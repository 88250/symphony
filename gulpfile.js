/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
/**
 * @file frontend tool.
 *
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.8.0.0, Oct 2, 2018
 */

'use strict'

var gulp = require('gulp')
var concat = require('gulp-concat')
var cleanCSS = require('gulp-clean-css')
var uglify = require('gulp-uglify')
var sass = require('gulp-sass')
var rename = require('gulp-rename')
var del = require('del')

function sassProcess () {
  return gulp.src('./src/main/webapp/scss/*.scss').
    pipe(sass({outputStyle: 'compressed'}).on('error', sass.logError)).
    pipe(gulp.dest('./src/main/webapp/css'))
}

function sassProcessWatch () {
  gulp.watch('./src/main/webapp/scss/*.scss', sassProcess)
}

gulp.task('watch', gulp.series(sassProcessWatch))

function cleanProcess () {
  return del(['./src/main/webapp/js/*.min.js'])
}

function minCodemirrorCSS () {
  // min css
  return gulp.src('./src/main/webapp/js/lib/editor/codemirror.css').
    pipe(cleanCSS()).
    pipe(concat('codemirror.min.css')).
    pipe(gulp.dest('./src/main/webapp/js/lib/editor/'))
}

function minArticleCSS () {
  // min article css
  return gulp.src([
    './src/main/webapp/js/lib/editor/codemirror.min.css',
    './src/main/webapp/js/lib/highlight/styles/github.css',
    './src/main/webapp/js/lib/diff2html/diff2html.min.css']).
    pipe(cleanCSS()).
    pipe(concat('article.min.css')).
    pipe(gulp.dest('./src/main/webapp/js/lib/compress/'))
}

function minJS () {
  // min js
  return gulp.src('./src/main/webapp/js/*.js').
    pipe(uglify()).
    pipe(rename({suffix: '.min'})).
    pipe(gulp.dest('./src/main/webapp/js/'))
}

function minUpload () {
  // concat js
  var jsJqueryUpload = [
    './src/main/webapp/js/lib/jquery/file-upload-9.10.1/vendor/jquery.ui.widget.js',
    './src/main/webapp/js/lib/jquery/file-upload-9.10.1/jquery.iframe-transport.js',
    './src/main/webapp/js/lib/jquery/file-upload-9.10.1/jquery.fileupload.js',
    './src/main/webapp/js/lib/jquery/file-upload-9.10.1/jquery.fileupload-process.js',
    './src/main/webapp/js/lib/jquery/file-upload-9.10.1/jquery.fileupload-validate.js']
  return gulp.src(jsJqueryUpload).
    pipe(uglify()).
    pipe(concat('jquery.fileupload.min.js')).
    pipe(gulp.dest('./src/main/webapp/js/lib/jquery/file-upload-9.10.1/'))
}

function minCodemirrorJS () {
  var jsCodemirror = [
    './src/main/webapp/js/lib/editor/codemirror.js',
    './src/main/webapp/js/overwrite/codemirror/addon/hint/show-hint.js',
    './src/main/webapp/js/lib/editor/placeholder.js',
    './src/main/webapp/js/lib/editor/editor.js',
    './src/main/webapp/js/lib/to-markdown.js']
  return gulp.src(jsCodemirror).
    pipe(uglify({output: {ascii_only: true}})) // https://github.com/b3log/symphony/issues/765
    .pipe(concat('codemirror.min.js')).
    pipe(gulp.dest('./src/main/webapp/js/lib/editor/'))
}

function minLibs () {
  var jsCommonLib = [
    './src/main/webapp/js/lib/jquery/jquery-3.1.0.min.js',
    './src/main/webapp/js/lib/md5.js',
    './src/main/webapp/js/lib/reconnecting-websocket.min.js',
    './src/main/webapp/js/lib/jquery/jquery.bowknot.min.js',
    './src/main/webapp/js/lib/ua-parser.min.js',
    './src/main/webapp/js/lib/jquery/jquery.hotkeys.js',
    './src/main/webapp/js/lib/jquery/jquery.pjax.js',
    './src/main/webapp/js/lib/nprogress/nprogress.js']
  return gulp.src(jsCommonLib).
    pipe(uglify()).
    pipe(concat('libs.min.js')).
    pipe(gulp.dest('./src/main/webapp/js/lib/compress/'))
}

function minArticleLibs () {
  var jsArticleLib = [
    './src/main/webapp/js/lib/editor/codemirror.min.js',
    './src/main/webapp/js/lib/highlight/highlight.pack.js',
    './src/main/webapp/js/lib/jquery/file-upload-9.10.1/jquery.fileupload.min.js',
    './src/main/webapp/js/lib/sound-recorder/SoundRecorder.js',
    './src/main/webapp/js/lib/jquery/jquery.qrcode.min.js',
    './src/main/webapp/js/lib/aplayer/APlayer.min.js',
    './src/main/webapp/js/lib/diff2html/diff2html.min.js',
    './src/main/webapp/js/lib/diff2html/diff2html-ui.min.js',
    './src/main/webapp/js/lib/diff2html/diff.min.js']
  return gulp.src(jsArticleLib).
    pipe(uglify()).
    pipe(concat('article-libs.min.js')).
    pipe(gulp.dest('./src/main/webapp/js/lib/compress/'))
}

gulp.task('default',
  gulp.series(cleanProcess, sassProcess,
    gulp.parallel(minCodemirrorCSS, minJS, minUpload,
      minCodemirrorJS, minLibs), gulp.parallel(minArticleCSS, minArticleLibs)))