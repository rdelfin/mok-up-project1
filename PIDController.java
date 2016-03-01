import java.util.*;

public class PIDController {
    private double Kp, Ki, Kd;
    private double err, prevErr, intErr;
    private double[] accumErr;
    private int errIdx;
    private double setpoint;
    
    public PIDController() {
        
    }
    
    public PIDController(double Kp, double Ki, double Kd, double setpoint) {
        this.Kp = Kp;
        this.Ki = Ki;
        this.Kd = Kd;
        this.setpoint = setpoint;
        err = 0;
        intErr = 0;
        prevErr = 0;
        accumErr = new double[10];
        for(double currErr : accumErr) currErr = 0;
        errIdx = 0;
    }
    
    public double output(double input, double time) {
        err = setpoint - input;
    	System.out.println("ERROR: " + err);
        intErr += err*time;

        accumErr[errIdx] = err;
        errIdx = (errIdx + 1) % accumErr.length;
        
        double windowIntErr = 0;
        for(int i = 0; i < accumErr.length; i++) {
    		windowIntErr += accumErr[i];
        }
        
        
        double control = Kp*err + Ki*windowIntErr + Kd*(err - prevErr) / time;
		
        prevErr = err;
        return control;
    }
    
    public double getSetpoint() { return setpoint; }

}
