public class
# Cookies
com.pkuhelper.lib.webconnection.Cookies

### Class Overview
一个用来设置和添加cookies的工具类。

含有一个私有静态成员变量**cookies**，以Key-Value形式存储域名和cookies内容。

### Public Methods
> public static void **setCookie**(HttpResponse response, String url)

将response中“Set-Cookie”应答头内容以Key-Value形式格式化。获取url所在的域名，并将域名与cookies以Key-Value形式暂存在本地内存。
- **Parameters**
	- *response* - cookies的源。
	- *url* - cookies将被暂存在*url*所在的域名的Key下。

***
> public static void **addCookie**(HttpRequestBase httpRequestBase)

将本地暂存的cookies加入Http请求的header标头。
- **Parameters**
	- *httpRequestBase* - 将被加入cookies的Http请求。
	
[Back to API docs](../../../docs.md)