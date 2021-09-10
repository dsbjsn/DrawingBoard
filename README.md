### 支持格式：文本、图片
### 支持操作：位移、旋转、缩放、裁剪、涂鸦、橡皮擦、撤销

DrawingBoardView:自定义画板View，图层容器，监听触摸对选中图层进行操作  
BaseInfo:图层基类，设置图层相关属性，执行画板传入的相关操作，子类BitmapInfo,TextInfo  

#### 画板功能
setCanvasRadius:设置圆角  
setAutoAlignment:是否自动对准中线  
setClipDrawBitmap:设置裁剪路径图片  
setGraffitiColor:设置涂鸦画笔颜色  
setGraffitiSize:设置涂鸦画笔大小  
setMinScale:设置缩放时的最小缩放倍数  
repealAction:撤销操作  
revocationAction:恢复撤销  

#### 图层功能
canDrag:是否允许位移  
canScale:是否允许缩放  
canRotate:是否允许旋转  
canClip:是否允许手势裁剪  
canRectClip:是否允许矩形裁剪  
canCircleClip:是否允许圆形裁剪  
canRubber:是否允许擦除  
canGraffiti:是否允许涂鸦  
limitRect:位移时是否限制在画板内  
showStoker:是否显示边框  

