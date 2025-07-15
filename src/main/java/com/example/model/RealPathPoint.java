package com.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents a real-time tracking point from listRealPath
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RealPathPoint {
    
    private boolean simulating;
    private boolean removed;
    private boolean hold;
    private int cycle;
    private int flightLevel;
    private int authorizedFlightLevel;
    private int trackFlightLevel;
    private int trackSpeed;
    private int durationSinceFirstTrack;
    private int planId;
    private int seqNum;
    private int previousTrackTime;
    private float previousAverageRhumb;
    
    @JsonProperty("suppressionVo")
    private SuppressionVo suppressionVo;
    
    @JsonProperty("ssr")
    private SSR ssr;
    
    @JsonProperty("kinematic")
    private Kinematic kinematic;
    
    @JsonProperty("accTypes")
    private List<String> accTypes;
    
    private String indicativeSafe;
    
    // Constructors
    public RealPathPoint() {}
    
    // Getters and Setters
    public boolean isSimulating() { return simulating; }
    public void setSimulating(boolean simulating) { this.simulating = simulating; }
    
    public boolean isRemoved() { return removed; }
    public void setRemoved(boolean removed) { this.removed = removed; }
    
    public boolean isHold() { return hold; }
    public void setHold(boolean hold) { this.hold = hold; }
    
    public int getCycle() { return cycle; }
    public void setCycle(int cycle) { this.cycle = cycle; }
    
    public int getFlightLevel() { return flightLevel; }
    public void setFlightLevel(int flightLevel) { this.flightLevel = flightLevel; }
    
    public int getAuthorizedFlightLevel() { return authorizedFlightLevel; }
    public void setAuthorizedFlightLevel(int authorizedFlightLevel) { this.authorizedFlightLevel = authorizedFlightLevel; }
    
    public int getTrackFlightLevel() { return trackFlightLevel; }
    public void setTrackFlightLevel(int trackFlightLevel) { this.trackFlightLevel = trackFlightLevel; }
    
    public int getTrackSpeed() { return trackSpeed; }
    public void setTrackSpeed(int trackSpeed) { this.trackSpeed = trackSpeed; }
    
    public int getDurationSinceFirstTrack() { return durationSinceFirstTrack; }
    public void setDurationSinceFirstTrack(int durationSinceFirstTrack) { this.durationSinceFirstTrack = durationSinceFirstTrack; }
    
    public int getPlanId() { return planId; }
    public void setPlanId(int planId) { this.planId = planId; }
    
    public int getSeqNum() { return seqNum; }
    public void setSeqNum(int seqNum) { this.seqNum = seqNum; }
    
    public int getPreviousTrackTime() { return previousTrackTime; }
    public void setPreviousTrackTime(int previousTrackTime) { this.previousTrackTime = previousTrackTime; }
    
    public float getPreviousAverageRhumb() { return previousAverageRhumb; }
    public void setPreviousAverageRhumb(float previousAverageRhumb) { this.previousAverageRhumb = previousAverageRhumb; }
    
    public SuppressionVo getSuppressionVo() { return suppressionVo; }
    public void setSuppressionVo(SuppressionVo suppressionVo) { this.suppressionVo = suppressionVo; }
    
    public SSR getSsr() { return ssr; }
    public void setSsr(SSR ssr) { this.ssr = ssr; }
    
    public Kinematic getKinematic() { return kinematic; }
    public void setKinematic(Kinematic kinematic) { this.kinematic = kinematic; }
    
    public List<String> getAccTypes() { return accTypes; }
    public void setAccTypes(List<String> accTypes) { this.accTypes = accTypes; }
    
    public String getIndicativeSafe() { return indicativeSafe; }
    public void setIndicativeSafe(String indicativeSafe) { this.indicativeSafe = indicativeSafe; }
    
    @Override
    public String toString() {
        return "RealPathPoint{" +
                "planId=" + planId +
                ", flightLevel=" + flightLevel +
                ", trackSpeed=" + trackSpeed +
                ", simulating=" + simulating +
                ", position=" + (kinematic != null && kinematic.getPosition() != null ? 
                    kinematic.getPosition().getLatitude() + "," + kinematic.getPosition().getLongitude() : "null") +
                '}';
    }
    
    // Nested classes
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SuppressionVo {
        private int times;
        
        public int getTimes() { return times; }
        public void setTimes(int times) { this.times = times; }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SSR {
        private String registration;
        private Transponder transponder;
        
        public String getRegistration() { return registration; }
        public void setRegistration(String registration) { this.registration = registration; }
        
        public Transponder getTransponder() { return transponder; }
        public void setTransponder(Transponder transponder) { this.transponder = transponder; }
        
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Transponder {
            private int code;
            
            public int getCode() { return code; }
            public void setCode(int code) { this.code = code; }
        }
    }
} 