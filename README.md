# NotePad
## 1.基本功能 
###  ①.时间戳显示
![](https://github.com/que123567/NotePad/blob/master/app/src/main/res/drawable/timestamp.png)

  笔记**创建**和**修改**的时间戳已经在数据库中保存，我直接把数据库查询结果通过 ```SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss aa")```将式转换为年月日时分秒AM/PM形式保存，然后插入数据库

  查询到**SimpleCursorAdapter源码**如下，发现要在**from**和**to**中分别添加进**时间戳的数据列对应名**以及我要显示在**xml中的对应资源ID**

  ```
  //源码
  public SimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
      super(context, layout, c);
      mTo = to;
      mOriginalFrom = from;
      findColumns(c, from);
  }
  ```
  ```
   //from 第二项为增加项 下同
   String[] dataColumns = {NotePad.Notes.COLUMN_NAME_TITLE,  NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE};

   //to
   int[] viewIDs = {android.R.id.text1, R.id.tv_time_stamp};
  ```
更改适配器之后，成功地显示了时间戳
#### 接着我在NodeEditor中的```updateNote ```方法中继续进行时间戳格式转换以确保在被笔记修改时，时间戳会相应改变。

####  部分代码
  ```
  private final void updateNote(String text, String title) {

     Toast.makeText(this, "笔记修改", Toast.LENGTH_SHORT).show();
     long now = System.currentTimeMillis();
     SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss aa");  
     String nowTime=sdf.format(new Date(now));

     // Sets up a map to contain values to be updated in the provider.
     ContentValues values = new ContentValues();
     values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, nowTime);
  ```
###  ②.按标题查询功能 
## 提供两种模式
### 1.精确搜索：实现了输入准确标题弹出该笔记的功能。
![](https://github.com/que123567/NotePad/blob/master/app/src/main/res/drawable/exa_search.png)
![](https://github.com/que123567/NotePad/blob/master/app/src/main/res/drawable/exa_search_res.png)

 #### 实现思路：我模仿原项目中Listview的item点击跳转，先查询全部结果，最后从结果中**获取标题对应列**的值与**输入的标题**匹配，返回匹配项的**id**（即数据库的第一列 主键_id），通过```ContentUris.withAppendedId```添加到uri末尾。

#### 数据库结构如下 ####

| _id    | title  |note |created|modifier|
| :------------- | :------------- |:-- |:-- |:--|
| 1              | 笔记1号     | 这个内容是我瞎编的| 1494478104       |1494478104
|2|笔记2号| 没有啊 |1494478100|1494478104|

 
```
  @TargetApi(Build.VERSION_CODES.KITKAT)
    private Boolean doSearch(String title) { //查询标题是否存在
        String[] TitleselectionArgs = {title};

        Cursor cursor = managedQuery(getIntent().getData(), PROJECTION, null, null, NotePad.Notes
                .DEFAULT_SORT_ORDER);//查询全部结果
        mUri = cursor.getNotificationUri();
        cursor.moveToFirst();
        do {
            if (title.equals(cursor.getString(cursor.getColumnIndex("title")))) {
                mUri = ContentUris.withAppendedId(mUri, cursor.getInt(0));
                // Toast.makeText(this,  cursor.getInt(0)+"?"+cursor.getString(1),
                // Toast.LENGTH_LONG).show();
                cursor.close();
                return true;
            }
        } while (cursor.moveToNext());
        cursor.close();
        return false;
    }
 ```
### 2.模糊搜索
![](https://github.com/que123567/NotePad/blob/master/app/src/main/res/drawable/search_vague.png)

![](https://github.com/que123567/NotePad/blob/master/app/src/main/res/drawable/search_vague_input.png)

![](https://github.com/que123567/NotePad/blob/master/app/src/main/res/drawable/search_vague_result.png)

#### 实现思路 

该功能附加在NotesList的menu中,通过输入标题，然后以" like ？"语句与通配符"%"查询得到结果.构造并设置一个新的适配器，将查询结果显示在listview中。

#### 注意细节 查询结果必须包含自增长列名"_id" 或者将某列：例如id，映射为_id 代码示例："id as_id"。否则报错 
```
 java.lang.IllegalArgumentException: column '_id' does not exist
```

#### 代码如下 

```
final EditText et = new EditText(this);
               new AlertDialog.Builder(NotesList.this).setTitle("输入查询").setView(et)
                       .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       input_word = et.getText().toString();
                       String[] search = {"_id", NotePad.Notes.COLUMN_NAME_TITLE, NotePad.Notes
                               .COLUMN_NAME_CREATE_DATE};
                       String selection = NotePad.Notes.COLUMN_NAME_TITLE + " like?";
                       String[] selectionArgs = {input_word + "%"};
                       Cursor cursors = managedQuery(getIntent().getData(), search, selection,
                               selectionArgs, NotePad.Notes.DEFAULT_SORT_ORDER);
                       cursors.moveToFirst();
                       String[] data = {NotePad.Notes.COLUMN_NAME_TITLE, NotePad.Notes
                               .COLUMN_NAME_CREATE_DATE};
                       final int[] viewID = {android.R.id.text1, R.id.tv_time};
                       SimpleCursorAdapter adapters = new SimpleCursorAdapter(NotesList.this,
                               R.layout.noteslist_item,
                               cursors,
                               data, viewID);
                       setListAdapter(adapters);
                   }
               }).show();

```
#### tip ： 在模糊搜索完之后没有清空搜索结果，再点击精准查询，查询结果仅在模糊搜索结果之中查询。 清空方式为：在模糊搜索中不输入任何字符直接点确定.
  功能演示如下

##  附加功能
###  1.九宫格解锁
参考
  笔记本属于个人私密应用，所有我为其添加了九宫格密码，防止个人隐私泄露。
 #### 代码细节: 

### 通过自定义view实现，代码量大概300+，太长仅贴出部分。
其中包含①横竖屏模式判断
②连接头尾宫格头尾时，自动连接中心点
③九宫格密码记录与重置
④九宫格数量自定义（要通过更改参数，开发者使用，用户无法更改）
 ```
 /**
  * 横竖屏判断
  */
 width = getWidth();
      height = getHeight();
      if (height > width) {//竖屏
          offsetY = (height - width) / 2;
          height = width;
      } else { //横屏
          offsetX = (height - width) / 2;
          width = height;
      }


/**
 * 计算按下或者移动的位置是否在九宫格上
 */
private Point checkPoint(float eventX, float eventY, float br) {
    for (int i = 0; i < points.length; i++) {
        for (int j = 0; j < points[i].length; j++) {
            Point point = points[i][j];
            double distance = getDistance(point.x, point.y, eventX, eventY);
            if (distance < br) {
                return point;
            }
        }
    }
    return null;
}

  ```



##### 功能演示如下
1.正常状态

![](https://github.com/que123567/NotePad/blob/master/app/src/main/res/drawable/lockview_normal.png)

2.连接数少于5，错误状态

![](https://github.com/que123567/NotePad/blob/master/app/src/main/res/drawable/lockview_error.png)

3.按下状态（有效连接）

![](https://github.com/que123567/NotePad/blob/master/app/src/main/res/drawable/lockview_setpassward.png)

### UI美化

##### 演示如下

![](https://github.com/que123567/NotePad/blob/master/app/src/main/res/drawable/noteslist.png)
