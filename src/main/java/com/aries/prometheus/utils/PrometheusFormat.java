package com.aries.prometheus.utils;

import org.json.JSONObject;

public class PrometheusFormat {

    private static final String newline = "\n";

    public static String dbMetricsInstanc(String metrics, int interval, JSONObject objRealDomain, JSONObject objRealInstance, JSONObject instanceJsonArr){
        StringBuffer sb = new StringBuffer();
        sb.append("# HELP jennifer_instance_dbmetrics_" + metrics + " instance " + interval + " min average value").append(newline);
        sb.append("# TYPE jennifer_instance_dbmetrics_" + metrics + " gauge").append(newline);
        sb.append("jennifer_instance_dbmetrics_" + metrics + "{jdomain=\"" + objRealDomain.getString("domainName") + "\", domain_id=\"" + objRealDomain.getInt("domainId") + "\", instance_name=\"" + objRealInstance.getString("instanceName") + "\"} " + instanceJsonArr.getDouble("value")).append(newline);

        return sb.toString();
    }

    public static String dbMetricsDomain(String metrics, int interval, JSONObject objRealDomain, JSONObject objMetricsDomain){
        StringBuffer sb = new StringBuffer();

        // Make text for prometheus
        sb.append("# HELP jennifer_domain_dbmetrics_" + metrics + " Domain " + interval +" average value").append(newline);
        sb.append("# TYPE jennifer_domain_dbmetrics_" + metrics + " gauge").append(newline);
        sb.append("jennifer_domain_dbmetrics_" + metrics + "{jdomain=" + objRealDomain.getString("domainName") + ", domain_id=" + objRealDomain.getInt("domainId") +"} " + objMetricsDomain.getDouble("value")).append(newline);
        return sb.toString();
    }

    public static String realtimeDomain(JSONObject objDomain){
        StringBuffer sb = new StringBuffer();
        // Make text for prometheus
        sb.append("# HELP jennifer_domain_tps Domain Total Transactions per second").append(newline);
        sb.append("# TYPE jennifer_domain_tps gauge").append(newline);
        sb.append("jennifer_domain_tps{jdomain=\"" + objDomain.getString("domainName") + "\", domain_id=\"" + objDomain.getInt("domainId") +"\"} " + objDomain.getDouble("tps")).append(newline);

        sb.append("# HELP jennifer_domain_response_time_milliseconds The average response time within the applicable range can be retrieved.").append(newline);
        sb.append("# TYPE jennifer_domain_response_time_milliseconds gauge").append(newline);
        sb.append("jennifer_domain_response_time_milliseconds{jdomain=\"" + objDomain.getString("domainName") + "\", domain_id=\"" + objDomain.getInt("domainId") +"\"} " + objDomain.getDouble("responseTime")).append(newline);

        sb.append("# HELP jennifer_domain_active_service_total Indicates the number of active services at the moment of collection; in other words, the number of transactions in operation.").append(newline);
        sb.append("# TYPE jennifer_domain_active_service_total counter").append(newline);
        sb.append("jennifer_domain_active_service_total{jdomain=\"" + objDomain.getString("domainName") + "\", domain_id=\"" + objDomain.getInt("domainId") +"\"} " + objDomain.getInt("activeService")).append(newline);

        sb.append("# HELP jennifer_domain_hit_hour Total time taken for applicable Calls").append(newline);
        sb.append("# TYPE jennifer_domain_hit_hour counter").append(newline);
        sb.append("jennifer_domain_hit_hour{jdomain=\"" + objDomain.getString("domainName") + "\", domain_id=\"" + objDomain.getInt("domainId") +"\"} " + objDomain.getInt("hitHour")).append(newline);

        return sb.toString();
    }

