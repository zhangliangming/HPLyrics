# 简介 #
该开源依赖库是乐乐音乐播放器里的一个歌词模块功能，现在把该功能模块独立出来进行优化，并弄成了一个开源依赖库，其它音乐播放器项目只要引用该库并调用接口，便可轻松实现与乐乐音乐播放器一样的动感歌词显示效果，其项目地址如下：[乐乐音乐播放器](https://github.com/zhangliangming/HappyPlayer5.git)。

# 日志 #

## v1.11##

- 修复最后一个字渐变出错的问题
## v1.10##

- 修改歌词每次刷新的间隔最少为100ms
## v1.9##

- 修改歌词每次刷新的间隔最少为20ms
## v1.8##

- 修复未读到下一行歌词时，上一行歌词渐变宽度为0的问题
## v1.7##

- 修复设置歌词读取器的问题
## v1.6##

- 还原
## v1.3##

- 2018-03-04
- 修复双行歌词加载歌词完成后，显示额外歌词渐变出错的问题
- 修改了多行歌词，滑动时的指示器渐变颜色
## v1.2##

- 添加歌词view获取歌词读取器方法
## v1.1##

- 添加歌词读取器获取歌词实体类方法
## v1.0##

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
	         compile 'com.github.zhangliangming:HPLyrics:v1.11'
	}`

# 调用Demo #

链接: [https://pan.baidu.com/s/1GAOKwfKqYCwFayOqgg-yCw](https://pan.baidu.com/s/1GAOKwfKqYCwFayOqgg-yCw) 密码: f29d

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
- play：设置歌词当前的播放进度
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