-----------------------------------------------------------------------
jd3Lib - fourth release alpha4
-----------------------------------------------------------------------

After a long time here is finaly a lifesign of the jd3lib. Jonatahn stopped the
development of the library because he had no time any more to work on it. So i 
took over and to development continues. This release is a small Bugfix release 
which contains all the work which was done after the alpha3 and there was quite 
a lot. The biggest improvements where made in the MPEGAudioFrameHeader class 
which should work with a lot more MP3 files than the alpha3 did. The next
release will be a complete Bugfix release. After the currently known bugs will 
be fixed the development will go on. New Features will be implemented after 
that please use the offical project Homepage 
http://sourceforge.net/projects/jd3lib
to file feature requests and bug  reports we need you all as our testers. One 
last word to the Jakarta Ant build.xml which was introduced in the last 
release. We made a step backwards and use only the standard java tools again. 
We do that to ensure that everybody can easily build the library.

Changes:
-just bugfixes and cleanup of sourcecode
-fixed Bug #562622 in the MP3FileFilter should work with any .mp3 Files now
-made improvements to the MPEGAudioFrameHeader Class so that it now accepts   
 most MPEG-Audio files

* Check out changes.txt for full list of changes

Instructions:
Just compile everything in the helliker/id3 directory. You may use the 
"make.helliker.id3.sh.or.bat" script to compile.
In win32 enviroments just starting the batch file would be enough. In Unix 
envirmoments you might have to change the
Right to executable, but then be able to run the script as shell script. 
Documentation is in the javadoc directory.
The MP3File class is the starting point.

Next Up:
-bug fixing
-redesign of the class structure

Future:
-support all versions of id3v2
-copy data between tags of different versions
-beta version perhaps?

Thanks go out to the following people for code snippets and other help:
Jonathan Hilliker,Reed Esau, Ralph Zazula, David Barron, Philip Gladstone,Tech'

Here are some projects using my library:(from alpha3 not up to date)
HTML Directory List Generator (http://hdlg.martinlabelle.com)
jMagePlayer (http://jmageplayer.magenet.com)
jReceiver (http://jreceiver.sourceforge.net)

* Let me know about your project, I am interested!

Contact Info:
gruni@users.sourceforge.net
http://sourceforge.net/projects/jd3lib
-----------------------------------------------------------------------
(c) 2002 Andreas Grunewald