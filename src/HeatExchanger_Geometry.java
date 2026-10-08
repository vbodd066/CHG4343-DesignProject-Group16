public class HeatExchanger_Geometry {

    private int n;                      // Number of hairpin modules
    private double l_Leg;               // Length of one straight leg (meters)
    private double dInner_In;           // Inside diameter of inner pipe (meters)
    private double dInner_Out;          // Outside diameter of inner pipe (meters)
    private double dOuter_In;           // Inside diameter of outer pipe (meters)
    private double k=45;                // Pipe wall thermal conductivity (W/m K)
    private double eps_r = 0.000045;    //

    // neither of these are used at the moment but will be important for when we want to implement the level 4 to get top grade
    private String configuration;   // "Parallel" or "Counter-Current"

    // Constructor method
    public HeatExchanger_Geometry(int n, double l_Leg, double dInner_In, double dInner_Out, double dOuter_In, String configuration) {
        this.n = n;
        this.l_Leg = l_Leg;
        this.dInner_In = dInner_In;
        this.dInner_Out = dInner_Out;
        this.dOuter_In = dOuter_In;
        this.configuration = configuration;
    }

    // Copy constructor
    public HeatExchanger_Geometry(HeatExchanger_Geometry source) {
        this.n = source.n;
        this.l_Leg = source.l_Leg;
        this.dInner_In = source.dInner_In;
        this.dInner_Out = source.dInner_Out;
        this.dOuter_In = source.dOuter_In;
        this.configuration = source.configuration;
    }

    // Getter methods
    public int getN() { return this.n; }
    public double getL_Leg() { return this.l_Leg; }
    public double getdInner_In() { return this.dInner_In; }
    public double getdInner_Out() { return this.dInner_Out; }
    public double getdOuter_In() { return this.dOuter_In; }
    public double getK() { return this.k; }
    public String getConfiguration() { return this.configuration; }
    public double getEps_r() { return this.eps_r; }

    // Total straight leg length (m)
    public double lStraightTotal() {
        return 2 * this.n * this.l_Leg;
    }

    // Inner pipe flow area (m^2)
    public double a_I() {
        return (Math.PI * Math.pow(this.dInner_In, 2)) / 4.0;
    }

    // Annulus flow area (m^2)
    public double a_A() {
        return (Math.PI * (Math.pow(this.dOuter_In, 2) - Math.pow(this.dInner_Out, 2))) / 4.0;
    }

    // Annulus hydraulic diameter (m)
    public double d_Ha() {
        return this.dOuter_In - this.dInner_Out;
    }

    // Heat transfer area (m^2)
    public double a_O() {
        return Math.PI * this.dInner_Out * lStraightTotal();
    }
}