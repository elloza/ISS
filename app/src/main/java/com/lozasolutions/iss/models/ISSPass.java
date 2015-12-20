package com.lozasolutions.iss.models;

/**
 * Created by Loza on 19/12/2015.
 */
public class ISSPass {

    Long duration;
    Long risetime;

    public ISSPass(Long duration, Long risetime) {
        this.duration = duration;
        this.risetime = risetime;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Long getRisetime() {
        return risetime;
    }

    public void setRisetime(Long risetime) {
        this.risetime = risetime;
    }



}
