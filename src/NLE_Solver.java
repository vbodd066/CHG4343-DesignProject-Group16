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
        //                                    NLE Algorithm Calculations
        //========================================================================================================

    // each incremental search or ridders method, we can simply create a new object, when we define the fluid properties, we can change the M_flow, then pass the other fluid, geometry, and create a new complete exchanger to calculate T out
    // then do T_out_M_flow (ie the new T out from our new object) - T_out_target, when this changes sign, we have a root
    // then we can use ridders to narrow down, again creating new objects for each iteration

    // calculates the new outlet temperature of the process stream for a given Mass Flow rate of Water
    // this uses the code from part 1 to calculate the process outlet temperature so we complete that requirement
    private double calculate_T_outProcess(double M_flow_hotWater){
        Fluid_Properties newHotWater = new Fluid_Properties(this.hotWater);                         // create a new hotWater stream
        if (M_flow_hotWater > M_FLOW_MAX || M_flow_hotWater < M_FLOW_MIN) {
            System.out.println("Invalid hot water flow rate entered: " + M_flow_hotWater + " kg/s");
            System.exit(0);                 // set the new Mass flow rate, this returns a boolean so we need to exit if its false because that means the change failed
        }
        newHotWater.setM_flow(M_flow_hotWater);
        Complete_Exchanger exchanger_i;
        // check if HotWater stream is the Inner or Annulus
        if (this.hotWaterInAnnulus ){
            // this code only executes if hotWaterInAnnulus == true
            exchanger_i = new Complete_Exchanger(this.geometry,this.process,newHotWater);            // takes the geometry, and then inner fluid first, then the annular fluid second
        }
        else {
            // this code only executes if hotWaterInAnnulus == false
            exchanger_i = new Complete_Exchanger(this.geometry,newHotWater,this.process);            // takes the geometry, and then inner fluid first, then the annular fluid second
        }
        // exchanger object with new mass flow of hot water, then we calculate T_out, which returns an array of temperatures with process stream at position [0]
         return exchanger_i.calculate_t_out(process,newHotWater)[0];
        // returns new outlet temperature of process stream
    }

    private double[] incremental_search (int n_intervals){
        double[] root_interval = new double[2];
        double stepSize = (M_FLOW_MAX-M_FLOW_MIN)/n_intervals;
        // incremental search loop
        for (int i=0;i<n_intervals+1;i++){
            // add a return statement once we find the interval that bounds the root
            return root_interval;
        }
        return null;            // if we make it to the end of the incremental search and don't find a root then there is a problem
    }

    // Ridders' method to narrow in on the root until we are below the tolerance
    private double findM_flow(double[] root){
        int i = 0;                  // start at iteration number 0, go until we get below the tolerance or hit 100 iterations
        double error=1;             // random number for the error to start at so that it's above the tolerance, and we enter the while loop
        while (i<maxIterations && error > tolerance){



            i++;
        }
        return i;
    }

}