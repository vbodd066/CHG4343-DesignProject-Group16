public class NLE_Solver {
    private double Iterations_max;
    private double Absolute_residual;
    private Complete_Exchanger complete_exchanger;
    private double T_out_desired;

    // constructor
    public NLE_Solver (Complete_Exchanger complete_exchanger, double T_out_desired, double Absolute_residual, double Iterations_max){
        if (complete_exchanger == null) System.exit(0);

        this.complete_exchanger = new Complete_Exchanger(complete_exchanger);
        this.T_out_desired = T_out_desired;
        this.Absolute_residual = Absolute_residual;
        this.Iterations_max = Iterations_max;
    }

    // copy constructor
    public NLE_Solver (NLE_Solver source){
        if (source == null) System.exit(0);

        this.complete_exchanger = new Complete_Exchanger(source.complete_exchanger);
        this.T_out_desired = source.T_out_desired;
        this.Absolute_residual = source.Absolute_residual;
        this.Iterations_max = source.Iterations_max;
    }

    // do we need getter methods?

    // do we need equals methods? - I don't think we have a use for either but DT always says to make them with any new class






    //========================================================================================================
    //                                    NLE Algorithm Calculations
    //========================================================================================================


}
