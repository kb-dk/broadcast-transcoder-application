0.8 
=====
* Modified transcoding of programs with exact files to remove all "-ss" and "-length" elements from the ffmpeg command line.

0.7.1
=====
* FinalMediaFileRenamerProcessor can now overwrite files when overwrite=true in `bta.behaivour.properties`

0.7.0
=====

Reklamefilm
------------
* Reklamefilm can transcode and generate snapshots
* Handle filenames with spaces, as can happen with reklamefilms

Choosing the right Audio Stream
-------------------------------
* Do not use the visuallyImpared audio unless there is no other choice

Configuration and packaging
--------------
* setenv use SCL the right way
* Reverted to the old webservice names
* Reverted bta-ws heightInPixels to 288, the value of 576 was a copy-pasta error. Credits goes to to TGC
* Include up2date database schemas (`dbschema.0.5.sql`). These schemas have been generated with `IntelliJ Database manager -> SQL scripts -> copy DDL to console`

Stage testing
-------------
* portObjectsFromProdToDevel.sh: Removed ekstra /objects/ from doms path
* Stagetest docs. Port objects do not auto enqueue 


0.6.0
=======

Configurable
------------
* Specified the the commands `ffmpegTranscodingAudioTransportStream`, `ffmpegMultiStreamAudioClipperCommand`, `ffmpegPreviewClipperCommand` and `snapshotExtractorCommand` in the config file (`bta.behaivour.properties`) rather than hardcoded. 
* The commands `WavTranscoderProcessor.getMultiClipCommand`, `OutputFileFfprobeAnalyser`, `PidAndAsepctRatioExtractorProcessor`, and `ReklamefilmFileResolverImpl` are still hardcoded, but will be handled later.  

Choosing the right Audio Stream
-------------------------------
* Use the stereo audio stream instead of the 5.1 channel, as this gives better transcoded sound
* Use stream index rather than stream pid when specifying audio stream for `ffmpeg`. Stream pids are lost when mpeg files are concatenated, but stream indexes should remain valid. The Kuana transcode workflow also use stream indexes 


Transcode to mp4, not flv
-------------------------
* Transcode to mp4.
* Fixed bug that caused the system to forgo making thumbnails
* Fixed bug where the snapshot extractor confused to params and thus made VERY small thumbnails

Formidlingsfiler pixelerer
--------------------------

* TODO: `-sample_fmt s16` in `ffmpegTranscodingString` in `bta.behaviour.properties`. Does this actually improve sound quality?
    * It is only a config change, so it might be rolled back after release.  
* Documentation of scripts `enqueueJobs.sh` and `queryChangesDoms.sh` 
* Use the first found audio stream, if no stereo audio stream is found. This bug caused the transcoding to fail for some files.

dorq subtitles
--------------

* Due to the new name of the dorq webservice, change this property
`dk.statsbiblioteket.mediestream.dorq.btawsroot` to the value `http://iapetus.statsbiblioteket.dk:9641/bta-dorq.ws/bta` 
in `msdrs@iapetus:/home/msdrs/dorq.properties`. This same should be done for the stage and prod versions of dorq.

* Increased timeout x5 for the dorq/bta webservice, as it failed to complete a transcoding in devel (`transcodingTimeoutDivisor=0.2` in `bta.behaviour.ws.properties` and `bta-dorq.behaviour.ws.properties`)

* broadcast-transcoder-webservice: Now package an archive containing both the bta.ws and the bta-dorq.ws. These can now coexist in the same tomcat, even.

* BTA webservice (and thus dorq webservice): Implemented the better file merge and cutting (use `ffmpeg`'s own logic, not `dd`)

* Implemented the new changes so the bta webservice and dorq webservice encode subtitles

* Reformatted `bta.behaviour.properties`, `bta.behaviour.ws.properties` and `bta-dorq.behaivour.ws.properties`


0.5.0
=====
* config/run_transcoder.conf and config/transcode-master.conf is now based on $SCRIPT_PATH, as this gives nicer paths without ..
* transcode-master.sh status and stop now actually works;  pgrep needed flag -a instead of -l

* Final deploy_on_devel script

* Updated UnistreamTrancoder to correctly handle programs that start less than 5 seconds into their first file.
* Double skip feature (ss before file -5 sec and ss +5 sec after transcode, to be sure we get a useful keyframe to start from).

* Use the stereo audio stream instead of the 5.1 channel, as this gives better transcoded sound

* Correctly merges multi-file mpegs

* Only find danish subtitles and skip subtitles for hearing impared
* Overlayed subtitles

* java 8 

* Padding that works with newer ffmpegs
* Use ffmpeg40
* setenv.sh sources /opt/rh/sb-btatools1/enable and /opt/ffmpeg40/enable to get both a vlc AND a ffmpeg

* Default config do not store the images as the larm user, when running as the bta user...

* Asked the ExternalJobRunner to shut up about it's internal state. I like to be able to read my logs
* Better file move for final media file, now logs the actual error
* BTA job query: A bit more logging
* Log the url of the doms server, when connecting. Makes it a bit easier to quickly see which Doms a BTA is configured for
* Use active program pid as thread name. This makes it easier to read the log files, when multiple threads/processes write to the same file
* Reklamefilm trancoding: Improved error logging

* Readme: made more readable

* synchronised config files with those from iapetus devel server


0.4.0
=====

* Added DORQ webservice method and config.
    * Note that you must have a separate instance of BTA webservice running for DORQ, as it uses a different behaivour.ws.properties file
* Leaked domsReadOnlyAdmin password.

* Fixed extractSnapshot.sh script parameters.

* Disable SIGINT in queue.sh
* Fix race condition in transcode script
* Rewrite transcode control scripts