package com.mytlogos.enterprise.tools


/*
 * Copyright 2016-2018 Davide Steduto, Davidea Solutions Sprl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import android.R
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION_CODES


/**
 * Modified short Version of Original FlexibleAdapter Library File "FlexibleUtils".
 *
 * @author Davide Steduto
 *
 * @since 27/01/2016 Created in main package
 * <br></br>17/12/2017 Moved into UI package
 * <br></br>12/05/2018 Added even more utils
 */

/*----------------*/ /* VERSIONS UTILS */ /*----------------*/
fun getVersionName(context: Context): String {
    return try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        pInfo.versionName
    } catch (e: PackageManager.NameNotFoundException) {
        context.getString(R.string.unknownName)
    }
}

fun getVersionCode(context: Context): Int {
    return try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        pInfo.versionCode
    } catch (e: PackageManager.NameNotFoundException) {
        0
    }
}

/**
 * API 26
 *
 * @see VERSION_CODES.O
 */
fun hasOreo(): Boolean {
    return Build.VERSION.SDK_INT >= VERSION_CODES.O
}

/**
 * API 24
 *
 * @see VERSION_CODES.N
 */
fun hasNougat(): Boolean {
    return Build.VERSION.SDK_INT >= VERSION_CODES.N
}

/**
 * API 23
 *
 * @see VERSION_CODES.M
 */
fun hasMarshmallow(): Boolean {
    return Build.VERSION.SDK_INT >= VERSION_CODES.M
}

/**
 * API 21
 *
 * @see VERSION_CODES.LOLLIPOP
 */
fun hasLollipop(): Boolean {
    return Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP
}

/**
 * API 16
 *
 * @see VERSION_CODES.JELLY_BEAN
 */
fun hasJellyBean(): Boolean {
    return Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN
}