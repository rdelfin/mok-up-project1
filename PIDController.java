import java.util.*;

public class PIDController {
    private double Kp, Ki, Kd;
    private double err, prevErr, intErr;
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
    }
    
    public double output(double input, double time) {
        err = setpoint - input;
    	System.out.println("ERROR: " + err);
        intErr += err*time;
        double control = Kp*err + Ki*intErr + Kd*(err - prevErr) / time;
		prevErr = err;
        return control;
    }
    
    public double getSetpoint() { return setpoint; }

}
