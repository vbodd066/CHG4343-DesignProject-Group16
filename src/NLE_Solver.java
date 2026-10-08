public class NLE_Solver {
    private HeatExchanger_Geometry geometry;
    private Fluid_Properties process;
    private Fluid_Properties hotWater;
    private boolean hotWaterInAnnulus;          // true -> process is inner, hot water is annular
    private double T_out_desired;
    private double tolerance;                   // absolute residual, deg C
    private int maxIterations;                  // int, not double
    private double M_FLOW_MAX = 8;              // kg/s hotWater
    private double M_FLOW_MIN = 0.5;            // kg/s hotWater


    // constructor
    public NLE_Solver (HeatExchanger_Geometry geometry, Fluid_Properties process, Fluid_Properties hotWater, boolean hotWaterInAnnulus, double T_out_desired, double tolerance, int maxIterations) {
        if (geometry == null || process == null || hotWater == null) System.exit(0);
        this.geometry = new HeatExchanger_Geometry(geometry);
        this.process = new Fluid_Properties(process);
        this.hotWater = new Fluid_Properties(hotWater);
        this.hotWaterInAnnulus = hotWaterInAnnulus;
        this.T_out_desired = T_out_desired;
        this.tolerance = tolerance;
        this.maxIterations = maxIterations;
    }

    // copy constructor
    public NLE_Solver (NLE_Solver source){
        if (source == null) System.exit(0);
        this.geometry = new HeatExchanger_Geometry(source.geometry);
        this.process = new Fluid_Properties(source.process);
        this.hotWater = new Fluid_Properties(source.hotWater);
        this.hotWaterInAnnulus = source.hotWaterInAnnulus;
        this.T_out_desired = source.T_out_desired;
        this.tolerance = source.tolerance;
        this.maxIterations = source.maxIterations;
    }


        //========================================================================================================
        //                                    NLE Solver Calculations
        //========================================================================================================

    // calculates the new outlet temperature of the process stream for a given mass flow rate of hotWater
    // this uses the code from part 1 to calculate the process outlet temperature so we complete that requirement
    private double calculate_T_outProcess(double M_flow_hotWater){
        Fluid_Properties newHotWater = new Fluid_Properties(this.hotWater);                          // create a new hotWater stream using the copy constructor
        if (M_flow_hotWater > M_FLOW_MAX || M_flow_hotWater < M_FLOW_MIN) {
            System.out.println("Invalid hot water flow rate entered: " + M_flow_hotWater + " kg/s");
            System.exit(0); }
        newHotWater.setM_flow(M_flow_hotWater);                                                      // set the new mass flow rate for hotWater
        Complete_Exchanger exchanger_i;                                                              // creates a new Complete_exchanger object
        if (this.hotWaterInAnnulus ){                                                                // check if HotWater stream is the Inner or Annulus
            exchanger_i = new Complete_Exchanger(this.geometry,this.process,newHotWater);            // takes the geometry, and then inner fluid first, then the annular fluid second
        } else {
            exchanger_i = new Complete_Exchanger(this.geometry,newHotWater,this.process);            // takes the geometry, and then inner fluid first, then the annular fluid second
        }
         return exchanger_i.calculate_t_out(process,newHotWater)[0]; }                               // return the new outlet temperature with the new exchanger and hotWater mass flow rate

    // helper method for finding roots
    private double f_x(double M_flow_i){
        return this.T_out_desired - calculate_T_outProcess(M_flow_i);
    }

    private double[] incremental_search (int n_intervals){
        double stepSize = (M_FLOW_MAX-M_FLOW_MIN)/n_intervals;
        // incremental search loop
        double f_lower = f_x(M_FLOW_MIN);
        for (int i = 0; i < n_intervals; i++) {
            double f_upper = f_x(M_FLOW_MIN + (i + 1) * stepSize);
            if (f_lower * f_upper <= 0){                         // this version also doesn't create an extra array beforehand, it just returns the two values
                return new double[] { M_FLOW_MIN + i * stepSize, M_FLOW_MIN + (i + 1) * stepSize };
            } f_lower = f_upper;                                // this version doesn't recalculate every value twice, once as an upper bound and again as a lower bound
        } return null; }                                        // if we make it to the end of the incremental search and don't find a root then there is a problem


    // Ridders' method to narrow in on the root until we are below the tolerance
    private double findM_flow(double[] root_interval){
        int i = 0;
        double[] root = new double[] {f_x(root_interval[0]),f_x(root_interval[1])};     // intialize root boundaries for ridders
        while (i<maxIterations){



            if ((root[1]-root[0])/2 < tolerance){           // exit condition when error < tolerance
                return (root[1]-root[0])/2; }
            i++; }                                          // if we are above tolerance, we increase i and go again
        return Double.NaN;                                  // hopefully we do not get to this point
    }


}