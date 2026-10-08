public class Cost_estimation {
    private double n_eff = 0.7;                         // pump efficiency
    private double h_year = 8000;                       // annual operating time: (hours)
    private double e_price = 0.12;                      // electricity price ($/kW hour)
    private Complete_Exchanger exchanger;               // local instance of the exchanger
    private double p_electric_inner;                    // process pump electrical power (W)
    private double p_electric_annular;                  // hot water pump electrical power (W)
    private double c_pump_inner;                        // annual pumping costs - process stream ($/Yr)
    private double c_pump_annular;                      // annual pumping costs - hot water stream($/Yr)
    private double c_capital_annual;                    // annualised capital costs ($/Yr)
    private double c_module;                            // installed cost of one module in $

    // constructor method
    public Cost_estimation (Complete_Exchanger exchanger, double c_module){
        if (exchanger == null) System.exit(0);
        if (c_module < 0) System.exit(0);
        this.exchanger = new Complete_Exchanger(exchanger); // copy constructor of the exchanger that we pass in to create the cost_estimation object
        this.c_module = c_module;
        this.calculate();
    }

    // copy constructor
    public Cost_estimation (Cost_estimation source){
        if (source == null) System.exit(0);
        this.exchanger = new Complete_Exchanger(source.exchanger);
        this.c_module = source.c_module;
        this.calculate();
    }


    // do we need getter methods?

    // do we need equals methods? - I don't think we have a use for either but DT always says to make them with any new class





    //========================================================================================================
    //                                    Price Estimation Calculations
    //========================================================================================================

    // calculate annual cost
    public void calculate(){
        double[] del_P = this.exchanger.calculate_P();
        Fluid_Properties inner = this.exchanger.getInner_Fluid();
        Fluid_Properties annular = this.exchanger.getAnnular_Fluid();

        this.p_electric_inner   = del_P[0] * (inner.getM_flow()   / inner.getRho())   / this.n_eff;
        this.p_electric_annular = del_P[1] * (annular.getM_flow() / annular.getRho()) / this.n_eff;
        this.c_pump_inner = this.p_electric_inner / 1000 * this.h_year * e_price;
        this.c_pump_annular = this.p_electric_annular / 1000 * this.h_year * e_price;
        this.c_capital_annual = this.exchanger.getGeometry().getN() * 0.18 * this.c_module;
    }

    // calculate annual cost
    public double c_annual(){
        return c_pump_annular + c_pump_inner + c_capital_annual ;
    }
}
