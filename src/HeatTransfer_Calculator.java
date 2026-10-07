public class HeatTransfer_Calculator {

    //========================================================================================================
    //                                     Equations for Inner Pipe
    //========================================================================================================

    // Calculate Graetz number for laminar flow
    public static double Gz_inner (double Re, double Pr, double Diameter_Inner_Inside, double l_straight_total){
        return ( Re * Pr * Diameter_Inner_Inside ) / l_straight_total;
    }

    // calculate smooth pipe friction factor for turbulent heat transfer rate
    public static double f_0_inner (double Re){
        return Math.pow((-1.8*Math.log10(6.9 / Re )),-2);
    }

    // Calculate Nusselt number
    public static double Nu_inner (double Re, double Gz, double Pr, double f_0){
        // laminar flow regime
        if (Re <= 2300 && Pr <= 2000 && Pr >= 0.5) {
            return 3.66 + (0.0668 * Gz)/(1 + (0.04 * Math.pow(Gz,0.666667)));
        }
        // turbulent flow regime
        if (Re >= 10000 && Pr <= 2000 && Pr >= 0.5){
            return ((f_0 / 8) * (Re - 1000) * Pr) / ( 1 + 12.7 * (Math.pow((f_0/8),0.5)) * (Math.pow(Pr,0.6667) -1));
        }

        // error handling if the Reynolds number is not cleanly in the laminar or turbulent regime
        return Double.NaN;
    }

    // Calculate the Heat Transfer Coefficent ( W/m^2 K)
    public static double h_i (double Nu, double k, double Diameter_Inner_Inside){
        return (( Nu * k ) / Diameter_Inner_Inside ) ;
    }







    //========================================================================================================
    //                                    Equations for Annulus Region
    //========================================================================================================

    // Calculate Alpha
    public static double alpha (double D_inner_out, double D_outer_in){
        return D_inner_out / D_outer_in ;
    }

    // Calculate Nusselt Number
    public static double Nu_annulus (double alpha) {
        // error check that alpha is within the range of the table
        if (alpha < 0.05 || alpha > 1.0) {
            return Double.NaN;
        }
        // hard code the table so that we can interpolate
        double[] alphaTable = {0.05, 0.10, 0.25, 0.50, 1.00};
        double[] nuTable    = {17.46, 11.56, 7.37, 5.74, 4.86};


        for (int i = 0; i < alphaTable.length - 1; i++) {
            if (alpha >= alphaTable[i] && alpha <= alphaTable[i + 1]) { // starts at 0.05, if alpha is > and < 0.10, then use these values
                double alpha_1 = alphaTable[i];                         // otherwise go to next alpha, stops at alphaTable.length-1 because there is no value in i+1 position at alpha = 1 since its the end of the table
                double alpha_2 = alphaTable[i + 1];
                double Nu_a1 = nuTable[i];
                double Nu_a2 = nuTable[i + 1];

                return Nu_a1 + (((alpha - alpha_1) / (alpha_2 - alpha_1)) * (Nu_a2 - Nu_a1));   // this equation handles if alpha = a value in the table, it will return that value
            }
        }

        return Double.NaN;      // code should never get here because we should always find an alpha within the for loop
                                // and then return, but java requires a return statement at the end of the method
    }

    // Calculate Modified Reynolds Number
    public static double Re_a_star(double Re_a, double alpha){
        return Re_a * ((((1 + Math.pow(alpha,2)) * Math.log(alpha) ) + (1 - Math.pow(alpha,2)))
                        / (Math.pow((1 - alpha),2) * Math.log(alpha) ));
    }

    // calculate friction factor
    public static double f_a_annulus (double Re_modified){
        return Math.pow(((1.8 * Math.log10(Re_modified)) - 1.5),-2);
    }

    // calculate correction term k1
    public static double k1_annulus(double Re_a, double Pr){
        return 1.07 + ( 900 / Re_a ) - ( 0.63 / (1 + 10 * Pr) );
    }

    // calculate geometry correction factor
    public static double f_geom(double alpha){
        return 0.75 * Math.pow(alpha,-0.17);
    }

    // calculate length correction factor
    public static double f_length (double d_h_a, double l_straight_total){
        return 1 + Math.pow( ( d_h_a / l_straight_total ) , 0.666667 );
    }

    // calculate Nusselt number for turbulent flow
    public static double Nu_annulus (double f_a_annulus, double Re, double Pr, double k1, double f_length, double f_geom){
        return f_geom * f_length * (((f_a_annulus/8)*Re*Pr) / (k1 + (12.7 * (Math.pow(f_a_annulus/8,0.5)) * (Math.pow(Pr,0.66667)-1) )) ) ;
    }

    // Calculate the Heat Transfer Coefficent ( W/m^2 K)
    public static double h_a (double Nu_annulus, double k_fluid_annulus, double d_h_a){
        return ((Nu_annulus * k_fluid_annulus) / d_h_a) ;
    }




}
