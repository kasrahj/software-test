package mizdooni.model;

public class Rating {
    public double food;
    public double service;
    public double ambiance;
    public double overall;

//    public Rating(double food, double service, double ambiance, double overall) {
//        this.food = food;
//        this.service = service;
//        this.ambiance = ambiance;
//        this.overall = overall;
//    }


    public int getStarCount() {
        return (int) Math.min(Math.round(overall), 5);
    }
}
