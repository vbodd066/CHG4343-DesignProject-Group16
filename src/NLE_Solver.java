public class NLE_Solver {
    private HeatExchanger_Geometry geometry;
    private Fluid_Properties process;
    private Fluid_Properties hotWater;
    private boolean hotWaterInAnnulus;                                                                  // true -> process is inner, hot water is annular
    private double T_out_desired;
    private double tolerance;                                                                           // absolute residual, deg C
    private int maxIterations;                                                                          // int
    private double M_FLOW_MAX = 8;                                                                      // kg/s hotWater
    private double M_FLOW_MIN = 0.5;                                                                    // kg/s hotWater

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

    // getter methods
    public double getT_out_desired(){ return this.T_out_desired; }


        //========================================================================================================
        //                                    NLE Solver Calculations
        //========================================================================================================

    // calculates the new outlet temperature of the process stream for a given mass flow rate of hotWater
    // this uses the code from part 1 to calculate the process outlet temperature so we complete that requirement
    private double calculate_T_outProcess(double M_flow_hotWater){
        Fluid_Properties newHotWater = new Fluid_Properties(this.hotWater);                             // create a new hotWater stream using the copy constructor
        if (M_flow_hotWater > M_FLOW_MAX || M_flow_hotWater < M_FLOW_MIN) {
            System.out.println("Invalid hot water flow rate entered: " + M_flow_hotWater + " kg/s");
            System.exit(0); }
        newHotWater.setM_flow(M_flow_hotWater);                                                         // set the new mass flow rate for hotWater
        Complete_Exchanger exchanger_i;                                                                 // creates a new Complete_exchanger object
        if (this.hotWaterInAnnulus ){                                                                   // check if HotWater stream is the Inner or Annulus
            exchanger_i = new Complete_Exchanger(this.geometry,this.process,newHotWater);               // takes the geometry, and then inner fluid first, then the annular fluid second
        } else {
            exchanger_i = new Complete_Exchanger(this.geometry,newHotWater,this.process);               // takes the geometry, and then inner fluid first, then the annular fluid second
        } return exchanger_i.calculate_t_out(process,newHotWater)[0]; }                                 // return the new outlet temperature with the new exchanger and hotWater mass flow rate

    // helper method for finding roots
    private double f_x(double M_flow_i){ return this.T_out_desired - calculate_T_outProcess(M_flow_i); }

    private double[] incremental_search (int n_intervals){
        double stepSize = (M_FLOW_MAX-M_FLOW_MIN)/n_intervals;
        // incremental search loop
        double f_lower = f_x(M_FLOW_MIN);
        for (int i = 0; i < n_intervals; i++) {
            double f_upper = f_x(M_FLOW_MIN + (i + 1) * stepSize);
            if (f_lower * f_upper < 0){                                                                // this version also doesn't create an extra array beforehand, it just returns the two values
                return new double[] { M_FLOW_MIN + i * stepSize, M_FLOW_MIN + (i + 1) * stepSize };
            } f_lower = f_upper;                                                                        // this version doesn't recalculate every value twice, once as an upper bound and again as a lower bound
        } return null; }                                                                                // if we make it to the end of the incremental search and don't find a root then there is a problem

    // Ridders' method to narrow in on the root until we are below the tolerance
    private double[] findM_flow(double[] root_interval){
        int i = 0;
        double xa = root_interval[0];                                                                   // initial lower bound of mass flow rate
        double xb = root_interval[1];                                                                   // initial upper bound of mass flow rate
        while (i<maxIterations){
            double x1 = (xa + xb) / 2;                                                                  // find midpoint (x1),  then use ridders to guess x3
            double x3 = x1 + ((x1 - xa)*(((Math.signum(f_x(xa)-f_x(xb)))*f_x(x1))/(Math.sqrt((f_x(x1) * f_x(x1))-(f_x(xa)*f_x(xb))))));

            // check if we found the root
            if ( Math.abs(f_x(x3)) < tolerance){                                                        // exit condition when absolute residual error < tolerance
                return new double[]{x3, Math.max(x3 - xa, xb - x3)}; }                                  // return the M_flow and the +/- error (pessimistic approach, max distance from x3 to either bound - often overstates the error as larger than it really is but this is the safest approach)

            // if we did not find root, apply ridders method again
            if (x3 > x1){                                                                               // [xa , x1 , x3 , xb]
                if (f_x(xa)*f_x(x1) < 0)
                    xb = x1;
                else if (f_x(x1)*f_x(x3)<0){
                    xa = x1; xb = x3;}
                else xa = x3; }
            else if ( x3 == x1 ) {                                                                      // weird case where x3 is rounded to be the same as the midpoint because its stored as a double
                if (f_x(xa)*f_x(x1) < 0)
                    xb = x1;
                else xa = x3; }
            else {                                                                                      // [xa , x3 , x1 , xb]
                if (f_x(xa)*f_x(x3) < 0)
                    xb = x3;
                else if (f_x(x3)*f_x(x1)<0){
                    xa = x3; xb = x1;}
                else xa = x1; }

            // update the iteration and try again
            i++; }
            // this is not a memory efficient way of doing it, we call f_x function many times and it creates a new complete_exchanger object every single time, we can call f_x 4 times, then store the values for that iteration to make it more memory efficient
        return null; }                                                                                  // hopefully we do not get to this point, means we went over maxIterations

    public double[] calculateM_flow_required(){
        return findM_flow(incremental_search(10)); }                                                                  // return the M_flow_hotWater in [0] and the error in [1]


}