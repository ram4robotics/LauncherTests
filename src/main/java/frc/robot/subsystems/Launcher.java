// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;


import java.util.function.Supplier;

import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.SparkMaxPIDController;
import com.revrobotics.CANSparkMax.ControlType;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.CANSparkMaxLowLevel.PeriodicFrame;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.CAN_IDs;
import frc.robot.Constants.LauncherConstants;

public class Launcher extends SubsystemBase {
  private CANSparkMax m_launcher1 = new CANSparkMax(CAN_IDs.launcher1_ID, MotorType.kBrushless);
  private CANSparkMax m_launcher2 = new CANSparkMax(CAN_IDs.launcher2_ID, MotorType.kBrushless);
  private RelativeEncoder m_encoder;
  private boolean launchHeight = true;
  private SparkMaxPIDController m_pidController;
  private double m_setpointRPM = LauncherConstants.desiredRPM;

  private final Supplier<Integer> m_povSupplier;

  /** Creates a new Launcher. */
  public Launcher(Supplier<Integer> povSupplier) {

    m_povSupplier = povSupplier;
    m_launcher1.restoreFactoryDefaults();
    m_launcher2.restoreFactoryDefaults();
    m_launcher1.setIdleMode(IdleMode.kCoast);
    m_launcher2.setIdleMode(IdleMode.kCoast);
    m_launcher1.setPeriodicFramePeriod(PeriodicFrame.kStatus1, 500);
    m_launcher1.setPeriodicFramePeriod(PeriodicFrame.kStatus2, 500);
    m_launcher2.setPeriodicFramePeriod(PeriodicFrame.kStatus0, 100);
    m_launcher2.setPeriodicFramePeriod(PeriodicFrame.kStatus1, 500);
    m_launcher2.setPeriodicFramePeriod(PeriodicFrame.kStatus2, 500);
    // m_launcher1.setOpenLoopRampRate(0.5);
    m_launcher2.follow(m_launcher1, true);

    /**
     * In order to use PID functionality for a controller, a SparkMaxPIDController object
     * is constructed by calling the getPIDController() method on an existing
     * CANSparkMax object
     */
    m_pidController = m_launcher1.getPIDController();

      // Encoder object created to display velocity values
    m_encoder = m_launcher1.getEncoder();
    // set PID coefficients
    m_pidController.setP(LauncherConstants.kP);
    m_pidController.setI(LauncherConstants.kI);
    m_pidController.setD(LauncherConstants.kD);
    m_pidController.setIZone(LauncherConstants.kIz);
    m_pidController.setFF(LauncherConstants.kFF);
    m_pidController.setOutputRange(LauncherConstants.kMinOutput, LauncherConstants.kMaxOutput);

    // display setpoint to dashboard
    SmartDashboard.putNumber("Launcher target RPM", m_setpointRPM);

  }

  @Override
  public void periodic() {
    int povValue = m_povSupplier.get();
    if (povValue == 180) {
      launchHigh();
    } 
    else if (povValue == 0) {
      launchLow();
    }

    // This method will be called once per scheduler run
    // m_setpointRPM = SmartDashboard.getNumber("Launcher target RPM", m_setpointRPM);
    SmartDashboard.putNumber("Current RPM", m_encoder.getVelocity());
  }

  @Override
  public void simulationPeriodic() {
    // This method will be called once per scheduler run during simulation
  }

    
  public void launch() {
    if (launchHeight) {
      m_pidController.setReference(LauncherConstants.launcherMotorHighSpeed * LauncherConstants.maxRPM, 
        ControlType.kVelocity);
    }
    else {  
      m_pidController.setReference(LauncherConstants.launcherMotorLowSpeed * LauncherConstants.maxRPM, 
        ControlType.kVelocity);
    }

    // if(launchHeight){ m_launcher1.set(LauncherConstants.launcherMotorHighSpeed);}
    // else {  m_launcher1.set(LauncherConstants.launcherMotorLowSpeed);}
  }

  public void unclogLauncher() {
    if(launchHeight) {m_launcher1.set(-LauncherConstants.launcherMotorHighSpeed / 2);}
    else { m_launcher1.set(- LauncherConstants.launcherMotorLowSpeed / 2);}
  }

  public void doNothing(){}

  public void stopLauncher() {
    m_launcher1.set(0.0);
  }

  public void launchHigh(){
    launchHeight = true;
    System.out.print("Launch High\n");
  }

  public void launchLow(){
    launchHeight = false;
    System.out.print("Launch Low\n");
  }
}
