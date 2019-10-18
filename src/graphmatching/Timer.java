/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphmatching;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 *
 * @author Nil
 */
final class Timer {
	private long startTime;
	private double duration;
	
	Timer() {
		start();
	}
	
	void start() {
		startTime = getCpuTime();
	}
	
	double sinceStart() {
		return (getCpuTime() - startTime)/ 1000000000.0;
	}
	
	double stop() {
		duration = sinceStart();
		return duration;
	}
	
	double getDuration() {
		return duration;
	}
	
	String getStr() {
		return main.prettyStr(duration);
	}
	
	public static long getCpuTime() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
        return bean.isCurrentThreadCpuTimeSupported( ) ?
            bean.getCurrentThreadCpuTime( ) : 0L;
    }
}
