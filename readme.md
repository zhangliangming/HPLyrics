# 简介 #
该开源依赖库是乐乐音乐播放器里的一个歌词模块功能，现在把该功能模块独立出来进行优化，并弄成了一个开源依赖库，其它音乐播放器项目只要引用该库并调用接口，便可轻松实现与乐乐音乐播放器一样的动感歌词显示效果，其项目地址如下：[乐乐音乐播放器](https://github.com/zhangliangming/HappyPlayer5.git)。

# 使用注意 #

- 1.x版本，只要是使用自定义view来实现，每次都使用handler去刷新view，但是如果handler队列中有很多任务执行，那就无法保证歌词每次都在100ms内刷新一次。
- 2.x版本，主要是使用surfaceview来实现，每次刷新时间为40ms，歌词渐变相对会流畅。
- 3.x版本，主要是使用TextureView来实现，每次刷新时间修改为50ms，TextureView支持view的相关动画属性

# 2.x版本使用注意 #
- 主题：我主要是使用Theme.AppCompat.Light.NoActionBar的主题，我试过其它的主题，会导致surfaceview背景为黑色，并且不能透明的问题。
- surfaceview存在的问题，没有view相关的旋转，位移等动画，所以我乐乐音乐的旋转界面会出现问题，如果有相关动画需求的，慎用。

# 3.x版本使用注意 #
- 设置硬件加速：android:hardwareAccelerated="true" 
- android4.0以上

# 日志 #
## v3.2 ##
- 2018-05-05
- 添加混淆
- 添加刷新时间

## v3.0 ##
- 2018-04-22
- surfaceview替换成TextureView

## v2.6 ##
- 2018-04-22
- 修复后台回到前台时，歌词视图内容为空的问题
- 修复初始歌词数据时，OffsetY值没还原的问题

## v2.4 ##
- 2018-04-21
- 自定义view替换成surfaceview
- 添加获取歌词参数方法

## v1.44 ##
- 2018-08-11
- 添加HandlerThread
- 修复歌词类型切换

## v1.40 ##
- 2018-06-02
- minSdkVersion 修改为19

## v1.36 ##
- 2018-05-12
- 双行歌词的默认歌词添加居左显示和居中显示模式
- 双行歌词不回手动设置字体大小标记

## v1.34 ##
- 2018-05-07
- 修复歌词快进点击按钮事件
- 2018-05-06
- 修复自定义view歌词

## v1.x ##

- 修复制作歌词无法完成的问题
- 修改音译歌词显示
- 添加制作音译歌词实体
- 修改制作翻译歌词实体
- 添加制作翻译歌词实体
- 添加修改绘画指示器颜色接口
- 修复制作歌词问题
- LyricsReader添加设置歌词数据
- 添加制作歌词实体
- 添加获取制作歌词状态接口
- 添加获取制作后的歌词接口
- 添加制作歌词预览视图
- 添加额外歌词生成图片视图预览和生成额外歌词图片功能
- 修复歌词生成图片问题
- 修复歌词生成图片问题
- 修复歌词生成图片视图的字体
- 修改部分int变量的类型为long
- 修改部分int变量的类型为float
- 添加歌词生成图片文件接口
- 添加歌词生成图片预览视图
- 修复通过歌曲文件名获取歌词文件问题
- 修复多行歌词未读时渐变的问题
- 修复最后一个字渐变出错的问题
- 修改歌词每次刷新的间隔最少为100ms
- 修改歌词每次刷新的间隔最少为20ms
- 修复未读到下一行歌词时，上一行歌词渐变宽度为0的问题
- 修复设置歌词读取器的问题
- 2018-03-04
- 修复双行歌词加载歌词完成后，显示额外歌词渐变出错的问题
- 修改了多行歌词，滑动时的指示器渐变颜色
## v1.2 ##

- 添加歌词view获取歌词读取器方法
## v1.1 ##

- 添加歌词读取器获取歌词实体类方法
## v1.0 ##

- 实现lrc、ksc、krc和hrc歌词格式的显示
- 实现双多行歌词的显示、字体大小、颜色、歌词换行
- 多行歌词的快进、平滑移动、颜色渐变

# 预览图 #

## 主界面 ##

![](https://i.imgur.com/QJnz3sV.png)

## 歌词文件读取并预览 ##

![](https://i.imgur.com/8ZJYEni.png)

## 双行歌词-动感歌词 ##

![](https://i.imgur.com/rDsotfc.png)

## 双行歌词-音译歌词 ##

![](https://i.imgur.com/Q8AOiAB.png)

## 双行歌词-翻译歌词 ##

![](https://i.imgur.com/wlWCzSr.png)

## 多行歌词-lrc歌词 ##

![](https://i.imgur.com/VgFCIyG.png)

## 多行歌词-动感歌词 ##

![](https://i.imgur.com/XkNMk7l.png)

## 多行歌词-音译歌词 ##

![](https://i.imgur.com/7X6AtbZ.png)

## 多行歌词-翻译歌词 ##

![](https://i.imgur.com/g4oZvRw.png)

## 多行歌词-快进 ##

![](https://i.imgur.com/d2g7jc1.png)


# Gradle #
1.root build.gradle

	`allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}`
	
2.app build.gradle

`dependencies {
	         compile 'com.github.zhangliangming:HPLyrics:v1.44'
	}`


# 混淆注意 #
-keep class com.zlm.hp.lyrics.** { *; }

# 调用Demo #

链接: [https://pan.baidu.com/s/1eA5pcs0pUnr9gXhosek9Bw](https://pan.baidu.com/s/1eA5pcs0pUnr9gXhosek9Bw "https://pan.baidu.com/s/1eA5pcs0pUnr9gXhosek9Bw") 密码: awcf

# 调用用法 #

![](https://i.imgur.com/eNPR7yy.png)

![](https://i.imgur.com/ITkxkjX.png)

# 部分API #
- setPaintColor：设置默认画笔颜色
- setPaintHLColor：设置高亮画笔颜色
- setExtraLyricsListener：设置额外歌词回调方法，多用于加载歌词完成后，根据额外歌词的状态来判断是否需要显示翻译、音译歌词按钮
- setSearchLyricsListener：无歌词时，搜索歌词接口
- setOnLrcClickListener：多行歌词中歌词快进时，点击播放按钮时，调用。
- setFontSize：设置默认画笔的字体大小，可根据参数来设置是否要刷新view
- setExtraLrcStatus：设置额外歌词状态
- setLyricsReader：设置歌词读取器
- play：设置歌词当前的播放进度（播放歌曲时调用一次即可）
- pause：暂停歌词
- seekto：快进歌词
- resume：唤醒
- initLrcData：初始化歌词内容

# 声明 #
由于该项目涉及到酷狗的动感歌词的版权问题，所以该项目的代码和内容仅用于学习用途
# 捐赠 #
如果该项目对您有所帮助，欢迎您的赞赏

- 微信

![](https://i.imgur.com/e3hERHh.png)

- 支付宝

![](https://i.imgur.com/29AcEPA.png)