// IMusicService.aidl
package com.example.musiccentral;

// AIDL interface for controlling music playback
interface IMusicService {
    /** Returns the list of available clip IDs (1…n) */
    int[] listClips();

    /** Starts playback of the clip with given ID */
    void play(int clipId);

    /** Pauses the current playback */
    void pause();

    /** Resumes playback if paused */
    void resume();

    /** Stops playback altogether */
    void stop();
}
