
SlidingDeleteListView
=====================

A simple custom ListView, sliding left will show a button, click this button to delete current select item of listview. Reference to the application QQ which was released by Tencent Inc.

<h1>Show</h1>
![image](https://github.com/YoungLeeForeverBoy/SlidingDeleteListView/blob/master/show.gif?raw=true)

<h1>Setup</h1>
* In Eclipse, import this project as a Android library project.
* Then add this project as a dependency to your project.

<h1>Usage</h1>
* In your activity layout file, use like follow, this widget has two attributes:buttonID and enableSliding.
  ![image](https://github.com/YoungLeeForeverBoy/SlidingDeleteListView/blob/master/1.jpg?raw=true)
* Then in your BaseAdapter's item layout file, like this:
  ![image](https://github.com/YoungLeeForeverBoy/SlidingDeleteListView/blob/master/2.jpg?raw=true)
* This widget has a callback listener OnItemButtonShowingListener:
  ![image](https://github.com/YoungLeeForeverBoy/SlidingDeleteListView/blob/master/3.jpg?raw=true)
* Then this widget use like the ListView, it's ok to set AdapterView.OnItemClickListener and AdapterView.OnItemLongClickListener
