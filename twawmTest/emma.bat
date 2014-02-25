rem http://d.hatena.ne.jp/halts/20120201/p1

d:

cd D:\Java\git\twawm\Twawm
cmd /C D:\Java\apache-ant-1.8.4\bin\ant.bat emma debug install

cd D:\Java\git\twawm\TwawmTest 
cmd /C D:\Java\apache-ant-1.8.4\bin\ant.bat emma debug install test

start bin\coverage.html

pause;
