package com.betfair.aping;

/**
 * @author Sateesh
 * @since 04/12/2014
 */
public class SportRiskProfile {
    public double riskFactor = 0.5;
    public double profitFactor = 0.80;
    public double giveUpFactor = 0.90;
    public double lowerEndOfOdds = 1.12;
    public double upperEndOfOdds = 1.2;

    public SportRiskProfile() {
    }

    public SportRiskProfile(double riskFactor, double profitFactor, double giveUpFactor, double lowerEndOfOdds, double upperEndOfOdds) {
        this.riskFactor = riskFactor;
        this.profitFactor = profitFactor;
        this.giveUpFactor = giveUpFactor;
        this.lowerEndOfOdds = lowerEndOfOdds;
        this.upperEndOfOdds = upperEndOfOdds;
    }

    private static SportRiskProfile soccer = new SportRiskProfile(0.5,0.9,0.7,1.12,1.3);
    private static SportRiskProfile tennis = new SportRiskProfile(0.7,0.9,0.9,1.15,1.2);
    private static SportRiskProfile general = new SportRiskProfile();

    public static SportRiskProfile getSportsStrategy(String sportName){
        if(sportName.contains("Soccer") || sportName.contains("Football")){
            return soccer;
        } if(sportName.contains("Tennis")){
            return tennis;
        } else {
            return general;
        }
    }

}