    public static String realtimeInstance(JSONObject objDomain, JSONObject objIns){
        StringBuffer sb = new StringBuffer();
        // Make text for prometheus
        sb.append("# HELP jennifer_instance_tps instance Total Transactions per second").append(newline);
        sb.append("# TYPE jennifer_instance_tps gauge").append(newline);
        sb.append("jennifer_instance_tps{jdomain=\"" + objDomain.getString("domainName") + "\", domain_id=\"" + objIns.getInt("domainId") + "\", instance_name=\"" + objIns.getString("instanceName") + "\"} " + objIns.getDouble("tps")).append(newline);

        sb.append("# HELP jennifer_instance_response_time_milliseconds The average response time within the applicable range can be retrieved.").append(newline);
        sb.append("# TYPE jennifer_instance_response_time_milliseconds gauge").append(newline);
        sb.append("jennifer_instance_response_time_milliseconds{jdomain=\"" + objDomain.getString("domainName") + "\", domain_id=\"" + objIns.getInt("domainId")+ "\", instance_name=\"" + objIns.getString("instanceName") + "\"} " + objIns.getDouble("responseTime")).append(newline);

        //activeService
        sb.append("# HELP jennifer_instance_active_service Indicates the number of active services at the moment of collection").append(newline);
        sb.append("# TYPE jennifer_instance_active_service counter").append(newline);
        sb.append("jennifer_instance_active_service{jdomain=\"" + objDomain.getString("domainName") + "\", domain_id=\"" + objIns.getInt("domainId")+ "\", instance_name=\"" + objIns.getString("instanceName") + "\"} " + objIns.getDouble("activeService")).append(newline);

        //concurrentUser
        sb.append("# HELP jennifer_instance_concurrent_user Indicates the number of people currently using ").append(newline);
        sb.append("# TYPE jennifer_instance_concurrent_user counter").append(newline);
        sb.append("jennifer_instance_concurrent_user{jdomain=\"" + objDomain.getString("domainName") + "\", domain_id=\"" + objIns.getInt("domainId")+ "\", instance_name=\"" + objIns.getString("instanceName") + "\"} " + objIns.getDouble("concurrentUser")).append(newline);

        //procCPU
        sb.append("# HELP jennifer_instance_process_cpu Indicates the CPU usage rate of a process being monitored").append(newline);
        sb.append("# TYPE jennifer_instance_process_cpu gauge").append(newline);
        sb.append("jennifer_instance_process_cpu{jdomain=\"" + objDomain.getString("domainName") + "\", domain_id=\"" + objIns.getInt("domainId")+ "\", instance_name=\"" + objIns.getString("instanceName") + "\"} " + objIns.getDouble("procCPU")).append(newline);

        //procMemory
        sb.append("# HELP jennifer_instance_process_memory_bytes Indicates the memory usage by the process being monitored.").append(newline);
        sb.append("# TYPE jennifer_instance_process_memory_bytes gauge").append(newline);
        sb.append("jennifer_instance_process_memory_bytes{jdomain=\"" + objDomain.getString("domainName") + "\", domain_id=\"" + objIns.getInt("domainId")+ "\", instance_name=\"" + objIns.getString("instanceName") + "\"} " + objIns.getDouble("procMemory")).append(newline);

        //heapCommitted //heapUsed
        sb.append("# HELP jennifer_instance_heap_memory_bytes Indicates the size of memory allocated to the heap memory area in the VM.").append(newline);
        sb.append("# TYPE jennifer_instance_heap_memory_bytes gauge").append(newline);
        sb.append("jennifer_instance_heap_memory_bytes{jdomain=\"" + objDomain.getString("domainName") + "\", domain_id=\"" + objIns.getInt("domainId")+ "\", instance_name=\"" + objIns.getString("instanceName") + "\"} " + objIns.getDouble("heapUsed")).append(newline);

        double usage = (objIns.getDouble("heapUsed") / objIns.getDouble("heapCommitted")) * 100 ;
        //heapCommitted //heapUsed
        sb.append("# HELP jennifer_instance_heap_memory_bytes Memory usage heapUsed/heapCommitted * 100.").append(newline);
        sb.append("# TYPE jennifer_instance_heap_memory_bytes gauge").append(newline);
        sb.append("jennifer_instance_heap_memory_usage{jdomain=\"" + objDomain.getString("domainName") + "\", domain_id=\"" + objIns.getInt("domainId")+ "\", instance_name=\"" + objIns.getString("instanceName") + "\"} " + usage).append(newline);

        //hitHour
        sb.append("# HELP jennifer_instance_visit_hour Total time taken for applicable Calls").append(newline);
        sb.append("# TYPE jennifer_instance_visit_hour gauge").append(newline);
        sb.append("jennifer_instance_process_memory_bytes{jdomain=\"" + objDomain.getString("domainName") + "\", domain_id=\"" + objIns.getInt("domainId")+ "\", instance_name=\"" + objIns.getString("instanceName") + "\"} " + objIns.getInt("hitHour")).append(newline);
        return sb.toString();
    }
}
