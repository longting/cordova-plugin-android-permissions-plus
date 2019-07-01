Android Permission Cordova Plugin Plus
========

这是一份来自 [cordova-plugin-android-permissions](https://github.com/NeoLSN/cordova-plugin-android-permissions) 的拷贝，原来的版本缺少维护，并且存在致命缺陷。 

This plugin is designed for supporting Android new permissions checking mechanism.

Since Android 6.0, the Android permissions checking mechanism has been changed. In the past, the permissions were granted by users when they decide to install the app. Now, the permissions should be granted by a user when he/she is using the app.

For old Android plugins you (developers) are using may not support this new mechanism or already stop updating. So either to find a new plugin to solving this problem, nor trying to add the mechanism in the old plugin. If you don't want to do those, you can try this plugin.

As a convenience we support browser and iOS platforms as well. But this plugin will simple reply that any permission checked of requested was granted.

新特性
---------
1. 解决了里面的若干bug
2. 并且去掉了废弃的接口，仅支持两个批量接口checkPermissions和requestPermissions
3. 代码中增加了必要的日志输出
4. 权限列表有增加

Installation
--------

```
cordova plugin add https://github.com/longting/cordova-plugin-android-permissions-plus.git
```

※ Support Android SDK >= 14

Usage
--------

### API

```javascript
var permissions = cordova.plugins.permissions;
permissions.checkPermissions(permissions, successCallback, errorCallback);
permissions.requestPermissions(permissions, successCallback, errorCallback);
```

### Permission Name

Following the Android design. See [Manifest.permission](http://developer.android.com/intl/zh-tw/reference/android/Manifest.permission.html).
```javascript
// Example
permissions.ACCESS_COARSE_LOCATION
permissions.CAMERA
permissions.GET_ACCOUNTS
permissions.READ_CONTACTS
permissions.READ_CALENDAR
...
```

#### Example multiple permissions
```js
var permissions = cordova.plugins.permissions;
var list = [
    permissions.WRITE_EXTERNAL_STORAGE,
    permissions.READ_CONTACTS,
    permissions.ACCESS_FINE_LOCATION
    ];

permissions.checkPermissions(list, success, error);// 批量检查指定权限

function error() {
    console.warn('检查或者获取权限发生的错误');
}

function success( status ) {
    console.log("检查完成")
    if( !status.hasPermission ) {
        permissions.requestPermissions(// 批量获取指定权限
            list,
            function(status) {
                if( !status.hasPermission ){
                    console.log("获取权限失败"+JSON.stringify(status))
                    error()
                } else {
                    console.log("申请权限成功")
                }                            
            },
            error
        );
    }else{
          console.log("有权限")

    }
}

```

License
--------

    Copyright (C) 2016 Jason Yang

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
